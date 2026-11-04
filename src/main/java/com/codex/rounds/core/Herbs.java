package com.codex.rounds.core;

import com.codex.rounds.core.farming.CropState;
import com.codex.rounds.core.farming.PatchKind;
import com.codex.rounds.core.farming.PatchState;
import com.codex.rounds.core.farming.Produce;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntToLongFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.gameval.ItemID;

/**
 * The herb ledger: what your own herb patches give you, learned from your harvests.
 *
 * <p>Each herb crop is followed from the patch's own value (the one Time Tracking reads): planted,
 * diseased, dead, picked clean. Herbs are counted as they are picked, from the grimy herbs that
 * come into the pack, or from Farming xp when they go straight into an open herb sack ("An open
 * sack will automatically store any grimy herbs ... harvested from herb patches", wiki, Herb
 * sack). The compost comes from the game's own message when it is used.
 *
 * <p>The wiki's expected yield sits beside your own: "A fully-grown patch initially has three
 * lives", "one additional life for regular compost, two for supercompost, and three for
 * ultracompost", each pick may save a life, and "the expected yield is" lives / (1 - chance to
 * save). The chance to save and its boosts follow the wiki's herb calculator
 * (Module:Herb Farming calculator), which this re-implements from its published constants.
 */
public final class Herbs
{
	/** "A fully-grown patch initially has three lives" (wiki, Farming). */
	public static final int BASE_LIVES = 3;
	/** CTS high: "CTShigh=80 for all herbs" (wiki, Farming). */
	public static final int CTS_HIGH = 80;
	/** Base chance of disease per growth stage, 27/128 (wiki, Module:Herb Farming calculator). */
	public static final double DISEASE = 27 / 128.0;
	/** Growth stages that can catch disease (the calculator's (1-P)^(n-2) over a five-state herb). */
	public static final int DISEASE_STAGES = 3;
	/** Picks closer together than this are the same herb run. */
	static final int RUN_GAP = 30 * 60;
	/** Crops kept; older ones drop off. */
	static final int KEEP = 400;
	/** Harvests of one herb before your own average stands in for the wiki's. */
	public static final int OWN_MIN = 3;
	/** Ended crops before your own death rate stands in for the wiki's. */
	public static final int OWN_DEATHS_MIN = 6;

	/** The fifteen herbs that can be planted, with the wiki calculator's CTS low and the harvest xp. */
	public enum Herb
	{
		GUAM(Produce.GUAM, ItemID.UNIDENTIFIED_GUAM, 25, 12.5),
		MARRENTILL(Produce.MARRENTILL, ItemID.UNIDENTIFIED_MARENTILL, 28, 15),
		TARROMIN(Produce.TARROMIN, ItemID.UNIDENTIFIED_TARROMIN, 31, 18),
		HARRALANDER(Produce.HARRALANDER, ItemID.UNIDENTIFIED_HARRALANDER, 36, 24),
		RANARR(Produce.RANARR, ItemID.UNIDENTIFIED_RANARR, 39, 30.5),
		TOADFLAX(Produce.TOADFLAX, ItemID.UNIDENTIFIED_TOADFLAX, 43, 38.5),
		IRIT(Produce.IRIT, ItemID.UNIDENTIFIED_IRIT, 46, 48.5),
		AVANTOE(Produce.AVANTOE, ItemID.UNIDENTIFIED_AVANTOE, 50, 61.5),
		KWUARM(Produce.KWUARM, ItemID.UNIDENTIFIED_KWUARM, 54, 78),
		SNAPDRAGON(Produce.SNAPDRAGON, ItemID.UNIDENTIFIED_SNAPDRAGON, 57, 98.5),
		HUASCA(Produce.HUASCA, ItemID.UNIDENTIFIED_HUASCA, 58, 110),
		CADANTINE(Produce.CADANTINE, ItemID.UNIDENTIFIED_CADANTINE, 60, 120),
		LANTADYME(Produce.LANTADYME, ItemID.UNIDENTIFIED_LANTADYME, 64, 151.5),
		DWARF_WEED(Produce.DWARF_WEED, ItemID.UNIDENTIFIED_DWARF_WEED, 67, 192),
		TORSTOL(Produce.TORSTOL, ItemID.UNIDENTIFIED_TORSTOL, 71, 224.5);

