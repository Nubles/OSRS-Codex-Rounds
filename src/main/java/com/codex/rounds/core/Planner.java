package com.codex.rounds.core;

import com.codex.rounds.core.farming.Action;
import com.codex.rounds.core.farming.PatchKind;
import com.codex.rounds.core.farming.Produce;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns the board into a round: which places to visit, in what order, what to do at each and
 * what to bring. The order starts greedy (nearest next) and is then improved by moving single
 * stops and reversing runs of stops until nothing gets faster.
 */
public final class Planner
{
	private Planner()
	{
	}

	public static Route plan(Board board, List<DailyState> dailies, Settings settings, Ability ability, Point start, long now)
	{
		long lookahead = settings.getLookaheadMinutes() * 60L;
		Map<Place, List<Task>> byPlace = new LinkedHashMap<>();
		Map<Daily, DailyState> dailyStates = new EnumMap<>(Daily.class);
		for (DailyState d : dailies)
		{
			dailyStates.put(d.getDaily(), d);
		}

		for (Board.Entry e : board.getEntries())
		{
			Task t = null;
			switch (e.getType())
			{
				case SPOT:
					Reading r = e.getReading();
					if (r.dueWithin(now, lookahead) && (r.getStatus() != Status.UNSEEN || settings.isIncludeUnseen()))
					{
						t = spotTask(e.getSpot(), r, settings, ability);
					}
					break;
				case DAILY:
					if (e.getStatus() == Status.READY)
					{
						DailyState ds = dailyStates.get(e.getDaily());
						t = new Task(Board.Type.DAILY, null, e.getDaily(), null, null, false, false,
							e.getDaily().getAction(), ds == null ? e.getDetail() : ds.getReward(), 20);
					}
					break;
				case KINGDOM:
					if (e.getStatus() == Status.READY)
					{
						t = kingdomTask(board.getKingdom(), settings);
					}
					break;
			}
			if (t != null)
			{
				byPlace.computeIfAbsent(e.getPlace(), p -> new ArrayList<>()).add(t);
			}
		}

		if (byPlace.isEmpty())
		{
			return new Route(new ArrayList<>(), new ArrayList<>(), now);
		}

		List<Place> order = order(new ArrayList<>(byPlace.keySet()), start, ability);
		List<Stop> stops = new ArrayList<>();
		Point at = start;
		for (Place p : order)
		{
			Travel.Way way = Travel.best(at, p, ability);
			stops.add(new Stop(p, way, byPlace.get(p)));
			at = exit(p);
		}
		return new Route(stops, Supplies.forStops(stops, dailyStates, board.getKingdom(), settings, ability), now);
	}

	/** Where you stand after finishing a place: the seabed and Hespori's cave come back up to the surface. */
	static Point exit(Place p)
	{
		return p.getVia() != null ? p.getLocalPoint() : p.getAnchor();
	}

	static int cost(Point from, Place to, Ability ability)
	{
		return Travel.best(from, to, ability).getSeconds();
	}

	static int total(List<Place> order, Point start, Ability ability, Map<Long, Integer> memo)
	{
		int sum = 0;
		Point at = start;
		Place prev = null;
		for (Place p : order)
		{
			long key = ((long) (prev == null ? 999 : prev.ordinal()) << 16) | p.ordinal();
			Integer c = memo.get(key);
			if (c == null)
			{
				c = cost(at, p, ability);
				memo.put(key, c);
			}
			sum += c;
			at = exit(p);
			prev = p;
		}
		return sum;
	}

	static List<Place> order(List<Place> places, Point start, Ability ability)
	{
		// Greedy: always go to the cheapest next place.
		List<Place> left = new ArrayList<>(places);
		List<Place> order = new ArrayList<>();
		Point at = start;
		while (!left.isEmpty())
		{
			Place best = null;
			int bestCost = Integer.MAX_VALUE;
			for (Place p : left)
			{
				int c = cost(at, p, ability);
				if (c < bestCost)
				{
					bestCost = c;
					best = p;
				}
			}
			order.add(best);
			left.remove(best);
			at = exit(best);
		}

		// Improve: relocate single stops and reverse runs until no move helps.
		Map<Long, Integer> memo = new java.util.HashMap<>();
		int current = total(order, start, ability, memo);
		boolean improved = true;
		int guard = 0;
		while (improved && guard++ < 50)
		{
			improved = false;
			for (int i = 0; i < order.size(); i++)
			{
				for (int j = 0; j < order.size(); j++)
				{
					if (i == j)
					{
						continue;
					}
					List<Place> trial = new ArrayList<>(order);
					Place moved = trial.remove(i);
					trial.add(j, moved);
					int t = total(trial, start, ability, memo);
					if (t < current)
					{
						order = trial;
						current = t;
						improved = true;
					}
				}
			}
			for (int i = 0; i < order.size() - 1; i++)
			{
				for (int j = i + 1; j < order.size(); j++)
				{
					List<Place> trial = new ArrayList<>(order);
					java.util.Collections.reverse(trial.subList(i, j + 1));
					int t = total(trial, start, ability, memo);
					if (t < current)
					{
						order = trial;
						current = t;
						improved = true;
					}
				}
			}
		}
		return order;
	}

