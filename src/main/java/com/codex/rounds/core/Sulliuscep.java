package com.codex.rounds.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;

/**
 * The Fossil Island sulliuscep run. From the OSRS wiki: "Chopping sulliusceps requires level 65
 * in Woodcutting and grants 127 experience per yield"; "There are six sulliusceps found
 * throughout the Tar Swamp", "only one is available at any given time", and after one falls "the
 * next sulliuscep will spawn, the location of which cycles through the same sequence", "unique to
 * each player". The strategy guide numbers that sequence "from 1 to 6".
 *
 * <p>The game draws each of the six as its own placement, {@code fossil_cep_multi1..6} in
 * RuneLite's gameval, which shows as the grown "Sulliuscep" or a "Sulliuscep sprout" by the
 * {@code FOSSIL_CEP} varbit. Rounds asks the client which placement is grown, so the cap to cut
 * comes from the game itself; it never guesses from a timer. The order the caps come in is learned
 * from the player's own runs; until a lap has been seen, the game's numbering stands in for it and
 * the next cap is labelled as expected.
 */
public final class Sulliuscep
{
	public static final int LEVEL = 65;
	public static final double XP = 127;
	public static final int CAPS = 6;
	/** The six placements, in the game's own numbering (gameval {@code FOSSIL_CEP_MULTI1..6}). */
	public static final int[] OBJECTS = {
		ObjectID.FOSSIL_CEP_MULTI1, ObjectID.FOSSIL_CEP_MULTI2, ObjectID.FOSSIL_CEP_MULTI3,
		ObjectID.FOSSIL_CEP_MULTI4, ObjectID.FOSSIL_CEP_MULTI5, ObjectID.FOSSIL_CEP_MULTI6
	};
	/** What a placement shows: "Sulliuscep" when it can be cut, "Sulliuscep sprout" otherwise. */
	public static final int GROWN = ObjectID.FOSSIL_CEP_GROWN;
	public static final int SPROUT = ObjectID.FOSSIL_CEP_SPROUT;
	/** The player variable the placements read; kept in the field notes only, its values are not documented. */
	public static final int VARBIT = VarbitID.FOSSIL_CEP;
	/** The six sulliusceps on RuneLite's world map (its RareTreeLocation list), used only to tell when the player is in the swamp. */
	static final Point[] MAP = {
		Point.of(3662, 3802), Point.of(3662, 3781), Point.of(3677, 3806),
		Point.of(3677, 3733), Point.of(3682, 3775), Point.of(3682, 3758)
	};
	/** How near one of the six counts as on the run. */
	static final int SWAMP_RANGE = 22;
	/** Woodcutting xp only counts for the run while the player stands this close to the grown cap. */
	static final int CHOP_RANGE = 3;
	/** Away from the swamp this long and the run is over. */
	static final int LEAVE_SECONDS = 150;
	/** "Nine mushrooms are needed to fill the mud pit in the Tar Swamp" (wiki, Mushroom). */
	public static final int MUD_PIT_MUSHROOMS = 9;
	static final int KEEP_RUNS = 30;
	static final int RATE_RUNS = 10;

	/** What a cut can give, from the wiki's fossil pre-roll and main mushroom tables. */
	public enum Haul
	{
		MUSHROOM(ItemID.BITTERCAP_MUSHROOM, "mushroom", "mushrooms"),
		MORT_MYRE(ItemID.MORTMYREMUSHROOM, "mort myre fungus", "mort myre fungi"),
		CAP(ItemID.FOSSIL_SULLIUSCEP_CAP, "sulliuscep cap", "sulliuscep caps"),
		NUMULITE(ItemID.FOSSIL_NUMULITE, "numulite", "numulites"),
		SMALL_FOSSIL(ItemID.FOSSIL_SMALL_UNID, "small fossil", "small fossils"),
		MEDIUM_FOSSIL(ItemID.FOSSIL_MEDIUM_UNID, "medium fossil", "medium fossils"),
		LARGE_FOSSIL(ItemID.FOSSIL_LARGE_UNID, "large fossil", "large fossils"),
		RARE_FOSSIL(ItemID.FOSSIL_RARE_UNID, "rare fossil", "rare fossils");

