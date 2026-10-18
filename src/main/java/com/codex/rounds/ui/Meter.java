package com.codex.rounds.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

/** A labelled bar engraved into the ink: kingdom approval, the coffer, a round's progress. */
public class Meter extends JComponent
{
	private final String label;
	private final String value;
	private final double fill;
	private final Color color;
	private final Double mark;

	/** @param mark where the target sits, 0 to 1, or null */
	public Meter(String label, String value, double fill, Color color, Double mark)
	{
		this.label = label;
		this.value = value;
		this.fill = Math.max(0, Math.min(1, fill));
		this.color = color;
		this.mark = mark;
		setOpaque(false);
		setPreferredSize(new Dimension(10, 30));
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		Theme.smooth(g);
		int w = getWidth();
		g.setFont(Theme.small());
		FontMetrics fm = g.getFontMetrics();
		Ui.shadowed(g, label, 0, 11, Theme.TEXT_MUTED);
		Ui.shadowed(g, value, w - fm.stringWidth(value), 11, color);
		int y = 17;
		RoundRectangle2D track = new RoundRectangle2D.Float(0.5f, y + 0.5f, w - 1, 8, 6, 6);
		g.setColor(Theme.INK_DEEP);
		g.fill(track);
		int fw = (int) Math.round((w - 2) * fill);
		if (fw > 0)
		{
			g.setPaint(new GradientPaint(0, y, Theme.mix(color, Color.WHITE, 0.25), 0, y + 8, Theme.mix(color, Theme.INK_DEEP, 0.35)));
			g.fill(new RoundRectangle2D.Float(1, y + 1, fw, 7, 5, 5));
		}
		g.setColor(Theme.STROKE);
		g.draw(track);
		if (mark != null)
		{
			int mx = (int) Math.round(1 + (w - 3) * mark);
			g.setColor(Theme.GOLD_BRIGHT);
			g.drawLine(mx, y - 1, mx, y + 9);
		}
		g.dispose();
	}
}
