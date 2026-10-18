package com.codex.rounds.core;

/** A tile in the world, kept apart from the client's own class so the planning code runs anywhere. */
public final class Point
{
	private final int x;
	private final int y;
	private final int plane;

	public Point(int x, int y, int plane)
	{
		this.x = x;
		this.y = y;
		this.plane = plane;
	}

	public static Point of(int x, int y)
	{
		return new Point(x, y, 0);
	}

	public static Point of(int x, int y, int plane)
	{
		return new Point(x, y, plane);
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getPlane()
	{
		return plane;
	}

	public int getRegionId()
	{
		return ((x >> 6) << 8) | (y >> 6);
	}

	/** Tiles between two points the way the game counts them, ignoring the plane. */
	public int distance(Point o)
	{
		return Math.max(Math.abs(x - o.x), Math.abs(y - o.y));
	}

	/** Underground and instanced areas sit far from the surface map; a walk between them is not a walk. */
	public boolean sameMap(Point o)
	{
		return (y >= 6400) == (o.y >= 6400);
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Point))
		{
			return false;
		}
		Point p = (Point) o;
		return x == p.x && y == p.y && plane == p.plane;
	}

	@Override
	public int hashCode()
	{
		return (x * 31 + y) * 4 + plane;
	}

	@Override
	public String toString()
	{
		return x + "," + y + "," + plane;
	}
}