		private final int itemId;
		private final String one;
		private final String many;

		Haul(int itemId, String one, String many)
		{
			this.itemId = itemId;
			this.one = one;
			this.many = many;
		}

		public int getItemId()
		{
			return itemId;
		}

		public String name(int n)
		{
			return Format.number(n) + " " + (n == 1 ? one : many);
		}

		static Haul of(int itemId)
		{
			for (Haul h : values())
			{
				if (h.itemId == itemId)
				{
					return h;
				}
			}
			return null;
		}
	}

	private Sulliuscep()
	{
	}

	/** The cap (1 to 6) a placement is, or 0 when it is not one of them. */
	public static int cap(int objectId)
	{
		for (int i = 0; i < OBJECTS.length; i++)
		{
			if (OBJECTS[i] == objectId)
			{
				return i + 1;
			}
		}
		return 0;
	}

	/** Whether a point is in the Tar Swamp, near enough to one of the six. */
	public static boolean inSwamp(Point p)
	{
		if (p == null || p.getPlane() != 0)
		{
			return false;
		}
		for (Point m : MAP)
		{
			if (Math.abs(m.getX() - p.getX()) <= SWAMP_RANGE && Math.abs(m.getY() - p.getY()) <= SWAMP_RANGE)
			{
				return true;
			}
		}
		return false;
	}

	/** One run as it ended, saved with the rest of Rounds' memory. */
	public static final class RunRecord
	{
		public long startedAt;
		public int seconds;
		/** All six fell. */
		public boolean complete;
		public long xp;
		/** Caps in the order they fell, and seconds into the run each one did. */
		public List<Integer> fell = new ArrayList<>();
		public List<Integer> splits = new ArrayList<>();
		/** Haul name to how many came in. */
		public Map<String, Integer> haul = new LinkedHashMap<>();

		public int count(Haul h)
		{
			Integer n = haul == null ? null : haul.get(h.name());
			return n == null ? 0 : n;
		}
	}

	/** What Rounds remembers of the account's sulliuscep runs. */
	public static final class Memo
	{
		/** Finished runs, newest last. */
		public List<RunRecord> runs = new ArrayList<>();
		/** Cap to the cap seen to grow after it, from the player's own runs. */
		public Map<Integer, Integer> after = new HashMap<>();
		/** Cap to where its placement stands, as x, y, plane, read from the scene. */
		public Map<Integer, int[]> where = new HashMap<>();
	}

	/** A run in progress; not saved, a run ends when the player leaves or logs out. */
	public static final class Run
	{
		final long startedAt;
		final List<Integer> fell = new ArrayList<>();
		final List<Integer> splits = new ArrayList<>();
		final Map<Haul, Integer> haul = new EnumMap<>(Haul.class);
		long xp;
		Long leftAt;

		Run(long startedAt)
		{
			this.startedAt = startedAt;
		}

		public long getStartedAt()
		{
			return startedAt;
		}

		public List<Integer> getFell()
		{
			return Collections.unmodifiableList(fell);
		}

		public List<Integer> getSplits()
		{
			return Collections.unmodifiableList(splits);
		}

		public long getXp()
		{
			return xp;
		}

		public int count(Haul h)
		{
			return haul.getOrDefault(h, 0);
		}
	}

	/**
	 * Follows the run from what the game shows each tick. Feed it the grown cap, where the player
	 * stands, Woodcutting xp gains and pack changes; it says when a run starts, which caps fell and
	 * when it ends.
	 */
	public static final class Tracker
	{
		private final Memo memo;
		private Run run;
		private int active;

		public Tracker(Memo memo)
		{
			this.memo = memo;
		}

