package com.codex.rounds.core;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;

/**
 * The tools and compost stored with the tool leprechaun. The game sends these varbits everywhere, so the store is
 * known without a visit. Large counts spill into an "extra" varbit worth 2 (tools) or 256 (compost) each.
 */
public final class Leprechaun
{
	/** Stops where a tool leprechaun stands by the patches, so stored tools can be taken out there. */
	public static final Set<Place> PLACES = EnumSet.of(Place.FARMING_GUILD, Place.FOSSIL_ISLAND, Place.UNDERWATER,
		Place.TAI_BWO_WANNAI, Place.PRIFDDINAS, Place.HOSIDIUS, Place.ETCETERIA, Place.BRIMHAVEN, Place.PORT_SARIM,
		Place.RIMMINGTON, Place.ARDOUGNE_MONASTERY, Place.CHAMPIONS_GUILD, Place.AL_KHARID, Place.DRAYNOR_MANOR,
		Place.CANIFIS, Place.KASTORI, Place.AUBURNVALE);

	public static final int[] NPCS = {NpcID.FARMING_TOOLS_LEPRECHAUN, NpcID.FOSSIL_LEPRECHAUN_UNDERWATER,
		NpcID.FARMING_TOOLS_LEPRECHAUN_VARLAMORE, NpcID.FARMING_TOOLS_LEPRECHAUN_DRAYNOR};

	private static final int NONE = -1;

	/** Item, base varbit, extra varbit, shift of the extra varbit. */
	private static final int[][] SLOTS = {
		{ItemID.RAKE, VarbitID.FARMING_TOOLS_RAKE, VarbitID.FARMING_TOOLS_EXTRARAKES, 1},
		{ItemID.DIBBER, VarbitID.FARMING_TOOLS_DIBBER, VarbitID.FARMING_TOOLS_EXTRADIBBERS, 1},
		{ItemID.SPADE, VarbitID.FARMING_TOOLS_SPADE, VarbitID.FARMING_TOOLS_EXTRASPADES, 1},
		{ItemID.SECATEURS, VarbitID.FARMING_TOOLS_SECATEURS, VarbitID.FARMING_TOOLS_EXTRASECATEURS, 1},
		{ItemID.BUCKET_COMPOST, VarbitID.FARMING_TOOLS_COMPOST, VarbitID.FARMING_TOOLS_EXTRACOMPOST, 8},
		{ItemID.BUCKET_SUPERCOMPOST, VarbitID.FARMING_TOOLS_SUPERCOMPOST, VarbitID.FARMING_TOOLS_EXTRASUPERCOMPOST, 8},
		{ItemID.BUCKET_ULTRACOMPOST, VarbitID.FARMING_TOOLS_ULTRACOMPOST, NONE, 0},
		{ItemID.PLANT_CURE, VarbitID.FARMING_TOOLS_PLANTCURE, NONE, 0},
	};

	private Leprechaun()
	{
	}

	public static Map<Integer, Integer> read(Vars vars)
	{
		Map<Integer, Integer> out = new HashMap<>();
		for (int[] s : SLOTS)
		{
			int n = vars.varbit(s[1]) + (s[2] == NONE ? 0 : vars.varbit(s[2]) << s[3]);
			if (n > 0)
			{
				out.put(s[0], n);
			}
		}
		// The secateurs slot holds magic secateurs when this flag is set.
		Integer secateurs = out.get(ItemID.SECATEURS);
		if (secateurs != null && vars.varbit(VarbitID.FARMING_TOOLS_FAIRYSECATEURS) > 0)
		{
			out.remove(ItemID.SECATEURS);
			out.put(ItemID.FAIRY_ENCHANTED_SECATEURS, secateurs);
		}
		return out;
	}

	/** Whether this need can come out of the leprechaun's store: farming tools, compost and cures. */
	public static boolean holds(Need n)
	{
		for (int id : n.getItems())
		{
			if (id == ItemID.FAIRY_ENCHANTED_SECATEURS)
			{
				return true;
			}
			for (int[] s : SLOTS)
			{
				if (s[0] == id)
				{
					return true;
				}
			}
		}
		return false;
	}
}
