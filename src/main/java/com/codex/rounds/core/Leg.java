package com.codex.rounds.core;

import java.util.Arrays;

/**
 * One way of getting to a place: an item or spell that teleports, a fairy ring, or plain
 * directions when nothing better is known. Landing spots come from the Shortest Path plugin's
 * transport data (Skretzo, BSD 2-Clause).
 */
public final class Leg
{
	public enum Kind
	{
		ITEM,
		SPELL,
		FAIRY_RING,
		DIRECTIONS
	}

	/** Standard, Ancient, Lunar, Arceuus: the game's spellbook varbit values. */
	public static final int STANDARD = 0;
	public static final int ANCIENT = 1;
	public static final int LUNAR = 2;
	public static final int ARCEUUS = 3;

	private final Kind kind;
	private final String label;
	private final Point landing;
	private final int[] items;
	private final int magicLevel;
	private final int spellbook;
	private final int extraSeconds;
	private Unlock requires;

	private Leg(Kind kind, String label, Point landing, int[] items, int magicLevel, int spellbook, int extraSeconds)
	{
		this.kind = kind;
		this.label = label;
		this.landing = landing;
		this.items = items;
		this.magicLevel = magicLevel;
		this.spellbook = spellbook;
		this.extraSeconds = extraSeconds;
	}

	/** Teleport with any one of these items. */
	public static Leg item(String label, int x, int y, int... items)
	{
		return new Leg(Kind.ITEM, label, Point.of(x, y), items, 0, -1, 0);
	}

	public static Leg spell(String label, int x, int y, int level, int spellbook)
	{
		return new Leg(Kind.SPELL, label, Point.of(x, y), new int[0], level, spellbook, 0);
	}

	/** A fairy ring, reached from a ring in the house or by teleporting to one first. */
	public static Leg ring(String code, int x, int y)
	{
		return new Leg(Kind.FAIRY_RING, "Fairy ring " + code, Point.of(x, y), new int[0], 0, -1, 25);
	}

	/** Plain directions, always possible but slow. */
	public static Leg directions(String label, int x, int y, int seconds)
	{
		return new Leg(Kind.DIRECTIONS, label, Point.of(x, y), new int[0], 0, -1, seconds);
	}

	/** The same leg, open only once the account has this unlock. */
	public Leg needs(Unlock unlock)
	{
		Leg copy = new Leg(kind, label, landing, items, magicLevel, spellbook, extraSeconds);
		copy.requires = unlock;
		return copy;
	}

	public Unlock getRequires()
	{
		return requires;
	}

	public Kind getKind()
	{
		return kind;
	}

	public String getLabel()
	{
		return label;
	}

	public Point getLanding()
	{
		return landing;
	}

	/** The items that can make this teleport, any one of them. */
	public int[] getItems()
	{
		return items.clone();
	}

	public int getMagicLevel()
	{
		return magicLevel;
	}

	public int getSpellbook()
	{
		return spellbook;
	}

	/** Rough seconds to use this leg before any walking: casting, rubbing, dialling a ring. */
	public int overheadSeconds()
	{
		switch (kind)
		{
			case ITEM:
				// Taking the item out, rubbing or operating it, the destination menu, the animation and the load.
				return 14;
			case SPELL:
				return 10;
			case FAIRY_RING:
				return 5 + extraSeconds;
			default:
				return extraSeconds;
		}
	}

	public boolean usesItem(int itemId)
	{
		return Arrays.stream(items).anyMatch(i -> i == itemId);
	}

	@Override
	public String toString()
	{
		return label;
	}
}
