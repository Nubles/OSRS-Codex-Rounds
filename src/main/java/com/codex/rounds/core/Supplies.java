package com.codex.rounds.core;

import com.codex.rounds.core.farming.Action;
import com.codex.rounds.core.farming.CropState;
import com.codex.rounds.core.farming.PatchKind;
import com.codex.rounds.core.farming.Produce;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.gameval.ItemID;

/** The pull list: everything a round needs, merged and in bank-tab order. */
public final class Supplies
{
	public static final int GARDENER_CLEAR_FEE = 200;
	static final int[] COMPOST_ULTRA = {ItemID.BUCKET_ULTRACOMPOST, ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED};
	static final int[] COMPOST_SUPER = {ItemID.BUCKET_SUPERCOMPOST, ItemID.BUCKET_ULTRACOMPOST, ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED};
	static final int[] SECATEURS = {ItemID.FAIRY_ENCHANTED_SECATEURS, ItemID.SECATEURS};
	static final int[] AXES = {ItemID.DRAGON_AXE, ItemID.CRYSTAL_AXE, ItemID.INFERNAL_AXE, ItemID.TRAILBLAZER_AXE, ItemID.RUNE_AXE,
		ItemID.ADAMANT_AXE, ItemID.MITHRIL_AXE, ItemID.BLACK_AXE, ItemID.STEEL_AXE, ItemID.IRON_AXE, ItemID.BRONZE_AXE,
		ItemID.CRYSTAL_AXE_INACTIVE, ItemID.INFERNAL_AXE_EMPTY};
	/** Any gloves protect from nightshade; these are the pairs most players wear, so the need is optional. */
	static final int[] GLOVES = {ItemID.LEATHER_GLOVES, ItemID.HUNDRED_GAUNTLETS_LEVEL_10, ItemID.HUNDRED_GAUNTLETS_LEVEL_9,
		ItemID.HUNDRED_GAUNTLETS_LEVEL_8, ItemID.FEROCIOUS_GLOVES, ItemID.GRACEFUL_GLOVES, ItemID.ICE_GLOVES};

	private Supplies()
	{
	}