		private final Produce produce;
		private final int grimyId;
		private final int ctsLow;
		private final double harvestXp;

		Herb(Produce produce, int grimyId, int ctsLow, double harvestXp)
		{
			this.produce = produce;
			this.grimyId = grimyId;
			this.ctsLow = ctsLow;
			this.harvestXp = harvestXp;
		}

		public Produce getProduce()
		{
			return produce;
		}

		/** The grimy herb that is picked. */
		public int getGrimyId()
		{
			return grimyId;
		}

		public int getSeedId()
		{
			return produce.getSeedId();
		}

		public int getLevel()
		{
			return produce.getLevel();
		}

		public int getCtsLow()
		{
			return ctsLow;
		}

		/** Farming xp for each herb picked (wiki, Herb seed). */
		public double getHarvestXp()
		{
			return harvestXp;
		}

		public String getName()
		{
			return produce.getName();
		}

		public static Herb of(Produce p)
		{
			for (Herb h : values())
			{
				if (h.produce == p)
				{
					return h;
				}
			}
			return null;
		}

		static Herb named(String name)
		{
			try
			{
				return name == null ? null : valueOf(name);
			}
			catch (IllegalArgumentException e)
			{
				return null;
			}
		}
	}

	/** What the soil was treated with: extra harvest lives and how far it cuts disease (wiki, Farming). */
	public enum Compost
	{
		NONE("no compost", 0, 1, -1),
		COMPOST("compost", 1, 2, ItemID.BUCKET_COMPOST),
		SUPERCOMPOST("supercompost", 2, 5, ItemID.BUCKET_SUPERCOMPOST),
		ULTRACOMPOST("ultracompost", 3, 10, ItemID.BUCKET_ULTRACOMPOST);

		private final String label;
		private final int lives;
		private final int diseaseDivisor;
		private final int itemId;

		Compost(String label, int lives, int diseaseDivisor, int itemId)
		{
			this.label = label;
			this.lives = lives;
			this.diseaseDivisor = diseaseDivisor;
			this.itemId = itemId;
		}

		public String getLabel()
		{
			return label;
		}

		public int getLives()
		{
			return lives;
		}

		/** "Compost reduces disease risk by 50%, supercompost by 80%, and ultracompost by 90%." */
		public int getDiseaseDivisor()
		{
			return diseaseDivisor;
		}

		/** The bucket priced for one use, or -1 for none. */
		public int getItemId()
		{
			return itemId;
		}

		static Compost named(String name)
		{
			try
			{
				return name == null ? null : valueOf(name);
			}
			catch (IllegalArgumentException e)
			{
				return null;
			}
		}

		public static Compost of(Settings.Compost c)
		{
			if (c == null)
			{
				return NONE;
			}
			switch (c)
			{
				case ULTRACOMPOST:
					return ULTRACOMPOST;
				case SUPERCOMPOST:
					return SUPERCOMPOST;
				default:
					return NONE;
			}
		}
	}

	/**
	 * The game's compost lines, as RuneLite's Time Tracking reads them (its CompostTracker): using
	 * it on a patch, Fertile Soil, a patch that already has it, and inspecting one.
	 */
	private static final Pattern[] COMPOST_LINES = {
		Pattern.compile("You treat the .+ with (ultra|super|)compost\\."),
		Pattern.compile("^The .+ has been treated with (ultra|super|)compost.*"),
		Pattern.compile("This .+ has already been (?:treated|fertilised) with (ultra|super|)compost.*"),
		Pattern.compile("This is an? .+\\. The soil has been treated with (ultra|super|)compost\\..*"),
	};

	/** The compost a game message says a patch has, or null when it says nothing of it. */
	public static Compost heard(String message)
	{
		if (message == null || !message.contains("compost"))
		{
			return null;
		}
		for (Pattern p : COMPOST_LINES)
		{
			Matcher m = p.matcher(message);
			if (m.matches())
			{
				switch (m.group(1))
				{
					case "ultra":
						return Compost.ULTRACOMPOST;
					case "super":
						return Compost.SUPERCOMPOST;
					default:
						return Compost.COMPOST;
				}
			}
		}
		return null;
	}