		public Memo getMemo()
		{
			return memo;
		}

		public Run getRun()
		{
			return run;
		}

		/** The grown cap as last read, 0 when unknown. */
		public int getActive()
		{
			return active;
		}

		/** Notes where a placement stands, from the scene. Returns true when it was new. */
		public boolean place(int cap, Point p)
		{
			if (cap < 1 || cap > CAPS || p == null)
			{
				return false;
			}
			int[] old = memo.where.get(cap);
			if (old != null && old[0] == p.getX() && old[1] == p.getY() && old[2] == p.getPlane())
			{
				return false;
			}
			memo.where.put(cap, new int[]{p.getX(), p.getY(), p.getPlane()});
			return true;
		}

		public Point where(int cap)
		{
			int[] w = memo.where.get(cap);
			return w == null ? null : Point.of(w[0], w[1], w[2]);
		}

		/**
		 * One tick: which cap is grown (0 when the game shows none) and where the player is.
		 * Returns the run that ended this tick, or null.
		 */
		public RunRecord tick(int grown, Point at, long now)
		{
			boolean swamp = inSwamp(at);
			RunRecord ended = null;
			if (grown != 0 && active != 0 && grown != active)
			{
				// The grown cap moved on: the last one fell (cut down, or killed with fungicide).
				memo.after.put(active, grown);
				if (run == null && swamp)
				{
					run = new Run(now);
				}
				if (run != null && !run.fell.contains(active))
				{
					run.fell.add(active);
					run.splits.add((int) (now - run.startedAt));
					if (run.fell.size() >= CAPS)
					{
						ended = finish(now);
					}
				}
			}
			if (grown != 0)
			{
				active = grown;
			}
			if (run != null)
			{
				if (swamp)
				{
					run.leftAt = null;
				}
				else if (run.leftAt == null)
				{
					run.leftAt = now;
				}
				else if (now - run.leftAt >= LEAVE_SECONDS)
				{
					ended = finish(run.leftAt);
				}
			}
			return ended;
		}

		/** Woodcutting xp gained this tick; it counts when the player stands by the grown cap. */
		public void xp(int gained, Point at, long now)
		{
			if (gained <= 0 || at == null || !inSwamp(at))
			{
				return;
			}
			Point cap = where(active);
			if (cap != null && (Math.abs(cap.getX() - at.getX()) > CHOP_RANGE || Math.abs(cap.getY() - at.getY()) > CHOP_RANGE))
			{
				return;
			}
			if (run == null)
			{
				run = new Run(now);
			}
			run.xp += gained;
		}

		/** Items that came into the pack this tick, counted when they are part of the haul and a run is on. */
		public void gained(Map<Integer, Integer> before, Map<Integer, Integer> after, Point at)
		{
			if (run == null || !inSwamp(at) || before == null || after == null)
			{
				return;
			}
			for (Haul h : Haul.values())
			{
				int d = after.getOrDefault(h.getItemId(), 0) - before.getOrDefault(h.getItemId(), 0);
				if (d > 0)
				{
					run.haul.merge(h, d, Integer::sum);
				}
			}
		}

		/** Logging out or hopping ends the run where it stands. */
		public RunRecord abandon(long now)
		{
			if (run == null)
			{
				return null;
			}
			return finish(run.leftAt != null ? run.leftAt : now);
		}

		private RunRecord finish(long end)
		{
			Run r = run;
			run = null;
			if (r.fell.isEmpty() && r.xp == 0)
			{
				return null;
			}
			RunRecord rec = new RunRecord();
			rec.startedAt = r.startedAt;
			rec.seconds = (int) Math.max(1, end - r.startedAt);
			rec.complete = r.fell.size() >= CAPS;
			rec.xp = r.xp;
			rec.fell = new ArrayList<>(r.fell);
			rec.splits = new ArrayList<>(r.splits);
			for (Map.Entry<Haul, Integer> e : r.haul.entrySet())
			{
				rec.haul.put(e.getKey().name(), e.getValue());
			}
			if (memo.runs == null)
			{
				memo.runs = new ArrayList<>();
			}
			memo.runs.add(rec);
			while (memo.runs.size() > KEEP_RUNS)
			{
				memo.runs.remove(0);
			}
			return rec;
		}
	}

