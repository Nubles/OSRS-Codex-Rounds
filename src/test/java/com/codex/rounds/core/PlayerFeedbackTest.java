package com.codex.rounds.core;

import com.codex.rounds.core.farming.PatchKind;
import com.codex.rounds.core.farming.Produce;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.gameval.ItemID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/** Fixes from player feedback: tool variants, bird house order, ironman seeds and pack space. */
public class PlayerFeedbackTest
{
	static final long T0 = CoreTest.T0;

	private static Map<Spot, Reading> live(Map<Spot, Seen> seen)
	{
		Map<Spot, Reading> live = new EnumMap<>(Spot.class);
		for (Spot sp : Spot.values())
		{
			live.put(sp, Growth.read(sp, seen.get(sp), T0, Growth.Offset.NONE));
		}
		return live;
	}

	private static Step stepAt(Point player)
	{
		Settings s = CoreTest.onlyFossil();
		Ability a = CoreTest.rich();
		Map<Spot, Seen> seen = CoreTest.fossilDue();
		Board b = Board.build(seen, Collections.emptyList(), null, s, a, T0, Growth.Offset.NONE);
		Route route = Planner.plan(b, Collections.emptyList(), s, a, Point.of(3164, 3486), T0);
		Progress progress = new Progress();
		progress.start(T0);
		Map<Integer, Integer> carried = new HashMap<>();
		for (Need n : route.getPulls())
		{
			carried.put(n.getItemId(), n.getQuantity());
		}
		return Guide.next(route, new Guide.World(player, live(seen), new EnumMap<>(Daily.class), null, carried, a, T0 + 30), progress, s);
	}

	@Test
	public void birdHousesStartInTheValley()
	{
		// Off the digsite pendant at the House on the Hill: the meadow is nearer, but the valley comes first.
		Step step = stepAt(Point.of(3764, 3869));
		assertEquals("Take a Magic Mushtree", step.getTitle());
		Spot target = step.getTarget().getSpot();
		assertTrue(target == Spot.BIRDHOUSE_VALLEY_NE || target == Spot.BIRDHOUSE_VALLEY_SW);
	}

	@Test
	public void meadowHousesWhenAlreadyThere()
	{
		Step step = stepAt(Point.of(3678, 3880));
		assertEquals("Empty the bird house", step.getTitle());
		assertEquals(Spot.BIRDHOUSE_MEADOW_N, step.getTarget().getSpot());
	}

	@Test
	public void buildingBirdHousesTakesAnyHammerOrChisel()
	{
		Settings s = CoreTest.onlyFossil();
		Ability a = Ability.builder().levels(99, 99, 99, 99).bankKnown(true).item(11194, 1).item(ItemID.BARLEY_SEED, 200)
			.item(ItemID.REDWOOD_LOGS, 20).build();
		Board b = Board.build(CoreTest.fossilDue(), Collections.emptyList(), null, s, a, T0, Growth.Offset.NONE);
		Route route = Planner.plan(b, Collections.emptyList(), s, a, Point.of(3164, 3486), T0);
		Need hammer = route.getPulls().stream().filter(n -> n.getLabel().equals("Hammer")).findFirst().orElse(null);
		Need chisel = route.getPulls().stream().filter(n -> n.getLabel().equals("Chisel")).findFirst().orElse(null);
		assertNotNull(hammer);
		assertNotNull(chisel);
		assertTrue(hammer.accepts(ItemID.IMCANDO_HAMMER));
		assertTrue(hammer.accepts(ItemID.IMCANDO_HAMMER_OFFHAND));
		assertTrue(chisel.accepts(ItemID.JEWELLERS_CHISEL));
	}

	@Test
	public void ironmenOnlyPlantWhatTheyOwn()
	{
		Ability main = Ability.builder().levels(99, 1, 1, 1).bankKnown(true).build();
		Ability iron = Ability.builder().levels(99, 1, 1, 1).bankKnown(true).ironman(true).build();
		Ability ironWithTeak = Ability.builder().levels(99, 1, 1, 1).bankKnown(true).ironman(true)
			.item(ItemID.PLANTPOT_TEAK_SAPLING, 1).build();
		assertNotNull(Planner.choosePlant(PatchKind.HARDWOOD_TREE, Settings.defaults(), main));
		assertNull(Planner.choosePlant(PatchKind.HARDWOOD_TREE, Settings.defaults(), iron));
		assertEquals(Produce.TEAK, Planner.choosePlant(PatchKind.HARDWOOD_TREE, Settings.defaults(), ironWithTeak));
		// Their pick is passed over when they don't have it.
		Settings mahogany = Settings.defaults();
		mahogany.crop(PatchKind.HARDWOOD_TREE, Produce.MAHOGANY);
		assertEquals(Produce.TEAK, Planner.choosePlant(PatchKind.HARDWOOD_TREE, mahogany, ironWithTeak));
		assertEquals(Produce.MAHOGANY, Planner.choosePlant(PatchKind.HARDWOOD_TREE, mahogany, main));
	}

	@Test
	public void countsPackSlots()
	{
		List<Need> pulls = Arrays.asList(
			new Need(Need.Kind.PLANT, "Mahogany sapling", 3, "", false, ItemID.PLANTPOT_MAHOGANY_SAPLING),
			new Need(Need.Kind.BIRDHOUSE, "Hop seeds", 40, "", false, ItemID.BARLEY_SEED),
			new Need(Need.Kind.TOOL, "Fishbowl helmet (worn)", 1, "", false, ItemID.HUNDRED_PIRATE_DIVING_HELMET),
			new Need(Need.Kind.TOOL, "Spade", 1, "", true, ItemID.SPADE));
		assertEquals(4, Supplies.slots(pulls, id -> id == ItemID.BARLEY_SEED));
	}

	@Test
	public void eachSpecialTreeCanBeLeftOff()
	{
		Settings s = Settings.defaults().spot(Spot.CRYSTAL_PRIF, false);
		assertTrue(!s.wants(Spot.CRYSTAL_PRIF));
		assertTrue(s.wants(Spot.REDWOOD_GUILD));
	}
}
