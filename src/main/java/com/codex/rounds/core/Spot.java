package com.codex.rounds.core;

import com.codex.rounds.core.farming.PatchKind;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

/**
 * Every patch and bird house space a round looks after. A patch's varbit is only sent while you are
 * near it, in the regions listed (RuneLite's Time Tracking plugin, FarmingWorld); bird house spaces
 * use player variables. Tiles were read from the game cache, from the objects carrying each varbit.
 */
public enum Spot
{
	BIRDHOUSE_VALLEY_NE("Verdant Valley, north-east", Place.FOSSIL_ISLAND, Group.BIRDHOUSES, VarPlayerID.BIRDHOUSE_TRANSMIT_C, Point.of(3768, 3761)),
	BIRDHOUSE_VALLEY_SW("Verdant Valley, south-west", Place.FOSSIL_ISLAND, Group.BIRDHOUSES, VarPlayerID.BIRDHOUSE_TRANSMIT_D, Point.of(3763, 3755)),
	BIRDHOUSE_MEADOW_N("Mushroom Meadow, north", Place.FOSSIL_ISLAND, Group.BIRDHOUSES, VarPlayerID.BIRDHOUSE_TRANSMIT_A, Point.of(3677, 3882)),
	BIRDHOUSE_MEADOW_S("Mushroom Meadow, south", Place.FOSSIL_ISLAND, Group.BIRDHOUSES, VarPlayerID.BIRDHOUSE_TRANSMIT_B, Point.of(3679, 3815)),

	HARDWOOD_FOSSIL_E("Fossil Island, east", Place.FOSSIL_ISLAND, Group.HARDWOODS, PatchKind.HARDWOOD_TREE, VarbitID.FARMING_TRANSMIT_A,
		Point.of(3715, 3835), NpcID.FOSSIL_SQUIRREL_GARDENER1, Bounds.FOSSIL_ISLAND, 14651, 14907, 14908, 15164, 14652, 14906, 14650, 15162, 15163),
	HARDWOOD_FOSSIL_M("Fossil Island, middle", Place.FOSSIL_ISLAND, Group.HARDWOODS, PatchKind.HARDWOOD_TREE, VarbitID.FARMING_TRANSMIT_B,
		Point.of(3708, 3833), NpcID.FOSSIL_SQUIRREL_GARDENER2, Bounds.FOSSIL_ISLAND, 14651, 14907, 14908, 15164, 14652, 14906, 14650, 15162, 15163),
	HARDWOOD_FOSSIL_W("Fossil Island, west", Place.FOSSIL_ISLAND, Group.HARDWOODS, PatchKind.HARDWOOD_TREE, VarbitID.FARMING_TRANSMIT_C,
		Point.of(3702, 3837), NpcID.FOSSIL_SQUIRREL_GARDENER3, Bounds.FOSSIL_ISLAND, 14651, 14907, 14908, 15164, 14652, 14906, 14650, 15162, 15163),
	SEAWEED_N("Seabed, north", Place.UNDERWATER, Group.SEAWEED, PatchKind.SEAWEED, VarbitID.FARMING_TRANSMIT_A,
		Point.of(3734, 10274), NpcID.FOSSIL_GARDENER_UNDERWATER, Bounds.ANY, 15008),
	SEAWEED_S("Seabed, south", Place.UNDERWATER, Group.SEAWEED, PatchKind.SEAWEED, VarbitID.FARMING_TRANSMIT_B,
		Point.of(3734, 10268), NpcID.FOSSIL_GARDENER_UNDERWATER, Bounds.ANY, 15008),

	HARDWOOD_ANGLERS("Anglers' Retreat", Place.ANGLERS_RETREAT, Group.HARDWOODS, PatchKind.HARDWOOD_TREE, VarbitID.FARMING_TRANSMIT_A,
		Point.of(2471, 2703), NpcID.FARMING_GARDENER_HARDWOOD_TREE_5, Bounds.ANY, 9770),
	HARDWOOD_SAVANNAH("Avium Savannah", Place.AVIUM_SAVANNAH, Group.HARDWOODS, PatchKind.HARDWOOD_TREE, VarbitID.FARMING_TRANSMIT_A,
		Point.of(1687, 2972), NpcID.FROG_QUEST_MARCELLUS, Bounds.ANY, 6702, 6446),

