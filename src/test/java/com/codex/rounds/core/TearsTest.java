package com.codex.rounds.core;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ObjectID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TearsTest
{
	/** Thursday 9 October 2025, 08:53:20 UTC. */
	static final long T0 = CoreTest.T0;
	static final long DAY = 86_400;

	static Tears.Record played(long at)
	{
		Tears.Record r = new Tears.Record();
		r.quest = true;
		r.qp = 200;
		r.xp = 50_000_000;
		r.skill = "Hunter";
		r.skillLevel = 70;
		Tears.played(r, at);
		return r;
	}

	@Test
	public void reopensAtMidnightUtcSevenDaysOn()
	{
		long opens = Tears.reopensAt(T0);
		// "resets at 00:00 UTC, 7 days after your last visit (can be played on the same day of the week in UTC)".
		assertEquals(0, opens % DAY);
		assertEquals(T0 / DAY + 7, opens / DAY);
		assertEquals(java.time.DayOfWeek.THURSDAY,
			java.time.Instant.ofEpochSecond(opens).atZone(java.time.ZoneOffset.UTC).getDayOfWeek());
		// A late visit still opens at the start of the seventh day, less than a full week later.
		assertTrue(opens - T0 < 7 * DAY);
		assertTrue(opens - T0 > 6 * DAY);
	}

	@Test
	public void lockedWithoutTheQuest()
	{
		Tears.Record r = new Tears.Record();
		DailyState st = Tears.state(r, T0);
		assertFalse(st.isUnlocked());
		assertFalse(Tears.state(null, T0).isUnlocked());
	}

	@Test
	public void unseenUntilAVisitOrJunaSaysWhen()
	{
		Tears.Record r = new Tears.Record();
		r.quest = true;
		DailyState st = Tears.state(r, T0);
		assertTrue(st.isUnlocked());
		assertFalse(st.isClaimable());
		assertTrue(st.isUnseen());
	}

	@Test
	public void waitsTheWeekThenForGains()
	{
		Tears.Record r = played(T0);
		DailyState st = Tears.state(r, T0 + 3600);
		assertFalse(st.isClaimable());
		assertFalse(st.isUnseen());
		assertEquals(Tears.reopensAt(T0), st.getReadyAt());
		assertEquals("Juna opens Thu 16 Oct", st.getWaiting());

		long later = Tears.reopensAt(T0) + 60;
		r.xp += 99_999;
		st = Tears.state(r, later);
		assertFalse(st.isClaimable());
		assertEquals("1 xp or 1 quest point to go: the week is up, Juna wants more adventures", st.getWaiting());

		r.xp += 1;
		assertTrue(Tears.state(r, later).isClaimable());

		r.xp -= 50_000;
		r.qp += 1;
		assertTrue(Tears.state(r, later).isClaimable());
	}

	@Test
	public void saysWhichSkillAndTheRate()
	{
		Tears.Record r = played(T0);
		r.qp += 1;
		DailyState st = Tears.state(r, Tears.reopensAt(T0));
		assertTrue(st.isClaimable());
		assertEquals("Trains Hunter, your lowest skill: 60 xp a tear", st.getReward());
		r.diaryBonus = true;
		r.skillLevel = 12;
		assertEquals("Trains Hunter, your lowest skill: under 60 xp a tear below level 30, +10% for the hard Lumbridge diary",
			Tears.reward(r));
	}

	@Test
	public void hearsJuna()
	{
		Tears.Record r = new Tears.Record();
		r.quest = true;
		assertTrue(Tears.hear(r, "It has not been long since your last visit. Come back in 3 days.", T0));
		assertEquals(Long.valueOf((T0 / DAY + 3) * DAY), r.opensAt);
		DailyState st = Tears.state(r, T0);
		assertFalse(st.isUnseen());
		assertEquals((T0 / DAY + 3) * DAY, st.getReadyAt());
		// Heard twice, nothing changes.
		assertFalse(Tears.hear(r, "It has not been long since your last visit. Come back in 3 days.", T0 + 5));

		Tears.Record t = new Tears.Record();
		t.quest = true;
		assertTrue(Tears.hear(t, "It has not been long since your last<br>visit. Come back tomorrow.", T0));
		assertEquals(Long.valueOf((T0 / DAY + 1) * DAY), t.opensAt);

		// Only the days are known: open again, with the gains rule as a caveat.
		DailyState open = Tears.state(r, (T0 / DAY + 3) * DAY);
		assertTrue(open.isClaimable());
		assertTrue(open.getReward().endsWith("(if you have gained 1 quest point or 100,000 xp since)"));
	}

	@Test
	public void hearsTheExperienceStillDue()
	{
		Tears.Record r = played(T0 - 8 * DAY);
		r.qpAtPlay = null;
		r.xpAtPlay = null;
		assertTrue(Tears.hear(r, "You cannot enter the cave again until you have gained either one quest point or 37,400 total XP.", T0));
		DailyState st = Tears.state(r, T0);
		assertFalse(st.isClaimable());
		assertEquals("37,400 xp or 1 quest point to go: the week is up, Juna wants more adventures", st.getWaiting());
		r.xp += 37_400;
		assertTrue(Tears.state(r, T0).isClaimable());
	}

	@Test
	public void letInStartsTheWeek()
	{
		Tears.Record r = played(T0 - 30 * DAY);
		r.opensAt = T0 + DAY;
		assertTrue(Tears.hear(r, "Your stories have entertained me. I will let you into the cave for a short time.", T0));
		assertEquals(Long.valueOf(T0), r.playedAt);
		assertNull(r.opensAt);
		assertEquals(Integer.valueOf(r.qp), r.qpAtPlay);
	}

	@Test
	public void lowestSkillFollowsTheWikiRules()
	{
		Map<Skill, Integer> xp = new EnumMap<>(Skill.class);
		for (Skill s : Skill.values())
		{
			xp.put(s, 1_000_000);
		}
		xp.put(Skill.HUNTER, 500);
		xp.put(Skill.FARMING, 500);
		// A tie goes to the skill earlier in the wiki's list: Farming before Hunter.
		assertEquals(Skill.FARMING, Tears.lowest(xp, true, true, true, true));

		xp.put(Skill.HERBLORE, 0);
		xp.put(Skill.RUNECRAFT, 0);
		xp.put(Skill.CONSTRUCTION, 0);
		xp.put(Skill.SAILING, 0);
		// Herblore, Runecraft, Construction and Sailing only count once they can be trained.
		assertEquals(Skill.FARMING, Tears.lowest(xp, false, false, false, false));
		assertEquals(Skill.HERBLORE, Tears.lowest(xp, true, true, true, true));
		assertEquals(Skill.RUNECRAFT, Tears.lowest(xp, true, false, true, true));
		assertEquals(Skill.CONSTRUCTION, Tears.lowest(xp, false, false, true, true));
		assertEquals(Skill.SAILING, Tears.lowest(xp, false, false, false, true));
	}

	@Test
	public void joinsTheRoundByGamesNecklaceAndPointsAtJuna()
	{
		Settings s = Settings.defaults().includeUnseen(false);
		Ability a = Ability.builder().levels(99, 99, 99, 99).bankKnown(true).item(3853, 1).build();
		Tears.Record r = played(T0 - 8 * DAY);
		r.qp += 1;
		List<DailyState> dailies = new ArrayList<>();
		dailies.add(Tears.state(r, T0));
		Board b = Board.build(new EnumMap<>(Spot.class), dailies, null, s, a, T0, Growth.Offset.NONE);
		Board.Entry e = b.getEntries().stream().filter(x -> x.getDaily() == Daily.TEARS).findFirst().orElse(null);
		assertNotNull(e);
		assertEquals(Status.READY, e.getStatus());

		Route route = Planner.plan(b, dailies, s, a, Point.of(3164, 3486), T0);
		assertEquals(1, route.getStops().size());
		Stop stop = route.getStops().get(0);
		assertEquals(Place.CHASM_OF_TEARS, stop.getPlace());
		assertEquals("Games necklace: Tears of Guthix", stop.getWay().getLeg().getLabel());
		assertTrue(route.getPulls().stream().anyMatch(n -> n.getKind() == Need.Kind.TELEPORT && n.accepts(3867)));

		Map<Daily, DailyState> map = new EnumMap<>(Daily.class);
		map.put(Daily.TEARS, dailies.get(0));
		Guide.World there = new Guide.World(Place.CHASM_OF_TEARS.getAnchor(), new EnumMap<>(Spot.class), map, null, new HashMap<>(), a, T0);
		Task task = stop.getTasks().get(0);
		Step step = Guide.taskStep(task, there, new Progress(), 0, 0, 1);
		assertEquals("Tell Juna a story", step.getTitle());
		assertEquals(Step.Target.Type.OBJECT, step.getTarget().getType());
		assertTrue(step.getTarget().matches(ObjectID.TOG_JUNA_1OP));

		// Once played, the chore is done.
		Tears.played(r, T0 + 120);
		map.put(Daily.TEARS, Tears.state(r, T0 + 180));
		assertTrue(Guide.isDone(task, there, new Progress(), s, T0));
	}

	@Test
	public void unseenTearsJoinOnlyWithUnseenChoresAndStayUntilKnown()
	{
		Ability a = Ability.builder().levels(99, 99, 99, 99).bankKnown(true).item(3853, 1).build();
		Tears.Record r = new Tears.Record();
		r.quest = true;
		List<DailyState> dailies = new ArrayList<>();
		dailies.add(Tears.state(r, T0));
		Settings off = Settings.defaults().includeUnseen(false);
		Board b = Board.build(new EnumMap<>(Spot.class), dailies, null, off, a, T0, Growth.Offset.NONE);
		assertTrue(Planner.plan(b, dailies, off, a, Point.of(3164, 3486), T0).isEmpty());

		Settings on = Settings.defaults().includeUnseen(true);
		Route route = Planner.plan(Board.build(new EnumMap<>(Spot.class), dailies, null, on, a, T0, Growth.Offset.NONE), dailies, on, a,
			Point.of(3164, 3486), T0);
		Stop chasm = route.getStops().stream().filter(st -> st.getPlace() == Place.CHASM_OF_TEARS).findFirst().orElse(null);
		assertNotNull(chasm);
		Task task = chasm.getTasks().get(0);
		Map<Daily, DailyState> map = new EnumMap<>(Daily.class);
		map.put(Daily.TEARS, dailies.get(0));
		Guide.World w = new Guide.World(Place.CHASM_OF_TEARS.getAnchor(), new EnumMap<>(Spot.class), map, null, new HashMap<>(), a, T0);
		assertFalse(Guide.isDone(task, w, new Progress(), on, T0));

		// Juna turns the player away: now it is known, and done for this round.
		Tears.hear(r, "It has not been long since your last visit. Come back in 2 days.", T0);
		map.put(Daily.TEARS, Tears.state(r, T0));
		assertTrue(Guide.isDone(task, w, new Progress(), on, T0));
	}

	@Test
	public void oldSavesStillLoadAndTheRecordRoundTrips()
	{
		Gson gson = new Gson();
		Memory old = gson.fromJson("{\"contractType\":0,\"dailies\":{\"ZAFF\":{\"unlocked\":true,\"claimable\":true,\"seenAt\":"
			+ T0 + ",\"reward\":\"15 battlestaves\",\"needQuantity\":0}}}", Memory.class);
		assertNull(old.tears);
		List<DailyState> states = old.dailyStates(T0);
		assertEquals(1, states.size());
		assertEquals(Daily.ZAFF, states.get(0).getDaily());

		old.tears().quest = true;
		Tears.played(old.tears, T0);
		Memory back = gson.fromJson(gson.toJson(old), Memory.class);
		assertEquals(Long.valueOf(T0), back.tears.playedAt);
		DailyState st = back.dailyStates(T0 + DAY).stream().filter(d -> d.getDaily() == Daily.TEARS).findFirst().orElse(null);
		assertNotNull(st);
		assertEquals(Tears.reopensAt(T0), st.getReadyAt());
		// Tears of Guthix is weekly: the daily reset does not bring it back.
		assertFalse(Daily.TEARS.resetsDaily());
	}
}
