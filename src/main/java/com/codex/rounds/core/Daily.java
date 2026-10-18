package com.codex.rounds.core;

import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

/**
 * The once-a-day buys and claims a round can pick up. Unlock and claimed checks follow RuneLite's
 * Daily Task Indicator plugin; amounts follow the diary reward tables.
 */
public enum Daily
{
	ZAFF("Zaff's battlestaves", Place.VARROCK, ItemID.BATTLESTAFF, "Buy battlestaves from Zaff", NpcID.ZAFF),
	CROMPERTY("Cromperty's essence", Place.EAST_ARDOUGNE, ItemID.BLANKRUNE_HIGH, "Claim noted essence from Wizard Cromperty",
		NpcID.CROMPERTY_POST_DIARY, NpcID.CROMPERTY_PRE_DIARY),
	FLAX("Flax to bowstrings", Place.SEERS_VILLAGE, ItemID.BOW_STRING, "Hand your flax to the Flax keeper", NpcID.DIARY_FLAX_KEEPER),
	RANTZ("Rantz's ogre arrows", Place.FELDIP_HILLS, ItemID.OGRE_ARROW, "Claim ogre arrows from Rantz",
		NpcID.RANTZ, NpcID._100_RANTZ, NpcID._100_RANTZ1, NpcID._100_RANTZ2),
	ROBIN("Robin's bonemeal and slime", Place.PORT_PHASMATYS, ItemID.POT_BONEMEAL, "Hand your bones to Robin", NpcID.AHOY_ROBIN),
	THIRUS("Thirus's dynamite", Place.LOVAKENGJ, ItemID.LOVAKENGJ_DYNAMITE_FUSED, "Claim dynamite from Thirus",
		NpcID.LOVAKENGJ_THIRUS_POST_DIARY, NpcID.LOVAKENGJ_THIRUS, NpcID.LOVAKENGJ_THIRUS_PRE_DIARY),
	BERT("Bert's sand", Place.YANILLE, ItemID.BUCKET_SAND, "Ask Bert for your sand", NpcID.HANDSAND_BERT_2OPS, NpcID.HANDSAND_BERT_1OP, NpcID.HANDSAND_BERT),
	HERB_BOXES("Herb boxes", Place.NIGHTMARE_ZONE, ItemID.NZONE_HERBBOX, "Buy herb boxes from Dominic Onion", NpcID.NZONE_HOST),
	LUNDAIL("Lundail's runes", Place.MAGE_ARENA, ItemID.DEATHRUNE, "Claim runes from Lundail", NpcID.MAGEARENA_RUNESHOP);

	public static final int ZAFF_PRICE = 7_000;
	public static final int HERB_BOX_POINTS = 9_500;
	public static final int HERB_BOX_MAX = 15;
	public static final int SAND_QUEST_COMPLETE = 160;
	public static final int ROBIN_PER_TIER = 13;
	public static final int BERT_SAND = 84;

	private final String label;
	private final Place place;
	private final int itemId;
	private final String action;
	private final int[] npcs;

	Daily(String label, Place place, int itemId, String action, int... npcs)
	{
		this.label = label;
		this.place = place;
		this.itemId = itemId;
		this.action = action;
		this.npcs = npcs;
	}

	public String getLabel()
	{
		return label;
	}

	public Place getPlace()
	{
		return place;
	}

	/** The item shown on the board for this daily. */
	public int getItemId()
	{
		return itemId;
	}

	/** What to do at the stop, as a step title. */
	public String getAction()
	{
		return action;
	}

	public int[] getNpcs()
	{
		return npcs.clone();
	}

	public boolean isNpc(int id)
	{
		for (int n : npcs)
		{
			if (n == id)
			{
				return true;
			}
		}
		return false;
	}

	/** Whether the account has earned this daily at all. */
	public boolean unlocked(Vars v)
	{
		switch (this)
		{
			case ZAFF:
				return done(v, VarbitID.VARROCK_DIARY_EASY_COMPLETE);
			case CROMPERTY:
				return done(v, VarbitID.ARDOUGNE_DIARY_MEDIUM_COMPLETE);
			case FLAX:
				return done(v, VarbitID.KANDARIN_DIARY_EASY_COMPLETE);
			case RANTZ:
				return done(v, VarbitID.WESTERN_DIARY_EASY_COMPLETE);
			case ROBIN:
				return done(v, VarbitID.MORYTANIA_DIARY_MEDIUM_COMPLETE);
			case THIRUS:
				return done(v, VarbitID.KOUREND_DIARY_MEDIUM_COMPLETE);
			case BERT:
				return v.varbit(VarbitID.IRONMAN) != 2 && v.varbit(VarbitID.HANDSAND_QUEST) >= SAND_QUEST_COMPLETE;
			case HERB_BOXES:
				return v.varbit(VarbitID.IRONMAN) == 0 && v.varp(VarPlayerID.NZONE_REWARDPOINTS) >= HERB_BOX_POINTS;
			case LUNDAIL:
				return done(v, VarbitID.WILDERNESS_DIARY_EASY_COMPLETE);
			default:
				return false;
		}
	}