	CALQUAT_TAI_BWO("Tai Bwo Wannai", Place.TAI_BWO_WANNAI, Group.TREES, PatchKind.CALQUAT, VarbitID.FARMING_TRANSMIT_A,
		Point.of(2796, 3101), NpcID.FARMING_GARDENER_CALQUAT, Bounds.ANY, 11056),
	CALQUAT_KASTORI("Kastori", Place.KASTORI, Group.TREES, PatchKind.CALQUAT, VarbitID.FARMING_TRANSMIT_A,
		Point.of(1367, 3032), NpcID.FARMING_GARDENER_CALQUAT_2, Bounds.ANY, 5423, 5167, 5424),
	CALQUAT_CONCH("The Great Conch", Place.GREAT_CONCH, Group.TREES, PatchKind.CALQUAT, VarbitID.FARMING_TRANSMIT_C,
		Point.of(3128, 2405), NpcID.FARMING_GARDENER_CALQUAT_3, Bounds.ANY,
		12581, 12325, 12326, 12327, 12580, 12582, 12583, 12836, 12837, 12838, 12839, 13092, 13093, 13194),
	CELASTRUS_GUILD("Farming Guild", Place.FARMING_GUILD, Group.TREES, PatchKind.CELASTRUS, VarbitID.FARMING_TRANSMIT_L,
		Point.of(1244, 3750), NpcID.FARMING_GARDENER_FARMGUILD_CELASTRUS, Bounds.ANY, -1),
	REDWOOD_GUILD("Farming Guild", Place.FARMING_GUILD, Group.TREES, PatchKind.REDWOOD, VarbitID.FARMING_TRANSMIT_I,
		Point.of(1228, 3754), NpcID.FARMING_GARDENER_FARMGUILD_REDWOOD, Bounds.ANY, -1),
	CRYSTAL_PRIF("Prifddinas", Place.PRIFDDINAS, Group.TREES, PatchKind.CRYSTAL_TREE, VarbitID.FARMING_TRANSMIT_E,
		Point.of(3291, 6118), -1, Bounds.ANY, 13151, 12895, 12894, 13150, 12994, 12993, 12737, 12738, 12126, 12127, 13250),

	SPIRIT_PORT_SARIM("Port Sarim", Place.PORT_SARIM, Group.SPIRIT_TREES, PatchKind.SPIRIT_TREE, VarbitID.FARMING_TRANSMIT_A,
		Point.of(3060, 3258), NpcID.FARMING_GARDENER_SPIRIT_TREE_1, Bounds.PORT_SARIM, 12082, 12083),
	SPIRIT_ETCETERIA("Etceteria", Place.ETCETERIA, Group.SPIRIT_TREES, PatchKind.SPIRIT_TREE, VarbitID.FARMING_TRANSMIT_B,
		Point.of(2613, 3858), NpcID.FARMING_GARDENER_SPIRIT_TREE_2, Bounds.ANY, 10300),
	SPIRIT_BRIMHAVEN("Brimhaven", Place.BRIMHAVEN, Group.SPIRIT_TREES, PatchKind.SPIRIT_TREE, VarbitID.FARMING_TRANSMIT_B,
		Point.of(2802, 3203), NpcID.FARMING_GARDENER_SPIRIT_TREE_3, Bounds.ANY, 11058, 11057),
	SPIRIT_HOSIDIUS("Hosidius", Place.HOSIDIUS, Group.SPIRIT_TREES, PatchKind.SPIRIT_TREE, VarbitID.FARMING_TRANSMIT_F,
		Point.of(1693, 3542), NpcID.FARMING_GARDENER_SPIRIT_TREE_4, Bounds.ANY, 6967, 6711),
	SPIRIT_GUILD("Farming Guild", Place.FARMING_GUILD, Group.SPIRIT_TREES, PatchKind.SPIRIT_TREE, VarbitID.FARMING_TRANSMIT_A,
		Point.of(1253, 3750), NpcID.FARMING_GARDENER_SPIRIT_TREE_5, Bounds.ANY, -1),

