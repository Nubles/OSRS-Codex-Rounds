package com.codex.rounds.core;

import java.util.Collections;
import java.util.List;

/** A place on the round, how to get there, and what to do on arrival. */
public final class Stop
{
	private final Place place;
	private final Travel.Way way;
	private final List<Task> tasks;
	private final int seconds;

	Stop(Place place, Travel.Way way, List<Task> tasks)
	{
		this.place = place;
		this.way = way;
		this.tasks = Collections.unmodifiableList(tasks);
		this.seconds = way.getSeconds() + tasks.stream().mapToInt(Task::getSeconds).sum();
	}

	public Place getPlace()
	{
		return place;
	}

	public Travel.Way getWay()
	{
		return way;
	}

	public List<Task> getTasks()
	{
		return tasks;
	}

	/** Travel plus work, in seconds. */
	public int getSeconds()
	{
		return seconds;
	}
}
