package com.codex.rounds.core;

import com.codex.rounds.core.farming.PatchKind;
import com.codex.rounds.core.farming.PatchState;
import com.codex.rounds.core.farming.Produce;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class HerbsTest
{
	static final long T0 = CoreTest.T0;
	static final Herbs.Boosts NONE = Herbs.Boosts.NONE;

	static PatchState herb(int value)
	{
		return PatchKind.HERB.forVarbitValue(value);
	}

	/** Prices for the tests: ranarr 7,000, its seed 30,000, ultracompost 1,000, torstol 9,000, its seed 70,000. */
	static final IntToLongFunction PRICE = id ->
	{
		Map<Integer, Integer> p = new HashMap<>();
		p.put(ItemID.UNIDENTIFIED_RANARR, 7000);
		p.put(ItemID.RANARR_SEED, 30000);
		p.put(ItemID.BUCKET_ULTRACOMPOST, 1000);
		p.put(ItemID.UNIDENTIFIED_TORSTOL, 9000);
		p.put(ItemID.TORSTOL_SEED, 70000);
		p.put(ItemID.UNIDENTIFIED_GUAM, 20);
		p.put(ItemID.GUAM_SEED, 5);
		return p.getOrDefault(id, 0);
	};

	@Test
	public void factsFromTheWikiAndGameval()
	{
		// Herb seed table: level, then harvest xp. Huasca is 65 (Huasca seed: "Farming level required: 65").
		assertEquals(65, Produce.HUASCA.getLevel());
		assertEquals(32, Herbs.Herb.RANARR.getLevel());
		assertEquals(30.5, Herbs.Herb.RANARR.getHarvestXp(), 0);
		assertEquals(85, Herbs.Herb.TORSTOL.getLevel());
		assertEquals(224.5, Herbs.Herb.TORSTOL.getHarvestXp(), 0);
		assertEquals(110, Herbs.Herb.HUASCA.getHarvestXp(), 0);
		// Module:Herb Farming calculator: Guam 25/80, Ranarr 39/80, Huasca 58/80, Torstol 71/80.
		assertEquals(25, Herbs.Herb.GUAM.getCtsLow());
		assertEquals(39, Herbs.Herb.RANARR.getCtsLow());
		assertEquals(58, Herbs.Herb.HUASCA.getCtsLow());
		assertEquals(71, Herbs.Herb.TORSTOL.getCtsLow());
		assertEquals(80, Herbs.CTS_HIGH);
		assertEquals(15, Herbs.Herb.values().length);
		// Grimy herbs: the wiki's item ids (Grimy ranarr weed 207, grimy torstol 219).
		assertEquals(207, Herbs.Herb.RANARR.getGrimyId());
		assertEquals(219, Herbs.Herb.TORSTOL.getGrimyId());
		assertEquals(5295, Herbs.Herb.RANARR.getSeedId());
		assertEquals(21483, ItemID.BUCKET_ULTRACOMPOST);
		assertEquals(7409, ItemID.FAIRY_ENCHANTED_SECATEURS);
		// Lives: three, plus one, two or three for compost; disease cut by 50%, 80% and 90%.
		assertEquals(3, Herbs.BASE_LIVES);
		assertEquals(3, Herbs.Compost.ULTRACOMPOST.getLives());
		assertEquals(2, Herbs.Compost.SUPERCOMPOST.getLives());
		assertEquals(1, Herbs.Compost.COMPOST.getLives());
		assertEquals(10, Herbs.Compost.ULTRACOMPOST.getDiseaseDivisor());
		assertEquals(5, Herbs.Compost.SUPERCOMPOST.getDiseaseDivisor());
		assertEquals(2, Herbs.Compost.COMPOST.getDiseaseDivisor());
		// The diaries.
		assertEquals(4478, VarbitID.KANDARIN_DIARY_ELITE_COMPLETE);
		assertEquals(7927, VarbitID.KOUREND_DIARY_HARD_COMPLETE);
		assertEquals(7925, VarbitID.KOUREND_DIARY_EASY_COMPLETE);
	}

	@Test
	public void matchesTheWikisFigureAtNinetyNine()
	{
		// "at level 99 Farming, with magic secateurs and a Farming cape, one can expect 4.712 herbs with no compost" (wiki, Herb).
		Herbs.Boosts both = new Herbs.Boosts(true, true, false, 0, false, false);
		for (Herbs.Herb h : Herbs.Herb.values())
		{
			assertEquals(4.712, Herbs.expectedYield(h, 99, Herbs.Compost.NONE, both, Spot.HERB_FALADOR), 0.0005);
		}
	}

	@Test
	public void chanceToSaveIsTheWikisInterpolation()
	{
		// Ranarr at 32: floor(39*67/98)=26, floor(80*31/98)=25, plus 1: 52/256.
		assertEquals(52 / 256.0, Herbs.chanceToSave(39, 80, 32), 1e-9);
		assertEquals(81 / 256.0, Herbs.chanceToSave(25, 80, 99), 1e-9);
		assertEquals(26 / 256.0, Herbs.chanceToSave(25, 80, 1), 1e-9);
		// Ultracompost adds three lives to three.
		double plain = Herbs.expectedYield(Herbs.Herb.RANARR, 70, Herbs.Compost.NONE, NONE, null);
		double ultra = Herbs.expectedYield(Herbs.Herb.RANARR, 70, Herbs.Compost.ULTRACOMPOST, NONE, null);
		assertEquals(2.0, ultra / plain, 1e-9);
	}

	@Test
	public void boostsGoInTheCalculatorsOrder()
	{
		// Catherby elite adds 25 after the item boost; the Guild and Hosidius take 10 with the Kourend hard diary.
		Herbs.Boosts b = new Herbs.Boosts(true, false, false, 3, true, false);
		assertEquals(88 + 25, Herbs.boosted(80, b, Spot.HERB_CATHERBY));
		assertEquals(88 + 10, Herbs.boosted(80, b, Spot.HERB_GUILD));
		assertEquals(88 + 10, Herbs.boosted(80, b, Spot.HERB_HOSIDIUS));
		assertEquals(88, Herbs.boosted(80, b, Spot.HERB_FALADOR));
		Herbs.Boosts medium = new Herbs.Boosts(false, false, false, 1, false, false);
		assertEquals(90, Herbs.boosted(80, medium, Spot.HERB_CATHERBY));
		Herbs.Boosts hard = new Herbs.Boosts(false, false, false, 2, false, false);
		assertEquals(97, Herbs.boosted(80, hard, Spot.HERB_CATHERBY));
		// Attas last: floor(80 * 1.05) = 84.
		Herbs.Boosts attas = new Herbs.Boosts(false, false, true, 0, false, false);
		assertEquals(84, Herbs.boosted(80, attas, Spot.HERB_FALADOR));
		// Item boost floors: floor(39 * 1.15) = 44.
		Herbs.Boosts both = new Herbs.Boosts(true, true, false, 0, false, false);
		assertEquals(44, Herbs.boosted(39, both, Spot.HERB_FALADOR));
	}

	@Test
	public void diseaseFreePatchesAndSurvival()
	{
		assertTrue(Herbs.diseaseFree(Spot.HERB_TROLL_STRONGHOLD, NONE));
		assertTrue(Herbs.diseaseFree(Spot.HERB_WEISS, NONE));
		assertTrue(Herbs.diseaseFree(Spot.HERB_HARMONY, NONE));
		assertFalse(Herbs.diseaseFree(Spot.HERB_HOSIDIUS, NONE));
		assertTrue(Herbs.diseaseFree(Spot.HERB_HOSIDIUS, new Herbs.Boosts(false, false, false, 0, false, true)));
		assertFalse(Herbs.diseaseFree(Spot.HERB_CATHERBY, NONE));
		assertEquals(1, Herbs.survival(Herbs.Compost.NONE, true), 0);
		double p = 27 / 128.0;
		assertEquals(Math.pow(1 - p, 3), Herbs.survival(Herbs.Compost.NONE, false), 1e-12);
		assertEquals(Math.pow(1 - p / 10, 3), Herbs.survival(Herbs.Compost.ULTRACOMPOST, false), 1e-12);
	}

	@Test
	public void hearsTheCompostLines()
	{
		assertEquals(Herbs.Compost.ULTRACOMPOST, Herbs.heard("You treat the herb patch with ultracompost."));
		assertEquals(Herbs.Compost.SUPERCOMPOST, Herbs.heard("You treat the herb patch with supercompost."));
		assertEquals(Herbs.Compost.COMPOST, Herbs.heard("You treat the herb patch with compost."));
		assertEquals(Herbs.Compost.ULTRACOMPOST, Herbs.heard("The herb patch has been treated with ultracompost."));
		assertEquals(Herbs.Compost.SUPERCOMPOST, Herbs.heard("This herb patch has already been treated with supercompost."));
		assertEquals(Herbs.Compost.ULTRACOMPOST, Herbs.heard("This is a herb patch. The soil has been treated with ultracompost. The patch needs weeding."));
		assertNull(Herbs.heard("You pick a Grimy ranarr weed."));
		assertNull(Herbs.heard("You fill the compost bin."));
	}

	@Test
	public void followsACropFromSeedToTheLastHerb()
	{
		Herbs.Memo m = new Herbs.Memo();
		Herbs.Tracker t = new Herbs.Tracker(m);
		Spot s = Spot.HERB_CATHERBY;
		t.compost(s, Herbs.Compost.ULTRACOMPOST);
		assertNull(t.patch(s, herb(3), herb(32), T0));
		Herbs.Crop c = t.growing(s);
		assertNotNull(c);
		assertTrue(c.plantSeen);
		assertEquals("ULTRACOMPOST", c.compost);
		assertNull(t.patch(s, herb(32), herb(33), T0 + 1200));
		assertNull(t.patch(s, herb(35), herb(38), T0 + 4800));
		for (int i = 0; i < 9; i++)
		{
			t.picked(s, Herbs.Herb.RANARR, 1, 70, NONE, T0 + 5000 + i * 2);
		}
		Herbs.Crop ended = t.patch(s, herb(36), herb(3), T0 + 5020);
		assertNotNull(ended);
		assertEquals(9, ended.picked);
		assertEquals(Herbs.expectedYield(Herbs.Herb.RANARR, 70, Herbs.Compost.ULTRACOMPOST, NONE, s), ended.expected, 1e-9);
		assertTrue(m.growing.isEmpty());
		assertEquals(1, m.crops.size());
		// 9 x 7,000 - 30,000 seed - 1,000 ultracompost.
		assertEquals(32000, Herbs.profit(ended, PRICE));
		String line = Herbs.summary(ended, PRICE);
		assertTrue(line, line.startsWith("Ranarr at Catherby: 9 herbs (the wiki expects "));
		assertTrue(line, line.endsWith("with ultracompost, +32,000 after seed and compost."));
	}

	@Test
	public void compostAfterPlantingCounts()
	{
		Herbs.Memo m = new Herbs.Memo();
		Herbs.Tracker t = new Herbs.Tracker(m);
		Spot s = Spot.HERB_FALADOR;
		t.patch(s, herb(3), herb(32), T0);
		t.compost(s, Herbs.Compost.SUPERCOMPOST);
		assertEquals("SUPERCOMPOST", t.growing(s).compost);
	}

	@Test
	public void seenPlantingWithoutCompostIsNone()
	{
		Herbs.Memo m = new Herbs.Memo();
		Herbs.Tracker t = new Herbs.Tracker(m);
		Spot s = Spot.HERB_FALADOR;
		t.patch(s, herb(3), herb(32), T0);
		t.picked(s, Herbs.Herb.RANARR, 4, 50, NONE, T0 + 5000);
		assertEquals("NONE", t.growing(s).compost);
		assertTrue(t.growing(s).expected > 3);
	}

	@Test
	public void unseenPlantingLeavesCompostUnknown()
	{
		Herbs.Memo m = new Herbs.Memo();
		Herbs.Tracker t = new Herbs.Tracker(m);
		Spot s = Spot.HERB_ARDOUGNE;
		// First look at the patch: already grown.
		t.picked(s, Herbs.Herb.RANARR, 7, 70, NONE, T0);
		Herbs.Crop c = t.patch(s, herb(38), herb(3), T0 + 20);
		assertNotNull(c);
		assertNull(c.compost);
		assertEquals(0, c.expected, 0);
		assertNull(Herbs.luck(m));
		assertTrue(Herbs.summary(c, null).equals("Ranarr at Ardougne: 7 herbs."));
	}

	@Test
	public void diseaseAndDeathAreRecorded()
	{
		Herbs.Memo m = new Herbs.Memo();
		Herbs.Tracker t = new Herbs.Tracker(m);
		Spot s = Spot.HERB_MORYTANIA;
		t.patch(s, herb(3), herb(32), T0);
		t.patch(s, herb(33), herb(141), T0 + 1200);
		assertTrue(t.growing(s).diseased);
		Herbs.Crop dead = t.patch(s, herb(141), herb(171), T0 + 2400);
		assertNotNull(dead);
		assertTrue(dead.died);
		assertTrue(dead.diseased);
		assertEquals("Ranarr at Morytania died of disease.", Herbs.summary(dead, PRICE));
		// Clearing the dead patch ends nothing more.
		assertNull(t.patch(s, herb(171), herb(3), T0 + 3000));
		assertEquals(1, m.crops.size());
		// A cured crop goes on and is picked.
		t.patch(s, herb(3), herb(32), T0 + 4000);
		t.patch(s, herb(33), herb(140), T0 + 5200);
		t.patch(s, herb(140), herb(34), T0 + 5300);
		t.picked(s, Herbs.Herb.RANARR, 6, 70, NONE, T0 + 9000);
		Herbs.Crop cured = t.patch(s, herb(36), herb(3), T0 + 9010);
		assertTrue(cured.diseased);
		assertFalse(cured.died);
		List<Herbs.PatchStat> ps = Herbs.patchStats(m, NONE);
		assertEquals(1, ps.size());
		assertEquals(2, ps.get(0).crops);
		assertEquals(1, ps.get(0).harvests);
		assertEquals(2, ps.get(0).diseased);
		assertEquals(1, ps.get(0).died);
	}

	@Test
	public void dugUpCropsAreNotLearnedFrom()
	{
		Herbs.Memo m = new Herbs.Memo();
		Herbs.Tracker t = new Herbs.Tracker(m);
		Spot s = Spot.HERB_FALADOR;
		t.patch(s, herb(3), herb(32), T0);
		assertNull(t.patch(s, herb(33), herb(3), T0 + 600));
		assertTrue(m.crops.isEmpty());
		assertTrue(m.growing.isEmpty());
	}

	@Test
	public void anotherHerbReplacesAnUnseenEnd()
	{
		Herbs.Memo m = new Herbs.Memo();
		Herbs.Tracker t = new Herbs.Tracker(m);
		Spot s = Spot.HERB_FALADOR;
		t.patch(s, herb(3), herb(32), T0);
		t.picked(s, Herbs.Herb.RANARR, 8, 70, NONE, T0 + 5000);
		// Rounds was away for the last pick and the replant: torstol is growing now.
		Herbs.Crop ended = t.patch(s, herb(37), herb(103), T0 + 9000);
		assertNotNull(ended);
		assertEquals("RANARR", ended.herb);
		assertEquals("TORSTOL", t.growing(s).herb);
	}

	@Test
	public void statsRunsAndTheBestHerb()
	{
		Herbs.Memo m = new Herbs.Memo();
		Herbs.Tracker t = new Herbs.Tracker(m);
		Spot[] spots = {Spot.HERB_FALADOR, Spot.HERB_CATHERBY, Spot.HERB_ARDOUGNE, Spot.HERB_MORYTANIA};
		long at = T0;
		int[][] yields = {{9, 8, 10, 7}, {8, 9, 11, 8}};
		for (int[] run : yields)
		{
			for (int i = 0; i < spots.length; i++)
			{
				t.compost(spots[i], Herbs.Compost.ULTRACOMPOST);
				t.patch(spots[i], herb(3), herb(32), at);
				t.picked(spots[i], Herbs.Herb.RANARR, run[i], 80, NONE, at + 4800);
				t.patch(spots[i], herb(36), herb(3), at + 4810 + i * 60);
			}
			at += 6 * 3600;
		}
		List<Herbs.Run> runs = Herbs.runs(m);
		assertEquals(2, runs.size());
		assertEquals(4, runs.get(1).crops.size());
		assertEquals(36, runs.get(1).herbs());
		assertEquals(36 * 7000 - 4 * 31000, runs.get(1).profit(PRICE));
		assertEquals(Math.round(36 * 30.5), runs.get(1).pickXp());

		List<Herbs.HerbStat> hs = Herbs.herbStats(m);
		assertEquals(1, hs.size());
		assertEquals(8, hs.get(0).harvests);
		assertEquals(70 / 8.0, hs.get(0).mean, 1e-9);
		assertEquals(Herbs.Compost.ULTRACOMPOST, hs.get(0).usual);
		double exp = Herbs.expectedYield(Herbs.Herb.RANARR, 80, Herbs.Compost.ULTRACOMPOST, NONE, null);
		assertEquals(70 / (8 * exp), Herbs.luck(m), 1e-9);
		assertEquals(8, Herbs.compared(m));
		// Eight crops seen to the end, none lost.
		assertEquals(0.0, Herbs.deathRate(m), 0);

		List<Herbs.Pick> picks = Herbs.best(m, 90, Herbs.Compost.ULTRACOMPOST, NONE, PRICE);
		// Ranarr from your own 8.75 a patch: 8.75 x 7,000 - 31,000 = 30,250. Torstol from the wiki.
		Herbs.Pick ranarr = null;
		for (Herbs.Pick p : picks)
		{
			if (p.herb == Herbs.Herb.RANARR)
			{
				ranarr = p;
			}
		}
		assertNotNull(ranarr);
		assertTrue(ranarr.own);
		assertEquals(30250, ranarr.value);
		// Torstol from the wiki at 90 with ultracompost: 79/256 to save, 8.68 a patch x 9,000 - 71,000, under ranarr's.
		assertEquals(Herbs.Herb.RANARR, picks.get(0).herb);
		assertEquals(Herbs.Herb.TORSTOL, picks.get(1).herb);
		assertFalse(picks.get(1).own);
		assertEquals(Math.round(6 / (1 - 79 / 256.0) * 9000) - 71000, picks.get(1).value);
		// Herbs without a price are left out; herbs above the level too.
		assertEquals(3, picks.size());
		assertEquals(2, Herbs.best(m, 84, Herbs.Compost.ULTRACOMPOST, NONE, PRICE).size());

		Herbs.Shown shown = Herbs.show(m, 90, Herbs.Compost.ULTRACOMPOST, NONE, PRICE);
		assertEquals(8, shown.harvests);
		assertEquals(runs.get(1).profit(PRICE), shown.lastProfit);
	}

	@Test
	public void savesAndLoadsWithGson()
	{
		Herbs.Memo m = new Herbs.Memo();
		Herbs.Tracker t = new Herbs.Tracker(m);
		t.compost(Spot.HERB_GUILD, Herbs.Compost.ULTRACOMPOST);
		t.patch(Spot.HERB_GUILD, herb(3), herb(32), T0);
		t.picked(Spot.HERB_GUILD, Herbs.Herb.RANARR, 9, 80, NONE, T0 + 5000);
		t.patch(Spot.HERB_GUILD, herb(36), herb(3), T0 + 5010);
		t.compost(Spot.HERB_FALADOR, Herbs.Compost.SUPERCOMPOST);
		Memory mem = new Memory();
		mem.herbs = m;
		Gson gson = new Gson();
		Memory back = gson.fromJson(gson.toJson(mem), Memory.class);
		assertEquals(1, back.herbs().crops.size());
		assertEquals(9, back.herbs().crops.get(0).picked);
		assertEquals("SUPERCOMPOST", back.herbs().compost.get("HERB_FALADOR"));
		// An older save without the ledger loads as empty.
		Memory old = gson.fromJson("{\"farming\":70}", Memory.class);
		assertNull(old.herbs);
		assertTrue(old.herbs().crops.isEmpty());
		assertEquals(0, Herbs.show(old.herbs(), 70, Herbs.Compost.NONE, NONE, PRICE).harvests);
		// A crop from a later version with an unknown herb is skipped, not thrown.
		Herbs.Crop odd = new Herbs.Crop();
		odd.herb = "NEWHERB";
		odd.picked = 5;
		back.herbs().crops.add(odd);
		assertEquals(1, Herbs.herbStats(back.herbs()).size());
	}

	@Test
	public void wordsForTheBoard()
	{
		assertEquals("+412k", Herbs.signed(412_000));
		assertEquals("-18,000", Herbs.signed(-18_000));
		assertEquals("+31k", Herbs.signedCompact(30_850));
		assertEquals("-2.4k", Herbs.signedCompact(-2_400));
		assertEquals("4.1k", Herbs.compact(4_100));
		assertEquals("1.2m", Herbs.compact(1_240_000));
		assertEquals("850", Herbs.compact(850));
		assertEquals("6% above the wiki's expectation", Herbs.luckText(1.06));
		assertEquals("4% below the wiki's expectation", Herbs.luckText(0.96));
		assertEquals("level with the wiki's expectation", Herbs.luckText(1.001));
	}
}
