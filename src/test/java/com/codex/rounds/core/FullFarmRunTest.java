package com.codex.rounds.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.gameval.ItemID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/** The full farm run: trees, fruit trees and herbs, leaving patches out, and rounds too big for one pack. */
public class FullFarmRunTest
{
	static final long T0 = CoreTest.T0;

	@Test
	public void newPatchesAreReadableWhereTheyStand()
	{
		Set<Spot.Group> groups = EnumSet.of(Spot.Group.TREE_PATCHES, Spot.Group.FRUIT_TREES, Spot.Group.HERBS);
		int n = 0;
		for (Spot s : Spot.values())
		{
			if (groups.contains(s.getGroup()))
			{
				assertTrue(s.name() + " point is outside its own regions or bounds", s.readableFrom(s.getPoint()));
				n++;
			}
		}
		assertEquals(7 + 7 + 10, n);
	}

	@Test
	public void leaveOutMatchesPlacesAndKinds()
	{
		List<Spot> out = Settings.leftOut("Lletya, Catherby herbs");
		assertTrue(out.contains(Spot.FRUIT_LLETYA));
		assertTrue(out.contains(Spot.HERB_CATHERBY));
		assertFalse(out.contains(Spot.FRUIT_CATHERBY));
		assertFalse(out.contains(Spot.HERB_FALADOR));
		assertTrue(Settings.leftOut("").isEmpty());
	}

	@Test
	public void lockedPatchesWaitForTheirUnlock()
	{
		Ability noQuests = Ability.builder().levels(99, 99, 99, 99).bankKnown(true).unlocks(Collections.emptySet()).build();
		Board b = Board.build(Collections.emptyMap(), Collections.emptyList(), null, Settings.defaults(), noQuests, T0, Growth.Offset.NONE);
		assertEquals(Status.LOCKED, status(b, Spot.HERB_WEISS));
		assertEquals(Status.LOCKED, status(b, Spot.FRUIT_LLETYA));
		assertTrue(status(b, Spot.HERB_FALADOR) != Status.LOCKED);
	}

	private static Status status(Board b, Spot spot)
	{
		for (Board.Entry e : b.getEntries())
		{
			if (e.getSpot() == spot)
			{
				return e.getStatus();
			}
		}
		return null;
	}

	@Test
	public void saplingsCountASlotEach()
	{
		List<Need> pulls = Arrays.asList(
			new Need(Need.Kind.PLANT, "Magic sapling", 5, "", false, ItemID.PLANTPOT_MAGIC_TREE_SAPLING),
			new Need(Need.Kind.PLANT, "Ranarr seed", 9, "", false, ItemID.RANARR_SEED),
			new Need(Need.Kind.TOOL, "Spade", 1, "", false, ItemID.SPADE));
		assertEquals(7, Supplies.slotsEstimate(pulls));
	}

	@Test
	public void bigRoundsSplitIntoTripsThatFit()
	{
		Ability a = Ability.builder()
			.levels(99, 99, 99, 99)
			.bankKnown(true)
			.fairyRings(true)
			.item(ItemID.PLANTPOT_MAGIC_TREE_SAPLING, 20)
			.item(ItemID.PLANTPOT_PALM_SAPLING, 20)
			.item(ItemID.RANARR_SEED, 50)
			.item(ItemID.BUCKET_ULTRACOMPOST, 60)
			.build();
		Settings s = Settings.defaults().kingdom(false);
		for (Daily d : Daily.values())
		{
			s.daily(d, false);
		}
		Board b = Board.build(Collections.emptyMap(), Collections.emptyList(), null, s, a, T0, Growth.Offset.NONE);
		Route route = Planner.plan(b, Collections.emptyList(), s, a, Point.of(3164, 3486), T0);
		assertTrue("expected several trips, got " + route.tripCount(), route.tripCount() > 1);
		assertEquals(0, route.tripStart(0));
		for (int t = 0; t < route.tripCount(); t++)
		{
			assertTrue("trip " + t + " overflows", Supplies.slotsEstimate(route.getTripPulls(t)) <= Supplies.PACK_SLOTS
				|| route.tripStart(t) == (t + 1 < route.tripCount() ? route.tripStart(t + 1) - 1 : route.getStops().size() - 1));
			assertEquals(t, route.tripOf(route.tripStart(t)));
		}

		// Arriving at a later trip's first stop without its supplies asks for a bank trip.
		int k = route.tripStart(1);
		Progress p = new Progress();
		p.start(T0);
		p.suppliesDone();
		assertFalse(p.isTripSupplied(1));
		p.tripSupplied(1);
		assertTrue(p.isTripSupplied(1));
		assertTrue(k > 0);
	}
}
