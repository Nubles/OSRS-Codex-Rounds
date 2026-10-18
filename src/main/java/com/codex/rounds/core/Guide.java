package com.codex.rounds.core;

import com.codex.rounds.core.farming.Action;
import com.codex.rounds.core.farming.PatchKind;
import com.codex.rounds.core.farming.Produce;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.runelite.api.gameval.ItemID;

/**
 * Works out the single next step of a round from what the game shows right now. It holds no
 * state: the round's plan, the live readings and the little the game doesn't show (compost,
 * payments, skips) go in, and one step comes out, so it can never drift out of step.
 */
public final class Guide
{
	static final int[] COMPOST = {ItemID.BUCKET_ULTRACOMPOST, ItemID.BUCKET_SUPERCOMPOST, ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED};

	private Guide()
	{
	}

	/** The game as it is this tick. */
	public static final class World
	{
		final Point player;
		final Map<Spot, Reading> live;
		final Map<Daily, DailyState> dailies;
		final Kingdom kingdom;
		final Map<Integer, Integer> carried;
		final Ability ability;
		final long now;

		public World(Point player, Map<Spot, Reading> live, Map<Daily, DailyState> dailies, Kingdom kingdom,
			Map<Integer, Integer> carried, Ability ability, long now)
		{
			this.player = player;
			this.live = live;
			this.dailies = dailies;
			this.kingdom = kingdom;
			this.carried = carried;
			this.ability = ability;
			this.now = now;
		}

		int carrying(int... ids)
		{
			int n = 0;
			for (int id : ids)
			{
				n += carried.getOrDefault(id, 0);
			}
			return n;
		}
	}

	/** Whether a chore is finished, from the live game. */
	public static boolean isDone(Task t, World w, Progress progress, Settings settings, long since)
	{
		if (progress.isSkipped(t))
		{
			return true;
		}
		switch (t.getType())
		{
			case DAILY:
				DailyState d = w.dailies.get(t.getDaily());
				return d != null && !d.isClaimable();
			case KINGDOM:
				return w.kingdom != null && !w.kingdom.needsVisit(settings.getApprovalTarget(), settings.getCofferTarget(), settings.getTopUpSlack());
			default:
				break;
		}
		Spot spot = t.getSpot();
		Reading r = w.live.get(spot);
		Reading planned = t.getReading();
		if (r == null || r.getStatus() == Status.UNSEEN)
		{
			return false;
		}
		if (spot.isBirdhouse())
		{
			if (r.getHouse() != Birdhouse.State.SEEDED)
			{
				return false;
			}
			return r.getSeen().getTime() >= since || (planned.getStatus() == Status.UNSEEN && r.getStatus() == Status.GROWING);
		}
		if (r.getStatus() == Status.READY)
		{
			// Nothing was meant to go in: an empty patch is as far as this round takes it.
			return t.getPlant() == null && (r.getAction() == Action.PLANT || r.getAction() == Action.RAKE);
		}
		if (planned.getStatus() == Status.GROWING && planned.getSeen() != null && r.getSeen() != null
			&& planned.getSeen().getValue() == r.getSeen().getValue() && r.getStatus() == Status.GROWING)
		{
			// Came early for something nearly ready: it still needs doing.
			return false;
		}
		if (progress.wasPlanted(spot))
		{
			if (t.isCompost() && !progress.isComposted(spot))
			{
				return false;
			}
			if (t.isProtect() && !progress.isPaid(spot))
			{
				return false;
			}
		}
		return true;
	}

