/*
 * Copyright (c) 2019 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.codex.rounds.core.farming;

import net.runelite.api.gameval.ItemID;

/**
 * What can grow in the patches a round visits. Growth timings are RuneLite's (Time Tracking
 * plugin); the seed, level and gardener's payment are added so a round knows what to plant and
 * what to bring.
 */
public enum Produce
{
	WEEDS("Weeds", null, ItemID.WEEDS, 5, 4, 0, 1, -1, 1),

	// Bushes
	REDBERRIES("Redberry", PatchKind.BUSH, ItemID.REDBERRIES, 20, 6, 20, 5, ItemID.REDBERRY_BUSH_SEED, 10, ItemID.SACK_CABBAGE_10, 4),
	CADAVABERRIES("Cadavaberry", PatchKind.BUSH, ItemID.CADAVABERRIES, 20, 7, 20, 5, ItemID.CADAVABERRY_BUSH_SEED, 22, ItemID.BASKET_TOMATO_5, 3),
	DWELLBERRIES("Dwellberry", PatchKind.BUSH, ItemID.DWELLBERRIES, 20, 8, 20, 5, ItemID.DWELLBERRY_BUSH_SEED, 36, ItemID.BASKET_STRAWBERRY_5, 3),
	JANGERBERRIES("Jangerberry", PatchKind.BUSH, ItemID.JANGERBERRIES, 20, 9, 20, 5, ItemID.JANGERBERRY_BUSH_SEED, 48, ItemID.WATERMELON, 6),
	WHITEBERRIES("Whiteberry", PatchKind.BUSH, ItemID.WHITE_BERRIES, 20, 9, 20, 5, ItemID.WHITEBERRY_BUSH_SEED, 59, ItemID.BITTERCAP_MUSHROOM, 8),
	POISON_IVY("Poison ivy", PatchKind.BUSH, ItemID.POISONIVY_BERRIES, 20, 9, 20, 5, ItemID.POISONIVY_BUSH_SEED, 70),

	// Cactus
	CACTUS("Cactus", PatchKind.CACTUS, ItemID.CACTUS_SPINE, 80, 8, 20, 4, ItemID.CACTUS_SEED, 55, ItemID.CADAVABERRIES, 6),
	POTATO_CACTUS("Potato cactus", PatchKind.CACTUS, ItemID.CACTUS_POTATO, 10, 8, 5, 7, ItemID.POTATO_CACTUS_SEED, 64, ItemID.SNAPE_GRASS, 8),

	// Hardwood
	TEAK("Teak", PatchKind.HARDWOOD_TREE, ItemID.TEAK_LOGS, 640, 8, 0, 1, ItemID.PLANTPOT_TEAK_SAPLING, 35, ItemID.LIMPWURT_ROOT, 15),
	MAHOGANY("Mahogany", PatchKind.HARDWOOD_TREE, ItemID.MAHOGANY_LOGS, 640, 9, 0, 1, ItemID.PLANTPOT_MAHOGANY_SAPLING, 55, ItemID.YANILLIAN_HOPS, 25),
	CAMPHOR("Camphor", PatchKind.HARDWOOD_TREE, ItemID.CAMPHOR_LOGS, 640, 9, 0, 1, ItemID.PLANTPOT_CAMPHOR_SAPLING, 66, ItemID.WHITE_BERRIES, 10),
	IRONWOOD("Ironwood", PatchKind.HARDWOOD_TREE, ItemID.IRONWOOD_LOGS, 640, 9, 0, 1, ItemID.PLANTPOT_IRONWOOD_SAPLING, 80, ItemID.CURRY_LEAF, 10),
	ROSEWOOD("Rosewood", PatchKind.HARDWOOD_TREE, ItemID.ROSEWOOD_LOGS, 640, 10, 0, 1, ItemID.PLANTPOT_ROSEWOOD_SAPLING, 92, ItemID.DRAGONFRUIT, 8),

	// Anima
	ATTAS("Attas", PatchKind.ANIMA, ItemID.ANIMA_ATTAS, 640, 9, 0, 1, ItemID.ATTAS_SEED, 76),
	IASOR("Iasor", PatchKind.ANIMA, ItemID.ANIMA_IASOR, 640, 9, 0, 1, ItemID.IASOR_SEED, 76),
	KRONOS("Kronos", PatchKind.ANIMA, ItemID.ANIMA_KRONOS, 640, 9, 0, 1, ItemID.KRONOS_SEED, 76),

