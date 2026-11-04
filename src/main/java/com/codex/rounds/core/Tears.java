package com.codex.rounds.core;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.Skill;

/**
 * Tears of Guthix, the weekly visit to Juna. Rules from the OSRS wiki: playable "every seven
 * days", resetting "at 00:00 UTC, 7 days after your last visit (can be played on the same day of
 * the week in UTC as the previous attempt)", and only once the player has "earned at least
 * 100,000 total experience points or one quest point since they last played it". The tears go to
 * "the skill with the least experience", ties broken by the wiki's skill order, with Runecraft,
 * Herblore, Construction and Sailing left out until they can be trained.
 *
 * <p>The game keeps no value Rounds can read for "eligible this week", so Rounds remembers the
 * visits it sees (and what Juna says when she turns the player away) in {@link Record}.
 */
public final class Tears
{
	public static final int DAYS = 7;
	public static final long XP_GAIN = 100_000;
	public static final int QP_GAIN = 1;
	/** The Chasm of Tears map region, as RuneLite's Tears of Guthix plugin uses it. */
	public static final int REGION = 12948;
	/** "at level 30 or higher, each tear awards 60 XP." */
	public static final int FULL_LEVEL = 30;
	public static final int XP_PER_TEAR = 60;

	private static final long DAY = 86_400;

	/** Juna's and the game's lines, from the wiki's Transcript:Juna. */
	static final String LET_IN = "Your stories have entertained me. I will let you into the cave for a short time.";
	static final String TOO_SOON = "It has not been long since your last visit.";
	static final String NO_ADVENTURES = "I will not let you into the cave again until you have had more adventures!";
	private static final Pattern COME_BACK = Pattern.compile("Come back (tomorrow|in (\\d+) days?)");
	private static final Pattern GAIN_DUE = Pattern.compile("until you have gained either one quest point or ([\\d,]+) total XP");

	/**
	 * The skill order the wiki gives for ties: "Attack, Strength, Ranged, Magic, Defence,
	 * Hitpoints, Prayer, Agility, Herblore, Thieving, Crafting, Runecraft, Mining, Smithing,
	 * Fishing, Cooking, Firemaking, Woodcutting, Fletching, Slayer, Farming, Construction, Hunter,
	 * and Sailing."
	 */
	static final Skill[] ORDER = {
		Skill.ATTACK, Skill.STRENGTH, Skill.RANGED, Skill.MAGIC, Skill.DEFENCE, Skill.HITPOINTS, Skill.PRAYER,
		Skill.AGILITY, Skill.HERBLORE, Skill.THIEVING, Skill.CRAFTING, Skill.RUNECRAFT, Skill.MINING, Skill.SMITHING,
		Skill.FISHING, Skill.COOKING, Skill.FIREMAKING, Skill.WOODCUTTING, Skill.FLETCHING, Skill.SLAYER, Skill.FARMING,
		Skill.CONSTRUCTION, Skill.HUNTER, Skill.SAILING
	};

	private Tears()
	{
	}

	/** What Rounds knows of the account's Tears of Guthix, saved with the rest of its memory. */
	public static final class Record
	{
		/** Tears of Guthix (the quest) is finished. */
		public boolean quest;
		/** When Rounds last saw the player play, or null when it never has. */
		public Long playedAt;
		/** Quest points and total experience as the last visit ended. */
		public Integer qpAtPlay;
		public Long xpAtPlay;
		/** When Juna said to come back, from her own words; null when not heard since the last visit. */
		public Long opensAt;
		/** The total experience the game said is still due (absolute), and the quest points that would do instead. */
		public Long xpDue;
		public Integer qpDue;
		/** Quest points and total experience as last read. */
		public int qp;
		public long xp;
		/** The skill the tears would go to now, its level, and whether the hard Lumbridge diary adds 10%. */
		public String skill;
		public int skillLevel;
		public boolean diaryBonus;
	}

	/** The start of the UTC day seven days after a visit: when Juna lets the player back in. */
	public static long reopensAt(long playedAt)
	{
		return (Math.floorDiv(playedAt, DAY) + DAYS) * DAY;
	}

	/** Notes a visit: the week starts again and the gains are counted from here. */
	public static void played(Record r, long now)
	{
		r.playedAt = now;
		r.qpAtPlay = r.qp;
		r.xpAtPlay = r.xp;
		r.opensAt = null;
		r.xpDue = null;
		r.qpDue = null;
	}