	CACTUS_AL_KHARID("Al Kharid", Place.AL_KHARID, Group.PATCHES, PatchKind.CACTUS, VarbitID.FARMING_TRANSMIT_A,
		Point.of(3315, 3202), NpcID.FARMING_GARDENER_CACTUS, Bounds.ANY, 13106, 13362, 13105),
	CACTUS_GUILD("Farming Guild", Place.FARMING_GUILD, Group.PATCHES, PatchKind.CACTUS, VarbitID.FARMING_TRANSMIT_F,
		Point.of(1264, 3747), NpcID.FARMING_GARDENER_FARMGUILD_T1, Bounds.ANY, -1),
	BUSH_CHAMPIONS("Champions' Guild", Place.CHAMPIONS_GUILD, Group.PATCHES, PatchKind.BUSH, VarbitID.FARMING_TRANSMIT_A,
		Point.of(3181, 3357), NpcID.FARMING_GARDENER_BUSH_1, Bounds.ANY, 12596),
	BUSH_RIMMINGTON("Rimmington", Place.RIMMINGTON, Group.PATCHES, PatchKind.BUSH, VarbitID.FARMING_TRANSMIT_A,
		Point.of(2940, 3221), NpcID.FARMING_GARDENER_BUSH_2, Bounds.ANY, 11570, 11826),
	BUSH_ARDOUGNE("Ardougne Monastery", Place.ARDOUGNE_MONASTERY, Group.PATCHES, PatchKind.BUSH, VarbitID.FARMING_TRANSMIT_A,
		Point.of(2617, 3225), NpcID.FARMING_GARDENER_BUSH_4, Bounds.ANY, 10290, 10546),
	BUSH_ETCETERIA("Etceteria", Place.ETCETERIA, Group.PATCHES, PatchKind.BUSH, VarbitID.FARMING_TRANSMIT_A,
		Point.of(2591, 3863), NpcID.FARMING_GARDENER_BUSH_3, Bounds.ANY, 10300),
	BUSH_GUILD("Farming Guild", Place.FARMING_GUILD, Group.PATCHES, PatchKind.BUSH, VarbitID.FARMING_TRANSMIT_B,
		Point.of(1260, 3733), NpcID.FARMING_GARDENER_FARMGUILD_T1, Bounds.ANY, -1),
	MUSHROOM_CANIFIS("Canifis", Place.CANIFIS, Group.PATCHES, PatchKind.MUSHROOM, VarbitID.FARMING_TRANSMIT_A,
		Point.of(3451, 3472), -1, Bounds.ANY, 13622, 13878),
	BELLADONNA_DRAYNOR("Draynor Manor", Place.DRAYNOR_MANOR, Group.PATCHES, PatchKind.BELLADONNA, VarbitID.FARMING_TRANSMIT_A,
		Point.of(3086, 3354), -1, Bounds.ANY, 12340),
	BELLADONNA_AUBURNVALE("Auburnvale", Place.AUBURNVALE, Group.PATCHES, PatchKind.BELLADONNA, VarbitID.FARMING_TRANSMIT_B,
		Point.of(1450, 3354), -1, Bounds.ANY, 5427, 5428, 5684),
	ANIMA_GUILD("Farming Guild", Place.FARMING_GUILD, Group.PATCHES, PatchKind.ANIMA, VarbitID.FARMING_TRANSMIT_M,
		Point.of(1232, 3723), -1, Bounds.ANY, -1),
	HESPORI_GUILD("Farming Guild", Place.HESPORI, Group.PATCHES, PatchKind.HESPORI, VarbitID.FARMING_TRANSMIT_J,
		Point.of(1247, 10087), -1, Bounds.ANY, 5021);


	/** How the board groups spots. */
	public enum Group
	{
		BIRDHOUSES("Bird houses"),
		SEAWEED("Giant seaweed"),
		HARDWOODS("Hardwood trees"),
		TREES("Special trees"),
		SPIRIT_TREES("Spirit trees"),
		PATCHES("Special patches");

		private final String label;

