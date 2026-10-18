package com.codex.rounds.core;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** What has happened during the current round that game variables don't show. */
public final class Progress
{
	private final Set<String> skipped = new HashSet<>();
	private final Map<Spot, Long> planted = new EnumMap<>(Spot.class);
	private final Map<Spot, Long> composted = new EnumMap<>(Spot.class);
	private final Map<Spot, Long> paid = new EnumMap<>(Spot.class);
	private boolean suppliesDone;
	private boolean leprechaunDone;
	private int bankedChores;
	private int bankedStops;
	private final Set<Spot> sawReady = EnumSet.noneOf(Spot.class);
	private long startedAt;

	public static String key(Task t)
	{
		switch (t.getType())
		{
			case SPOT:
				return "spot:" + t.getSpot().name();
			case DAILY:
				return "daily:" + t.getDaily().name();
			default:
				return "kingdom";
		}
	}

	public void skip(Task t)
	{
		skipped.add(key(t));
	}

	public boolean isSkipped(Task t)
	{
		return skipped.contains(key(t));
	}

	public void planted(Spot s, long when)
	{
		planted.put(s, when);
		composted.remove(s);
		paid.remove(s);
	}

	public boolean wasPlanted(Spot s)
	{
		return planted.containsKey(s);
	}

	public void composted(Spot s, long when)
	{
		composted.put(s, when);
	}

	public boolean isComposted(Spot s)
	{
		return composted.containsKey(s);
	}

	public void paid(Spot s, long when)
	{
		paid.put(s, when);
	}

	public boolean isPaid(Spot s)
	{
		return paid.containsKey(s);
	}

	public boolean isSuppliesDone()
	{
		return suppliesDone;
	}

	public void suppliesDone()
	{
		this.suppliesDone = true;
	}

	/** Chores and stops finished before a mid-round replan, which drops them from the new route. */
	public void bank(int chores, int stops)
	{
		bankedChores += chores;
		bankedStops += stops;
	}

	public int getBankedChores()
	{
		return bankedChores;
	}

	public int getBankedStops()
	{
		return bankedStops;
	}

	/** A fresh reading during the round showed the spot ready, so growing again afterwards means it was harvested. */
	public void sawReady(Spot spot)
	{
		sawReady.add(spot);
	}

	public boolean wasSeenReady(Spot spot)
	{
		return sawReady.contains(spot);
	}

	public boolean isLeprechaunDone()
	{
		return leprechaunDone;
	}

	public void leprechaunDone()
	{
		this.leprechaunDone = true;
	}

	public long getStartedAt()
	{
		return startedAt;
	}

	public void start(long now)
	{
		skipped.clear();
		planted.clear();
		composted.clear();
		paid.clear();
		suppliesDone = false;
		leprechaunDone = false;
		bankedChores = 0;
		bankedStops = 0;
		sawReady.clear();
		startedAt = now;
	}

	public boolean isStarted()
	{
		return startedAt > 0;
	}

	public void stop()
	{
		startedAt = 0;
	}
}
