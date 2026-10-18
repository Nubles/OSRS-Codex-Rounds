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
