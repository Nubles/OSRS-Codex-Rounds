package com.codex.rounds.core;

/** A daily's standing today: unlocked, claimable, what it gives and needs. */
public final class DailyState
{
	private final Daily daily;
	private final boolean unlocked;
	private final boolean claimable;
	private final String reward;
	private final Need need;
	private final long readyAt;
	private final String waiting;
	private final boolean unseen;

	public DailyState(Daily daily, boolean unlocked, boolean claimable, String reward, Need need)
	{
		this(daily, unlocked, claimable, reward, need, 0, null, false);
	}

	/**
	 * @param readyAt when it comes back, or 0 for the next daily reset
	 * @param waiting what the board says while it is not claimable, or null for "Claimed today"
	 * @param unseen  Rounds cannot tell yet whether it is waiting
	 */
	public DailyState(Daily daily, boolean unlocked, boolean claimable, String reward, Need need, long readyAt, String waiting, boolean unseen)
	{
		this.daily = daily;
		this.unlocked = unlocked;
		this.claimable = claimable;
		this.reward = reward;
		this.need = need;
		this.readyAt = readyAt;
		this.waiting = waiting;
		this.unseen = unseen;
	}

	public static DailyState of(Daily d, Vars v)
	{
		boolean unlocked = d.unlocked(v);
		boolean claimable = unlocked && d.claimable(v);
		return new DailyState(d, unlocked, claimable, unlocked ? d.reward(v) : "", claimable ? d.need(v) : null);
	}

	public Daily getDaily()
	{
		return daily;
	}

	public boolean isUnlocked()
	{
		return unlocked;
	}

	public boolean isClaimable()
	{
		return claimable;
	}

	public String getReward()
	{
		return reward;
	}

	/** What to bring, or null. */
	public Need getNeed()
	{
		return need;
	}

	/** When it comes back, or 0 for the next daily reset. */
	public long getReadyAt()
	{
		return readyAt;
	}

	/** What the board says while it waits, or null for the daily wording. */
	public String getWaiting()
	{
		return waiting;
	}

	/** Whether Rounds has not seen enough to know if it is waiting. */
	public boolean isUnseen()
	{
		return unseen;
	}

	/** The state after the daily reset: claimable again if it was ever unlocked. */
	public DailyState afterReset()
	{
		return new DailyState(daily, unlocked, unlocked, reward, need);
	}
}