	/** What raises the chance to save a life, as worn, carried and unlocked. */
	public static final class Boosts
	{
		/** Magic secateurs worn or in the pack: +10%. */
		public final boolean secateurs;
		/** A Farming or max cape worn: +5%. */
		public final boolean cape;
		/** An attas plant in the anima patch: +5%. */
		public final boolean attas;
		/** Kandarin diary: 0 none or easy, 1 medium, 2 hard, 3 elite (Catherby only). */
		public final int kandarin;
		/** Kourend & Kebos hard diary (Hosidius and the Farming Guild). */
		public final boolean kourendHard;
		/** Kourend & Kebos easy diary: Hosidius patches are disease-free. */
		public final boolean kourendEasy;

		public Boosts(boolean secateurs, boolean cape, boolean attas, int kandarin, boolean kourendHard, boolean kourendEasy)
		{
			this.secateurs = secateurs;
			this.cape = cape;
			this.attas = attas;
			this.kandarin = kandarin;
			this.kourendHard = kourendHard;
			this.kourendEasy = kourendEasy;
		}

		public static final Boosts NONE = new Boosts(false, false, false, 0, false, false);

		/** Item bonus in percent: secateurs 10, cape 5, both 15. */
		int itemPercent()
		{
			return (secateurs ? 10 : 0) + (cape ? 5 : 0);
		}

		/** The flat CTS a diary adds at this patch: Catherby 10/17/25, Hosidius and the Guild 10. */
		int diary(Spot spot)
		{
			if (spot == Spot.HERB_CATHERBY)
			{
				return kandarin >= 3 ? 25 : kandarin == 2 ? 17 : kandarin == 1 ? 10 : 0;
			}
			if ((spot == Spot.HERB_HOSIDIUS || spot == Spot.HERB_GUILD) && kourendHard)
			{
				return 10;
			}
			return 0;
		}
	}

	private Herbs()
	{
	}

	// ---- The wiki's expectation ----

	/**
	 * A CTS constant with the boosts in the calculator's order: item, then diary, then attas.
	 * Kept in whole numbers ("ItemBoost(CTS) = floor(CTS x (1 + ItemBonus))", then "+ DiaryBonus",
	 * then floor(x 1.05) for attas).
	 */
	static int boosted(int cts, Boosts b, Spot spot)
	{
		int c = cts * (100 + b.itemPercent()) / 100;
		c += b.diary(spot);
		if (b.attas)
		{
			c = c * 105 / 100;
		}
		return c;
	}

	/** The chance one pick saves a life: (floor(low(99-F)/98) + floor(high(F-1)/98) + 1) / 256, at most 1. */
	public static double chanceToSave(int low, int high, int level)
	{
		int f = Math.max(1, Math.min(99, level));
		int v = low * (99 - f) / 98 + high * (f - 1) / 98 + 1;
		return Math.max(0, Math.min(1, v / 256.0));
	}

	/** The wiki's expected herbs from one patch that lives to harvest. */
	public static double expectedYield(Herb h, int level, Compost c, Boosts b, Spot spot)
	{
		Boosts boosts = b == null ? Boosts.NONE : b;
		double save = chanceToSave(boosted(h.ctsLow, boosts, spot), boosted(CTS_HIGH, boosts, spot), level);
		if (save >= 1)
		{
			return Double.POSITIVE_INFINITY;
		}
		return (BASE_LIVES + (c == null ? 0 : c.lives)) / (1 - save);
	}

	/** Whether a patch can never catch disease, on the wiki's rules for the herb patches. */
	public static boolean diseaseFree(Spot spot, Boosts b)
	{
		if (spot == null)
		{
			return false;
		}
		switch (spot)
		{
			case HERB_TROLL_STRONGHOLD:
			case HERB_WEISS:
			case HERB_HARMONY:
				return true;
			case HERB_HOSIDIUS:
				return b != null && b.kourendEasy;
			default:
				return false;
		}
	}

