package com.codex.rounds.core;

/**
 * Times each teleport leg of a round, from the moment the guide says to travel to the moment the player stands
 * in the stop, so later routes are planned with how long that leg really takes this player.
 */
public final class TravelTimer
{
	/** A finished leg: its key and the seconds it took. */
	public static final class Lap
	{
		private final String key;
		private final int seconds;

		Lap(String key, int seconds)
		{
			this.key = key;
			this.seconds = seconds;
		}

		public String getKey()
		{
			return key;
		}

		public int getSeconds()
		{
			return seconds;
		}
	}

	private Place target;
	private String key;
	private int estimate;
	private long startMs;

	public static String key(Place place, Leg leg)
	{
		return place.name() + "|" + leg.getLabel();
	}

	/** Called on every refresh while a round runs; returns a lap when the player arrives. */
	public Lap update(Step step, Route route, Point player, Ability ability, long nowMs)
	{
		if (target != null && target.contains(player))
		{
			Lap lap = null;
			int seconds = (int) Math.round((nowMs - startMs) / 1000.0);
			// A detour (banking, a chat, a break) is not the leg's time.
			if (key != null && seconds >= 2 && seconds <= estimate * 3 + 30)
			{
				lap = new Lap(key, seconds);
			}
			reset();
			return lap;
		}
		if (step.getKind() != Step.Kind.TRAVEL || step.getStopIndex() < 0 || step.getStopIndex() >= route.getStops().size())
		{
			reset();
			return null;
		}
		Place place = route.getStops().get(step.getStopIndex()).getPlace();
		if (place != target)
		{
			Travel.Way way = Travel.best(player, place, ability);
			target = place;
			key = way.getLeg() == null ? null : key(place, way.getLeg());
			estimate = way.getSeconds();
			startMs = nowMs;
		}
		return null;
	}

	public void reset()
	{
		target = null;
		key = null;
	}
}