	public static Step next(Route route, World w, Progress progress, Settings settings)
	{
		if (route == null || route.isEmpty())
		{
			return Step.IDLE;
		}
		long since = progress.isStarted() ? progress.getStartedAt() : route.getBuiltAt();
		int total = route.taskCount();
		int done = 0;
		int current = -1;
		for (int i = 0; i < route.getStops().size(); i++)
		{
			Stop s = route.getStops().get(i);
			int left = 0;
			for (Task t : s.getTasks())
			{
				if (isDone(t, w, progress, settings, since))
				{
					done++;
				}
				else
				{
					left++;
				}
			}
			if (left > 0 && current < 0)
			{
				current = i;
			}
		}
		if (current < 0)
		{
			return new Step(Step.Kind.IDLE, "Round complete", "Every stop is done. Nice work.", null, Collections.emptyList(), null, null,
				-1, done, total);
		}

		Stop stop = route.getStops().get(current);
		if (done == 0 && current == 0 && !progress.isSuppliesDone() && !stop.getPlace().contains(w.player))
		{
			List<Need> missing = missing(route, w);
			if (!missing.isEmpty())
			{
				List<Integer> ids = new ArrayList<>();
				StringBuilder b = new StringBuilder();
				for (Need n : missing)
				{
					ids.add(n.getItemId());
					if (b.length() > 0)
					{
						b.append(", ");
					}
					b.append(n.getQuantity() > 1 ? Format.number(n.getQuantity()) + " x " : "").append(n.getLabel().toLowerCase());
				}
				return new Step(Step.Kind.SUPPLIES, "Get your supplies", "Still to withdraw: " + b, null, ids, null, null, current, done, total);
			}
		}

		Place place = stop.getPlace();
		if (!place.contains(w.player))
		{
			return travel(stop, place, w, current, done, total);
		}

		if (Leprechaun.PLACES.contains(place) && !progress.isLeprechaunDone())
		{
			List<Need> take = fromLeprechaun(route.getPulls(), w);
			if (!take.isEmpty())
			{
				List<Integer> ids = new ArrayList<>();
				StringBuilder b = new StringBuilder();
				for (Need n : take)
				{
					ids.add(n.getItemId());
					if (b.length() > 0)
					{
						b.append(", ");
					}
					b.append(n.getQuantity() > 1 ? Format.number(n.getQuantity()) + " x " : "").append(n.getLabel().toLowerCase());
				}
				return new Step(Step.Kind.SUPPLIES, "Take tools from the leprechaun", "Stored with him: " + b,
					Step.Target.npc(place.getAnchor(), Leprechaun.NPCS), ids, null, null, current, done, total);
			}
		}

		Task task = nearest(stop, w, progress, settings, since);
		return taskStep(task, w, progress, current, done, total);
	}

	static Step travel(Stop stop, Place place, World w, int current, int done, int total)
	{
		Place via = place.getVia();
		if (via != null && via.contains(w.player))
		{
			Step.Target target = place.getLocalObject() >= 0
				? Step.Target.object(place.getLocalPoint(), place.getLocalObject())
				: Step.Target.tile(place.getLocalPoint());
			return new Step(Step.Kind.TRAVEL, place.getLocalStep(), "On to " + place.getLabel() + ": " + place.getDetail(), target,
				Collections.emptyList(), place.getLocalPoint(), null, current, done, total);
		}
		Travel.Way way = Travel.best(w.player, place, w.ability);
		Place landing = way.getPlace();
		String detail = (landing != place ? landing.getLabel() + ", then " + place.getLocalStep().toLowerCase() : place.getLabel() + ": " + place.getDetail());
		Leg leg = way.getLeg();
		if (leg == null)
		{
			return new Step(Step.Kind.TRAVEL, "Walk to " + landing.getLabel(), detail, Step.Target.tile(landing.getAnchor()),
				Collections.emptyList(), landing.getAnchor(), null, current, done, total);
		}
		List<Integer> items = new ArrayList<>();
		String title;
		switch (leg.getKind())
		{
			case ITEM:
				items.addAll(owned(leg.getItems(), w.ability));
				title = leg.getLabel();
				break;
			case SPELL:
				title = "Cast " + leg.getLabel();
				break;
			case FAIRY_RING:
				title = leg.getLabel();
				detail = "Dial " + leg.getLabel().replace("Fairy ring ", "") + " for " + detail;
				items.addAll(owned(new int[]{772, 9084, 13280, 13342}, w.ability));
				break;
			default:
				title = leg.getLabel();
				break;
		}
		return new Step(Step.Kind.TRAVEL, title, detail, null, items, landing.getAnchor(), null, current, done, total);
	}

	/** The variants the player has, so the card shows one item, not every version of it. */
	static List<Integer> owned(int[] ids, Ability ability)
	{
		List<Integer> out = new ArrayList<>();
		for (int id : ids)
		{
			if (ability.owns(id))
			{
				out.add(id);
			}
		}
		if (out.isEmpty() && ids.length > 0)
		{
			out.add(ids[0]);
		}
		return out;
	}

	/** The closest unfinished chore at the stop, so the player never doubles back. */
	static Task nearest(Stop stop, World w, Progress progress, Settings settings, long since)
	{
		Task best = null;
		int bestDist = Integer.MAX_VALUE;
		for (Task t : stop.getTasks())
		{
			if (isDone(t, w, progress, settings, since))
			{
				continue;
			}
			Point p = where(t);
			int d = p == null || w.player == null ? 0 : p.distance(w.player);
			if (best == null || d < bestDist)
			{
				best = t;
				bestDist = d;
			}
		}
		return best;
	}