	/** The wiki's chance a crop grows through without dying, when disease is left uncured. */
	public static double survival(Compost c, boolean diseaseFree)
	{
		if (diseaseFree)
		{
			return 1;
		}
		double p = DISEASE / (c == null ? 1 : c.diseaseDivisor);
		return Math.pow(1 - p, DISEASE_STAGES);
	}

	// ---- What is remembered ----

	/** One herb crop, from planting (when seen) to the last herb picked or its death. */
	public static final class Crop
	{
		public String spot;
		public String herb;
		/** The compost it had, or null when Rounds did not see. */
		public String compost;
		public long plantedAt;
		/** Whether Rounds saw it planted (so "no compost" is known rather than guessed). */
		public boolean plantSeen;
		public long endedAt;
		public int picked;
		public boolean diseased;
		public boolean died;
		/** The wiki's expected yield for this crop with the boosts it was picked with; 0 when the compost is not known. */
		public double expected;

		public Herb herb()
		{
			return Herb.named(herb);
		}

		public Compost compost()
		{
			return Compost.named(compost);
		}

		public Spot spot()
		{
			try
			{
				return spot == null ? null : Spot.valueOf(spot);
			}
			catch (IllegalArgumentException e)
			{
				return null;
			}
		}

		public boolean harvested()
		{
			return picked > 0;
		}
	}

	/** Everything the ledger keeps for an account, saved with the rest of Rounds' memory. */
	public static final class Memo
	{
		/** Finished crops, oldest first. */
		public List<Crop> crops = new ArrayList<>();
		/** Spot name to the crop growing there now. */
		public Map<String, Crop> growing = new HashMap<>();
		/** Spot name to compost put on an empty patch, waiting for the seed. */
		public Map<String, String> compost = new HashMap<>();
	}

	/** Follows the herb patches from what the game shows. Not thread safe: the client thread only. */
	public static final class Tracker
	{
		private final Memo memo;

		public Tracker(Memo memo)
		{
			this.memo = memo;
			if (memo.crops == null)
			{
				memo.crops = new ArrayList<>();
			}
			if (memo.growing == null)
			{
				memo.growing = new HashMap<>();
			}
			if (memo.compost == null)
			{
				memo.compost = new HashMap<>();
			}
		}

		public Memo getMemo()
		{
			return memo;
		}

		public Crop growing(Spot spot)
		{
			return memo.growing.get(spot.name());
		}

		/**
		 * The patch's value changed from one state to another. Returns the crop that ended with it
		 * (picked clean or dead), or null.
		 */
		public Crop patch(Spot spot, PatchState was, PatchState now, long t)
		{
			if (spot == null || spot.getKind() != PatchKind.HERB || now == null)
			{
				return null;
			}
			String key = spot.name();
			Crop c = memo.growing.get(key);
			if (now.isEmpty())
			{
				return c == null ? null : end(c, t);
			}
			Herb h = Herb.of(now.getProduce());
			if (h == null)
			{
				// Dead herbs show without saying which; goutweed is not a herb run crop.
				if (now.getCropState() == CropState.DEAD && c != null)
				{
					c.died = true;
					return end(c, t);
				}
				return null;
			}
			Crop ended = null;
			if (c != null && !h.name().equals(c.herb))
			{
				// Something else grows there now: the last crop went without Rounds seeing it end.
				ended = end(c, t);
				c = null;
			}
			if (c == null)
			{
				c = start(spot, h, now.getCropState() == CropState.GROWING && now.getStage() == 0
					&& (was == null || was.isEmpty()), t);
			}
			if (now.getCropState() == CropState.DISEASED)
			{
				c.diseased = true;
			}
			else if (now.getCropState() == CropState.DEAD)
			{
				c.died = true;
				return end(c, t);
			}
			return ended;
		}

		private Crop start(Spot spot, Herb h, boolean seen, long t)
		{
			Crop c = new Crop();
			c.spot = spot.name();
			c.herb = h.name();
			c.plantedAt = t;
			c.plantSeen = seen;
			String pending = memo.compost.remove(spot.name());
			c.compost = pending;
			memo.growing.put(spot.name(), c);
			return c;
		}

