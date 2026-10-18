package com.codex.rounds.core;

import static com.codex.rounds.core.Leg.directions;
import static com.codex.rounds.core.Leg.item;
import static com.codex.rounds.core.Leg.ring;
import static com.codex.rounds.core.Leg.spell;
import static com.codex.rounds.core.Teleports.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The stops a round can make. Each has the spot to head for, how far around it counts as
 * arrived, and the ways of getting there best first. A place reached from another (the seabed
 * from Fossil Island, Hespori's cave from the Farming Guild) names it and the last step between.
 */
public enum Place
{
	FOSSIL_ISLAND("Fossil Island", "Birdhouses and hardwoods", Point.of(3760, 3868), 140,
		item("Digsite pendant: Fossil Island", 3763, 3869, DIGSITE_PENDANT).needs(Unlock.BONE_VOYAGE),
		directions("Barge from the Digsite's east side", 3362, 3445, 150)),
	UNDERWATER("Fossil Island seabed", "Giant seaweed", Point.of(3731, 10278), 30,
		FOSSIL_ISLAND, "Dive from the rowboat on the north coast", Point.of(3761, 3898), 30919),
	FARMING_GUILD("Farming Guild", "Kebos Lowlands", Point.of(1248, 3735), 45,
		item("Farming cape", 1248, 3725, FARMING_CAPE),
		item("Max cape: Farming Guild", 1248, 3725, MAX_CAPE),
		item("Skills necklace: Farming Guild", 1248, 3719, SKILLS_NECKLACE),
		ring("CIR", 1302, 3762),
		directions("Minecart or walk to the Farming Guild", 1219, 3738, 120)),
	HESPORI("Hespori's cave", "Beneath the Farming Guild", Point.of(1246, 10086), 20,
		FARMING_GUILD, "Enter the cave in the guild's top tier", Point.of(1231, 3728), 34499),
	TAI_BWO_WANNAI("Tai Bwo Wannai", "Karamja", Point.of(2795, 3100), 30,
		item("Tai Bwo Wannai teleport scroll", 2789, 3066, TAI_BWO_WANNAI_SCROLL),
		ring("CKR", 2801, 3003),
		directions("Walk south from Brimhaven", 2795, 3100, 150)),
	KASTORI("Kastori", "Varlamore", Point.of(1366, 3031), 30,
		item("Quetzal whistle: Kastori", 1344, 3022, QUETZAL_WHISTLE).needs(Unlock.QUETZAL_KASTORI),
		item("Pendant of ates: Kastori", 1366, 3086, PENDANT_OF_ATES).needs(Unlock.ATES_KASTORI),
		ring("CKQ", 1359, 2941).needs(Unlock.CHILDREN_OF_THE_SUN),
		directions("Quetzal or walk south from Civitas to Kastori", 1344, 3022, 150)),
	GREAT_CONCH("The Great Conch", "Calquat patch", Point.of(3127, 2404), 40,
		ring("CJQ", 3178, 2447).needs(Unlock.GREAT_CONCH),
		directions("Sail to the Great Conch", 3127, 2404, 180)),
	ANGLERS_RETREAT("Anglers' Retreat", "Hardwood patch", Point.of(2470, 2702), 40,
		directions("Sail to Anglers' Retreat", 2470, 2702, 180)),
	AVIUM_SAVANNAH("Avium Savannah", "Hardwood patch", Point.of(1686, 2971), 30,
		ring("AJP", 1651, 3010).needs(Unlock.CHILDREN_OF_THE_SUN),
		item("Quetzal whistle: Colossal Wyrm Remains", 1670, 2933, QUETZAL_WHISTLE).needs(Unlock.QUETZAL_WYRM),
		directions("Quetzal to Outer Fortis and walk south", 1700, 3037, 120)),
	PORT_SARIM("Port Sarim", "Spirit tree patch", Point.of(3059, 3257), 25,
		item("Explorer's ring: Cabbage patch", 3052, 3291, EXPLORERS_RING),
		item("Amulet of glory: Draynor Village", 3105, 3251, GLORY),
		directions("Walk south from Falador", 3059, 3257, 150)),
	ETCETERIA("Etceteria", "Bush and spirit tree patches", Point.of(2600, 3860), 30,
		item("Ring of wealth: Miscellania", 2534, 3862, RING_OF_WEALTH).needs(Unlock.THRONE_OF_MISCELLANIA),
		ring("CIP", 2513, 3884).needs(Unlock.FREMENNIK_TRIALS),
		directions("Boat from Rellekka to Miscellania", 2581, 3845, 150)),
	MISCELLANIA("Miscellania", "Advisor Ghrim, castle first floor", Point.of(2499, 3857, 1), 25,
		item("Ring of wealth: Miscellania", 2534, 3862, RING_OF_WEALTH).needs(Unlock.THRONE_OF_MISCELLANIA),
		ring("CIP", 2513, 3884).needs(Unlock.FREMENNIK_TRIALS),
		directions("Boat from Rellekka to Miscellania", 2581, 3845, 150)),
	BRIMHAVEN("Brimhaven", "Spirit tree patch", Point.of(2801, 3202), 25,
		item("Brimhaven tablet", 2757, 3179, BRIMHAVEN_TABLET),
		item("Amulet of glory: Karamja", 2918, 3176, GLORY),
		directions("Boat from Ardougne to Brimhaven", 2772, 3234, 150)),
	HOSIDIUS("Hosidius", "Spirit tree patch", Point.of(1692, 3541), 25,
		item("Skills necklace: Woodcutting Guild", 1662, 3505, SKILLS_NECKLACE),
		item("Hosidius tablet", 1743, 3517, HOSIDIUS_TABLET),
		item("Xeric's talisman: Xeric's Glade", 1752, 3566, XERICS_TALISMAN),
		directions("Walk west from Hosidius", 1692, 3541, 150)),
	PRIFDDINAS("Prifddinas", "Crystal tree patch", Point.of(3291, 6118), 30,
		item("Teleport crystal: Prifddinas", 3264, 6066, TELEPORT_CRYSTAL).needs(Unlock.SONG_OF_THE_ELVES),
		item("Prifddinas tablet", 3239, 6077, PRIFDDINAS_TABLET).needs(Unlock.SONG_OF_THE_ELVES),
		item("Construction cape: Prifddinas", 3239, 6076, CONSTRUCTION_CAPE).needs(Unlock.SONG_OF_THE_ELVES),
		directions("Enter Prifddinas from the Elven lands", 3291, 6118, 180)),
	AL_KHARID("Al Kharid", "Cactus patch", Point.of(3315, 3202), 25,
		item("Ring of dueling: Emir's Arena", 3315, 3235, RING_OF_DUELING),
		item("Amulet of glory: Al Kharid", 3293, 3163, GLORY),
		directions("Walk east from Lumbridge", 3315, 3202, 150)),
	CHAMPIONS_GUILD("Champions' Guild", "Bush patch", Point.of(3181, 3357), 25,
		item("Combat bracelet: Champions' Guild", 3192, 3368, COMBAT_BRACELET),
		item("Varrock teleport tablet", 3213, 3424, VARROCK_TABLET),
		spell("Varrock Teleport", 3213, 3424, 25, Leg.STANDARD),
		directions("Walk south from Varrock", 3181, 3357, 120)),
	RIMMINGTON("Rimmington", "Bush patch", Point.of(2940, 3221), 25,
		item("Rimmington tablet", 2953, 3224, RIMMINGTON_TABLET),
		item("Construction cape: Rimmington", 2954, 3224, CONSTRUCTION_CAPE),
		item("Max cape: Rimmington portal", 2954, 3224, MAX_CAPE),
		directions("Walk west from Port Sarim", 2940, 3221, 120)),
	ARDOUGNE_MONASTERY("Ardougne Monastery", "Bush patch", Point.of(2617, 3225), 25,
		item("Ardougne cloak: Monastery", 2607, 3221, ARDOUGNE_CLOAK),
		ring("DJP", 2658, 3230),
		ring("AIR", 2700, 3247),
		item("Ardougne teleport tablet", 2662, 3305, ARDOUGNE_TABLET).needs(Unlock.PLAGUE_CITY),
		directions("Walk south from East Ardougne", 2617, 3225, 120)),
	CANIFIS("Canifis", "Mushroom patch", Point.of(3451, 3472), 25,
		ring("CKS", 3447, 3470),
		item("Kharyrll tablet", 3494, 3473, KHARYRLL_TABLET),
		spell("Kharyrll Teleport", 3494, 3473, 66, Leg.ANCIENT),
		directions("Walk west from Canifis", 3451, 3472, 120)),
	DRAYNOR_MANOR("Draynor Manor", "Belladonna patch", Point.of(3086, 3354), 25,
		item("Draynor Manor tablet", 3109, 3351, DRAYNOR_MANOR_TABLET),
		spell("Draynor Manor Teleport", 3109, 3351, 17, Leg.ARCEUUS),
		item("Amulet of glory: Draynor Village", 3105, 3251, GLORY),
		directions("Walk north from Draynor Village", 3086, 3354, 120)),
	AUBURNVALE("Auburnvale", "Belladonna patch", Point.of(1450, 3354), 25,
		ring("AIS", 1429, 3324),
		item("Quetzal whistle: Auburnvale", 1411, 3361, QUETZAL_WHISTLE),
		directions("Quetzal to Auburnvale", 1411, 3361, 120)),
	VARROCK("Varrock", "Zaff's staff shop", Point.of(3202, 3434), 20,
		item("Varrock teleport tablet", 3213, 3424, VARROCK_TABLET),
		spell("Varrock Teleport", 3213, 3424, 25, Leg.STANDARD),
		item("Ring of wealth: Grand Exchange", 3163, 3478, RING_OF_WEALTH),
		directions("Walk to Varrock square", 3202, 3434, 150)),
	EAST_ARDOUGNE("East Ardougne", "Wizard Cromperty's house", Point.of(2684, 3323), 20,
		item("Ardougne teleport tablet", 2662, 3305, ARDOUGNE_TABLET).needs(Unlock.PLAGUE_CITY),
		spell("Ardougne Teleport", 2662, 3305, 51, Leg.STANDARD).needs(Unlock.PLAGUE_CITY),
		ring("BLR", 2740, 3351),
		directions("Walk to East Ardougne", 2684, 3323, 150)),
	SEERS_VILLAGE("Seers' Village", "The flax field", Point.of(2744, 3444), 20,
		item("Kandarin headgear: Sherlock", 2730, 3420, KANDARIN_HEADGEAR),
		item("Camelot teleport tablet", 2757, 3479, CAMELOT_TABLET),
		spell("Camelot Teleport", 2757, 3479, 45, Leg.STANDARD),
		directions("Walk to the Seers' Village flax field", 2744, 3444, 150)),
	FELDIP_HILLS("Feldip Hills", "Rantz's cave", Point.of(2631, 2982), 20,
		ring("AKS", 2571, 2956),
		item("Feldip hills teleport scroll", 2541, 2925, FELDIP_HILLS_SCROLL),
		item("Hunter cape", 2556, 2917, HUNTER_CAPE),
		directions("Walk south from Yanille", 2631, 2982, 180)),
	PORT_PHASMATYS("Port Phasmatys", "Robin, in the bar", Point.of(3675, 3495), 20,
		item("Ectophial", 3660, 3522, ECTOPHIAL),
		ring("ALQ", 3597, 3495),
		directions("Walk east from Canifis", 3675, 3495, 150)),
	LOVAKENGJ("Lovakengj", "Thirus, by the blast mine", Point.of(1489, 3862), 35,
		item("Xeric's talisman: Xeric's Inferno", 1504, 3815, XERICS_TALISMAN),
		ring("CIS", 1639, 3868),
		directions("Minecart to Lovakengj", 1518, 3733, 150)),
	YANILLE("Yanille", "Bert, in his house by the west gate", Point.of(2550, 3100), 20,
		item("Yanille tablet", 2544, 3096, YANILLE_TABLET),
		ring("CIQ", 2528, 3127),
		directions("Minigame teleport: Nightmare Zone", 2609, 3114, 60)),
	NIGHTMARE_ZONE("Nightmare Zone", "Dominic Onion, north of Yanille", Point.of(2609, 3115), 20,
		item("Yanille tablet", 2544, 3096, YANILLE_TABLET),
		ring("CIQ", 2528, 3127),
		directions("Minigame teleport: Nightmare Zone", 2609, 3114, 30)),
	MAGE_ARENA("Mage Arena bank", "Lundail, deep in the Wilderness", Point.of(2535, 4714), 25,
		directions("Mage Arena lever, then the sparkling pool", 2535, 4714, 240));

	private final String label;
	private final String detail;
	private final Point anchor;
	private final int radius;
	private final List<Leg> legs;
	private final Place via;
	private final String localStep;
	private final Point localPoint;
	private final int localObject;

	Place(String label, String detail, Point anchor, int radius, Leg... legs)
	{
		this.label = label;
		this.detail = detail;
		this.anchor = anchor;
		this.radius = radius;
		this.legs = Collections.unmodifiableList(Arrays.asList(legs));
		this.via = null;
		this.localStep = null;
		this.localPoint = null;
		this.localObject = -1;
	}

	Place(String label, String detail, Point anchor, int radius, Place via, String localStep, Point localPoint, int localObject)
	{
		this.label = label;
		this.detail = detail;
		this.anchor = anchor;
		this.radius = radius;
		this.legs = Collections.emptyList();
		this.via = via;
		this.localStep = localStep;
		this.localPoint = localPoint;
		this.localObject = localObject;
	}

	public String getLabel()
	{
		return label;
	}

	public String getDetail()
	{
		return detail;
	}

	public Point getAnchor()
	{
		return anchor;
	}

	/** The ways to get here, best first. Empty for a place reached through {@link #getVia()}. */
	public List<Leg> getLegs()
	{
		return legs;
	}

	/** The place this one is reached through, or null. */
	public Place getVia()
	{
		return via;
	}

	/** The last step from {@link #getVia()} to here. */
	public String getLocalStep()
	{
		return localStep;
	}

	public Point getLocalPoint()
	{
		return localPoint;
	}

	/** The object to use for the last step from the place this one is reached through, or -1. */
	public int getLocalObject()
	{
		return localObject;
	}

	/** Whether someone standing here has arrived. */
	public boolean contains(Point p)
	{
		return p != null && p.sameMap(anchor) && p.distance(anchor) <= radius;
	}
}