	static Task kingdomTask(Kingdom k, Settings s)
	{
		List<String> parts = new ArrayList<>();
		int topUp = k.topUp(s.getCofferTarget());
		if (topUp > s.getTopUpSlack())
		{
			parts.add("deposit " + Format.coins(topUp));
		}
		if (k.approvalPercent() < s.getApprovalTarget())
		{
			parts.add("raise approval from " + k.approvalPercent() + "% to " + s.getApprovalTarget() + "%");
		}
		String detail = parts.isEmpty() ? "Check in with Advisor Ghrim" : capitalise(String.join(" and ", parts));
		int seconds = 30 + (k.approvalPercent() < s.getApprovalTarget() ? (s.getApprovalTarget() - k.approvalPercent()) * 6 : 0);
		return new Task(Board.Type.KINGDOM, null, null, null, null, false, false, "Look after Miscellania", detail, seconds);
	}

	static String capitalise(String s)
	{
		return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	static Task spotTask(Spot spot, Reading r, Settings settings, Ability ability)
	{
		if (spot.isBirdhouse())
		{
			Birdhouse.State st = r.getHouse();
			String detail;
			int secs;
			if (st == null)
			{
				detail = "Look at the space; build and seed it if empty";
				secs = 12;
			}
			else
			{
				switch (st)
				{
					case EMPTY:
						detail = "Build a bird house and add seeds";
						secs = 10;
						break;
					case BUILT:
						detail = "Add ten seeds";
						secs = 5;
						break;
					default:
						detail = r.getStatus() == Status.READY ? "Collect, rebuild and add seeds" : "Nearly full: collect, rebuild and reseed";
						secs = 15;
						break;
				}
			}
			return new Task(Board.Type.SPOT, spot, null, r, null, false, false, spot.getTitle(), detail, secs);
		}

		Produce plant = leavesEmpty(spot, r, settings) ? choosePlant(spot.getKind(), settings, ability) : null;
		boolean compost = plant != null && settings.getCompost() != Settings.Compost.NONE && spot.getKind() != PatchKind.HESPORI
			&& spot.getKind() != PatchKind.CRYSTAL_TREE;
		boolean protect = plant != null && settings.isProtect() && plant.isProtectable() && spot.getGardener() >= 0;
		String detail = spotDetail(spot, r, plant);
		int secs = workSeconds(spot, r) + (plant != null ? 10 : 0) + (compost ? 3 : 0) + (protect ? 8 : 0);
		return new Task(Board.Type.SPOT, spot, null, r, plant, compost, protect, spot.getTitle(), detail, secs);
	}

	static boolean isTree(Spot spot)
	{
		PatchKind k = spot.getKind();
		return k == PatchKind.HARDWOOD_TREE || k == PatchKind.REDWOOD || k == PatchKind.SPIRIT_TREE;
	}

	static boolean regrows(PatchKind k)
	{
		return k == PatchKind.BUSH || k == PatchKind.CACTUS;
	}

	/** Whether this visit leaves the patch empty and ready to replant. */
	static boolean leavesEmpty(Spot spot, Reading r, Settings settings)
	{
		PatchKind k = spot.getKind();
		if (r.getStatus() == Status.UNSEEN)
		{
			return true;
		}
		if (r.getAction() == null)
		{
			return false;
		}
		switch (r.getAction())
		{
			case PLANT:
			case RAKE:
			case CLEAR:
			case CHOP:
				return true;
			case CHECK_HEALTH:
				if (k == PatchKind.SPIRIT_TREE)
				{
					return !settings.isKeepSpiritTrees();
				}
				return !regrows(k);
			case HARVEST:
				return !regrows(k);
			default:
				return false;
		}
	}

	/** The crop to plant: the player's pick if they can plant it, else the best they own, else the best they could. */
	public static Produce choosePlant(PatchKind kind, Settings settings, Ability ability)
	{
		Produce chosen = settings.getCrop(kind);
		if (chosen != null && chosen.getLevel() <= ability.getFarming())
		{
			return chosen;
		}
		Produce bestOwned = null;
		Produce bestAny = null;
		for (Produce p : Produce.forKind(kind))
		{
			if (p.getLevel() > ability.getFarming())
			{
				continue;
			}
			bestAny = p;
			if (ability.owns(p.getSeedId()))
			{
				bestOwned = p;
			}
		}
		if (bestOwned != null)
		{
			return bestOwned;
		}
		if (kind == PatchKind.ANIMA || kind == PatchKind.HESPORI)
		{
			// Rare seeds: only plan one the account has, or might have when the bank is unknown.
			return ability.isBankKnown() ? null : bestAny;
		}
		return bestAny;
	}

	static int workSeconds(Spot spot, Reading r)
	{
		if (r.getStatus() == Status.UNSEEN || r.getAction() == null)
		{
			return 15;
		}
		PatchKind k = spot.getKind();
		switch (r.getAction())
		{
			case HARVEST:
				if (k == PatchKind.HESPORI)
				{
					return 90;
				}
				return k == PatchKind.SEAWEED || k == PatchKind.CALQUAT ? 25 : 12;
			case CHECK_HEALTH:
				return isTree(spot) ? 15 : 20;
			case CHOP:
			case CLEAR:
				return 10;
			case RAKE:
				return 15;
			case CURE:
			case PRUNE:
				return 8;
			default:
				return 5;
		}
	}

	static String spotDetail(Spot spot, Reading r, Produce plant)
	{
		String then = plant == null ? "" : ", then plant " + seedName(plant);
		if (r.getStatus() == Status.UNSEEN)
		{
			return "Not seen yet: look at it" + (plant == null ? "" : " and plant " + seedName(plant) + " if it is empty");
		}
		Produce p = r.getProduce();
		String crop = p == null ? "" : p.getName().toLowerCase();
		switch (r.getAction())
		{
			case PLANT:
				return plant == null ? "Empty" : "Plant " + seedName(plant);
			case RAKE:
				return "Rake the weeds" + then;
			case CHECK_HEALTH:
				if (spot.getKind() == PatchKind.SPIRIT_TREE && plant == null)
				{
					return "Check the spirit tree's health and keep it";
				}
				return "Check the " + crop + "'s health" + clearText(spot, r) + then;
			case HARVEST:
				return harvestText(spot, crop) + then;
			case CHOP:
			case CLEAR:
				return capitalise(clearText(spot, r).replaceFirst("^, ", "")) + then;
			case CURE:
				return "Cure the disease with a plant cure";
			case PRUNE:
				return "Prune the disease with secateurs";
			default:
				return r.getSummary();
		}
	}

	static String clearText(Spot spot, Reading r)
	{
		if (r.getAction() == Action.CLEAR && r.getStatus() == Status.READY)
		{
			return ", dig it up";
		}
		if (isTree(spot) && spot.getGardener() >= 0)
		{
			return ", pay the gardener 200 coins to clear it";
		}
		if (spot.getKind() == PatchKind.CRYSTAL_TREE || spot.getKind() == PatchKind.CELASTRUS)
		{
			return ", chop it down and dig up the stump";
		}
		return ", dig it up";
	}

	static String harvestText(Spot spot, String crop)
	{
		switch (spot.getKind())
		{
			case HESPORI:
				return "Defeat Hespori and pick its seeds";
			case SEAWEED:
				return "Pick the giant seaweed";
			case MUSHROOM:
				return "Pick the bittercaps";
			case BELLADONNA:
				return "Pick the nightshade (wear gloves)";
			case CALQUAT:
				return "Pick the calquat fruit and dig up the tree";
			case CELASTRUS:
				return "Harvest the bark, then clear the tree";
			case BUSH:
				return "Pick the berries";
			case CACTUS:
				return "cactus".equals(crop) ? "Pick the cactus spines" : "Pick the potato cacti";
			default:
				return "Harvest the " + crop;
		}
	}

	/** Whether a crop goes in as a sapling (planted with a spade) rather than a seed (with a dibber). */
	public static boolean isSapling(Produce p)
	{
		switch (p.getKind())
		{
			case HARDWOOD_TREE:
			case CALQUAT:
			case SPIRIT_TREE:
			case CELASTRUS:
			case REDWOOD:
			case CRYSTAL_TREE:
				return true;
			default:
				return false;
		}
	}

	/** What goes in the ground, with an article: "a teak sapling", "a seaweed spore". */
	public static String seedName(Produce p)
	{
		switch (p.getKind())
		{
			case SEAWEED:
				return "a seaweed spore";
			case MUSHROOM:
				return "a mushroom spore";
			case HESPORI:
				return "a Hespori seed";
			case BUSH:
				return article(p.getName()) + " bush seed";
			default:
				return article(p.getName()) + (isSapling(p) ? " sapling" : " seed");
		}
	}

	static String article(String name)
	{
		String n = name.toLowerCase();
		boolean vowel = "aeiou".indexOf(n.charAt(0)) >= 0;
		return (vowel ? "an " : "a ") + n;
	}
}