		/** The game said this patch has compost. */
		public void compost(Spot spot, Compost type)
		{
			if (spot == null || spot.getKind() != PatchKind.HERB || type == null)
			{
				return;
			}
			Crop c = memo.growing.get(spot.name());
			if (c != null && c.picked == 0)
			{
				c.compost = type.name();
			}
			else if (c == null)
			{
				memo.compost.put(spot.name(), type.name());
			}
		}

		/** Herbs picked from the patch, with the boosts and level they were picked with. */
		public void picked(Spot spot, Herb h, int n, int level, Boosts b, long t)
		{
			if (spot == null || h == null || n <= 0)
			{
				return;
			}
			Crop c = memo.growing.get(spot.name());
			if (c == null || !h.name().equals(c.herb))
			{
				if (c != null)
				{
					end(c, t);
				}
				c = start(spot, h, false, t);
			}
			if (c.picked == 0)
			{
				if (c.compost == null && c.plantSeen)
				{
					c.compost = Compost.NONE.name();
				}
				Compost cp = c.compost();
				c.expected = cp == null ? 0 : expectedYield(h, level, cp, b, spot);
			}
			c.picked += n;
		}

		private Crop end(Crop c, long t)
		{
			memo.growing.remove(c.spot);
			memo.compost.remove(c.spot);
			if (!c.harvested() && !c.died)
			{
				// Dug up, or gone while Rounds was not looking: nothing to learn from.
				return null;
			}
			c.endedAt = t;
			memo.crops.add(c);
			while (memo.crops.size() > KEEP)
			{
				memo.crops.remove(0);
			}
			return c;
		}
	}

	// ---- What it adds up to ----

	/** One herb's record from your own harvests. */
	public static final class HerbStat
	{
		public final Herb herb;
		/** Patches picked. */
		public final int harvests;
		public final double mean;
		/** Harvests whose compost was known, and the means over just those. */
		public final int compared;
		public final double comparedMean;
		public final double expectedMean;
		public final Compost usual;

		HerbStat(Herb herb, int harvests, double mean, int compared, double comparedMean, double expectedMean, Compost usual)
		{
			this.herb = herb;
			this.harvests = harvests;
			this.mean = mean;
			this.compared = compared;
			this.comparedMean = comparedMean;
			this.expectedMean = expectedMean;
			this.usual = usual;
		}
	}

	/** One patch's record. */
	public static final class PatchStat
	{
		public final Spot spot;
		public final int crops;
		public final int harvests;
		public final int herbs;
		public final int diseased;
		public final int died;
		public final boolean diseaseFree;

		PatchStat(Spot spot, int crops, int harvests, int herbs, int diseased, int died, boolean diseaseFree)
		{
			this.spot = spot;
			this.crops = crops;
			this.harvests = harvests;
			this.herbs = herbs;
			this.diseased = diseased;
			this.died = died;
			this.diseaseFree = diseaseFree;
		}

		public double mean()
		{
			return harvests == 0 ? 0 : herbs / (double) harvests;
		}
	}

	/** A herb run: crops that ended within half an hour of each other. */
	public static final class Run
	{
		public final long startedAt;
		public final long endedAt;
		public final List<Crop> crops;

		Run(List<Crop> crops)
		{
			this.crops = crops;
			this.startedAt = crops.get(0).endedAt;
			this.endedAt = crops.get(crops.size() - 1).endedAt;
		}

		public int herbs()
		{
			int n = 0;
			for (Crop c : crops)
			{
				n += c.picked;
			}
			return n;
		}

		public int harvested()
		{
			int n = 0;
			for (Crop c : crops)
			{
				n += c.harvested() ? 1 : 0;
			}
			return n;
		}

		public int died()
		{
			int n = 0;
			for (Crop c : crops)
			{
				n += c.died ? 1 : 0;
			}
			return n;
		}

		/** Farming xp from picking (planting and compost xp are left out). */
		public long pickXp()
		{
			double xp = 0;
			for (Crop c : crops)
			{
				Herb h = c.herb();
				if (h != null)
				{
					xp += c.picked * h.harvestXp;
				}
			}
			return Math.round(xp);
		}