	// Special
	SEAWEED("Giant seaweed", PatchKind.SEAWEED, ItemID.GIANT_SEAWEED, 10, 5, 0, 4, ItemID.SEAWEED_SEED, 23),
	MUSHROOM("Bittercap", PatchKind.MUSHROOM, ItemID.BITTERCAP_MUSHROOM, 40, 7, 0, 7, ItemID.MUSHROOM_SEED, 53),
	BELLADONNA("Belladonna", PatchKind.BELLADONNA, ItemID.NIGHTSHADE, 80, 5, 0, 1, ItemID.BELLADONNA_SEED, 63),
	CALQUAT("Calquat", PatchKind.CALQUAT, ItemID.CALQUAT_FRUIT, 160, 9, 0, 7, ItemID.PLANTPOT_CALQUAT_SAPLING, 72, ItemID.POISONIVY_BERRIES, 8),
	SPIRIT_TREE("Spirit tree", PatchKind.SPIRIT_TREE, ItemID.PLANTPOT_SPIRIT_TREE_SAPLING, 320, 13, 0, 1, ItemID.PLANTPOT_SPIRIT_TREE_SAPLING, 83,
		ItemID.MM_MONKEY_NUTS, 5, ItemID.MM_MONKEY_BAR, 1, ItemID.LUNAR_GROUNDTOOTH, 1),
	CELASTRUS("Celastrus", PatchKind.CELASTRUS, ItemID.BATTLESTAFF, 160, 6, 0, 4, ItemID.PLANTPOT_CELASTRUS_TREE_SAPLING, 85, ItemID.CACTUS_POTATO, 8),
	REDWOOD("Redwood", PatchKind.REDWOOD, ItemID.REDWOOD_LOGS, 640, 11, 0, 1, ItemID.PLANTPOT_REDWOOD_TREE_SAPLING, 90, ItemID.DRAGONFRUIT, 6),
	HESPORI("Hespori", PatchKind.HESPORI, ItemID.HESPORI_SEED, 640, 4, 0, 2, ItemID.HESPORI_SEED, 65),
	CRYSTAL_TREE("Crystal tree", PatchKind.CRYSTAL_TREE, ItemID.PLANTPOT_CRYSTAL_TREE_SAPLING, 80, 7, 0, 1, ItemID.PLANTPOT_CRYSTAL_TREE_SAPLING, 74),

	// Coral, in the nurseries under the Great Conch: four 40 minute stages (OSRS Wiki, Coral farming),
	// protected by Chet for five of the coral below it (giant seaweed for elkhorn).
	ELKHORN_CORAL("Elkhorn coral", PatchKind.CORAL, ItemID.CORAL_ELKHORN, 40, 5, 0, 1, ItemID.CORAL_ELKHORN_FRAG, 28, ItemID.GIANT_SEAWEED, 5),
	PILLAR_CORAL("Pillar coral", PatchKind.CORAL, ItemID.CORAL_PILLAR, 40, 5, 0, 1, ItemID.CORAL_PILLAR_FRAG, 52, ItemID.CORAL_ELKHORN, 5),
	UMBRAL_CORAL("Umbral coral", PatchKind.CORAL, ItemID.CORAL_UMBRAL, 40, 5, 0, 1, ItemID.CORAL_UMBRAL_FRAG, 77, ItemID.CORAL_PILLAR, 5),

