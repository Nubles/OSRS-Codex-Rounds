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

	Route(List<Stop> stops, List<Need> pulls, long builtAt)
	{
		this.stops = Collections.unmodifiableList(stops);
		this.pulls = Collections.unmodifiableList(pulls);
		this.builtAt = builtAt;
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
