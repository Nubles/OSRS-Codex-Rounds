package com.codex.rounds.core;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SulliuscepTest
{
	static final long T0 = CoreTest.T0;
	/** Where RuneLite's world map puts the six (RareTreeLocation), given to the six placements in the game's numbering for these tests. */
	static final Point[] AT = {
		Point.of(3662, 3802), Point.of(3662, 3781), Point.of(3677, 3806),
		Point.of(3677, 3733), Point.of(3682, 3775), Point.of(3682, 3758)
	};
	static final Point CAMP = Point.of(3724, 3808);

	@Test
	public void factsFromTheWikiAndGameval()
	{
		// "requires level 65 in Woodcutting and grants 127 experience per yield" (wiki); WoodcuttingAction SULLIUSCEPS(.., 65, 127).
		assertEquals(65, Sulliuscep.LEVEL);
		assertEquals(127, Sulliuscep.XP, 0);
		// "There are six sulliusceps found throughout the Tar Swamp".
		assertEquals(6, Sulliuscep.CAPS);
		assertEquals(6, Sulliuscep.OBJECTS.length);
		assertEquals(31420, ObjectID.FOSSIL_CEP_MULTI1);
		assertEquals(31425, ObjectID.FOSSIL_CEP_MULTI6);
		assertEquals(30602, Sulliuscep.GROWN);
		assertEquals(30603, Sulliuscep.SPROUT);
		assertEquals(5808, VarbitID.FOSSIL_CEP);
		// Item ids the wiki gives for the yields: Mushroom 6004, Sulliuscep cap 21626.
		assertEquals(6004, Sulliuscep.Haul.MUSHROOM.getItemId());
		assertEquals(21626, Sulliuscep.Haul.CAP.getItemId());
		assertEquals(ItemID.MORTMYREMUSHROOM, Sulliuscep.Haul.MORT_MYRE.getItemId());
		assertEquals(9, Sulliuscep.MUD_PIT_MUSHROOMS);
	}

	@Test
	public void capNumbersFollowTheGamesPlacements()
	{
		assertEquals(1, Sulliuscep.cap(ObjectID.FOSSIL_CEP_MULTI1));
		assertEquals(4, Sulliuscep.cap(ObjectID.FOSSIL_CEP_MULTI4));
		assertEquals(0, Sulliuscep.cap(ObjectID.FOSSIL_CEP_GROWN));
		assertEquals(0, Sulliuscep.cap(1));
	}

	@Test
	public void swampIsAroundTheSix()
	{
		for (Point p : AT)
		{
			assertTrue(Sulliuscep.inSwamp(p));
		}
		assertFalse(Sulliuscep.inSwamp(CAMP));
		assertFalse(Sulliuscep.inSwamp(Point.of(3662, 3802, 1)));
		assertFalse(Sulliuscep.inSwamp(null));
	}

	/** A scripted run, tick by tick, the way the plugin feeds the tracker. */
	static final class Replay
	{
		final Sulliuscep.Memo memo = new Sulliuscep.Memo();
		final Sulliuscep.Tracker t = new Sulliuscep.Tracker(memo);
		long now = T0;
		Map<Integer, Integer> pack = new HashMap<>();
		Sulliuscep.RunRecord ended;

		Replay()
		{
			for (int i = 0; i < 6; i++)
			{
				t.place(i + 1, AT[i]);
			}
		}

		void tick(int grown, Point at)
		{
			Sulliuscep.RunRecord e = t.tick(grown, at, now);
			if (e != null)
			{
				ended = e;
			}
			now += 1;
		}

		void get(int item, int n)
		{
			Map<Integer, Integer> next = new HashMap<>(pack);
			next.merge(item, n, Integer::sum);
			t.gained(pack, next, AT[Math.max(0, t.getActive() - 1)]);
			pack = next;
		}

		/** Chops the grown cap {@code chops} times, then it falls and {@code then} grows. */
		void cut(int cap, int chops, int then, int seconds)
		{
			Point at = AT[cap - 1];
			tick(cap, at);
			for (int i = 0; i < chops; i++)
			{
				t.xp(127, at, now);
				get(i % 10 == 9 ? ItemID.MORTMYREMUSHROOM : ItemID.BITTERCAP_MUSHROOM, 1);
			}
			now += seconds;
			tick(then, at);
		}

		void lap(int[] order, int seconds)
		{
			for (int i = 0; i < order.length; i++)
			{
				cut(order[i], 16, order[(i + 1) % order.length], seconds);
			}
		}
	}

	@Test
	public void aFullLapIsOneRunWithTheCapsInTheOrderTheyFell()
	{
		Replay r = new Replay();
		r.tick(1, CAMP);
		assertNull("Nothing starts off the swamp", r.t.getRun());
		r.lap(new int[]{1, 2, 3, 4, 5, 6}, 60);
		assertNotNull(r.ended);
		assertTrue(r.ended.complete);
		assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), r.ended.fell);
		assertEquals(6, r.ended.splits.size());
		assertEquals(6 * 16 * 127, r.ended.xp);
		assertEquals(6 * 15, r.ended.count(Sulliuscep.Haul.MUSHROOM));
		assertEquals(6, r.ended.count(Sulliuscep.Haul.MORT_MYRE));
		assertNull("The run is over", r.t.getRun());
		assertEquals(1, r.memo.runs.size());
		assertTrue("A whole lap teaches the order", Sulliuscep.learned(r.memo));
		assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6}, Sulliuscep.order(r.memo));
	}

	@Test
	public void theOrderIsLearnedFromTheGameNotAssumed()
	{
		Replay r = new Replay();
		int[] seen = {2, 5, 1, 6, 3, 4};
		assertArrayEquals("Before a lap: the game's numbering", new int[]{1, 2, 3, 4, 5, 6}, Sulliuscep.order(r.memo));
		assertEquals(3, Sulliuscep.next(r.memo, 2));
		assertFalse(Sulliuscep.nextSeen(r.memo, 2));
		r.lap(seen, 40);
		assertTrue(Sulliuscep.learned(r.memo));
		assertArrayEquals(new int[]{1, 6, 3, 4, 2, 5}, Sulliuscep.order(r.memo));
		assertEquals(5, Sulliuscep.next(r.memo, 2));
		assertTrue(Sulliuscep.nextSeen(r.memo, 2));
	}

	@Test
	public void theGhostIsTheFastestFullRun()
	{
		Replay r = new Replay();
		int[] lap = {1, 2, 3, 4, 5, 6};
		r.lap(lap, 70);
		Sulliuscep.RunRecord slow = r.ended;
		r.now += 600;
		r.lap(lap, 50);
		Sulliuscep.RunRecord fast = r.ended;
		assertTrue(fast.seconds < slow.seconds);
		assertEquals(fast, Sulliuscep.best(r.memo));
		assertTrue(Sulliuscep.summary(fast, r.memo).contains("your fastest yet"));
		assertTrue(Sulliuscep.summary(slow, r.memo).contains("(best " + Format.clock(fast.seconds) + ")"));

		// A third run, slower: two caps down, the plaque compares the second fall with the best run's.
		r.now += 600;
		r.cut(1, 16, 2, 60);
		r.cut(2, 16, 3, 60);
		Sulliuscep.Shown s = Sulliuscep.show(r.t, AT[2], r.now);
		assertTrue(s.running);
		assertEquals(Arrays.asList(1, 2), s.fell);
		assertNotNull(s.lastDiff);
		assertEquals(fast.splits.get(1) < r.t.getRun().getSplits().get(1), s.lastDiff > 0);
		assertTrue(Sulliuscep.ghostText(s), Sulliuscep.ghostText(s).endsWith("behind your best at the second cap."));
		assertTrue(s.ghostFell >= 2);
		assertEquals("Cut sulliuscep 3", s.title);
		assertTrue(s.detail, s.detail.startsWith("right beside you. Then 4."));
	}

	@Test
	public void leavingTheSwampEndsAPartRun()
	{
		Replay r = new Replay();
		r.cut(1, 16, 2, 60);
		r.cut(2, 10, 2, 30);
		assertNotNull(r.t.getRun());
		r.tick(2, CAMP);
		long left = r.now - 1;
		for (int i = 0; i < Sulliuscep.LEAVE_SECONDS; i++)
		{
			r.tick(2, CAMP);
		}
		assertNotNull(r.ended);
		assertFalse(r.ended.complete);
		assertEquals(Arrays.asList(1), r.ended.fell);
		assertEquals("The run ends when the player left", left - r.ended.startedAt, r.ended.seconds);
		assertNull("A part run is never the best", Sulliuscep.best(r.memo));
		assertTrue(Sulliuscep.xpPerHour(r.memo) > 0);
		assertTrue(Sulliuscep.summary(r.ended, r.memo).startsWith("sulliuscep run ended after"));
	}

	@Test
	public void comingBackInTimeKeepsTheRun()
	{
		Replay r = new Replay();
		r.cut(1, 16, 2, 60);
		for (int i = 0; i < 30; i++)
		{
			r.tick(2, CAMP);
		}
		r.cut(2, 16, 3, 60);
		assertNull(r.ended);
		assertEquals(Arrays.asList(1, 2), r.t.getRun().getFell());
	}

	@Test
	public void logoutEndsTheRunAndAnEmptyRunIsNotKept()
	{
		Replay r = new Replay();
		r.tick(3, AT[2]);
		assertNull(r.t.abandon(r.now));
		r.cut(3, 16, 4, 60);
		Sulliuscep.RunRecord e = r.t.abandon(r.now);
		assertNotNull(e);
		assertEquals(1, r.memo.runs.size());
		assertNull(r.t.getRun());
	}

	@Test
	public void xpOnlyCountsBesideTheGrownCap()
	{
		Replay r = new Replay();
		r.tick(1, AT[0]);
		// Chopping elsewhere in the swamp (roots, vines) is not the sulliuscep.
		r.t.xp(50, AT[3], r.now);
		assertNull(r.t.getRun());
		r.t.xp(127, AT[0], r.now);
		assertEquals(127, r.t.getRun().getXp());
		// Off the swamp nothing counts.
		r.t.xp(127, CAMP, r.now);
		assertEquals(127, r.t.getRun().getXp());
	}

	@Test
	public void xpAnHourComesFromTheRunsThemselves()
	{
		Replay r = new Replay();
		r.lap(new int[]{1, 2, 3, 4, 5, 6}, 60);
		Sulliuscep.RunRecord a = r.ended;
		long expected = Math.round(a.xp * 3600.0 / a.seconds);
		assertEquals(expected, Sulliuscep.xpPerHour(r.memo));
		assertEquals(1, Sulliuscep.rateRuns(r.memo));
		Map<Sulliuscep.Haul, Double> per = Sulliuscep.haulPerRun(r.memo);
		assertEquals(6.0, per.get(Sulliuscep.Haul.MORT_MYRE), 0.001);
		assertFalse(per.containsKey(Sulliuscep.Haul.CAP));
	}

	@Test
	public void haulReadsLikeASentence()
	{
		Map<Sulliuscep.Haul, Integer> h = new java.util.LinkedHashMap<>();
		h.put(Sulliuscep.Haul.MUSHROOM, 41);
		h.put(Sulliuscep.Haul.MORT_MYRE, 2);
		h.put(Sulliuscep.Haul.CAP, 1);
		assertEquals("41 mushrooms, 2 mort myre fungi and 1 sulliuscep cap", Sulliuscep.haulText(h));
		assertNull(Sulliuscep.haulText(new HashMap<Sulliuscep.Haul, Integer>()));
	}

	@Test
	public void directionReadsFromThePlayer()
	{
		assertEquals("12 tiles north-east", Sulliuscep.direction(Point.of(0, 0), Point.of(12, 10)));
		assertEquals("20 tiles south", Sulliuscep.direction(Point.of(0, 0), Point.of(2, -20)));
		assertEquals("right beside you", Sulliuscep.direction(Point.of(0, 0), Point.of(1, 1)));
		assertNull(Sulliuscep.direction(null, Point.of(1, 1)));
	}

	@Test
	public void savedRunsSurviveGsonAndOldSavesLoad()
	{
		Replay r = new Replay();
		r.lap(new int[]{1, 2, 3, 4, 5, 6}, 60);
		Memory m = new Memory();
		m.sulliuscep = r.memo;
		Gson gson = new Gson();
		Memory back = gson.fromJson(gson.toJson(m), Memory.class);
		Sulliuscep.Memo b = back.sulliuscep();
		assertEquals(1, b.runs.size());
		assertEquals(6, b.runs.get(0).count(Sulliuscep.Haul.MORT_MYRE));
		assertTrue(Sulliuscep.learned(b));
		assertArrayEquals(new int[]{3662, 3802, 0}, b.where.get(1));

		Memory old = gson.fromJson("{\"farming\":50}", Memory.class);
		assertNull(old.sulliuscep);
		assertNotNull(old.sulliuscep().runs);
		Sulliuscep.Shown s = Sulliuscep.show(new Sulliuscep.Tracker(old.sulliuscep()), null, T0);
		assertEquals("Find the grown sulliuscep", s.title);
		assertFalse(s.running);
	}
}