	static Point where(Task t)
	{
		switch (t.getType())
		{
			case SPOT:
				return t.getSpot().getPoint();
			case DAILY:
				return t.getDaily().getPlace().getAnchor();
			default:
				return Place.MISCELLANIA.getAnchor();
		}
	}

	static Step taskStep(Task t, World w, Progress progress, int current, int done, int total)
	{
		switch (t.getType())
		{
			case DAILY:
				Daily d = t.getDaily();
				Point near = d.getPlace().getAnchor();
				List<Integer> items = new ArrayList<>();
				DailyState ds = w.dailies.get(d);
				if (ds != null && ds.getNeed() != null)
				{
					items.add(ds.getNeed().getItemId());
				}
				return new Step(Step.Kind.TASK, d.getAction(), t.getDetail(), Step.Target.npc(near, d.getNpcs()), items, near, t, current, done, total);
			case KINGDOM:
				return kingdomStep(t, w, current, done, total);
			default:
				return spotStep(t, w, progress, current, done, total);
		}
	}

	static Step kingdomStep(Task t, World w, int current, int done, int total)
	{
		Kingdom k = w.kingdom;
		Point ghrim = Place.MISCELLANIA.getAnchor();
		if (k != null && t.getDetail().toLowerCase().contains("deposit") && k.getCoffer() < Kingdom.MAX_COFFER)
		{
			return new Step(Step.Kind.TASK, "Top up the coffer", "Talk to Advisor Ghrim and choose to deposit coins. " + t.getDetail() + ".",
				Step.Target.npc(ghrim, 5447, 5448, 3670), Collections.singletonList(ItemID.COINS), ghrim, t, current, done, total);
		}
		String pct = k == null ? "" : " (now " + k.approvalPercent() + "%)";
		return new Step(Step.Kind.TASK, "Raise approval" + pct,
			"Chop maples, mine coal, fish or rake herbs on Miscellania until approval is full, then collect from Advisor Ghrim.",
			null, Collections.emptyList(), ghrim, t, current, done, total);
	}

	static Step spotStep(Task t, World w, Progress progress, int current, int done, int total)
	{
		Spot spot = t.getSpot();
		Reading r = w.live.get(spot);
		Point at = spot.getPoint();
		Step.Target here = Step.Target.spot(spot);
		if (r == null || r.getStatus() == Status.UNSEEN)
		{
			return task(t, "Look at the " + spot.kindName().toLowerCase(), spot.getLabel() + ": walk up to it so the round can read it.", here, at,
				current, done, total);
		}

		if (spot.isBirdhouse())
		{
			Birdhouse.State st = r.getHouse();
			if (st == Birdhouse.State.SEEDED && r.getStatus() == Status.READY)
			{
				return task(t, "Empty the bird house", spot.getLabel() + ": collect the catch.", here, at, current, done, total);
			}
			if (st == Birdhouse.State.SEEDED)
			{
				return task(t, "Wait for the bird house", "Full in " + Format.duration(r.secondsLeft(w.now)) + ".", here, at, current, done, total);
			}
			if (st == Birdhouse.State.BUILT)
			{
				return task(t, "Add ten seeds", "Use hop seeds on the bird house.", here, at, current, done, total, Birdhouse.SEEDS);
			}
			Birdhouse.Type type = Supplies.houseType(w.ability);
			int[] houses = carriedHouses(w);
			if (houses.length > 0)
			{
				Birdhouse.Type have = Birdhouse.Type.forItem(houses[0]);
				return task(t, "Build the bird house", "Click the space to set up your " + have.getName().toLowerCase() + ".", here, at, current, done, total,
					houses);
			}
			return task(t, "Craft a bird house", "Use a chisel on your " + Supplies.logName(type).toLowerCase()
				+ " with clockwork in your pack, then build it on the space.", here, at, current, done, total, ItemID.CHISEL, type.getLogId());
		}

		String where = spot.getLabel();
		PatchKind kind = spot.getKind();
		Produce plant = t.getPlant();
		if (r.getStatus() == Status.GROWING || r.getStatus() == Status.DONE)
		{
			if (progress.wasPlanted(spot) && t.isCompost() && !progress.isComposted(spot))
			{
				return task(t, "Compost the patch", where + ": use your compost on it.", here, at, current, done, total, COMPOST);
			}
			if (progress.wasPlanted(spot) && t.isProtect() && !progress.isPaid(spot) && plant != null)
			{
				int[] pay = plant.getPayment();
				int[] ids = new int[pay.length];
				StringBuilder b = new StringBuilder();
				for (int i = 0; i + 1 < pay.length; i += 2)
				{
					ids[i] = pay[i];
					ids[i + 1] = pay[i] + 1;
					if (b.length() > 0)
					{
						b.append(", ");
					}
					b.append(pay[i + 1]).append(' ').append(Supplies.itemName(pay[i]).toLowerCase());
				}
				return task(t, "Pay the gardener", "Talk to the gardener and pay " + b + " to protect it.",
					Step.Target.npc(at, spot.getGardener()), at, current, done, total, ids);
			}
			long left = r.secondsLeft(w.now);
			return task(t, "Not ready yet", where + ": ready in " + Format.duration(left) + ". Wait, or skip it this round.", here, at,
				current, done, total);
		}

		Action a = r.getAction();
		if (a == null)
		{
			a = Action.WAIT;
		}
		switch (a)
		{
			case RAKE:
				return task(t, "Rake the patch", where + (plant == null ? "" : ": then plant " + Planner.seedName(plant) + "."), here, at,
					current, done, total, ItemID.RAKE);
			case PLANT:
				if (plant == null)
				{
					return task(t, "Leave it empty", where + ": nothing chosen to plant. Skip it.", here, at, current, done, total);
				}
				return task(t, "Plant " + Planner.seedName(plant), where + ": use it on the patch.", here, at, current, done, total, plant.getSeedId());
			case CHECK_HEALTH:
				return task(t, "Check its health", where + ": check the " + spot.kindName().toLowerCase() + "'s health.", here, at, current, done, total);
			case HARVEST:
				return task(t, Planner.harvestText(spot, r.getProduce() == null ? "" : r.getProduce().getName().toLowerCase()), where, here, at,
					current, done, total, kind == PatchKind.BELLADONNA ? new int[]{ItemID.LEATHER_GLOVES} : Supplies.SECATEURS);
			case CHOP:
				if (Planner.isTree(spot) && spot.getGardener() >= 0)
				{
					return task(t, "Pay to clear the tree", where + ": ask the gardener to remove it for 200 coins.",
						Step.Target.npc(at, spot.getGardener()), at, current, done, total, ItemID.COINS);
				}
				return task(t, "Chop it down", where, here, at, current, done, total);
			case CLEAR:
				return task(t, "Dig it up", where + ": clear it with your spade.", here, at, current, done, total, ItemID.SPADE);
			case CURE:
				return task(t, "Cure the disease", where + ": use a plant cure on it.", here, at, current, done, total, ItemID.PLANT_CURE);
			case PRUNE:
				return task(t, "Prune the disease", where + ": prune it with secateurs.", here, at, current, done, total, Supplies.SECATEURS);
			default:
				return task(t, r.getSummary(), where, here, at, current, done, total);
		}
	}

