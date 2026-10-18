package com.codex.rounds.core;

import com.codex.rounds.core.farming.Action;
import com.codex.rounds.core.farming.Produce;

/** One chore at a stop: a patch or bird house, a daily, or the kingdom. */
public final class Task
{
	private final Board.Type type;
	private final Spot spot;
	private final Daily daily;
	private final Reading reading;
	private final Produce plant;
	private final boolean compost;
	private final boolean protect;
	private final String title;
	private final String detail;
	private final int seconds;

	Task(Board.Type type, Spot spot, Daily daily, Reading reading, Produce plant, boolean compost, boolean protect,
		String title, String detail, int seconds)
	{
		this.type = type;
		this.spot = spot;
		this.daily = daily;
		this.reading = reading;
		this.plant = plant;
		this.compost = compost;
		this.protect = protect;
		this.title = title;
		this.detail = detail;
		this.seconds = seconds;
	}

	public Board.Type getType()
	{
		return type;
	}

	public Spot getSpot()
	{
		return spot;
	}

	public Daily getDaily()
	{
		return daily;
	}

	public Reading getReading()
	{
		return reading;
	}

	/** What goes in the patch once it is clear, or null to leave it. */
	public Produce getPlant()
	{
		return plant;
	}

	public boolean isCompost()
	{
		return compost;
	}

	public boolean isProtect()
	{
		return protect;
	}

	public String getTitle()
	{
		return title;
	}

	public String getDetail()
	{
		return detail;
	}

	public int getSeconds()
	{
		return seconds;
	}

	/** Whether the gardener is paid 200 coins to clear a grown tree. */
	public boolean isGardenerClear()
	{
		return spot != null && reading != null && spot.getGardener() >= 0
			&& (reading.getAction() == Action.CHOP || reading.getAction() == Action.CHECK_HEALTH)
			&& plant != null && Planner.isTree(spot);
	}
}