		public long profit(IntToLongFunction price)
		{
			long p = 0;
			for (Crop c : crops)
			{
				p += Herbs.profit(c, price);
			}
			return p;
		}
	}

	/** A herb worth planting now, by what it should leave you after the seed and compost. */
	public static final class Pick
	{
		public final Herb herb;
		/** Expected coins a patch, after the seed and compost, death risk included. */
		public final long value;
		public final double yield;
		/** Whether the yield is your own average (else the wiki's). */
		public final boolean own;
		public final int harvests;
		public final long herbPrice;

		Pick(Herb herb, long value, double yield, boolean own, int harvests, long herbPrice)
		{
			this.herb = herb;
			this.value = value;
			this.yield = yield;
			this.own = own;
			this.harvests = harvests;
			this.herbPrice = herbPrice;
		}
	}

	/** Coins one crop made at today's prices: herbs picked less the seed and the compost. */
	public static long profit(Crop c, IntToLongFunction price)
	{
		Herb h = c.herb();
		if (h == null)
		{
			return 0;
		}
		long p = (long) c.picked * price(price, h.grimyId) - price(price, h.getSeedId());
		Compost cp = c.compost();
		if (cp != null && cp.itemId > 0)
		{
			p -= price(price, cp.itemId);
		}
		return p;
	}

	private static long price(IntToLongFunction price, int id)
	{
		return price == null || id <= 0 ? 0 : Math.max(0, price.applyAsLong(id));
	}

	/** Each herb you have picked, most picked first. */
	public static List<HerbStat> herbStats(Memo m)
	{
		Map<Herb, List<Crop>> by = new EnumMap<>(Herb.class);
		for (Crop c : crops(m))
		{
			Herb h = c.herb();
			if (h != null && c.harvested())
			{
				by.computeIfAbsent(h, k -> new ArrayList<>()).add(c);
			}
		}
		List<HerbStat> out = new ArrayList<>();
		for (Map.Entry<Herb, List<Crop>> e : by.entrySet())
		{
			List<Crop> list = e.getValue();
			int sum = 0;
			int cn = 0;
			int csum = 0;
			double esum = 0;
			Map<Compost, Integer> composts = new EnumMap<>(Compost.class);
			for (Crop c : list)
			{
				sum += c.picked;
				if (c.expected > 0 && !Double.isInfinite(c.expected))
				{
					cn++;
					csum += c.picked;
					esum += c.expected;
				}
				Compost cp = c.compost();
				if (cp != null)
				{
					composts.merge(cp, 1, Integer::sum);
				}
			}
			Compost usual = null;
			int best = 0;
			for (Map.Entry<Compost, Integer> ce : composts.entrySet())
			{
				if (ce.getValue() > best)
				{
					best = ce.getValue();
					usual = ce.getKey();
				}
			}
			out.add(new HerbStat(e.getKey(), list.size(), sum / (double) list.size(), cn, cn == 0 ? 0 : csum / (double) cn,
				cn == 0 ? 0 : esum / cn, usual));
		}
		out.sort((a, b) -> b.harvests != a.harvests ? b.harvests - a.harvests : a.herb.compareTo(b.herb));
		return out;
	}

	/** Each herb patch you have grown in, in the board's order. */
	public static List<PatchStat> patchStats(Memo m, Boosts b)
	{
		Map<Spot, int[]> by = new EnumMap<>(Spot.class);
		for (Crop c : crops(m))
		{
			Spot s = c.spot();
			if (s == null)
			{
				continue;
			}
			int[] v = by.computeIfAbsent(s, k -> new int[5]);
			v[0]++;
			v[1] += c.harvested() ? 1 : 0;
			v[2] += c.picked;
			v[3] += c.diseased ? 1 : 0;
			v[4] += c.died ? 1 : 0;
		}
		List<PatchStat> out = new ArrayList<>();
		by.forEach((s, v) -> out.add(new PatchStat(s, v[0], v[1], v[2], v[3], v[4], diseaseFree(s, b))));
		return out;
	}