		Group(String label)
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}
	}

	/** Extra conditions on where a patch's varbit can be trusted, from RuneLite's FarmingWorld. */
	enum Bounds
	{
		ANY,
		GROUND,
		FOSSIL_ISLAND,
		PORT_SARIM;

		boolean contains(Point loc)
		{
			switch (this)
			{
				case FOSSIL_ISLAND:
					// Hardwood varbits are sent anywhere on the island's ground floor, but a tick early
					// when climbing some ladders and stairs.
					if (loc.getX() == 3753 && loc.getY() >= 3868 && loc.getY() <= 3870)
					{
						return false;
					}
					if ((loc.getX() == 3729 || loc.getX() == 3728 || loc.getX() == 3747 || loc.getX() == 3746)
						&& loc.getY() <= 3832 && loc.getY() >= 3830)
					{
						return false;
					}
					return loc.getPlane() == 0;
				case GROUND:
					return loc.getPlane() == 0;
				case PORT_SARIM:
					return loc.getY() < 3272;
				default:
					return true;
			}
		}
	}

	private final String label;
	private final Place place;
	private final Group group;
	private final PatchKind kind;
	private final int varbit;
	private final int varp;
	private final Point point;
	private final int gardener;
	private final Bounds bounds;
	private final int[] regions;

	Spot(String label, Place place, Group group, int varp, Point point)
	{
		this(label, place, group, null, -1, varp, point, -1, Bounds.GROUND, new int[]{14650, 14651, 14652, 14906, 14907, 15162, 15163});
	}

	Spot(String label, Place place, Group group, PatchKind kind, int varbit, Point point, int gardener, Bounds bounds, int... regions)
	{
		this(label, place, group, kind, varbit, -1, point, gardener, bounds, regions);
	}

	Spot(String label, Place place, Group group, PatchKind kind, int varbit, int varp, Point point, int gardener, Bounds bounds, int[] regions)
	{
		this.label = label;
		this.place = place;
		this.group = group;
		this.kind = kind;
		this.varbit = varbit;
		this.varp = varp;
		this.point = point;
		this.gardener = gardener;
		this.bounds = bounds;
		this.regions = regions.length == 1 && regions[0] == -1 ? new int[]{4922, 5177, 5178, 5179, 4921, 4923, 4665, 4666, 4667} : regions;
	}

	public String getLabel()
	{
		return label;
	}

	/** "Calquat, Kastori": what the spot is and where. */
	public String getTitle()
	{
		return kindName() + ", " + label;
	}

	public String kindName()
	{
		if (isBirdhouse())
		{
			return "Bird house";
		}
		switch (kind)
		{
			case HARDWOOD_TREE:
				return "Hardwood";
			case SEAWEED:
				return "Seaweed";
			case CALQUAT:
				return "Calquat";
			case CELASTRUS:
				return "Celastrus";
			case REDWOOD:
				return "Redwood";
			case CRYSTAL_TREE:
				return "Crystal tree";
			case SPIRIT_TREE:
				return "Spirit tree";
			case CACTUS:
				return "Cactus";
			case BUSH:
				return "Bush";
			case MUSHROOM:
				return "Mushroom";
			case BELLADONNA:
				return "Belladonna";
			case ANIMA:
				return "Anima";
			case HESPORI:
				return "Hespori";
			default:
				return kind.name();
		}
	}

	public Place getPlace()
	{
		return place;
	}

	public Group getGroup()
	{
		return group;
	}

	/** The patch kind, or null for a bird house space. */
	public PatchKind getKind()
	{
		return kind;
	}

	public boolean isBirdhouse()
	{
		return kind == null;
	}

	public int getVarbit()
	{
		return varbit;
	}

	public int getVarp()
	{
		return varp;
	}

	public Point getPoint()
	{
		return point;
	}

	/** The gardener who protects this patch, or -1. */
	public int getGardener()
	{
		return gardener;
	}

	/** Whether this spot's variable can be trusted from where the player stands. */
	public boolean readableFrom(Point loc)
	{
		if (loc == null)
		{
			return false;
		}
		int region = loc.getRegionId();
		for (int r : regions)
		{
			if (r == region)
			{
				return bounds.contains(loc);
			}
		}
		return false;
	}

	/** Where RuneLite's Time Tracking plugin keeps this spot, in its "timetracking" profile config. */
	public String timeTrackingKey()
	{
		return isBirdhouse() ? "birdhouse." + varp : regions[0] + "." + varbit;
	}

	/** A stable key for saved data. */
	public String key()
	{
		return name().toLowerCase();
	}

	public static List<Spot> inGroup(Group group)
	{
		List<Spot> list = new ArrayList<>();
		for (Spot s : values())
		{
			if (s.group == group)
			{
				list.add(s);
			}
		}
		return Collections.unmodifiableList(list);
	}
}