	/** Whether today's claim is still waiting. */
	public boolean claimable(Vars v)
	{
		if (!unlocked(v))
		{
			return false;
		}
		switch (this)
		{
			case ZAFF:
				return v.varbit(VarbitID.ZAFF_LAST_CLAIMED) == 0;
			case CROMPERTY:
				return v.varbit(VarbitID.ARDOUGNE_FREE_ESSENCE) == 0;
			case FLAX:
				return v.varbit(VarbitID.SEERS_FREE_FLAX) == 0;
			case RANTZ:
				return v.varbit(VarbitID.WESTERN_RANTZ_ARROWS) == 0;
			case ROBIN:
				return v.varbit(VarbitID.MORYTANIA_SLIME_CLAIMED) < amount(v);
			case THIRUS:
				return v.varbit(VarbitID.KOUREND_FREE_DYNAMITE) == 0;
			case BERT:
				return v.varbit(VarbitID.YANILLE_SAND_CLAIMED) == 0;
			case HERB_BOXES:
				return v.varbit(VarbitID.NZONE_HERBBOXES_PURCHASED) < HERB_BOX_MAX;
			case LUNDAIL:
				return v.varbit(VarbitID.LUNDAIL_LAST_CLAIMED) == 0;
			default:
				return false;
		}
	}

	/** How many come today, or 0 when the amount is random. */
	public int amount(Vars v)
	{
		int t;
		switch (this)
		{
			case ZAFF:
				return pick(tier(v, VarbitID.VARROCK_DIARY_EASY_COMPLETE), 15, 30, 60, 120);
			case CROMPERTY:
				t = tier(v, VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE);
				return pick(t, 0, 100, 150, 250);
			case FLAX:
				return pick(tier(v, VarbitID.KANDARIN_DIARY_EASY_COMPLETE), 30, 60, 120, 250);
			case RANTZ:
				return pick(tier(v, VarbitID.WESTERN_DIARY_EASY_COMPLETE), 25, 50, 100, 150);
			case ROBIN:
				t = tier(v, VarbitID.MORYTANIA_DIARY_EASY_COMPLETE);
				return Math.max(0, t - 1) * ROBIN_PER_TIER;
			case THIRUS:
				return pick(tier(v, VarbitID.KOUREND_DIARY_EASY_COMPLETE), 0, 20, 40, 80);
			case BERT:
				return BERT_SAND;
			case HERB_BOXES:
				int left = HERB_BOX_MAX - v.varbit(VarbitID.NZONE_HERBBOXES_PURCHASED);
				return Math.max(0, Math.min(left, v.varp(VarPlayerID.NZONE_REWARDPOINTS) / HERB_BOX_POINTS));
			default:
				return 0;
		}
	}

	/** Robin hands out a share at a time; how many are left today. */
	public int remaining(Vars v)
	{
		if (this == ROBIN)
		{
			return Math.max(0, amount(v) - v.varbit(VarbitID.MORYTANIA_SLIME_CLAIMED));
		}
		return amount(v);
	}

	/** What the board says comes back, such as "120 battlestaves". */
	public String reward(Vars v)
	{
		int n = remaining(v);
		switch (this)
		{
			case ZAFF:
				return n + " battlestaves for " + Format.coins((long) n * ZAFF_PRICE);
			case CROMPERTY:
				return n + " noted pure essence";
			case FLAX:
				return n + " flax spun into bowstrings";
			case RANTZ:
				return n + " ogre arrows";
			case ROBIN:
				return n + " bonemeal and buckets of slime";
			case THIRUS:
				return n + " dynamite";
			case BERT:
				return n + " buckets of sand";
			case HERB_BOXES:
				return n + " herb boxes for " + Format.number((long) n * HERB_BOX_POINTS) + " points";
			case LUNDAIL:
				return "A random handful of runes";
			default:
				return "";
		}
	}

	/** What the player needs to bring for it, or null. */
	public Need need(Vars v)
	{
		int n = remaining(v);
		switch (this)
		{
			case ZAFF:
				return new Need(Need.Kind.COINS, "Coins", n * ZAFF_PRICE, "Zaff's battlestaves", false, ItemID.COINS);
			case FLAX:
				return new Need(Need.Kind.DAILY, "Flax (noted is fine)", n, "the Flax keeper", false, ItemID.FLAX, ItemID.FLAX + 1);
			case ROBIN:
				return new Need(Need.Kind.DAILY, "Bones (noted is fine)", n, "Robin", false, ItemID.BONES, ItemID.BONES + 1);
			default:
				return null;
		}
	}

	/** Diary tiers done from easy upwards: easy is 1, elite 4. Each tier's varbit follows the last. */
	static int tier(Vars v, int easyVarbit)
	{
		int t = 0;
		for (int i = 0; i < 4; i++)
		{
			if (v.varbit(easyVarbit + i) == 1)
			{
				t = i + 1;
			}
		}
		return t;
	}

	private static int pick(int tier, int... byTier)
	{
		return tier <= 0 ? 0 : byTier[Math.min(tier, byTier.length) - 1];
	}

	private static boolean done(Vars v, int varbit)
	{
		return v.varbit(varbit) == 1;
	}
}
