package com.codex.rounds.ui;

import com.codex.rounds.core.Board;
import com.codex.rounds.core.Route;
import com.codex.rounds.core.Step;
import java.util.Collections;
import java.util.Map;

/** Everything the panel shows, taken in one go on the client thread. */
public final class View
{
	public final boolean loggedIn;
	public final boolean known;
	public final Board board;
	public final Route route;
	public final Step step;
	public final boolean running;
	public final long startedAt;
	public final long now;
	public final boolean bankKnown;
	/** Items in the pack and worn, then what the bank holds. */
	public final Map<Integer, Integer> carried;
	public final Map<Integer, Integer> bank;
	public final String lastRound;
	/** Tools and compost with the tool leprechaun. */
	public final Map<Integer, Integer> stored;
	/** Whether the round's first stop has a leprechaun to take them from. */
	public final boolean leprechaunFirst;
	/** A suggestion to wait for more chores, or null. */
	public final String forecast;
	/** Any ironman mode: nothing short can be bought. */
	public final boolean ironman;
	/** Roughly the pack slots the pull list takes. */
	public final int slots;

	public View(boolean loggedIn, boolean known, Board board, Route route, Step step, boolean running, long startedAt, long now,
		boolean bankKnown, Map<Integer, Integer> carried, Map<Integer, Integer> bank, String lastRound, Map<Integer, Integer> stored, boolean leprechaunFirst, String forecast,
		boolean ironman, int slots)
	{
		this.slots = slots;
		this.forecast = forecast;
		this.ironman = ironman;
		this.stored = stored;
		this.leprechaunFirst = leprechaunFirst;
		this.loggedIn = loggedIn;
		this.known = known;
		this.board = board;
		this.route = route;
		this.step = step;
		this.running = running;
		this.startedAt = startedAt;
		this.now = now;
		this.bankKnown = bankKnown;
		this.carried = carried;
		this.bank = bank;
		this.lastRound = lastRound;
	}

	public static View empty()
	{
		return new View(false, false, null, Route.EMPTY, Step.IDLE, false, 0, 0, false, Collections.emptyMap(), Collections.emptyMap(), null, Collections.emptyMap(), false, null, false, 0);
	}

	int stored(int... ids)
	{
		int n = 0;
		for (int id : ids)
		{
			n += stored.getOrDefault(id, 0);
		}
		return n;
	}

	int carrying(int... ids)
	{
		int n = 0;
		for (int id : ids)
		{
			n += carried.getOrDefault(id, 0);
		}
		return n;
	}

	int banked(int... ids)
	{
		int n = 0;
		for (int id : ids)
		{
			n += bank.getOrDefault(id, 0);
		}
		return n;
	}
}