	// Trees
	OAK("Oak", PatchKind.TREE, ItemID.OAK_LOGS, 40, 5, 0, 1, ItemID.PLANTPOT_OAK_SAPLING, 15, ItemID.BASKET_TOMATO_5, 1),
	WILLOW("Willow", PatchKind.TREE, ItemID.WILLOW_LOGS, 40, 7, 0, 1, ItemID.PLANTPOT_WILLOW_SAPLING, 30, ItemID.BASKET_APPLE_5, 1),
	MAPLE("Maple", PatchKind.TREE, ItemID.MAPLE_LOGS, 40, 9, 0, 1, ItemID.PLANTPOT_MAPLE_SAPLING, 45, ItemID.BASKET_ORANGE_5, 1),
	YEW("Yew", PatchKind.TREE, ItemID.YEW_LOGS, 40, 11, 0, 1, ItemID.PLANTPOT_YEW_SAPLING, 60, ItemID.CACTUS_SPINE, 10),
	MAGIC("Magic", PatchKind.TREE, ItemID.MAGIC_LOGS, 40, 13, 0, 1, ItemID.PLANTPOT_MAGIC_TREE_SAPLING, 75, ItemID.COCONUT, 25),

	// Fruit trees
	APPLE("Apple", PatchKind.FRUIT_TREE, ItemID.COOKING_APPLE, 160, 7, 45, 7, ItemID.PLANTPOT_APPLE_SAPLING, 27, ItemID.SWEETCORN, 9),
	BANANA("Banana", PatchKind.FRUIT_TREE, ItemID.BANANA, 160, 7, 45, 7, ItemID.PLANTPOT_BANANA_SAPLING, 33, ItemID.BASKET_APPLE_5, 4),
	ORANGE("Orange", PatchKind.FRUIT_TREE, ItemID.ORANGE, 160, 7, 45, 7, ItemID.PLANTPOT_ORANGE_SAPLING, 39, ItemID.BASKET_STRAWBERRY_5, 3),
	CURRY("Curry", PatchKind.FRUIT_TREE, ItemID.CURRY_LEAF, 160, 7, 45, 7, ItemID.PLANTPOT_CURRY_SAPLING, 42, ItemID.BASKET_BANANA_5, 5),
	PINEAPPLE("Pineapple", PatchKind.FRUIT_TREE, ItemID.PINEAPPLE, 160, 7, 45, 7, ItemID.PLANTPOT_PINEAPPLE_SAPLING, 51, ItemID.WATERMELON, 10),
	PAPAYA("Papaya", PatchKind.FRUIT_TREE, ItemID.PAPAYA, 160, 7, 45, 7, ItemID.PLANTPOT_PAPAYA_SAPLING, 57, ItemID.PINEAPPLE, 10),
	PALM("Palm", PatchKind.FRUIT_TREE, ItemID.COCONUT, 160, 7, 45, 7, ItemID.PLANTPOT_PALM_SAPLING, 68, ItemID.PAPAYA, 15),
	DRAGONFRUIT("Dragonfruit", PatchKind.FRUIT_TREE, ItemID.DRAGONFRUIT, 160, 7, 45, 7, ItemID.PLANTPOT_DRAGONFRUIT_SAPLING, 81, ItemID.COCONUT, 15),

