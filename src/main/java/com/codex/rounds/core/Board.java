package com.codex.rounds.core;

import com.codex.rounds.core.farming.PatchKind;
import com.codex.rounds.core.farming.Produce;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.runelite.api.gameval.ItemID;

/** Every chore a round covers and where each one stands. */
public final class Board
{
	public enum Type
	{
		SPOT,
		DAILY,
		KINGDOM
	}

	public static final class Entry
	{
		private final Type type;
		private final Spot spot;
		private final Daily daily;
		private final Status status;
		private final String title;
		private final String detail;
		private final long readyAt;
		private final int itemId;
		private final Reading reading;

		Entry(Type type, Spot spot, Daily daily, Status status, String title, String detail, long readyAt, int itemId, Reading reading)
		{
			this.type = type;
			this.spot = spot;
			this.daily = daily;
			this.status = status;
			this.title = title;
			this.detail = detail;
			this.readyAt = readyAt;
			this.itemId = itemId;
			this.reading = reading;
		}

		public Type getType()
		{
			return type;
		}

		public Spot getSpot()
		{
			return spot;
		}

		public Daily getDaily()
		{
			return daily;
		}

		public Status getStatus()
		{
			return status;
		}

		public String getTitle()
		{
			return title;
		}

		public String getDetail()
		{
			return detail;
		}

		public long getReadyAt()
		{
			return readyAt;
		}

		public int getItemId()
		{
			return itemId;
		}

		/** The spot's reading, or null for dailies and the kingdom. */
		public Reading getReading()
		{
			return reading;
		}

		public Place getPlace()
		{
			switch (type)
			{
				case SPOT:
					return spot.getPlace();
				case DAILY:
					return daily.getPlace();
				default:
					return Place.MISCELLANIA;
			}
		}
	}

	private final List<Entry> entries;
	private final Kingdom kingdom;
	private final long now;

	private Board(List<Entry> entries, Kingdom kingdom, long now)
	{
		this.entries = Collections.unmodifiableList(entries);
		this.kingdom = kingdom;
		this.now = now;
	}

	public List<Entry> getEntries()
	{
		return entries;
	}

	/** The kingdom as known, or null when Throne of Miscellania isn't done or never seen. */
	public Kingdom getKingdom()
	{
		return kingdom;
	}

	public long getNow()
	{
		return now;
	}

	public long count(Status s)
	{
		return entries.stream().filter(e -> e.status == s).count();
	}

	/** The next thing that becomes ready, or null. */
	public Entry nextUp()
	{
		return entries.stream()
			.filter(e -> e.status == Status.GROWING && e.readyAt > now)
			.min(Comparator.comparingLong(Entry::getReadyAt))
			.orElse(null);
	}

	/** A wait worth making: how long, how many more chores it adds, and which groups they are. */
	public static final class Forecast
	{
		private final long waitSeconds;
		private final int more;
		private final List<String> groups;

		Forecast(long waitSeconds, int more, List<String> groups)
		{
			this.waitSeconds = waitSeconds;
			this.more = more;
			this.groups = groups;
		}

		public long getWaitSeconds()
		{
			return waitSeconds;
		}

		public int getMore()
		{
			return more;
		}

		public List<String> getGroups()
		{
			return groups;
		}
	}

	/** How far ahead a wait is worth suggesting. */
	static final long FORECAST_HORIZON = 45 * 60;

	/**
	 * Whether waiting a little brings enough chores in to be worth it: the moment within the horizon that adds the
	 * most chores for each minute waited, when it adds at least two. Minutes are padded by ten so a wait of one
	 * minute for one chore does not beat a wait of fifteen for six.
	 *
	 * @param after only chores ready later than this count, so ones already in the round are not counted twice
	 */
	public Forecast forecast(long after)
	{
		List<Entry> coming = new ArrayList<>();
		for (Entry e : entries)
		{
			if (e.status == Status.GROWING && e.readyAt > Math.max(now, after) && e.readyAt <= now + FORECAST_HORIZON)
			{
				coming.add(e);
			}
		}
		coming.sort(Comparator.comparingLong(Entry::getReadyAt));
		Forecast best = null;
		double bestRate = 0;
		for (int i = 0; i < coming.size(); i++)
		{
			// Several chores often come ready on the same tick; take them all.
			if (i + 1 < coming.size() && coming.get(i + 1).readyAt == coming.get(i).readyAt)
			{
				continue;
			}
			int more = i + 1;
			long wait = coming.get(i).readyAt - now;
			double rate = more / (wait / 60.0 + 10);
			if (more >= 2 && rate > bestRate)
			{
				List<String> groups = new ArrayList<>();
				for (Entry e : coming.subList(0, more))
				{
					String g = e.spot != null ? e.spot.getGroup().getLabel().toLowerCase() : "dailies";
					if (!groups.contains(g))
					{
						groups.add(g);
					}
				}
				best = new Forecast(wait, more, groups);
				bestRate = rate;
			}
		}
		return best;
	}

