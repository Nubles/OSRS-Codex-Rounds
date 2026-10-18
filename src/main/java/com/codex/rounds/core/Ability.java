package com.codex.rounds.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** What the account can use: items it owns, levels, spellbook and travel unlocks. */
public final class Ability
{
	private final Map<Integer, Integer> owned;
	private final boolean bankKnown;
	private final int farming;
	private final int hunter;
	private final int crafting;
	private final int magic;
	private final int spellbook;
	private final boolean fairyRings;

	private Ability(Builder b)
	{
		this.owned = Collections.unmodifiableMap(new HashMap<>(b.owned));
		this.bankKnown = b.bankKnown;
		this.farming = b.farming;
		this.hunter = b.hunter;
		this.crafting = b.crafting;
		this.magic = b.magic;
		this.spellbook = b.spellbook;
		this.fairyRings = b.fairyRings;
	}

	public static Builder builder()
	{
		return new Builder();
	}

	/** Everything at 99, owning nothing known: for previews before any data. */
	public static Ability unknown()
	{
		return builder().levels(99, 99, 99, 99).build();
	}

	/** How many of any of these items the account holds across the bank, pack and worn gear. */
	public int count(int... ids)
	{
		int n = 0;
		for (int id : ids)
		{
			n += owned.getOrDefault(id, 0);
		}
		return n;
	}

	/** Whether the account owns one; when the bank was never seen, assume it might. */
	public boolean mayOwn(int... ids)
	{
		return !bankKnown || count(ids) > 0;
	}

	public boolean owns(int... ids)
	{
		return count(ids) > 0;
	}

	public boolean isBankKnown()
	{
		return bankKnown;
	}

	public int getFarming()
	{
		return farming;
	}

	public int getHunter()
	{
		return hunter;
	}

	public int getCrafting()
	{
		return crafting;
	}

	public int getMagic()
	{
		return magic;
	}

	public int getSpellbook()
	{
		return spellbook;
	}

	public boolean hasFairyRings()
	{
		return fairyRings;
	}

	/** Whether a way of travelling is open to this account. */
	public boolean canUse(Leg leg)
	{
		switch (leg.getKind())
		{
			case ITEM:
				return mayOwn(leg.getItems());
			case SPELL:
				return magic >= leg.getMagicLevel() && spellbook == leg.getSpellbook();
			case FAIRY_RING:
				return fairyRings;
			default:
				return true;
		}
	}

	public static final class Builder
	{
		private final Map<Integer, Integer> owned = new HashMap<>();
		private boolean bankKnown;
		private int farming = 1;
		private int hunter = 1;
		private int crafting = 1;
		private int magic = 1;
		private int spellbook;
		private boolean fairyRings;

		public Builder item(int id, int qty)
		{
			if (id >= 0 && qty > 0)
			{
				owned.merge(id, qty, Integer::sum);
			}
			return this;
		}

		public Builder items(Map<Integer, Integer> items)
		{
			if (items != null)
			{
				items.forEach(this::item);
			}
			return this;
		}

		public Builder bankKnown(boolean known)
		{
			this.bankKnown = known;
			return this;
		}

		public Builder levels(int farming, int hunter, int crafting, int magic)
		{
			this.farming = farming;
			this.hunter = hunter;
			this.crafting = crafting;
			this.magic = magic;
			return this;
		}

		public Builder spellbook(int spellbook)
		{
			this.spellbook = spellbook;
			return this;
		}

		public Builder fairyRings(boolean fairyRings)
		{
			this.fairyRings = fairyRings;
			return this;
		}

		public Ability build()
		{
			return new Ability(this);
		}
	}
}
