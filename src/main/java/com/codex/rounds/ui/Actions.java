package com.codex.rounds.ui;

/** What the panel's buttons ask the plugin to do. */
public interface Actions
{
	void start();

	/** Start a round of just the bird houses and seaweed. */
	void startQuick();

	void stop();

	void skip();

	void replan();

	/** Lay the pull list out as a bank tab, now if the bank is open or the next time it opens. */
	void bankTab();
}