	static int[] carriedHouses(World w)
	{
		List<Integer> ids = new ArrayList<>();
		Birdhouse.Type[] all = Birdhouse.Type.values();
		for (int i = all.length - 1; i >= 0; i--)
		{
			if (all[i].getHunterLevel() <= w.ability.getHunter() && w.carrying(all[i].getItemId()) > 0)
			{
				ids.add(all[i].getItemId());
			}
		}
		return ids.stream().mapToInt(Integer::intValue).toArray();
	}

	private static Step task(Task t, String title, String detail, Step.Target target, Point at, int current, int done, int total, int... items)
	{
		List<Integer> list = new ArrayList<>();
		for (int i : items)
		{
			list.add(i);
		}
		return new Step(Step.Kind.TASK, title, detail, target, list, at, t, current, done, total);
	}

	/** Required lines of the pull list the player isn't carrying enough of. */
	/** What still has to come from the bank: stored tools are left out when the first stop has a leprechaun. */
	public static List<Need> missing(Route route, World w)
	{
		boolean leprechaunFirst = !route.getStops().isEmpty() && Leprechaun.PLACES.contains(route.getStops().get(0).getPlace());
		List<Need> out = new ArrayList<>();
		for (Need n : route.getPulls())
		{
			if (n.isOptional())
			{
				continue;
			}
			int have = w.carrying(n.getItems());
			if (leprechaunFirst && Leprechaun.holds(n))
			{
				have += w.ability.stored(n.getItems());
			}
			if (have < n.getQuantity())
			{
				out.add(n);
			}
		}
		return out;
	}

	/** Tools and compost the round needs that are with the leprechaun rather than in the pack. */
	static List<Need> fromLeprechaun(List<Need> pulls, World w)
	{
		List<Need> out = new ArrayList<>();
		for (Need n : pulls)
		{
			if (Leprechaun.holds(n) && w.carrying(n.getItems()) < n.getQuantity() && w.ability.stored(n.getItems()) > 0)
			{
				out.add(n);
			}
		}
		return out;
	}
}