	// ---- What the player is shown ----

	/** The fastest run where all six fell, or null. */
	public static RunRecord best(Memo m)
	{
		RunRecord best = null;
		if (m == null || m.runs == null)
		{
			return null;
		}
		for (RunRecord r : m.runs)
		{
			if (r.complete && (best == null || r.seconds < best.seconds))
			{
				best = r;
			}
		}
		return best;
	}

	/** Woodcutting xp an hour over the last runs that cut anything, or 0 before there are any. */
	public static int xpPerHour(Memo m)
	{
		if (m == null || m.runs == null)
		{
			return 0;
		}
		long xp = 0;
		long secs = 0;
		int n = 0;
		for (int i = m.runs.size() - 1; i >= 0 && n < RATE_RUNS; i--)
		{
			RunRecord r = m.runs.get(i);
			if (r.xp <= 0 || r.seconds < 30)
			{
				continue;
			}
			xp += r.xp;
			secs += r.seconds;
			n++;
		}
		return secs == 0 ? 0 : (int) Math.round(xp * 3600.0 / secs);
	}

	/** Runs that went into {@link #xpPerHour}. */
	public static int rateRuns(Memo m)
	{
		int n = 0;
		if (m == null || m.runs == null)
		{
			return 0;
		}
		for (int i = m.runs.size() - 1; i >= 0 && n < RATE_RUNS; i--)
		{
			RunRecord r = m.runs.get(i);
			if (r.xp > 0 && r.seconds >= 30)
			{
				n++;
			}
		}
		return n;
	}

	/** The haul per full run, averaged over the full runs kept. */
	public static Map<Haul, Double> haulPerRun(Memo m)
	{
		Map<Haul, Double> out = new LinkedHashMap<>();
		int runs = 0;
		if (m != null && m.runs != null)
		{
			for (RunRecord r : m.runs)
			{
				if (!r.complete)
				{
					continue;
				}
				runs++;
				for (Haul h : Haul.values())
				{
					out.merge(h, (double) r.count(h), Double::sum);
				}
			}
		}
		if (runs == 0)
		{
			return Collections.emptyMap();
		}
		final int n = runs;
		out.replaceAll((h, v) -> v / n);
		out.values().removeIf(v -> v == 0);
		return out;
	}

	/**
	 * The order the caps come in. Once the player's runs have shown a whole lap (each cap followed by
	 * another, all six in one cycle) it is that lap, starting from cap 1; until then it is the game's
	 * own numbering.
	 */
	public static int[] order(Memo m)
	{
		int[] out = new int[CAPS];
		if (learned(m))
		{
			int c = 1;
			for (int i = 0; i < CAPS; i++)
			{
				out[i] = c;
				c = m.after.get(c);
			}
			return out;
		}
		for (int i = 0; i < CAPS; i++)
		{
			out[i] = i + 1;
		}
		return out;
	}

	/** Whether the player's own runs have shown the whole cycle. */
	public static boolean learned(Memo m)
	{
		if (m == null || m.after == null || m.after.size() < CAPS)
		{
			return false;
		}
		int c = 1;
		boolean[] seen = new boolean[CAPS + 1];
		for (int i = 0; i < CAPS; i++)
		{
			Integer n = m.after.get(c);
			if (n == null || n < 1 || n > CAPS || seen[c])
			{
				return false;
			}
			seen[c] = true;
			c = n;
		}
		return c == 1;
	}

	/** The cap that grows after this one: seen in the player's runs, or else the next number. */
	public static int next(Memo m, int cap)
	{
		Integer n = m == null || m.after == null ? null : m.after.get(cap);
		return n != null ? n : cap % CAPS + 1;
	}

