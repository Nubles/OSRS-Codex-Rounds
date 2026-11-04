package com.codex.rounds.core;

import com.codex.rounds.core.farming.PatchKind;
import com.codex.rounds.core.farming.Produce;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** The player's choices for a round, gathered from the plugin's config. */
public final class Settings
{
	public enum Compost
	{
		ULTRACOMPOST("Ultracompost"),
		SUPERCOMPOST("Supercompost"),
		NONE("None");

		private final String label;

		Compost(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	private final Set<Spot.Group> groups = EnumSet.allOf(Spot.Group.class);
	private final Set<Spot> off = EnumSet.noneOf(Spot.class);
	private final Set<Daily> dailies = EnumSet.complementOf(EnumSet.of(Daily.LUNDAIL));
	private final Map<PatchKind, Produce> crops = new EnumMap<>(PatchKind.class);
	private boolean kingdom = true;
	private int approvalTarget = 100;
	private int cofferTarget = 750_000;
	private int topUpSlack = 100_000;
	private Compost compost = Compost.ULTRACOMPOST;
	private boolean protect = true;
	private boolean keepSpiritTrees = true;
	private boolean includeUnseen = true;
	private int lookaheadMinutes = 10;
	private Produce contract;

	public static Settings defaults()
	{
		return new Settings();
	}

	public boolean wants(Spot spot)
	{
		return groups.contains(spot.getGroup()) && !off.contains(spot);
	}

	public boolean wants(Daily daily)
	{
		return dailies.contains(daily);
	}

	/** The groups a quick loop covers: the Fossil Island chores that come round every hour. */
	public static final Set<Spot.Group> QUICK = EnumSet.of(Spot.Group.BIRDHOUSES, Spot.Group.SEAWEED);

	/** A copy for a quick loop: only bird houses and seaweed, no dailies or kingdom, the rest as set. */
	public Settings quickLoop()
	{
		Settings q = new Settings();
		q.groups.retainAll(QUICK);
		q.groups.retainAll(groups);
		q.off.addAll(off);
		q.dailies.clear();
		q.crops.putAll(crops);
		q.kingdom = false;
		q.approvalTarget = approvalTarget;
		q.cofferTarget = cofferTarget;
		q.topUpSlack = topUpSlack;
		q.compost = compost;
		q.protect = protect;
		q.keepSpiritTrees = keepSpiritTrees;
		q.includeUnseen = includeUnseen;
		q.lookaheadMinutes = lookaheadMinutes;
		q.contract = contract;
		return q;
	}

	public Settings group(Spot.Group g, boolean on)
	{
		if (on)
		{
			groups.add(g);
		}
		else
		{
			groups.remove(g);
		}
		return this;
	}

	public Settings spot(Spot s, boolean on)
	{
		if (on)
		{
			off.remove(s);
		}
		else
		{
			off.add(s);
		}
		return this;
	}

	public Settings daily(Daily d, boolean on)
	{
		if (on)
		{
			dailies.add(d);
		}
		else
		{
			dailies.remove(d);
		}
		return this;
	}

	/**
	 * The spots a player's "Leave out" list names: each comma-separated entry matches every spot at a
	 * place ("Lletya", "Weiss"), or one kind there when it names both ("Catherby herbs", "Falador tree").
	 */
	public static java.util.List<Spot> leftOut(String list)
	{
		java.util.List<Spot> out = new java.util.ArrayList<>();
		if (list == null || list.trim().isEmpty())
		{
			return out;
		}
		java.util.List<String> words = new java.util.ArrayList<>();
		for (String w : list.split("[,;\\n]"))
		{
			String t = w.trim().toLowerCase();
			if (!t.isEmpty())
			{
				words.add(t);
			}
		}
		for (Spot s : Spot.values())
		{
			String label = s.getLabel().toLowerCase();
			String place = s.getPlace().getLabel().toLowerCase();
			String kind = s.kindName().toLowerCase();
			String singular = kind.endsWith("s") ? kind.substring(0, kind.length() - 1) : kind;
			for (String w : words)
			{
				boolean both = w.contains(singular) && (w.contains(label) || w.contains(place));
				boolean placeOnly = !w.contains(" ") || w.equals(label) || w.equals(place);
				if (both || placeOnly && (label.startsWith(w) || place.startsWith(w)))
				{
					out.add(s);
					break;
				}
			}
		}
		return out;
	}

	/** Plant this in every patch of the kind, or null for the best the account can plant. */
	public Settings crop(PatchKind kind, Produce produce)
	{
		if (produce == null)
		{
			crops.remove(kind);
		}
		else
		{
			crops.put(kind, produce);
		}
		return this;
	}

	/** The running farming contract's crop when it grows in a guild patch Rounds tends, or null. */
	public Settings contract(Produce produce)
	{
		this.contract = produce;
		return this;
	}

	public Produce getContract()
	{
		return contract;
	}

	public Produce getCrop(PatchKind kind)
	{
		return crops.get(kind);
	}

	public boolean isKingdom()
	{
		return kingdom;
	}

	public Settings kingdom(boolean on)
	{
		this.kingdom = on;
		return this;
	}

	public int getApprovalTarget()
	{
		return approvalTarget;
	}

	public int getCofferTarget()
	{
		return cofferTarget;
	}

	public int getTopUpSlack()
	{
		return topUpSlack;
	}

	public Settings kingdomTargets(int approvalPercent, int coffer, int slack)
	{
		this.approvalTarget = Math.max(0, Math.min(100, approvalPercent));
		this.cofferTarget = Math.max(0, Math.min(Kingdom.MAX_COFFER, coffer));
		this.topUpSlack = Math.max(0, slack);
		return this;
	}

	public Compost getCompost()
	{
		return compost;
	}

	public Settings compost(Compost c)
	{
		this.compost = c;
		return this;
	}

	public boolean isProtect()
	{
		return protect;
	}

	public Settings protect(boolean p)
	{
		this.protect = p;
		return this;
	}

	public boolean isKeepSpiritTrees()
	{
		return keepSpiritTrees;
	}

	public Settings keepSpiritTrees(boolean k)
	{
		this.keepSpiritTrees = k;
		return this;
	}

	public boolean isIncludeUnseen()
	{
		return includeUnseen;
	}

	public Settings includeUnseen(boolean u)
	{
		this.includeUnseen = u;
		return this;
	}

	public int getLookaheadMinutes()
	{
		return lookaheadMinutes;
	}

	public Settings lookahead(int minutes)
	{
		this.lookaheadMinutes = Math.max(0, minutes);
		return this;
	}
}