	static List<Need> forStops(List<Stop> stops, Map<Daily, DailyState> dailies, Kingdom kingdom, Settings settings, Ability ability)
	{
		Map<String, Need> lines = new LinkedHashMap<>();
		boolean anyPatch = false;
		boolean rake = false;
		boolean dibber = false;
		boolean spade = false;
		boolean gloves = false;
		boolean cure = false;
		boolean prune = false;
		boolean axe = false;
		int coins = 0;
		List<String> coinWhy = new ArrayList<>();
		int compost = 0;
		int houses = 0;
		int seeds = 0;

		for (Stop stop : stops)
		{
			Travel.Way way = stop.getWay();
			if (way.getLeg() != null && way.getLeg().getKind() == Leg.Kind.ITEM)
			{
				Leg leg = way.getLeg();
				int[] ids = leg.getItems();
				boolean consumable = leg.getLabel().contains("tablet") || leg.getLabel().contains("scroll");
				add(lines, new Need(Need.Kind.TELEPORT, teleportName(leg), 1, stop.getPlace().getLabel(), false, preferOwned(ids, ability)),
					consumable);
			}
			else if (way.getLeg() != null && way.getLeg().getKind() == Leg.Kind.SPELL)
			{
				add(lines, new Need(Need.Kind.TELEPORT, "Runes: " + way.getLeg().getLabel(), 1, stop.getPlace().getLabel(), true, ItemID.LAWRUNE), true);
			}
			if (stop.getPlace() == Place.UNDERWATER)
			{
				add(lines, new Need(Need.Kind.TOOL, "Fishbowl helmet (worn)", 1, "diving", false, ItemID.HUNDRED_PIRATE_DIVING_HELMET), false);
				add(lines, new Need(Need.Kind.TOOL, "Diving apparatus (worn)", 1, "diving", false, ItemID.HUNDRED_PIRATE_DIVING_BACKPACK), false);
				add(lines, new Need(Need.Kind.TOOL, "Flippers", 1, "faster swimming", true, ItemID.MUDSKIPPER_FLIPPERS, ItemID.DARK_FLIPPERS), false);
			}

			for (Task t : stop.getTasks())
			{
				switch (t.getType())
				{
					case DAILY:
						DailyState ds = dailies.get(t.getDaily());
						Need n = ds == null ? null : ds.getNeed();
						if (n != null && n.getKind() == Need.Kind.COINS)
						{
							coins += n.getQuantity();
							coinWhy.add(n.getWhy());
						}
						else if (n != null)
						{
							add(lines, n, true);
						}
						break;
					case KINGDOM:
						int topUp = kingdom == null ? 0 : kingdom.topUp(settings.getCofferTarget());
						if (topUp > settings.getTopUpSlack())
						{
							coins += topUp;
							coinWhy.add("the Miscellania coffer");
						}
						break;
					case SPOT:
						Spot spot = t.getSpot();
						Reading r = t.getReading();
						if (spot.isBirdhouse())
						{
							Birdhouse.State st = r.getHouse();
							if (st == null || st == Birdhouse.State.EMPTY || st == Birdhouse.State.SEEDED)
							{
								houses++;
							}
							seeds += Birdhouse.SEEDS_PER_HOUSE;
							break;
						}
						anyPatch = true;
						Action a = r.getAction();
						if (r.getStatus() == Status.UNSEEN || a == Action.RAKE || t.getPlant() != null)
						{
							rake = rake || r.getStatus() == Status.UNSEEN || a == Action.RAKE;
						}
						if (a == Action.CLEAR || (t.getPlant() != null && Planner.isSapling(t.getPlant())))
						{
							spade = true;
						}
						if (r.getPatch() != null && r.getPatch().getCropState() == CropState.DISEASED)
						{
							cure |= a == Action.CURE;
							prune |= a == Action.PRUNE;
						}
						if (spot.getKind() == PatchKind.BELLADONNA && a == Action.HARVEST)
						{
							gloves = true;
						}
						boolean chops = a == Action.CHOP || (a == Action.CHECK_HEALTH || a == Action.HARVEST)
							&& (spot.getKind() == PatchKind.CELASTRUS || spot.getKind() == PatchKind.CRYSTAL_TREE);
						if (chops && t.getPlant() != null && !(Planner.isTree(spot) && spot.getGardener() >= 0))
						{
							axe = true;
						}
						if (t.isGardenerClear())
						{
							coins += GARDENER_CLEAR_FEE;
							coinWhy.add("gardeners clearing trees");
						}
						Produce plant = t.getPlant();
						if (plant != null)
						{
							dibber |= !Planner.isSapling(plant);
							add(lines, new Need(Need.Kind.PLANT, Planner.capitalise(Planner.seedName(plant).replaceFirst("^an? ", "")), 1,
								spot.getLabel(), r.getStatus() == Status.UNSEEN, plant.getSeedId()), true);
							if (t.isCompost())
							{
								compost++;
							}
							if (t.isProtect())
							{
								int[] pay = plant.getPayment();
								for (int i = 0; i + 1 < pay.length; i += 2)
								{
									add(lines, new Need(Need.Kind.PAYMENT, itemName(pay[i]), pay[i + 1], plant.getName().toLowerCase(), false, pay[i], pay[i] + 1),
										true);
								}
							}
						}
						break;
				}
			}
		}

		if (anyPatch)
		{
			add(lines, new Need(Need.Kind.TOOL, "Spade", 1, "planting and clearing", !spade, ItemID.SPADE), false);
			if (rake)
			{
				add(lines, new Need(Need.Kind.TOOL, "Rake", 1, "weeds", false, ItemID.RAKE), false);
			}
			if (dibber)
			{
				add(lines, new Need(Need.Kind.TOOL, "Seed dibber", 1, "seeds and spores", false, ItemID.DIBBER), false);
			}
			add(lines, new Need(Need.Kind.TOOL, "Magic secateurs", 1, "bigger harvests", !prune, SECATEURS), false);
			if (axe)
			{
				add(lines, new Need(Need.Kind.TOOL, "Axe (any)", 1, "chopping before replanting", false, AXES), false);
			}
			if (gloves)
			{
				add(lines, new Need(Need.Kind.TOOL, "Gloves (any pair)", 1, "nightshade", true, GLOVES), false);
			}
			if (cure)
			{
				add(lines, new Need(Need.Kind.CARE, "Plant cure", 1, "disease", false, ItemID.PLANT_CURE), true);
			}
		}
		if (compost > 0)
		{
			boolean ultra = settings.getCompost() == Settings.Compost.ULTRACOMPOST;
			if (ability.owns(ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED))
			{
				add(lines, new Need(Need.Kind.CARE, "Bottomless compost bucket", 1, compost + " patches", false,
					ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED), false);
			}
			else
			{
				add(lines, new Need(Need.Kind.CARE, ultra ? "Ultracompost" : "Supercompost", compost, "every new planting", false,
					ultra ? COMPOST_ULTRA : COMPOST_SUPER), true);
			}
		}
		if (houses > 0 || seeds > 0)
		{
			Birdhouse.Type type = houseType(ability);
			boolean premade = ability.owns(type.getItemId()) || !ability.isBankKnown();
			if (houses > 0 && premade)
			{
				add(lines, new Need(Need.Kind.BIRDHOUSE, type.getName(), houses, "Fossil Island", false, type.getItemId()), true);
			}
			else if (houses > 0)
			{
				add(lines, new Need(Need.Kind.BIRDHOUSE, logName(type), houses, "building bird houses", false, type.getLogId()), true);
				add(lines, new Need(Need.Kind.BIRDHOUSE, "Clockwork", houses, "building bird houses", false, ItemID.POH_CLOCKWORK_MECHANISM), true);
				add(lines, new Need(Need.Kind.TOOL, "Hammer", 1, "building bird houses", false, ItemID.HAMMER), false);
				add(lines, new Need(Need.Kind.TOOL, "Chisel", 1, "building bird houses", false, ItemID.CHISEL), false);
			}
			add(lines, new Need(Need.Kind.BIRDHOUSE, "Hop seeds", seeds, "ten a bird house", false, seedOrder(ability)), true);
		}
		if (coins > 0)
		{
			add(lines, new Need(Need.Kind.COINS, "Coins", coins, String.join(", ", new java.util.LinkedHashSet<>(coinWhy)), false, ItemID.COINS), true);
		}

		List<Need> out = new ArrayList<>(lines.values());
		out.sort(Comparator.comparing(Need::getKind).thenComparing(Need::isOptional));
		return out;
	}