	/** Whether {@link #next} comes from the player's own runs. */
	public static boolean nextSeen(Memo m, int cap)
	{
		return m != null && m.after != null && m.after.containsKey(cap);
	}

	/** "12 tiles north-east", from the player to a point; null when either is unknown. */
	public static String direction(Point from, Point to)
	{
		if (from == null || to == null)
		{
			return null;
		}
		int dx = to.getX() - from.getX();
		int dy = to.getY() - from.getY();
		int d = Math.max(Math.abs(dx), Math.abs(dy));
		if (d <= 1)
		{
			return "right beside you";
		}
		String ns = Math.abs(dy) * 2 < Math.abs(dx) ? "" : dy > 0 ? "north" : "south";
		String ew = Math.abs(dx) * 2 < Math.abs(dy) ? "" : dx > 0 ? "east" : "west";
		String way = ns.isEmpty() ? ew : ew.isEmpty() ? ns : ns + "-" + ew;
		return d + " tiles " + way;
	}

	/** Everything the panel and plaque show about the run. */
	public static final class Shown
	{
		/** The grown cap, 0 when not read yet. */
		public final int active;
		public final int[] order;
		public final boolean orderLearned;
		public final List<Integer> fell;
		public final boolean running;
		public final int elapsed;
		/** The fastest full run, or null. */
		public final RunRecord best;
		/** Caps the fastest run had down by this point of the run. */
		public final int ghostFell;
		/** Seconds behind (positive) or ahead of the fastest run at the last cap that fell, or null. */
		public final Integer lastDiff;
		public final Map<Haul, Integer> haul;
		public final long xp;
		public final int xpHour;
		public final int rateRuns;
		public final RunRecord last;
		public final String title;
		public final String detail;
		public final int next;
		public final boolean nextSeen;
		public final Map<Haul, Double> perRun;

		Shown(int active, int[] order, boolean orderLearned, List<Integer> fell, boolean running, int elapsed, RunRecord best, int ghostFell,
			Integer lastDiff, Map<Haul, Integer> haul, long xp, int xpHour, int rateRuns, RunRecord last, String title, String detail,
			int next, boolean nextSeen, Map<Haul, Double> perRun)
		{
			this.active = active;
			this.order = order;
			this.orderLearned = orderLearned;
			this.fell = fell;
			this.running = running;
			this.elapsed = elapsed;
			this.best = best;
			this.ghostFell = ghostFell;
			this.lastDiff = lastDiff;
			this.haul = haul;
			this.xp = xp;
			this.xpHour = xpHour;
			this.rateRuns = rateRuns;
			this.last = last;
			this.title = title;
			this.detail = detail;
			this.next = next;
			this.nextSeen = nextSeen;
			this.perRun = perRun;
		}
	}

	/** What to show now, from the tracker, where the player stands and the clock. */
	public static Shown show(Tracker t, Point at, long now)
	{
		Memo m = t.getMemo();
		Run run = t.getRun();
		int active = t.getActive();
		RunRecord best = best(m);
		List<Integer> fell = run == null ? Collections.emptyList() : new ArrayList<>(run.fell);
		int elapsed = run == null ? 0 : (int) Math.max(0, now - run.startedAt);
		int ghost = 0;
		Integer diff = null;
		if (best != null && run != null)
		{
			for (int s : best.splits)
			{
				if (s <= elapsed)
				{
					ghost++;
				}
			}
			int k = run.splits.size();
			if (k > 0 && best.splits.size() >= k)
			{
				diff = run.splits.get(k - 1) - best.splits.get(k - 1);
			}
		}
		Map<Haul, Integer> haul = new LinkedHashMap<>();
		if (run != null)
		{
			for (Haul h : Haul.values())
			{
				if (run.count(h) > 0)
				{
					haul.put(h, run.count(h));
				}
			}
		}
		String title;
		String detail;
		int next = active == 0 ? 0 : next(m, active);
		boolean seen = active != 0 && nextSeen(m, active);
		if (active == 0)
		{
			title = "Find the grown sulliuscep";
			detail = "Rounds reads which of the six is grown once you are on Fossil Island.";
		}
		else
		{
			title = "Cut sulliuscep " + active;
			String where = direction(at, t.where(active));
			detail = (where != null ? where + ". " : "") + "Then " + next + (seen ? "" : " (expected)") + ".";
		}
		RunRecord last = m.runs == null || m.runs.isEmpty() ? null : m.runs.get(m.runs.size() - 1);
		return new Shown(active, order(m), learned(m), fell, run != null, elapsed, best, ghost, diff, haul,
			run == null ? 0 : run.xp, xpPerHour(m), rateRuns(m), last, title, detail, next, seen, haulPerRun(m));
	}

