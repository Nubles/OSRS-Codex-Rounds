package com.codex.rounds.core;

import java.util.Collections;
import java.util.List;

/** The one thing to do next, and what to light up for it. */
public final class Step
{
	public enum Kind
	{
		/** Nothing to do: the round is finished or there is nothing due. */
		IDLE,
		SUPPLIES,
		TRAVEL,
		TASK
	}

	/** What to point at in the world. */
	public static final class Target
	{
		public enum Type
		{
			/** A patch or bird house space, matched by its varbit or varp near a tile. */
			SPOT,
			/** Any NPC with one of these ids. */
			NPC,
			/** An object with one of these ids near a tile. */
			OBJECT,
			/** Just a tile. */
			TILE
		}

		private final Type type;
		private final Spot spot;
		private final int[] ids;
		private final Point point;

		private Target(Type type, Spot spot, int[] ids, Point point)
		{
			this.type = type;
			this.spot = spot;
			this.ids = ids;
			this.point = point;
		}

		public static Target spot(Spot s)
		{
			return new Target(Type.SPOT, s, new int[0], s.getPoint());
		}

		public static Target npc(Point near, int... ids)
		{
			return new Target(Type.NPC, null, ids, near);
		}

		public static Target object(Point near, int... ids)
		{
			return new Target(Type.OBJECT, null, ids, near);
		}

		public static Target tile(Point p)
		{
			return new Target(Type.TILE, null, new int[0], p);
		}

		public Type getType()
		{
			return type;
		}

		public Spot getSpot()
		{
			return spot;
		}

		public int[] getIds()
		{
			return ids.clone();
		}

		public boolean matches(int id)
		{
			for (int i : ids)
			{
				if (i == id)
				{
					return true;
				}
			}
			return false;
		}

		public Point getPoint()
		{
			return point;
		}
	}

	public static final Step IDLE = new Step(Kind.IDLE, "Nothing due", "Every chore is growing or done.", null, Collections.emptyList(),
		null, null, -1, 0, 0);

	private final Kind kind;
	private final String title;
	private final String detail;
	private final Target target;
	private final List<Integer> items;
	private final Point mapPoint;
	private final Task task;
	private final int stopIndex;
	private final int done;
	private final int total;

	public Step(Kind kind, String title, String detail, Target target, List<Integer> items, Point mapPoint, Task task,
		int stopIndex, int done, int total)
	{
		this.kind = kind;
		this.title = title;
		this.detail = detail;
		this.target = target;
		this.items = Collections.unmodifiableList(items);
		this.mapPoint = mapPoint;
		this.task = task;
		this.stopIndex = stopIndex;
		this.done = done;
		this.total = total;
	}

	public Kind getKind()
	{
		return kind;
	}

	public String getTitle()
	{
		return title;
	}

	public String getDetail()
	{
		return detail;
	}

	/** What to outline and arrow, or null. */
	public Target getTarget()
	{
		return target;
	}

	/** Items to light up in the inventory, worn gear and bank. */
	public List<Integer> getItems()
	{
		return items;
	}

	/** Where to put the world map marker, or null. */
	public Point getMapPoint()
	{
		return mapPoint;
	}

	/** The chore this step belongs to, or null. */
	public Task getTask()
	{
		return task;
	}

	/** Which stop of the round is current, or -1. */
	public int getStopIndex()
	{
		return stopIndex;
	}

	public int getDone()
	{
		return done;
	}

	public int getTotal()
	{
		return total;
	}
}