	/**
	 * Reads one of Juna's lines or the game's message, returning true when it changed the record.
	 * "Come back tomorrow" or "Come back in N days" sets when she opens again; the experience
	 * message sets what is still due.
	 */
	public static boolean hear(Record r, String text, long now)
	{
		if (text == null)
		{
			return false;
		}
		String t = text.replace("<br>", " ").replaceAll("\\s+", " ");
		if (t.contains(LET_IN))
		{
			played(r, now);
			return true;
		}
		if (t.contains(TOO_SOON))
		{
			Matcher m = COME_BACK.matcher(t);
			if (m.find())
			{
				int days = m.group(2) == null ? 1 : Integer.parseInt(m.group(2));
				long opens = (Math.floorDiv(now, DAY) + days) * DAY;
				if (r.opensAt == null || r.opensAt != opens)
				{
					r.opensAt = opens;
					if (r.playedAt == null)
					{
						// The visit was on the day a week before she opens; its time of day is unknown.
						r.playedAt = opens - DAYS * DAY;
					}
					return true;
				}
			}
			return false;
		}
		Matcher g = GAIN_DUE.matcher(t);
		if (g.find())
		{
			long due = Long.parseLong(g.group(1).replace(",", ""));
			long xpDue = r.xp + due;
			int qpDue = r.qp + QP_GAIN;
			boolean changed = r.xpDue == null || r.xpDue != xpDue || r.qpDue == null || r.qpDue != qpDue;
			r.xpDue = xpDue;
			r.qpDue = qpDue;
			if (r.opensAt == null || r.opensAt > now)
			{
				// The game only asks for more adventures once the days are up.
				r.opensAt = Math.floorDiv(now, DAY) * DAY;
			}
			return changed;
		}
		return false;
	}

	/** When Juna opens again, or 0 when Rounds does not know. */
	public static long opensAt(Record r)
	{
		long t = 0;
		if (r.playedAt != null)
		{
			t = reopensAt(r.playedAt);
		}
		if (r.opensAt != null)
		{
			t = Math.max(t, r.opensAt);
		}
		return t;
	}

	/** Whether enough has been gained since the last visit, or null when Rounds cannot tell. */
	public static Boolean gained(Record r)
	{
		if (r.xpDue != null && r.qpDue != null)
		{
			return r.xp >= r.xpDue || r.qp >= r.qpDue;
		}
		if (r.qpAtPlay == null || r.xpAtPlay == null)
		{
			return null;
		}
		return r.qp >= r.qpAtPlay + QP_GAIN || r.xp >= r.xpAtPlay + XP_GAIN;
	}

	/** Experience still to gain before Juna lets the player in, or 0 when none or unknown. */
	public static long xpShort(Record r)
	{
		long target = r.xpDue != null ? r.xpDue : r.xpAtPlay != null ? r.xpAtPlay + XP_GAIN : 0;
		return Math.max(0, target - r.xp);
	}

	/** This week's standing as a daily: unlocked, claimable, when it opens and what it gives. */
	public static DailyState state(Record r, long now)
	{
		if (r == null || !r.quest)
		{
			return new DailyState(Daily.TEARS, false, false, "", null);
		}
		String reward = reward(r);
		long opens = opensAt(r);
		if (opens == 0)
		{
			return new DailyState(Daily.TEARS, true, false, reward, null, 0,
				"Not seen yet: play once, or talk to Juna, and Rounds keeps the week", true);
		}
		if (now < opens)
		{
			return new DailyState(Daily.TEARS, true, false, reward, null, opens, "Juna opens " + day(opens), false);
		}
		Boolean gained = gained(r);
		if (gained != null && !gained)
		{
			long xp = xpShort(r);
			return new DailyState(Daily.TEARS, true, false, reward, null, 0,
				Format.number(xp > 0 ? xp : XP_GAIN) + " xp or 1 quest point to go: the week is up, Juna wants more adventures", false);
		}
		String why = gained == null ? reward + " (if you have gained 1 quest point or 100,000 xp since)" : reward;
		return new DailyState(Daily.TEARS, true, true, why, null, 0, null, false);
	}

	/** "Trains Hunter, your lowest skill: 60 xp a tear". */
	static String reward(Record r)
	{
		if (r.skill == null)
		{
			return "Trains your lowest skill";
		}
		String per = r.skillLevel >= FULL_LEVEL ? XP_PER_TEAR + " xp a tear" : "under " + XP_PER_TEAR + " xp a tear below level " + FULL_LEVEL;
		return "Trains " + r.skill + ", your lowest skill: " + per + (r.diaryBonus ? ", +10% for the hard Lumbridge diary" : "");
	}

	/** "Wed 11 Nov", in UTC like the reset. */
	static String day(long epoch)
	{
		return DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH).withZone(ZoneOffset.UTC).format(Instant.ofEpochSecond(epoch));
	}

	/**
	 * The skill the tears would train: the least experience among the skills the player can
	 * train, the wiki's order breaking ties.
	 *
	 * @param xp            experience per skill
	 * @param runecraft     Rune Mysteries is done
	 * @param herblore      Druidic Ritual is done
	 * @param construction  the player owns a house
	 * @param sailing       Pandemonium is done
	 */
	public static Skill lowest(Map<Skill, Integer> xp, boolean runecraft, boolean herblore, boolean construction, boolean sailing)
	{
		Skill best = null;
		int least = Integer.MAX_VALUE;
		for (Skill s : ORDER)
		{
			if (s == Skill.RUNECRAFT && !runecraft || s == Skill.HERBLORE && !herblore
				|| s == Skill.CONSTRUCTION && !construction || s == Skill.SAILING && !sailing)
			{
				continue;
			}
			Integer x = xp.get(s);
			if (x != null && x < least)
			{
				least = x;
				best = s;
			}
		}
		return best;
	}
}
