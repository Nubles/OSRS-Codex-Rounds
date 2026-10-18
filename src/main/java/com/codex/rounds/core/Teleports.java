package com.codex.rounds.core;

/** Item ids for the teleports a round suggests; charged variants are listed so any of them counts. */
public final class Teleports
{
	static final int[] DIGSITE_PENDANT = {11190, 11191, 11192, 11193, 11194};
	static final int[] SKILLS_NECKLACE = {11105, 11107, 11109, 11111, 11968, 11970};
	static final int[] FARMING_CAPE = {9810, 9811};
	static final int[] MAX_CAPE = {13280, 13342};
	static final int[] GLORY = {1706, 1708, 1710, 1712, 11976, 11978, 10354, 10356, 10358, 10360, 11964, 11966, 19707};
	static final int[] RING_OF_WEALTH = {11980, 11982, 11984, 11986, 11988, 20786, 20787, 20788, 20789, 20790};
	static final int[] COMBAT_BRACELET = {11118, 11120, 11122, 11124, 11972, 11974};
	static final int[] RING_OF_DUELING = {2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566};
	static final int[] ARDOUGNE_CLOAK = {13121, 13122, 13123, 13124, 20760};
	static final int[] ARDOUGNE_CLOAK_FARM = {13122, 13123, 13124, 20760};
	static final int[] EXPLORERS_RING = {13126, 13127, 13128};
	static final int[] XERICS_TALISMAN = {13393};
	static final int[] ECTOPHIAL = {4251, 4252};
	static final int[] TELEPORT_CRYSTAL = {6099, 6100, 6101, 6102, 13102, 23946};
	static final int[] PENDANT_OF_ATES = {29893};
	static final int[] QUETZAL_WHISTLE = {29271, 29273, 29275, 33120};
	static final int[] KANDARIN_HEADGEAR = {13139, 13140};
	static final int[] HUNTER_CAPE = {9948, 9949};
	static final int[] CONSTRUCTION_CAPE = {9789, 9790};

	static final int VARROCK_TABLET = 8007;
	static final int CAMELOT_TABLET = 8010;
	static final int ARDOUGNE_TABLET = 8011;
	static final int RIMMINGTON_TABLET = 11741;
	static final int BRIMHAVEN_TABLET = 11745;
	static final int YANILLE_TABLET = 11746;
	static final int HOSIDIUS_TABLET = 19651;
	static final int DRAYNOR_MANOR_TABLET = 19615;
	static final int PRIFDDINAS_TABLET = 23771;
	static final int KHARYRLL_TABLET = 12779;
	static final int FELDIP_HILLS_SCROLL = 12404;
	static final int TAI_BWO_WANNAI_SCROLL = 12409;

	/** Staffs that work fairy rings without the Lumbridge elite diary. */
	public static final int[] FAIRY_STAFFS = {772, 9084};

	private Teleports()
	{
	}

	static int[] with(int[] items, int... more)
	{
		int[] all = new int[items.length + more.length];
		System.arraycopy(items, 0, all, 0, items.length);
		System.arraycopy(more, 0, all, items.length, more.length);
		return all;
	}
}