	/** Against the ghost of the fastest run: "0:12 behind your best at cap 3." or "Your best had 2 down by now." */
	public static String ghostText(Shown c)
	{
		if (c.best == null || !c.running)
		{
			return null;
		}
		if (c.lastDiff == null)
		{
			return "Your best had " + c.ghostFell + " down by now.";
		}
		int k = c.fell.size();
		if (c.lastDiff == 0)
		{
			return "Level with your best at the " + ordinal(k) + " cap.";
		}
		return Format.clock(Math.abs(c.lastDiff)) + (c.lastDiff > 0 ? " behind" : " ahead of") + " your best at the " + ordinal(k) + " cap.";
	}

	static String ordinal(int n)
	{
		switch (n)
		{
			case 1:
				return "first";
			case 2:
				return "second";
			case 3:
				return "third";
			case 4:
				return "fourth";
			case 5:
				return "fifth";
			default:
				return "sixth";
		}
	}

	/** "41 mushrooms, 2 mort myre fungi and 1 sulliuscep cap"; null when nothing came in. */
	public static String haulText(Map<Haul, ? extends Number> haul)
	{
		List<String> parts = new ArrayList<>();
		for (Map.Entry<Haul, ? extends Number> e : haul.entrySet())
		{
			double v = e.getValue().doubleValue();
			if (v <= 0)
			{
				continue;
			}
			int n = (int) Math.round(v);
			parts.add(n == 0 ? "under 1 " + e.getKey().one : e.getKey().name(n));
		}
		if (parts.isEmpty())
		{
			return null;
		}
		if (parts.size() == 1)
		{
			return parts.get(0);
		}
		return String.join(", ", parts.subList(0, parts.size() - 1)) + " and " + parts.get(parts.size() - 1);
	}

	/** The chat line when a run ends. */
	public static String summary(RunRecord r, Memo m)
	{
		StringBuilder sb = new StringBuilder("sulliuscep run ");
		sb.append(r.complete ? "complete in " : "ended after ").append(Format.clock(r.seconds));
		sb.append(", ").append(r.fell.size()).append(r.fell.size() == 1 ? " cap fell" : " caps fell");
		RunRecord best = best(m);
		if (r.complete && best == r)
		{
			sb.append(", your fastest yet");
		}
		else if (r.complete && best != null)
		{
			sb.append(" (best ").append(Format.clock(best.seconds)).append(")");
		}
		Map<Haul, Integer> h = new LinkedHashMap<>();
		for (Haul x : Haul.values())
		{
			if (r.count(x) > 0)
			{
				h.put(x, r.count(x));
			}
		}
		String haul = haulText(h);
		if (haul != null)
		{
			sb.append(". ").append(Character.toUpperCase(haul.charAt(0))).append(haul.substring(1));
		}
		if (r.xp > 0 && r.seconds >= 30)
		{
			sb.append(". ").append(Format.number(Math.round(r.xp * 3600.0 / r.seconds))).append(" Woodcutting xp an hour");
		}
		return sb.append('.').toString();
	}
}