	/** Merges a line in: stackable lines add quantities, single items stay at one. */
	private static void add(Map<String, Need> lines, Need n, boolean stack)
	{
		String key = n.getKind() + "|" + n.getLabel();
		Need had = lines.get(key);
		if (had == null)
		{
			lines.put(key, n);
		}
		else if (stack)
		{
			lines.put(key, had.plus(n.getQuantity(), n.getWhy()));
		}
		else
		{
			lines.put(key, had.plus(0, n.getWhy()));
		}
	}

	/** The best bird house the account can place, preferring one it already owns. */
	public static Birdhouse.Type houseType(Ability ability)
	{
		Birdhouse.Type best = Birdhouse.Type.bestFor(ability.getHunter());
		for (int i = best.ordinal(); i >= 0; i--)
		{
			Birdhouse.Type t = Birdhouse.Type.values()[i];
			if (ability.owns(t.getItemId()) && ability.count(t.getItemId()) >= 4)
			{
				return t;
			}
		}
		return best;
	}

	/** Any hop seed will do; list the ones the account has most of first. */
	static int[] seedOrder(Ability ability)
	{
		Integer[] ids = new Integer[Birdhouse.SEEDS.length];
		for (int i = 0; i < ids.length; i++)
		{
			ids[i] = Birdhouse.SEEDS[i];
		}
		java.util.Arrays.sort(ids, (a, b) -> Integer.compare(ability.count(b), ability.count(a)));
		int[] out = new int[ids.length];
		for (int i = 0; i < ids.length; i++)
		{
			out[i] = ids[i];
		}
		return out;
	}

	/** Puts the variant the account owns first so the icon and highlight match it. */
	static int[] preferOwned(int[] ids, Ability ability)
	{
		int[] out = ids.clone();
		for (int i = 0; i < out.length; i++)
		{
			if (ability.owns(out[i]))
			{
				int t = out[0];
				out[0] = out[i];
				out[i] = t;
				break;
			}
		}
		return out;
	}

	static String teleportName(Leg leg)
	{
		String l = leg.getLabel();
		int colon = l.indexOf(':');
		return colon > 0 ? l.substring(0, colon) : l;
	}

	static String logName(Birdhouse.Type t)
	{
		switch (t)
		{
			case NORMAL:
				return "Logs";
			default:
				String n = t.name().charAt(0) + t.name().substring(1).toLowerCase();
				return n + " logs";
		}
	}

	static String itemName(int id)
	{
		switch (id)
		{
			case ItemID.CABBAGE:
				return "Cabbages";
			case ItemID.TOMATO:
				return "Tomatoes";
			case ItemID.STRAWBERRY:
				return "Strawberries";
			case ItemID.WATERMELON:
				return "Watermelons";
			case ItemID.BITTERCAP_MUSHROOM:
				return "Bittercap mushrooms";
			case ItemID.CADAVABERRIES:
				return "Cadava berries";
			case ItemID.SNAPE_GRASS:
				return "Snape grass";
			case ItemID.LIMPWURT_ROOT:
				return "Limpwurt roots";
			case ItemID.YANILLIAN_HOPS:
				return "Yanillian hops";
			case ItemID.POISONIVY_BERRIES:
				return "Poison ivy berries";
			case ItemID.CACTUS_POTATO:
				return "Potato cacti";
			case ItemID.DRAGONFRUIT:
				return "Dragonfruit";
			case ItemID.MM_MONKEY_NUTS:
				return "Monkey nuts";
			case ItemID.MM_MONKEY_BAR:
				return "Monkey bar";
			case ItemID.SACK_CABBAGE_10:
				return "Sacks of cabbages";
			case ItemID.BASKET_TOMATO_5:
				return "Baskets of tomatoes";
			case ItemID.BASKET_STRAWBERRY_5:
				return "Baskets of strawberries";
			case ItemID.WHITE_BERRIES:
				return "White berries";
			case ItemID.CURRY_LEAF:
				return "Curry leaves";
			case ItemID.LUNAR_GROUNDTOOTH:
				return "Ground tooth";
			default:
				return "Payment";
		}
	}
}
