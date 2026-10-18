package com.codex.rounds.core;

import java.util.Locale;

/** Text for numbers and times, the way the game writes them. */
public final class Format
{
	private Format()
	{
	}

	public static String number(long n)
	{
		return String.format(Locale.ROOT, "%,d", n);
	}

	/** 7,000 / 840k / 1.2m coins. */
	public static String coins(long n)
	{
		return stack(n) + " coins";
	}

	public static String stack(long n)
	{
		if (n >= 10_000_000)
		{
			return (n / 1_000_000) + "m";
		}
		if (n >= 1_000_000)
		{
			return trim(n / 1_000_000.0) + "m";
		}
		if (n >= 100_000)
		{
			return (n / 1000) + "k";
		}
		return number(n);
	}

	private static String trim(double d)
	{
		String s = String.format(Locale.ROOT, "%.1f", d);
		return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
	}

	/** "now", "4 min", "1 hr 20 min", "2 days 3 hr". */
	public static String duration(long seconds)
	{
		if (seconds <= 0)
		{
			return "now";
		}
		long mins = (seconds + 59) / 60;
		if (mins < 60)
		{
			return mins + " min";
		}
		long hrs = mins / 60;
		long rem = mins % 60;
		if (hrs < 24)
		{
			return rem == 0 ? hrs + " hr" : hrs + " hr " + rem + " min";
		}
		long days = hrs / 24;
		long h = hrs % 24;
		return (days == 1 ? "1 day" : days + " days") + (h == 0 ? "" : " " + h + " hr");
	}

	/** A short duration for tight spaces: "4m", "1h 20m", "2d 3h". */
	public static String shortDuration(long seconds)
	{
		if (seconds <= 0)
		{
			return "now";
		}
		long mins = (seconds + 59) / 60;
		if (mins < 60)
		{
			return mins + "m";
		}
		long hrs = mins / 60;
		long rem = mins % 60;
		if (hrs < 24)
		{
			return rem == 0 ? hrs + "h" : hrs + "h " + rem + "m";
		}
		long days = hrs / 24;
		long h = hrs % 24;
		return h == 0 ? days + "d" : days + "d " + h + "h";
	}

	/** Seconds as m:ss for a round's running clock. */
	public static String clock(long seconds)
	{
		long s = Math.max(0, seconds);
		if (s >= 3600)
		{
			return String.format(Locale.ROOT, "%d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60);
		}
		return String.format(Locale.ROOT, "%d:%02d", s / 60, s % 60);
	}
}
