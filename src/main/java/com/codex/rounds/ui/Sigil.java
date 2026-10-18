package com.codex.rounds.ui;

import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/** The Rounds mark: a green sprout inside a gilt arrow that runs nearly all the way round. */
public final class Sigil
{
	private Sigil()
	{
	}

	public static BufferedImage icon(int size)
	{
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		Theme.smooth(g);
		paint(g, 0, 0, size);
		g.dispose();
		return image;
	}

	/** Draws the mark in a size by size box. */
	public static void paint(Graphics2D g, float x, float y, float size)
	{
		float s = size / 16f;
		float cx = x + 8 * s;
		float cy = y + 8 * s;
		float r = 6.2f * s;

		// The round: a gilt arc from the top, clockwise, stopping short so the arrowhead shows.
		Arc2D arc = new Arc2D.Float(cx - r, cy - r, r * 2, r * 2, 70, -300, Arc2D.OPEN);
		g.setStroke(new BasicStroke(Math.max(2.4f, 2.6f * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(Theme.INK_DEEP);
		g.draw(arc);
		g.setStroke(new BasicStroke(Math.max(1.2f, 1.5f * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setPaint(new GradientPaint(x, y, Theme.GOLD_BRIGHT, x, y + size, Theme.GOLD_DIM));
		g.draw(arc);

		// The arrowhead at the arc's end, pointing on round the circle towards the gap at the top.
		double end = Math.toRadians(70 - 300);
		float ex = (float) (cx + r * Math.cos(end));
		float ey = (float) (cy - r * Math.sin(end));
		float dx = (float) Math.sin(end);
		float dy = (float) Math.cos(end);
		float nx = (float) Math.cos(end);
		float ny = (float) -Math.sin(end);
		Path2D head = new Path2D.Float();
		head.moveTo(ex + dx * 2.8f * s, ey + dy * 2.8f * s);
		head.lineTo(ex + nx * 2.1f * s - dx * 0.4f * s, ey + ny * 2.1f * s - dy * 0.4f * s);
		head.lineTo(ex - nx * 2.1f * s - dx * 0.4f * s, ey - ny * 2.1f * s - dy * 0.4f * s);
		head.closePath();
		g.setStroke(new BasicStroke(Math.max(0.6f, 0.5f * s)));
		g.setColor(Theme.GOLD_BRIGHT);
		g.fill(head);
		g.setColor(Theme.INK_DEEP);
		g.draw(head);

		// The sprout: a stem and two leaves, lit from above.
		g.setStroke(new BasicStroke(Math.max(0.9f, 0.9f * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(Theme.mix(Theme.GREEN, Theme.INK_DEEP, 0.25));
		g.drawLine(Math.round(cx), Math.round(cy + 3.2f * s), Math.round(cx), Math.round(cy - 0.6f * s));
		Path2D left = leaf(cx, cy - 0.2f * s, -1, s);
		Path2D right = leaf(cx, cy - 1.0f * s, 1, s);
		g.setPaint(new GradientPaint(cx, cy - 4 * s, Theme.mix(Theme.GREEN, Theme.TEXT, 0.35), cx, cy + 2 * s, Theme.mix(Theme.GREEN, Theme.INK_DEEP, 0.3)));
		g.fill(left);
		g.fill(right);
		g.setStroke(new BasicStroke(Math.max(0.5f, 0.45f * s)));
		g.setColor(Theme.alpha(Theme.INK_DEEP, 200));
		g.draw(left);
		g.draw(right);

		// A small mound of earth at the sprout's foot.
		g.setColor(Theme.mix(Theme.GOLD_DIM, Theme.INK_DEEP, 0.4));
		g.fill(new Ellipse2D.Float(cx - 2.4f * s, cy + 2.6f * s, 4.8f * s, 1.8f * s));
	}

	private static Path2D leaf(float bx, float by, int side, float s)
	{
		float tipX = bx + side * 3.6f * s;
		float tipY = by - 2.6f * s;
		Path2D p = new Path2D.Float();
		p.moveTo(bx, by);
		p.quadTo(bx + side * 0.6f * s, tipY - 0.6f * s, tipX, tipY);
		p.quadTo(bx + side * 3.2f * s, by + 0.4f * s, bx, by);
		p.closePath();
		return p;
	}
}
