package com.codex.rounds.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.List;
import javax.swing.JComponent;

/**
 * The six sulliusceps as a trail of seals, in the order they grow: gilt for the ones that fell
 * this run, a green ring for the one to cut now, quiet rings for those still ahead.
 */
public class CepTrail extends JComponent
{
	private static final int R = 10;

	private final int[] order;
	private final List<Integer> fell;
	private final int active;

	public CepTrail(int[] order, List<Integer> fell, int active)
	{
		this.order = order;
		this.fell = fell;
		this.active = active;
		setOpaque(false);
		setPreferredSize(new Dimension(10, 2 * R + 16));
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		Theme.smooth(g);
		int n = order.length;
		int w = getWidth();
		int cy = R + 2;
		double step = n > 1 ? (w - 2.0 * R - 4) / (n - 1) : 0;

		Stroke dash = new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{2f, 3f}, 0f);
		Stroke solid = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		for (int i = 0; i + 1 < n; i++)
		{
			int x1 = (int) Math.round(R + 2 + i * step) + R;
			int x2 = (int) Math.round(R + 2 + (i + 1) * step) - R;
			boolean done = fell.contains(order[i]) && (fell.contains(order[i + 1]) || order[i + 1] == active);
			g.setStroke(done ? solid : dash);
			g.setColor(done ? Theme.GOLD_DIM : Theme.STROKE);
			g.drawLine(x1, cy, x2, cy);
		}
		g.setStroke(new BasicStroke(1.4f));

		g.setFont(Theme.small());
		FontMetrics fm = g.getFontMetrics();
		for (int i = 0; i < n; i++)
		{
			int cap = order[i];
			int cx = (int) Math.round(R + 2 + i * step);
			Ellipse2D seal = new Ellipse2D.Float(cx - R, cy - R, 2 * R, 2 * R);
			boolean down = fell.contains(cap);
			boolean now = cap == active;
			Color num;
			if (now)
			{
				g.setPaint(new RadialGradientPaint(cx, cy, R + 6, new float[]{0f, 1f},
					new Color[]{Theme.alpha(Theme.GREEN, 110), Theme.alpha(Theme.GREEN, 0)}));
				g.fill(new Ellipse2D.Float(cx - R - 6, cy - R - 6, 2 * R + 12, 2 * R + 12));
				g.setPaint(new GradientPaint(cx, cy - R, Theme.mix(Theme.CARD_TOP, Theme.GREEN, 0.25), cx, cy + R, Theme.INK_DEEP));
				g.fill(seal);
				g.setColor(Theme.GREEN);
				g.setStroke(new BasicStroke(2f));
				g.draw(seal);
				g.setStroke(new BasicStroke(1.4f));
				num = Theme.mix(Theme.GREEN, Color.WHITE, 0.35);
			}
			else if (down)
			{
				g.setPaint(new GradientPaint(cx, cy - R, Theme.GOLD_BRIGHT, cx, cy + R, Theme.mix(Theme.GOLD, Theme.INK, 0.35)));
				g.fill(seal);
				g.setColor(Theme.mix(Theme.GOLD_DIM, Theme.INK, 0.3));
				g.draw(seal);
				num = Theme.INK;
			}
			else
			{
				g.setColor(Theme.INK_DEEP);
				g.fill(seal);
				g.setColor(Theme.STROKE);
				g.draw(seal);
				num = Theme.TEXT_FAINT;
			}
			g.setFont(Theme.title());
			FontMetrics bold = g.getFontMetrics();
			String s = String.valueOf(cap);
			int tx = cx - bold.stringWidth(s) / 2;
			int ty = cy + bold.getAscent() / 2 - 1;
			if (down)
			{
				g.setColor(num);
				g.drawString(s, tx, ty);
			}
			else
			{
				Ui.shadowed(g, s, tx, ty, num);
			}
			g.setFont(Theme.small());
			String under = now ? "cut" : null;
			if (under != null)
			{
				Ui.shadowed(g, under, cx - fm.stringWidth(under) / 2, cy + R + 12, now ? Theme.GREEN : Theme.GOLD_DIM);
			}
		}
		g.dispose();
	}
}
