package com.codex.rounds.core;

import java.util.Arrays;

/** One line of the pull list: any of some items, how many, and why. */
public final class Need
{
	public enum Kind
	{
		TELEPORT("Teleports"),
		TOOL("Tools"),
		PLANT("Seeds and saplings"),
		CARE("Compost and cures"),
		PAYMENT("Gardener payments"),
		BIRDHOUSE("Bird houses"),
		DAILY("Dailies"),
		COINS("Coins");

		private final String label;

		Kind(String label)
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}
	}

	private final Kind kind;
	private final String label;
	private final int[] items;
	private final int quantity;
	private final String why;
	private final boolean optional;

	public Need(Kind kind, String label, int quantity, String why, boolean optional, int... items)
	{
		this.kind = kind;
		this.label = label;
		this.items = items;
		this.quantity = quantity;
		this.why = why;
		this.optional = optional;
	}

	public Kind getKind()
	{
		return kind;
	}

	public String getLabel()
	{
		return label;
	}

	/** The first item is the one to show; any of them will do. */
	public int[] getItems()
	{
		return items.clone();
	}

	public int getItemId()
	{
		return items.length == 0 ? -1 : items[0];
	}

	public int getQuantity()
	{
		return quantity;
	}

	public String getWhy()
	{
		return why;
	}

	public boolean isOptional()
	{
		return optional;
	}

	public boolean accepts(int itemId)
	{
		return Arrays.stream(items).anyMatch(i -> i == itemId);
	}

	/** The same need with a larger quantity, for merging lines. */
	Need plus(int more, String moreWhy)
	{
		String w = why == null || why.isEmpty() ? moreWhy : moreWhy == null || moreWhy.isEmpty() || why.contains(moreWhy) ? why : why + ", " + moreWhy;
		return new Need(kind, label, quantity + more, w, optional, items);
	}

	@Override
	public String toString()
	{
		return quantity + " x " + label;
	}
}