	/** Your own share of crops that died, over the crops seen to the end; null before there are enough. */
	public static Double deathRate(Memo m)
	{
		int n = 0;
		int dead = 0;
		for (Crop c : crops(m))
		{
			if (c.plantSeen || c.died)
			{
				n++;
				dead += c.died ? 1 : 0;
			}
		}
		return n < OWN_DEATHS_MIN ? null : dead / (double) n;
	}

	/** Your picked herbs against the wiki's expectation for the same crops, as a ratio; null with none to compare. */
	public static Double luck(Memo m)
	{
		double got = 0;
		double exp = 0;
		for (Crop c : crops(m))
		{
			if (c.harvested() && c.expected > 0 && !Double.isInfinite(c.expected))
			{
				got += c.picked;
				exp += c.expected;
			}
		}
		return exp == 0 ? null : got / exp;
	}

	/** Harvests that {@link #luck} rests on. */
	public static int compared(Memo m)
	{
		int n = 0;
		for (Crop c : crops(m))
		{
			if (c.harvested() && c.expected > 0 && !Double.isInfinite(c.expected))
			{
				n++;
			}
		}
		return n;
	}

	/** Herb runs, oldest first. */
	public static List<Run> runs(Memo m)
	{
		List<Run> out = new ArrayList<>();
		List<Crop> cur = new ArrayList<>();
		long last = Long.MIN_VALUE;
		List<Crop> sorted = new ArrayList<>(crops(m));
		sorted.sort((a, b) -> Long.compare(a.endedAt, b.endedAt));
		for (Crop c : sorted)
		{
			if (!cur.isEmpty() && c.endedAt - last > RUN_GAP)
			{
				out.add(new Run(cur));
				cur = new ArrayList<>();
			}
			cur.add(c);
			last = c.endedAt;
		}
		if (!cur.isEmpty())
		{
			out.add(new Run(cur));
		}
		return out;
	}

	/**
	 * The herbs that should leave you the most a patch now: your own average where you have
	 * {@link #OWN_MIN} harvests, the wiki's for your level, compost and boosts otherwise; your own
	 * death rate once you have enough crops, else the wiki's for that compost. Herbs without a
	 * price are left out.
	 */
	public static List<Pick> best(Memo m, int level, Compost usual, Boosts b, IntToLongFunction price)
	{
		Map<Herb, HerbStat> stats = new EnumMap<>(Herb.class);
		for (HerbStat s : herbStats(m))
		{
			stats.put(s.herb, s);
		}
		Compost cp = usual == null ? Compost.NONE : usual;
		Double deaths = deathRate(m);
		double alive = deaths != null ? 1 - deaths : survival(cp, false);
		List<Pick> out = new ArrayList<>();
		for (Herb h : Herb.values())
		{
			if (h.getLevel() > level)
			{
				continue;
			}
			long hp = price(price, h.grimyId);
			if (hp <= 0)
			{
				continue;
			}
			HerbStat s = stats.get(h);
			boolean own = s != null && s.harvests >= OWN_MIN;
			double yield = own ? s.mean : expectedYield(h, level, cp, b, null);
			long cost = price(price, h.getSeedId()) + (cp.itemId > 0 ? price(price, cp.itemId) : 0);
			long value = Math.round(alive * yield * hp) - cost;
			out.add(new Pick(h, value, yield, own, s == null ? 0 : s.harvests, hp));
		}
		out.sort((x, y) -> Long.compare(y.value, x.value));
		return out;
	}

	private static List<Crop> crops(Memo m)
	{
		return m == null || m.crops == null ? Collections.emptyList() : m.crops;
	}

	// ---- What the player is shown ----

	/** Everything the board shows about herb runs. */
	public static final class Shown
	{
		public final int harvests;
		public final List<HerbStat> herbs;
		public final List<PatchStat> patches;
		public final List<Pick> picks;
		public final Run last;
		/** The last run's profit at today's prices. */
		public final long lastProfit;
		public final Double luck;
		public final int compared;
		public final Double deathRate;
		public final Compost usual;

