package com.codex.rounds.core;

/**
 * Picks how to reach a place and what it costs in seconds. Running covers about two tiles a
 * game tick, so walking is priced a little above 0.3 seconds a tile to allow for turns and doors.
 */
public final class Travel
{
	static final double SECONDS_PER_TILE = 0.36;
	/** Beyond this, walking from the last stop loses to any teleport. */
	static final int WALK_LIMIT = 140;

	private Travel()
	{
	}

	/** A way to a place: walking from where you are, a leg, or a leg to another place then a short step. */
	public static final class Way
	{
		private final Place place;
		private final Leg leg;
		private final boolean walk;
		private final int seconds;

		Way(Place place, Leg leg, boolean walk, int seconds)
		{
			this.place = place;
			this.leg = leg;
			this.walk = walk;
			this.seconds = seconds;
		}

		/** The place the leg lands at: the place itself, or the one it is reached through. */
		public Place getPlace()
		{
			return place;
		}

		/** The teleport or route to take, or null when walking from the last stop. */
		public Leg getLeg()
		{
			return leg;
		}

		public boolean isWalk()
		{
			return walk;
		}

		public int getSeconds()
		{
			return seconds;
		}

		public String describe()
		{
			if (walk)
			{
				return "Walk to " + place.getLabel();
			}
			return leg.getLabel();
		}
	}

	static int walkSeconds(Point a, Point b)
	{
		return (int) Math.round(a.distance(b) * SECONDS_PER_TILE) + 2;
	}

	/** The quickest way to a place from a spot, using only what the account can use. */
	public static Way best(Point from, Place place, Ability ability)
	{
		if (place.getVia() != null)
		{
			Way first = best(from, place.getVia(), ability);
			int local = walkSeconds(place.getVia().getAnchor(), place.getLocalPoint()) + 12;
			return new Way(first.place, first.leg, first.walk, first.seconds + local);
		}
		Way best = null;
		if (from != null && from.sameMap(place.getAnchor()) && from.distance(place.getAnchor()) <= WALK_LIMIT)
		{
			best = new Way(place, null, true, walkSeconds(from, place.getAnchor()));
		}
		for (Leg leg : place.getLegs())
		{
			if (!ability.canUse(leg))
			{
				continue;
			}
			int s = leg.overheadSeconds() + walkSeconds(leg.getLanding(), place.getAnchor());
			if (best == null || s < best.seconds)
			{
				best = new Way(place, leg, false, s);
			}
		}
		if (best == null)
		{
			// Every place lists plain directions last, so this only happens for a place with no legs.
			best = new Way(place, null, true, from == null ? 300 : walkSeconds(from, place.getAnchor()));
		}
		return best;
	}
}
