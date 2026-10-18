package com.codex.rounds;

import com.codex.rounds.core.Settings;
import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(CodexRoundsConfig.GROUP)
public interface CodexRoundsConfig extends Config
{
	String GROUP = "codexrounds";

	enum Hardwood
	{
		BEST("Best I can plant"),
		TEAK("Teak"),
		MAHOGANY("Mahogany"),
		CAMPHOR("Camphor"),
		IRONWOOD("Ironwood"),
		ROSEWOOD("Rosewood");

		private final String label;

		Hardwood(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	enum Bush
	{
		BEST("Best I can plant"),
		REDBERRIES("Redberry"),
		CADAVABERRIES("Cadavaberry"),
		DWELLBERRIES("Dwellberry"),
		JANGERBERRIES("Jangerberry"),
		WHITEBERRIES("Whiteberry"),
		POISON_IVY("Poison ivy");

		private final String label;

		Bush(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	enum Cactus
	{
		BEST("Best I can plant"),
		CACTUS("Cactus"),
		POTATO_CACTUS("Potato cactus");

		private final String label;

		Cactus(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	enum Anima
	{
		BEST("Best I own"),
		ATTAS("Attas"),
		IASOR("Iasor"),
		KRONOS("Kronos");

		private final String label;

		Anima(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	@ConfigSection(name = "Round", description = "How the round is planned and shown", position = 0)
	String ROUND = "round";

	@ConfigSection(name = "Chores", description = "What goes on the round", position = 1)
	String CHORES = "chores";

	@ConfigSection(name = "Dailies", description = "Daily buys and claims to pick up on the way", position = 2)
	String DAILIES = "dailies";

	@ConfigSection(name = "Planting", description = "What to plant and how to look after it", position = 3)
	String PLANTING = "planting";

	@ConfigSection(name = "Kingdom of Miscellania", description = "Keeping the kingdom's approval and coffer topped up", position = 4)
	String KINGDOM = "kingdom";

	@ConfigSection(name = "Highlights", description = "What lights up in the game", position = 5)
	String HIGHLIGHTS = "highlights";

	@ConfigItem(
		keyName = "lookahead",
		name = "Include soon-ready",
		description = "Put chores on the round that will be ready within this many minutes, so you can catch them on the way",
		section = ROUND,
		position = 0
	)
	@Range(max = 120)
	@Units(Units.MINUTES)
	default int lookahead()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "includeUnseen",
		name = "Include unseen patches",
		description = "Visit patches Rounds has never read, to learn their state",
		section = ROUND,
		position = 1
	)
	default boolean includeUnseen()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notifyReady",
		name = "Notify when ready",
		description = "Send a notification when a round's worth of chores comes ready",
		section = ROUND,
		position = 2
	)
	default boolean notifyReady()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notifyAt",
		name = "Notify at",
		description = "How many chores must be ready before the notification fires",
		section = ROUND,
		position = 3
	)
	@Range(min = 1, max = 30)
	default int notifyAt()
	{
		return 4;
	}

	@ConfigItem(
		keyName = "plaque",
		name = "Step plaque",
		description = "Show the next step in a plaque in the game view while a round is running",
		section = ROUND,
		position = 4
	)
	default boolean plaque()
	{
		return true;
	}

	@ConfigItem(
		keyName = "chatSummary",
		name = "Round summary in chat",
		description = "When a round ends, post its time and what it did in the chat box",
		section = ROUND,
		position = 5
	)
	default boolean chatSummary()
	{
		return true;
	}

	@ConfigItem(
		keyName = "birdhouses",
		name = "Bird houses",
		description = "The four bird house spaces on Fossil Island",
		section = CHORES,
		position = 0
	)
	default boolean birdhouses()
	{
		return true;
	}

	@ConfigItem(
		keyName = "seaweed",
		name = "Giant seaweed",
		description = "The two seabed patches off Fossil Island",
		section = CHORES,
		position = 1
	)
	default boolean seaweed()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hardwoods",
		name = "Hardwood trees",
		description = "Fossil Island, Anglers' Retreat and Avium Savannah",
		section = CHORES,
		position = 2
	)
	default boolean hardwoods()
	{
		return true;
	}

	@ConfigItem(
		keyName = "specialTrees",
		name = "Special trees",
		description = "Calquat, celastrus, redwood and crystal trees",
		section = CHORES,
		position = 3
	)
	default boolean specialTrees()
	{
		return true;
	}

	@ConfigItem(
		keyName = "spiritTrees",
		name = "Spirit trees",
		description = "Every spirit tree patch",
		section = CHORES,
		position = 4
	)
	default boolean spiritTrees()
	{
		return true;
	}

	@ConfigItem(
		keyName = "specialPatches",
		name = "Special patches",
		description = "Cactus, bush, mushroom, belladonna, anima and Hespori",
		section = CHORES,
		position = 5
	)
	default boolean specialPatches()
	{
		return true;
	}

	@ConfigItem(
		keyName = "zaff",
		name = "Zaff's battlestaves",
		description = "Buy your daily battlestaves from Zaff in Varrock (needs the Varrock diary)",
		section = DAILIES,
		position = 0
	)
	default boolean zaff()
	{
		return true;
	}

	@ConfigItem(
		keyName = "cromperty",
		name = "Cromperty's essence",
		description = "Claim noted pure essence from Wizard Cromperty (needs the medium Ardougne diary)",
		section = DAILIES,
		position = 1
	)
	default boolean cromperty()
	{
		return true;
	}

	@ConfigItem(
		keyName = "flax",
		name = "Flax to bowstrings",
		description = "Have the Seers' Village flax keeper spin your flax (needs the Kandarin diary)",
		section = DAILIES,
		position = 2
	)
	default boolean flax()
	{
		return true;
	}

	@ConfigItem(
		keyName = "rantz",
		name = "Rantz's ogre arrows",
		description = "Claim ogre arrows from Rantz (needs the Western Provinces diary)",
		section = DAILIES,
		position = 3
	)
	default boolean rantz()
	{
		return true;
	}

	@ConfigItem(
		keyName = "robin",
		name = "Robin's bonemeal",
		description = "Hand bones to Robin for bonemeal and slime (needs the medium Morytania diary)",
		section = DAILIES,
		position = 4
	)
	default boolean robin()
	{
		return true;
	}

	@ConfigItem(
		keyName = "thirus",
		name = "Thirus's dynamite",
		description = "Claim dynamite from Thirus (needs the medium Kourend & Kebos diary)",
		section = DAILIES,
		position = 5
	)
	default boolean thirus()
	{
		return true;
	}

	@ConfigItem(
		keyName = "bert",
		name = "Bert's sand",
		description = "Ask Bert in Yanille for your buckets of sand (needs The Hand in the Sand)",
		section = DAILIES,
		position = 6
	)
	default boolean bert()
	{
		return true;
	}

	@ConfigItem(
		keyName = "herbBoxes",
		name = "Herb boxes",
		description = "Buy herb boxes with Nightmare Zone points",
		section = DAILIES,
		position = 7
	)
	default boolean herbBoxes()
	{
		return true;
	}

	@ConfigItem(
		keyName = "lundail",
		name = "Lundail's runes",
		description = "Claim runes from Lundail at the Mage Arena, deep in the Wilderness",
		section = DAILIES,
		position = 8
	)
	default boolean lundail()
	{
		return false;
	}

	@ConfigItem(
		keyName = "compost",
		name = "Compost",
		description = "What to treat every new planting with",
		section = PLANTING,
		position = 0
	)
	default Settings.Compost compost()
	{
		return Settings.Compost.ULTRACOMPOST;
	}

	@ConfigItem(
		keyName = "protect",
		name = "Pay gardeners",
		description = "Pay the gardener to protect every crop that can be protected",
		section = PLANTING,
		position = 1
	)
	default boolean protect()
	{
		return true;
	}

	@ConfigItem(
		keyName = "keepSpiritTrees",
		name = "Keep spirit trees",
		description = "Leave grown spirit trees standing as teleports instead of clearing and replanting",
		section = PLANTING,
		position = 2
	)
	default boolean keepSpiritTrees()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hardwood",
		name = "Hardwood",
		description = "What to plant in hardwood patches",
		section = PLANTING,
		position = 3
	)
	default Hardwood hardwood()
	{
		return Hardwood.BEST;
	}

	@ConfigItem(
		keyName = "bush",
		name = "Bush",
		description = "What to plant in bush patches",
		section = PLANTING,
		position = 4
	)
	default Bush bush()
	{
		return Bush.BEST;
	}

	@ConfigItem(
		keyName = "cactus",
		name = "Cactus",
		description = "What to plant in cactus patches",
		section = PLANTING,
		position = 5
	)
	default Cactus cactus()
	{
		return Cactus.BEST;
	}

	@ConfigItem(
		keyName = "anima",
		name = "Anima",
		description = "Which anima seed to plant",
		section = PLANTING,
		position = 6
	)
	default Anima anima()
	{
		return Anima.BEST;
	}

	@ConfigItem(
		keyName = "kingdom",
		name = "Look after the kingdom",
		description = "Put Miscellania on the round when approval or the coffer falls below your targets (needs Throne of Miscellania)",
		section = KINGDOM,
		position = 0
	)
	default boolean kingdom()
	{
		return true;
	}

	@ConfigItem(
		keyName = "approvalTarget",
		name = "Approval target",
		description = "Visit when approval falls below this",
		section = KINGDOM,
		position = 1
	)
	@Range(max = 100)
	@Units(Units.PERCENT)
	default int approvalTarget()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "cofferTarget",
		name = "Coffer target",
		description = "Top the coffer up to this many coins. 750,000 keeps the full daily payout after Royal Trouble; 500,000 before it",
		section = KINGDOM,
		position = 2
	)
	@Range(max = 7_500_000)
	default int cofferTarget()
	{
		return 750_000;
	}

	@ConfigItem(
		keyName = "topUpSlack",
		name = "Top up when short by",
		description = "Only visit for the coffer once it is this far below the target",
		section = KINGDOM,
		position = 3
	)
	@Range(max = 7_500_000)
	default int topUpSlack()
	{
		return 150_000;
	}

	@ConfigItem(
		keyName = "outline",
		name = "Outline the target",
		description = "Outline the patch, bird house or person to use next",
		section = HIGHLIGHTS,
		position = 0
	)
	default boolean outline()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hintArrow",
		name = "Hint arrow",
		description = "Point the game's hint arrow at the next target",
		section = HIGHLIGHTS,
		position = 1
	)
	default boolean hintArrow()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightItems",
		name = "Highlight items",
		description = "Light up the item to use in your pack, worn gear and bank",
		section = HIGHLIGHTS,
		position = 2
	)
	default boolean highlightItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "mapMarker",
		name = "World map marker",
		description = "Mark the next stop on the world map",
		section = HIGHLIGHTS,
		position = 3
	)
	default boolean mapMarker()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight colour",
		description = "The colour of outlines and item highlights",
		section = HIGHLIGHTS,
		position = 4
	)
	default Color highlightColor()
	{
		return new Color(246, 214, 144, 230);
	}

	@ConfigItem(
		keyName = "bankTab",
		name = "Round bank tab",
		description = "Open a bank tab with exactly what the round needs, a row for each kind, when you bank for a round",
		section = HIGHLIGHTS,
		position = 5
	)
	default boolean bankTab()
	{
		return true;
	}
}