	/**
	 * Builds the board.
	 *
	 * @param seen    the last value seen for each spot
	 * @param dailies today's dailies, or empty when nothing is known
	 * @param kingdom the kingdom today, or null
	 */
	public static Board build(Map<Spot, Seen> seen, List<DailyState> dailies, Kingdom kingdom, Settings settings,
		Ability ability, long now, Growth.Offset offset)
	{
		List<Entry> list = new ArrayList<>();
		for (Spot spot : Spot.values())
		{
			Reading r = Growth.read(spot, seen.get(spot), now, offset);
			if (!settings.wants(spot))
			{
				r = r.as(Status.OFF, "Left out of rounds");
			}
			else if (r.getStatus() == Status.UNSEEN)
			{
				int need = requiredLevel(spot);
				if (spot.isBirdhouse() ? ability.getHunter() < need : ability.getFarming() < need)
				{
					r = r.as(Status.LOCKED, "Needs " + need + (spot.isBirdhouse() ? " Hunter" : " Farming"));
				}
				else if (!settings.isIncludeUnseen())
				{
					r = r.as(Status.OFF, "Not seen yet");
				}
			}
			list.add(new Entry(Type.SPOT, spot, null, r.getStatus(), spot.getTitle(), r.getSummary(), r.getReadyAt(), icon(spot, r), r));
		}

		for (DailyState d : dailies)
		{
			Status s;
			String detail;
			if (!d.isUnlocked())
			{
				s = Status.LOCKED;
				detail = unlockHint(d.getDaily());
			}
			else if (!settings.wants(d.getDaily()))
			{
				s = Status.OFF;
				detail = "Left out of rounds";
			}
			else if (d.isClaimable())
			{
				s = Status.READY;
				detail = d.getReward();
			}
			else
			{
				s = Status.DONE;
				detail = "Claimed today";
			}
			long reset = (now / 86400 + 1) * 86400;
			list.add(new Entry(Type.DAILY, null, d.getDaily(), s, d.getDaily().getLabel(), detail, s == Status.DONE ? reset : 0, d.getDaily().getItemId(), null));
		}

		if (kingdom != null)
		{
			Status s;
			String detail = kingdom.approvalPercent() + "% approval, " + Format.coins(kingdom.getCoffer()) + " in the coffer"
				+ (kingdom.isEstimated() ? " (estimated)" : "");
			if (!settings.isKingdom())
			{
				s = Status.OFF;
			}
			else if (kingdom.needsVisit(settings.getApprovalTarget(), settings.getCofferTarget(), settings.getTopUpSlack()))
			{
				s = Status.READY;
			}
			else
			{
				s = Status.DONE;
			}
			list.add(new Entry(Type.KINGDOM, null, null, s, "Kingdom of Miscellania", detail, 0, ItemID.POH_TREASURE_TEAK, null));
		}
		return new Board(list, kingdom, now);
	}

	static String unlockHint(Daily d)
	{
		switch (d)
		{
			case ZAFF:
				return "Needs the easy Varrock diary";
			case CROMPERTY:
				return "Needs the medium Ardougne diary";
			case FLAX:
				return "Needs the easy Kandarin diary";
			case RANTZ:
				return "Needs the easy Western Provinces diary";
			case ROBIN:
				return "Needs the medium Morytania diary";
			case THIRUS:
				return "Needs the medium Kourend & Kebos diary";
			case BERT:
				return "Needs The Hand in the Sand";
			case HERB_BOXES:
				return "Needs 9,500 Nightmare Zone points (not for ironmen)";
			case LUNDAIL:
				return "Needs the easy Wilderness diary";
			default:
				return "Locked";
		}
	}

	/** The lowest level that opens a spot. */
	public static int requiredLevel(Spot spot)
	{
		switch (spot)
		{
			case CELASTRUS_GUILD:
			case REDWOOD_GUILD:
				return 85;
			case SPIRIT_GUILD:
			case ANIMA_GUILD:
			case HESPORI_GUILD:
				return 65;
			case BUSH_GUILD:
			case CACTUS_GUILD:
				return 45;
			default:
				break;
		}
		if (spot.isBirdhouse())
		{
			return 5;
		}
		PatchKind kind = spot.getKind();
		int min = 99;
		for (Produce p : Produce.forKind(kind))
		{
			min = Math.min(min, p.getLevel());
		}
		return min;
	}

	static int icon(Spot spot, Reading r)
	{
		if (spot.isBirdhouse())
		{
			Birdhouse.Type t = r.getSeen() == null ? null : Birdhouse.type(r.getSeen().getValue());
			return t == null ? ItemID.BIRDHOUSE_NORMAL : t.getItemId();
		}
		Produce p = r.getProduce();
		if (p != null)
		{
			return p.getItemId();
		}
		List<Produce> all = Produce.forKind(spot.getKind());
		return all.isEmpty() ? -1 : all.get(all.size() - 1).getItemId();
	}
}
