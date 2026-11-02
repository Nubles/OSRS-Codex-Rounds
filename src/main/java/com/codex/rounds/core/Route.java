package com.codex.rounds.core;

import java.util.Collections;
import java.util.List;

/** A planned round: stops in order and the pull list. */
public final class Route
{
	public static final Route EMPTY = new Route(Collections.emptyList(), Collections.emptyList(), 0);

	private final List<Stop> stops;
	private final List<Need> pulls;
	private final long builtAt;
	/** The stop each trip from the bank starts at; one trip starting at 0 when everything fits in a pack. */
	private final List<Integer> tripStarts;
	private final List<List<Need>> tripPulls;

	Route(List<Stop> stops, List<Need> pulls, long builtAt)
	{
		this(stops, pulls, builtAt, Collections.singletonList(0), Collections.singletonList(pulls));
	}

	Route(List<Stop> stops, List<Need> pulls, long builtAt, List<Integer> tripStarts, List<List<Need>> tripPulls)
	{
		this.stops = Collections.unmodifiableList(stops);
		this.pulls = Collections.unmodifiableList(pulls);
		this.builtAt = builtAt;
		this.tripStarts = Collections.unmodifiableList(tripStarts);
		this.tripPulls = Collections.unmodifiableList(tripPulls);
	}

	/** Trips from the bank this round takes: more than one when its supplies don't fit in one pack. */
	public int tripCount()
	{
		return tripStarts.size();
	}

	/** The first stop of a trip. */
	public int tripStart(int trip)
	{
		return tripStarts.get(trip);
	}

	/** The trip a stop belongs to. */
	public int tripOf(int stop)
	{
		int trip = 0;
		for (int i = 0; i < tripStarts.size(); i++)
		{
			if (tripStarts.get(i) <= stop)
			{
				trip = i;
			}
		}
		return trip;
	}

	/** What one trip takes from the bank. */
	public List<Need> getTripPulls(int trip)
	{
		return tripPulls.get(trip);
	}

	public List<Stop> getStops()
	{
		return stops;
	}

	public List<Need> getPulls()
	{
		return pulls;
	}

	public long getBuiltAt()
	{
		return builtAt;
	}

	public boolean isEmpty()
	{
		return stops.isEmpty();
	}

	public int getSeconds()
	{
		return stops.stream().mapToInt(Stop::getSeconds).sum();
	}

	public int taskCount()
	{
		return stops.stream().mapToInt(s -> s.getTasks().size()).sum();
	}
}
