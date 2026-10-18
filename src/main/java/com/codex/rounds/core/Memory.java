package com.codex.rounds.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything Rounds remembers for one account, saved as JSON in the RuneLite profile. Fields
 * are plain so Gson can read older saves; anything missing reads as empty.
 */
public final class Memory
{
	/** Spot key to "value:unixtime", the same form as RuneLite's Time Tracking. */
	public Map<String, String> seen = new HashMap<>();
	/** Items counted across bank, pack and worn gear. */
	public Map<Integer, Integer> bank = new HashMap<>();
	public Map<Integer, Integer> carried = new HashMap<>();
	public long bankTime;
	/** Measured teleport legs: key to average seconds and how many laps it is drawn from. */
	public Map<String, Pace> paces = new HashMap<>();
	/** Tools and compost with the tool leprechaun, as last read. */
	public Map<Integer, Integer> stored = new HashMap<>();

	public Integer kingdomApproval;
	public Integer kingdomCoffer;
	public long kingdomTime;
	public boolean kingdomQuest;
	public boolean royalTrouble;

	/** Daily name to what was last seen of it. */
	public Map<String, DailySnap> dailies = new HashMap<>();

	public int farming = 1;
	public int hunter = 1;
	public int crafting = 1;
	public int magic = 1;
	public int spellbook;
	public boolean fairyRings;

	/** Finished rounds, newest last, kept short. */
	public List<RoundRecord> rounds = new ArrayList<>();

	public static final class DailySnap
	{
		public boolean unlocked;
		public boolean claimable;
		public long seenAt;
		public String reward;
		public int needQuantity;
	}

	public static final class RoundRecord
	{
		public long finishedAt;
		public int seconds;
		public int chores;
		public int stops;

		public RoundRecord()
		{
		}

		public RoundRecord(long finishedAt, int seconds, int chores, int stops)
		{
			this.finishedAt = finishedAt;
			this.seconds = seconds;
			this.chores = chores;
			this.stops = stops;
		}
	}

	public Seen seen(Spot spot)
	{
		return seen == null ? null : Seen.parse(seen.get(spot.key()));
	}

	public void put(Spot spot, Seen s)
	{
		if (seen == null)
		{
			seen = new HashMap<>();
		}
		seen.put(spot.key(), s.toString());
	}

	public Map<Spot, Seen> seenMap()
	{
		Map<Spot, Seen> out = new java.util.EnumMap<>(Spot.class);
		for (Spot s : Spot.values())
		{
			Seen v = seen(s);
			if (v != null)
			{
				out.put(s, v);
			}
		}
		return out;
	}

	/** Takes an outside reading (from Time Tracking) when it is newer than ours. */
	public boolean adopt(Spot spot, Seen outside)
	{
		if (outside == null)
		{
			return false;
		}
		Seen mine = seen(spot);
		if (mine == null || (outside.getTime() > mine.getTime() && outside.getValue() != mine.getValue()))
		{
			put(spot, outside);
			return true;
		}
		return false;
	}

	/** The kingdom today, estimated from the last snapshot, or null when unknown. */
	public Kingdom kingdom(long now)
	{
		if (!kingdomQuest || kingdomApproval == null || kingdomCoffer == null || kingdomTime <= 0)
		{
			return null;
		}
		return Kingdom.estimate(kingdomApproval, kingdomCoffer, kingdomTime, now, royalTrouble);
	}

	/** Dailies as last seen, reset at midnight UTC since. */
	public List<DailyState> dailyStates(long now)
	{
		List<DailyState> out = new ArrayList<>();
		if (dailies == null)
		{
			return out;
		}
		for (Daily d : Daily.values())
		{
			DailySnap snap = dailies.get(d.name());
			if (snap == null)
			{
				continue;
			}
			Need need = null;
			if (snap.needQuantity > 0)
			{
				need = d.need(new Vars()
				{
					@Override
					public int varbit(int id)
					{
						return 0;
					}

					@Override
					public int varp(int id)
					{
						return 0;
					}
				});
				if (need != null)
				{
					need = new Need(need.getKind(), need.getLabel(), snap.needQuantity, need.getWhy(), need.isOptional(), need.getItems());
				}
			}
			DailyState st = new DailyState(d, snap.unlocked, snap.claimable, snap.reward == null ? "" : snap.reward, need);
			boolean reset = now / 86400 > snap.seenAt / 86400;
			out.add(reset ? st.afterReset() : st);
		}
		return out;
	}

	public void snapDaily(DailyState st, long now)
	{
		if (dailies == null)
		{
			dailies = new HashMap<>();
		}
		DailySnap snap = new DailySnap();
		snap.unlocked = st.isUnlocked();
		snap.claimable = st.isClaimable();
		snap.seenAt = now;
		snap.reward = st.getReward();
		snap.needQuantity = st.getNeed() == null ? 0 : st.getNeed().getQuantity();
		dailies.put(st.getDaily().name(), snap);
	}

	public Ability ability()
	{
		Ability.Builder b = Ability.builder()
			.levels(farming, hunter, crafting, magic)
			.spellbook(spellbook)
			.fairyRings(fairyRings)
			.bankKnown(bankTime > 0)
			.items(bank)
			.items(carried)
			.stored(stored)
			.paces(paceMap());
		return b.build();
	}

	/** A leg's running average, weighted toward recent laps so it follows the player getting quicker. */
	public static final class Pace
	{
		public double seconds;
		public int laps;
	}

	/** Laps it takes before a measured leg replaces the estimate. */
	static final int PACE_LAPS = 2;
	private static final int PACE_WINDOW = 5;

	public void lap(String key, int seconds)
	{
		if (paces == null)
		{
			paces = new HashMap<>();
		}
		Pace p = paces.computeIfAbsent(key, k -> new Pace());
		int n = Math.min(p.laps, PACE_WINDOW - 1);
		p.seconds = (p.seconds * n + seconds) / (n + 1);
		p.laps++;
	}

	Map<String, Integer> paceMap()
	{
		Map<String, Integer> out = new HashMap<>();
		if (paces != null)
		{
			paces.forEach((k, p) ->
			{
				if (p != null && p.laps >= PACE_LAPS)
				{
					out.put(k, (int) Math.round(p.seconds));
				}
			});
		}
		return out;
	}

	public void record(RoundRecord r)
	{
		if (rounds == null)
		{
			rounds = new ArrayList<>();
		}
		rounds.add(r);
		while (rounds.size() > 30)
		{
			rounds.remove(0);
		}
	}

	/** The fastest finished round, or null. */
	public RoundRecord best()
	{
		RoundRecord best = null;
		if (rounds != null)
		{
			for (RoundRecord r : rounds)
			{
				if (best == null || r.seconds < best.seconds)
				{
					best = r;
				}
			}
		}
		return best;
	}
}