	// Herbs: they cannot be protected, so ultracompost and magic secateurs do the work
	GUAM("Guam", PatchKind.HERB, ItemID.GUAM_LEAF, 20, 5, 0, 3, ItemID.GUAM_SEED, 9),
	MARRENTILL("Marrentill", PatchKind.HERB, ItemID.MARENTILL, 20, 5, 0, 3, ItemID.MARRENTILL_SEED, 14),
	TARROMIN("Tarromin", PatchKind.HERB, ItemID.TARROMIN, 20, 5, 0, 3, ItemID.TARROMIN_SEED, 19),
	HARRALANDER("Harralander", PatchKind.HERB, ItemID.HARRALANDER, 20, 5, 0, 3, ItemID.HARRALANDER_SEED, 26),
	RANARR("Ranarr", PatchKind.HERB, ItemID.RANARR_WEED, 20, 5, 0, 3, ItemID.RANARR_SEED, 32),
	TOADFLAX("Toadflax", PatchKind.HERB, ItemID.TOADFLAX, 20, 5, 0, 3, ItemID.TOADFLAX_SEED, 38),
	IRIT("Irit", PatchKind.HERB, ItemID.IRIT_LEAF, 20, 5, 0, 3, ItemID.IRIT_SEED, 44),
	AVANTOE("Avantoe", PatchKind.HERB, ItemID.AVANTOE, 20, 5, 0, 3, ItemID.AVANTOE_SEED, 50),
	KWUARM("Kwuarm", PatchKind.HERB, ItemID.KWUARM, 20, 5, 0, 3, ItemID.KWUARM_SEED, 56),
	HUASCA("Huasca", PatchKind.HERB, ItemID.HUASCA, 20, 5, 0, 3, ItemID.HUASCA_SEED, 65),
	SNAPDRAGON("Snapdragon", PatchKind.HERB, ItemID.SNAPDRAGON, 20, 5, 0, 3, ItemID.SNAPDRAGON_SEED, 62),
	CADANTINE("Cadantine", PatchKind.HERB, ItemID.CADANTINE, 20, 5, 0, 3, ItemID.CADANTINE_SEED, 67),
	LANTADYME("Lantadyme", PatchKind.HERB, ItemID.LANTADYME, 20, 5, 0, 3, ItemID.LANTADYME_SEED, 73),
	DWARF_WEED("Dwarf weed", PatchKind.HERB, ItemID.DWARF_WEED, 20, 5, 0, 3, ItemID.DWARF_WEED_SEED, 79),
	TORSTOL("Torstol", PatchKind.HERB, ItemID.TORSTOL, 20, 5, 0, 3, ItemID.TORSTOL_SEED, 85),
	/** Only ever read from the patch: never offered for planting. */
	GOUTWEED("Goutweed", PatchKind.HERB, ItemID.EADGAR_GOUTWEED_HERB, 20, 5, 0, 2, -1, Produce.NEVER_PLANTED),
	/** An herb the patch shows without saying which (RuneLite reads it the same way). */
	ANYHERB("Herbs", PatchKind.HERB, ItemID.GUAM_LEAF, 20, 5, 0, 3, -1, Produce.NEVER_PLANTED);

	/** A level no account reaches, for crops read from a patch but never planted. */
	public static final int NEVER_PLANTED = 1000;

	private final String name;
	private final PatchKind kind;
	private final int itemId;
	private final int tickrate;
	private final int stages;
	private final int regrowTickrate;
	private final int harvestStages;
	private final int seedId;
	private final int level;
	/** Item, amount pairs the gardener takes to protect the crop, empty when it cannot be protected. */
	private final int[] payment;

	Produce(String name, PatchKind kind, int itemId, int tickrate, int stages, int regrowTickrate, int harvestStages,
		int seedId, int level, int... payment)
	{
		this.name = name;
		this.kind = kind;
		this.itemId = itemId;
		this.tickrate = tickrate;
		this.stages = stages;
		this.regrowTickrate = regrowTickrate;
		this.harvestStages = harvestStages;
		this.seedId = seedId;
		this.level = level;
		this.payment = payment;
	}

	public String getName()
	{
		return name;
	}

	public PatchKind getKind()
	{
		return kind;
	}

	/** The item the crop is shown with. */
	public int getItemId()
	{
		return itemId;
	}

	/** Minutes per growth tick. */
	public int getTickrate()
	{
		return tickrate;
	}

	/** States while growing, typically growth ticks plus one. */
	public int getStages()
	{
		return stages;
	}

	/** Minutes for the produce to regrow once picked, or zero when it does not. */
	public int getRegrowTickrate()
	{
		return regrowTickrate;
	}

	/** States while harvestable: the lives of the patch. */
	public int getHarvestStages()
	{
		return harvestStages;
	}

	/** What is planted: a seed, spore or sapling. */
	public int getSeedId()
	{
		return seedId;
	}

	/** Farming level needed to plant it. */
	public int getLevel()
	{
		return level;
	}

	public int[] getPayment()
	{
		return payment.clone();
	}

	public boolean isProtectable()
	{
		return payment.length > 0;
	}

	/** Minutes from planting to fully grown. */
	public int growMinutes()
	{
		return tickrate * Math.max(0, stages - 1);
	}

	/** Every crop that grows in a kind of patch, lowest level first. */
	public static java.util.List<Produce> forKind(PatchKind kind)
	{
		java.util.List<Produce> list = new java.util.ArrayList<>();
		for (Produce p : values())
		{
			if (p.kind == kind)
			{
				list.add(p);
			}
		}
		list.sort(java.util.Comparator.comparingInt(Produce::getLevel));
		return list;
	}
}