		Shown(int harvests, List<HerbStat> herbs, List<PatchStat> patches, List<Pick> picks, Run last, long lastProfit, Double luck,
			int compared, Double deathRate, Compost usual)
		{
			this.harvests = harvests;
			this.herbs = herbs;
			this.patches = patches;
			this.picks = picks;
			this.last = last;
			this.lastProfit = lastProfit;
			this.luck = luck;
			this.compared = compared;
			this.deathRate = deathRate;
			this.usual = usual;
		}
	}

	public static Shown show(Memo m, int level, Compost usual, Boosts b, IntToLongFunction price)
	{
		List<Run> runs = runs(m);
		Run last = runs.isEmpty() ? null : runs.get(runs.size() - 1);
		int harvests = 0;
		for (Crop c : crops(m))
		{
			harvests += c.harvested() ? 1 : 0;
		}
		return new Shown(harvests, herbStats(m), patchStats(m, b), best(m, level, usual, b, price), last,
			last == null ? 0 : last.profit(price), luck(m), compared(m), deathRate(m), usual);
	}

	/** "+412k" or "-18k" or "+7,400". */
	public static String signed(long coins)
	{
		return (coins < 0 ? "-" : "+") + Format.stack(Math.abs(coins));
	}

	/** "31k", "4.1k", "1.2m", "850": short enough for a badge. */
	public static String compact(long coins)
	{
		long a = Math.abs(coins);
		String out;
		if (a >= 1_000_000)
		{
			out = trim(a / 1_000_000.0) + "m";
		}
		else if (a >= 10_000)
		{
			out = Math.round(a / 1000.0) + "k";
		}
		else if (a >= 1000)
		{
			out = trim(a / 1000.0) + "k";
		}
		else
		{
			out = String.valueOf(a);
		}
		return coins < 0 ? "-" + out : out;
	}

	/** "+31k" or "-2.4k". */
	public static String signedCompact(long coins)
	{
		return coins < 0 ? compact(coins) : "+" + compact(coins);
	}

	private static String trim(double d)
	{
		String s = String.format(Locale.ROOT, "%.1f", d);
		return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
	}

	/** One decimal: "8.4". */
	public static String one(double v)
	{
		return String.format(Locale.ROOT, "%.1f", v);
	}

	/** "+6%" against the wiki, or "level with" it. */
	public static String luckText(double ratio)
	{
		long pct = Math.round((ratio - 1) * 100);
		if (pct == 0)
		{
			return "level with the wiki's expectation";
		}
		return (pct > 0 ? pct + "% above" : -pct + "% below") + " the wiki's expectation";
	}

	/** The chat line when a herb patch is picked clean or dies. */
	public static String summary(Crop c, IntToLongFunction price)
	{
		Herb h = c.herb();
		Spot s = c.spot();
		String where = s == null ? "the patch" : s.getLabel();
		String name = h == null ? "Herbs" : h.getName();
		if (c.died && !c.harvested())
		{
			return name + " at " + where + " died" + (c.diseased ? " of disease" : "") + ".";
		}
		StringBuilder sb = new StringBuilder(name).append(" at ").append(where).append(": ")
			.append(c.picked).append(c.picked == 1 ? " herb" : " herbs");
		if (c.expected > 0 && !Double.isInfinite(c.expected))
		{
			double d = c.picked - c.expected;
			sb.append(" (the wiki expects ").append(one(c.expected)).append(Math.abs(d) < 0.05 ? ")" : ", " + (d > 0 ? "+" : "") + one(d) + ")");
		}
		Compost cp = c.compost();
		if (cp != null)
		{
			sb.append(" with ").append(cp.label);
		}
		if (price != null)
		{
			sb.append(", ").append(signed(profit(c, price))).append(" after seed").append(cp != null && cp != Compost.NONE ? " and compost" : "");
		}
		return sb.append('.').toString();
	}

	/** Herb name to count, for tests and notes. */
	static Map<String, Integer> tally(List<Crop> crops)
	{
		Map<String, Integer> out = new LinkedHashMap<>();
		for (Crop c : crops)
		{
			out.merge(c.herb, c.picked, Integer::sum);
		}
		return out;
	}
}
