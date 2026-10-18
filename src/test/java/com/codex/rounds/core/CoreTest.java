package com.codex.rounds.core;

import com.codex.rounds.core.farming.Action;
import com.codex.rounds.core.farming.Produce;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CoreTest
{
	static final long T0 = 1_760_000_000L;

	static Vars vars(Map<Integer, Integer> bits, Map<Integer, Integer> ps)
	{
		return new Vars()
		{
			@Override
			public int varbit(int id)
			{
				return bits.getOrDefault(id, 0);
			}

			@Override
			public int varp(int id)
			{
				return ps.getOrDefault(id, 0);
			}
		};
	}

	@Test
	public void birdhouseVarps()
	{
		assertEquals(Birdhouse.State.EMPTY, Birdhouse.state(0));
		assertEquals(Birdhouse.State.BUILT, Birdhouse.state(1));
		assertEquals(Birdhouse.State.SEEDED, Birdhouse.state(3));
		assertEquals(Birdhouse.Type.NORMAL, Birdhouse.type(3));
		assertEquals(Birdhouse.Type.REDWOOD, Birdhouse.type(27));
		assertEquals(Birdhouse.State.SEEDED, Birdhouse.state(27));
		assertEquals(Birdhouse.State.UNKNOWN, Birdhouse.state(28));
		assertEquals(Birdhouse.Type.MAGIC, Birdhouse.Type.bestFor(80));
	}

	@Test
	public void birdhouseFillsAfterFiftyMinutes()
	{
		Seen seeded = new Seen(24, T0);
		Reading early = Growth.read(Spot.BIRDHOUSE_MEADOW_N, seeded, T0 + 49 * 60, Growth.Offset.NONE);
		assertEquals(Status.GROWING, early.getStatus());
		assertEquals(60, early.secondsLeft(T0 + 49 * 60));
		Reading full = Growth.read(Spot.BIRDHOUSE_MEADOW_N, seeded, T0 + 50 * 60, Growth.Offset.NONE);
		assertEquals(Status.READY, full.getStatus());
	}

	@Test
	public void teakGrowsToHealthCheck()
	{
		// Teak saplings are varbit 8 when planted and take seven 640-minute ticks.
		long planted = T0 - (T0 % (640 * 60)) + 60;
		Seen seen = new Seen(8, planted);
		Reading growing = Growth.read(Spot.HARDWOOD_FOSSIL_E, seen, planted + 3 * 640 * 60, Growth.Offset.NONE);
		assertEquals(Status.GROWING, growing.getStatus());
		assertEquals(Produce.TEAK, growing.getProduce());
		Reading ready = Growth.read(Spot.HARDWOOD_FOSSIL_E, seen, planted + 7 * 640 * 60, Growth.Offset.NONE);
		assertEquals(Status.READY, ready.getStatus());
		assertEquals(Action.CHECK_HEALTH, ready.getAction());
	}

	@Test
	public void bushesRegrowThenReady()
	{
		// Redberry bush picked down to stage 0 (value 10) regrows a berry every 20 minutes.
		long seen = T0 - (T0 % 1200) + 30;
		Reading r = Growth.read(Spot.BUSH_RIMMINGTON, new Seen(10, seen), seen + 20 * 60, Growth.Offset.NONE);
		assertEquals(Status.GROWING, r.getStatus());
		Reading full = Growth.read(Spot.BUSH_RIMMINGTON, new Seen(10, seen), seen + 4 * 20 * 60, Growth.Offset.NONE);
		assertEquals(Status.READY, full.getStatus());
		assertEquals(Action.HARVEST, full.getAction());
	}

	@Test
	public void weedsAndClearSoil()
	{
		assertEquals(Action.PLANT, Growth.read(Spot.CALQUAT_KASTORI, new Seen(3, T0), T0, Growth.Offset.NONE).getAction());
		assertEquals(Action.RAKE, Growth.read(Spot.CALQUAT_KASTORI, new Seen(0, T0), T0, Growth.Offset.NONE).getAction());
		// A calquat with every fruit picked is dug up and replanted.
		assertEquals(Action.CLEAR, Growth.read(Spot.CALQUAT_KASTORI, new Seen(12, T0), T0, Growth.Offset.NONE).getAction());
	}

	@Test
	public void spiritTreeGrownIsDone()
	{
		assertEquals(Status.DONE, Growth.read(Spot.SPIRIT_PORT_SARIM, new Seen(20, T0), T0, Growth.Offset.NONE).getStatus());
	}

	@Test
	public void offsetShiftsTicks()
	{
		long t = 1_000_000_200L;
		long plain = Growth.tickTime(5, 1, t, Growth.Offset.NONE);
		long shifted = Growth.tickTime(5, 1, t, new Growth.Offset(2, 640));
		assertEquals(0, plain % 300);
		assertEquals(120, (300 + plain - shifted) % 300);
	}

	@Test
	public void kingdomDecays()
	{
		long day = 86400;
		Kingdom k = Kingdom.estimate(127, 1_000_000, T0, T0 + 5 * day, false);
		// 50k a day while over 500k, then a tenth.
		assertEquals(750_000, k.getCoffer());
		assertEquals(127 - (int) (5 * 0.025f * 127), k.getApproval());
		assertTrue(k.isEstimated());
		Kingdom rt = Kingdom.estimate(127, 1_000_000, T0, T0 + 2 * day, true);
		assertEquals(850_000, rt.getCoffer());
		assertEquals(0, k.topUp(500_000));
		assertEquals(Kingdom.MAX_COFFER - 750_000, k.topUp(99_000_000));
		assertTrue(k.needsVisit(100, 750_000, 100_000));
	}

	@Test
	public void dailiesFollowDiaries()
	{
		Map<Integer, Integer> bits = new HashMap<>();
		bits.put(VarbitID.VARROCK_DIARY_EASY_COMPLETE, 1);
		bits.put(VarbitID.VARROCK_DIARY_MEDIUM_COMPLETE, 1);
		bits.put(VarbitID.VARROCK_DIARY_HARD_COMPLETE, 1);
		bits.put(VarbitID.MORYTANIA_DIARY_EASY_COMPLETE, 1);
		bits.put(VarbitID.MORYTANIA_DIARY_MEDIUM_COMPLETE, 1);
		bits.put(VarbitID.MORYTANIA_DIARY_HARD_COMPLETE, 1);
		bits.put(VarbitID.MORYTANIA_SLIME_CLAIMED, 10);
		Vars v = vars(bits, new HashMap<>());
		assertTrue(Daily.ZAFF.claimable(v));
		assertEquals(60, Daily.ZAFF.amount(v));
		assertEquals(60 * Daily.ZAFF_PRICE, Daily.ZAFF.need(v).getQuantity());
		assertEquals(26, Daily.ROBIN.amount(v));
		assertEquals(16, Daily.ROBIN.remaining(v));
		assertFalse(Daily.CROMPERTY.unlocked(v));
		bits.put(VarbitID.ZAFF_LAST_CLAIMED, 1);
		assertFalse(Daily.ZAFF.claimable(v));
	}

	@Test
	public void formats()
	{
		assertEquals("now", Format.duration(0));
		assertEquals("4 min", Format.duration(4 * 60));
		assertEquals("1 hr 20 min", Format.duration(80 * 60));
		assertEquals("2 days 3 hr", Format.duration((51 * 60) * 60));
		assertEquals("840k coins", Format.coins(840_000));
		assertEquals("1.2m", Format.stack(1_200_000));
		assertEquals("12:05", Format.clock(725));
	}

	static Ability rich()
	{
		return Ability.builder()
			.levels(99, 99, 99, 99)
			.bankKnown(true)
			.fairyRings(true)
			.item(11194, 1) // digsite pendant (5)
			.item(11980, 1) // ring of wealth (5)
			.item(ItemID.BIRDHOUSE_REDWOOD, 20)
			.item(ItemID.BARLEY_SEED, 200)
			.item(ItemID.SEAWEED_SEED, 50)
			.item(ItemID.PLANTPOT_MAHOGANY_SAPLING, 10)
			.item(ItemID.BUCKET_ULTRACOMPOST, 50)
			.build();
	}

	static Map<Spot, Seen> fossilDue()
	{
		Map<Spot, Seen> seen = new EnumMap<>(Spot.class);
		long longAgo = T0 - 6 * 3600;
		seen.put(Spot.BIRDHOUSE_VALLEY_NE, new Seen(27, longAgo));
		seen.put(Spot.BIRDHOUSE_VALLEY_SW, new Seen(27, longAgo));
		seen.put(Spot.BIRDHOUSE_MEADOW_N, new Seen(27, longAgo));
		seen.put(Spot.BIRDHOUSE_MEADOW_S, new Seen(27, longAgo));
		seen.put(Spot.SEAWEED_N, new Seen(10, longAgo));
		seen.put(Spot.SEAWEED_S, new Seen(10, longAgo));
		return seen;
	}

	static Settings onlyFossil()
	{
		Settings s = Settings.defaults().includeUnseen(false).kingdom(false);
		for (Daily d : Daily.values())
		{
			s.daily(d, false);
		}
		return s;
	}

	@Test
	public void plansFossilIslandThenSeabed()
	{
		Settings s = onlyFossil();
		Ability a = rich();
		Board b = Board.build(fossilDue(), Collections.emptyList(), null, s, a, T0, Growth.Offset.NONE);
		assertEquals(6, b.count(Status.READY));
		Route route = Planner.plan(b, Collections.emptyList(), s, a, Point.of(3164, 3486), T0);
		assertEquals(2, route.getStops().size());
		assertEquals(Place.FOSSIL_ISLAND, route.getStops().get(0).getPlace());
		assertEquals(Place.UNDERWATER, route.getStops().get(1).getPlace());
		assertTrue(route.getStops().get(1).getWay().isWalk() || route.getStops().get(1).getWay().getPlace() == Place.FOSSIL_ISLAND);

		Map<String, Integer> pulls = new HashMap<>();
		for (Need n : route.getPulls())
		{
			pulls.put(n.getLabel(), n.getQuantity());
		}
		assertEquals(Integer.valueOf(4), pulls.get("Redwood bird house"));
		assertEquals(Integer.valueOf(40), pulls.get("Hop seeds"));
		assertEquals(Integer.valueOf(2), pulls.get("Seaweed spore"));
		assertEquals(Integer.valueOf(2), pulls.get("Ultracompost"));
		assertNotNull(pulls.get("Fishbowl helmet (worn)"));
		assertNotNull(pulls.get("Digsite pendant"));
		assertNotNull(pulls.get("Seed dibber"));
	}

	@Test
	public void plansDailiesAndKingdom()
	{
		Settings s = Settings.defaults().includeUnseen(false);
		Ability a = rich();
		Map<Integer, Integer> bits = new HashMap<>();
		bits.put(VarbitID.VARROCK_DIARY_EASY_COMPLETE, 1);
		Vars v = vars(bits, new HashMap<>());
		List<DailyState> dailies = new ArrayList<>();
		for (Daily d : Daily.values())
		{
			dailies.add(DailyState.of(d, v));
		}
		Kingdom k = new Kingdom(100, 200_000, true, false);
		Board b = Board.build(new EnumMap<>(Spot.class), dailies, k, s, a, T0, Growth.Offset.NONE);
		Route route = Planner.plan(b, dailies, s, a, Point.of(3164, 3486), T0);
		assertEquals(2, route.getStops().size());
		Need coins = route.getPulls().stream().filter(n -> n.getKind() == Need.Kind.COINS).findFirst().orElse(null);
		assertNotNull(coins);
		assertEquals(15 * Daily.ZAFF_PRICE + 550_000, coins.getQuantity());
		// The ring of wealth reaches Miscellania.
		Stop misc = route.getStops().stream().filter(st -> st.getPlace() == Place.MISCELLANIA).findFirst().orElse(null);
		assertNotNull(misc);
	}

	@Test
	public void choosesBestOwnedCrop()
	{
		Ability a = rich();
		assertEquals(Produce.MAHOGANY, Planner.choosePlant(com.codex.rounds.core.farming.PatchKind.HARDWOOD_TREE, Settings.defaults(), a));
		Ability poor = Ability.builder().levels(40, 1, 1, 1).bankKnown(true).build();
		assertEquals(Produce.TEAK, Planner.choosePlant(com.codex.rounds.core.farming.PatchKind.HARDWOOD_TREE, Settings.defaults(), poor));
		assertNull(Planner.choosePlant(com.codex.rounds.core.farming.PatchKind.ANIMA, Settings.defaults(), poor));
	}

	@Test
	public void guideWalksTheRound()
	{
		Settings s = onlyFossil();
		Ability a = rich();
		Map<Spot, Seen> seen = fossilDue();
		Board b = Board.build(seen, Collections.emptyList(), null, s, a, T0, Growth.Offset.NONE);
		Route route = Planner.plan(b, Collections.emptyList(), s, a, Point.of(3164, 3486), T0);
		Progress progress = new Progress();
		progress.start(T0);

		Map<Integer, Integer> carried = new HashMap<>();
		Map<Spot, Reading> live = new EnumMap<>(Spot.class);
		for (Spot sp : Spot.values())
		{
			live.put(sp, Growth.read(sp, seen.get(sp), T0, Growth.Offset.NONE));
		}
		Guide.World atBank = new Guide.World(Point.of(3164, 3486), live, new EnumMap<>(Daily.class), null, carried, a, T0);
		Step first = Guide.next(route, atBank, progress, s);
		assertEquals(Step.Kind.SUPPLIES, first.getKind());

		for (Need n : route.getPulls())
		{
			carried.put(n.getItemId(), n.getQuantity());
		}
		Step travel = Guide.next(route, atBank, progress, s);
		assertEquals(Step.Kind.TRAVEL, travel.getKind());
		assertTrue(travel.getItems().contains(11194));

		Guide.World onIsland = new Guide.World(Point.of(3678, 3880), live, new EnumMap<>(Daily.class), null, carried, a, T0 + 30);
		Step empty = Guide.next(route, onIsland, progress, s);
		assertEquals(Step.Kind.TASK, empty.getKind());
		assertEquals("Empty the bird house", empty.getTitle());
		assertEquals(Spot.BIRDHOUSE_MEADOW_N, empty.getTarget().getSpot());

		// Collected and rebuilt: the space goes empty, then built, then seeded again.
		live.put(Spot.BIRDHOUSE_MEADOW_N, Growth.read(Spot.BIRDHOUSE_MEADOW_N, new Seen(0, T0 + 40), T0 + 40, Growth.Offset.NONE));
		assertEquals("Build the bird house", Guide.next(route, onIsland, progress, s).getTitle());
		live.put(Spot.BIRDHOUSE_MEADOW_N, Growth.read(Spot.BIRDHOUSE_MEADOW_N, new Seen(25, T0 + 45), T0 + 45, Growth.Offset.NONE));
		assertEquals("Add ten seeds", Guide.next(route, onIsland, progress, s).getTitle());
		live.put(Spot.BIRDHOUSE_MEADOW_N, Growth.read(Spot.BIRDHOUSE_MEADOW_N, new Seen(27, T0 + 50), T0 + 50, Growth.Offset.NONE));
		Step next = Guide.next(route, onIsland, progress, s);
		assertEquals(Spot.BIRDHOUSE_MEADOW_S, next.getTarget().getSpot());
		assertEquals(1, next.getDone());
	}

	@Test
	public void readsTheLeprechaunsStore()
	{
		Map<Integer, Integer> bits = new HashMap<>();
		bits.put(VarbitID.FARMING_TOOLS_SPADE, 1);
		bits.put(VarbitID.FARMING_TOOLS_SECATEURS, 1);
		bits.put(VarbitID.FARMING_TOOLS_FAIRYSECATEURS, 1);
		bits.put(VarbitID.FARMING_TOOLS_SUPERCOMPOST, 44);
		bits.put(VarbitID.FARMING_TOOLS_EXTRASUPERCOMPOST, 1);
		Map<Integer, Integer> stored = Leprechaun.read(vars(bits, new HashMap<>()));
		assertEquals(Integer.valueOf(1), stored.get(ItemID.SPADE));
		assertEquals(Integer.valueOf(1), stored.get(ItemID.FAIRY_ENCHANTED_SECATEURS));
		assertFalse(stored.containsKey(ItemID.SECATEURS));
		assertEquals(Integer.valueOf(300), stored.get(ItemID.BUCKET_SUPERCOMPOST));
	}

	@Test
	public void takesStoredToolsFromTheLeprechaun()
	{
		Settings s = onlyFossil();
		Map<Integer, Integer> store = new HashMap<>();
		store.put(ItemID.DIBBER, 1);
		store.put(ItemID.SPADE, 1);
		Ability a = Ability.builder().levels(99, 99, 99, 99).bankKnown(true).fairyRings(true)
			.item(11194, 1).item(ItemID.BIRDHOUSE_REDWOOD, 20).item(ItemID.BARLEY_SEED, 200).item(ItemID.SEAWEED_SEED, 50)
			.item(ItemID.BUCKET_ULTRACOMPOST, 50).stored(store).build();
		Map<Spot, Seen> seen = fossilDue();
		Board b = Board.build(seen, Collections.emptyList(), null, s, a, T0, Growth.Offset.NONE);
		Route route = Planner.plan(b, Collections.emptyList(), s, a, Point.of(3164, 3486), T0);
		assertEquals(Place.FOSSIL_ISLAND, route.getStops().get(0).getPlace());

		Map<Integer, Integer> carried = new HashMap<>();
		for (Need n : route.getPulls())
		{
			if (a.stored(n.getItems()) == 0)
			{
				carried.put(n.getItemId(), n.getQuantity());
			}
		}
		Map<Spot, Reading> live = new EnumMap<>(Spot.class);
		for (Spot sp : Spot.values())
		{
			live.put(sp, Growth.read(sp, seen.get(sp), T0, Growth.Offset.NONE));
		}
		Guide.World atBank = new Guide.World(Point.of(3164, 3486), live, new EnumMap<>(Daily.class), null, carried, a, T0);
		Progress progress = new Progress();
		progress.start(T0);
		// The dibber waits with the leprechaun, so the bank has nothing more to give.
		assertTrue(Guide.missing(route, atBank).isEmpty());
		assertEquals(Step.Kind.TRAVEL, Guide.next(route, atBank, progress, s).getKind());

		Guide.World onIsland = new Guide.World(Point.of(3678, 3880), live, new EnumMap<>(Daily.class), null, carried, a, T0 + 30);
		Step take = Guide.next(route, onIsland, progress, s);
		assertEquals("Take tools from the leprechaun", take.getTitle());
		assertTrue(take.getItems().contains(ItemID.DIBBER));

		carried.put(ItemID.DIBBER, 1);
		carried.put(ItemID.SPADE, 1);
		assertEquals(Step.Kind.TASK, Guide.next(route, onIsland, progress, s).getKind());
	}

	@Test
	public void laysThePullListOutInRows()
	{
		List<Need> pulls = Arrays.asList(
			new Need(Need.Kind.TELEPORT, "Digsite pendant", 1, "Fossil Island", false, 11194, 11193),
			new Need(Need.Kind.TOOL, "Spade", 1, "planting", false, ItemID.SPADE),
			new Need(Need.Kind.TOOL, "Seed dibber", 1, "seeds", false, ItemID.DIBBER));
		Map<Integer, Integer> bank = new HashMap<>();
		bank.put(11193, 1);
		int[] l = BankTab.layout(pulls, bank);
		assertEquals(11193, l[0]);
		assertEquals(-1, l[1]);
		assertEquals(ItemID.SPADE, l[8]);
		assertEquals(ItemID.DIBBER, l[9]);
		assertEquals(10, l.length);
		assertTrue(BankTab.items(pulls).contains(11194));
	}

	@Test
	public void learnsHowLongALegTakes()
	{
		Settings s = onlyFossil();
		Ability a = rich();
		Map<Spot, Seen> seen = fossilDue();
		Board b = Board.build(seen, Collections.emptyList(), null, s, a, T0, Growth.Offset.NONE);
		Point bank = Point.of(3164, 3486);
		Route route = Planner.plan(b, Collections.emptyList(), s, a, bank, T0);
		Step travel = new Step(Step.Kind.TRAVEL, "Digsite pendant", "", null, Collections.emptyList(), null, null, 0, 0, 6);

		TravelTimer timer = new TravelTimer();
		assertNull(timer.update(travel, route, bank, a, 1_000));
		TravelTimer.Lap lap = timer.update(travel, route, Point.of(3678, 3880), a, 21_000);
		assertNotNull(lap);
		assertEquals(20, lap.getSeconds());

		Memory m = new Memory();
		m.lap(lap.getKey(), 20);
		assertEquals(0, m.ability().pace(lap.getKey()));
		m.lap(lap.getKey(), 30);
		assertEquals(25, m.ability().pace(lap.getKey()));
		Ability learned = Ability.builder().items(Collections.singletonMap(11194, 1)).paces(m.paceMap()).build();
		assertEquals(25, Travel.best(bank, Place.FOSSIL_ISLAND, learned).getSeconds());

		// A detour far beyond the estimate is not counted.
		timer.update(travel, route, bank, a, 0);
		assertNull(timer.update(travel, route, Point.of(3678, 3880), a, 600_000));
	}

	@Test
	public void forecastsAWorthwhileWait()
	{
		Settings s = onlyFossil();
		Ability a = rich();
		Map<Spot, Seen> seen = new EnumMap<>(Spot.class);
		// Bird houses seeded 40 minutes ago fill in ten; seaweed planted long ago is ready now.
		long seeded = T0 - 40 * 60;
		seen.put(Spot.BIRDHOUSE_VALLEY_NE, new Seen(27, seeded));
		seen.put(Spot.BIRDHOUSE_VALLEY_SW, new Seen(27, seeded));
		seen.put(Spot.BIRDHOUSE_MEADOW_N, new Seen(27, seeded));
		seen.put(Spot.BIRDHOUSE_MEADOW_S, new Seen(27, seeded));
		seen.put(Spot.SEAWEED_N, new Seen(10, T0 - 6 * 3600));
		Board b = Board.build(seen, Collections.emptyList(), null, s, a, T0, Growth.Offset.NONE);
		Board.Forecast f = b.forecast(T0);
		assertNotNull(f);
		assertEquals(4, f.getMore());
		assertTrue(f.getWaitSeconds() > 9 * 60 && f.getWaitSeconds() <= 10 * 60);
		assertEquals(Collections.singletonList("bird houses"), f.getGroups());
		// Already inside the round's lookahead: nothing more to wait for.
		assertNull(b.forecast(T0 + 15 * 60));
	}
}
