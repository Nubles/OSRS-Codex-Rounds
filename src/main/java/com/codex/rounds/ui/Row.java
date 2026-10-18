package com.codex.rounds.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * One line of the board or the pull list: an item in its well, a title, a quiet second line
 * and a coloured badge on the right that says where it stands.
 */
public class Row extends JComponent
{
	static final int HEIGHT = 40;

	private final ItemIcons icons;
	private final int itemId;
	private final String title;
	private final String sub;
	private final String badge;
	private final Color badgeColor;
	private final boolean dim;
	private BufferedImage icon;

	public Row(ItemIcons icons, int itemId, String title, String sub, String badge, Color badgeColor, boolean dim)
	{
		this.icons = icons;
		this.itemId = itemId;
		this.title = title;
		this.sub = sub;
		this.badge = badge;
		this.badgeColor = badgeColor;
		this.dim = dim;
		setOpaque(false);
		setPreferredSize(new Dimension(10, HEIGHT));
		setToolTipText(sub == null || sub.isEmpty() ? title : title + ": " + sub);
		if (icons != null && itemId >= 0)
		{
			icon = icons.icon(itemId, this::repaint);
		}
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		Theme.smooth(g);
		int w = getWidth();
		int h = getHeight();
		RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 2, 8, 8);
		g.setPaint(new GradientPaint(0, 0, dim ? Theme.mix(Theme.CARD, Theme.INK, 0.5) : Theme.CARD_TOP, 0, h, Theme.CARD));
		g.fill(shape);
		g.setColor(Theme.STROKE_SOFT);
		g.draw(shape);

		if (icons != null && itemId >= 0)
		{
			Well.paint(g, 3, 3, icon, null);
		}
		else
		{
			// No item: a status dot sits where the well would be.
			g.setColor(badgeColor);
			g.fill(new Ellipse2D.Float(14, h / 2f - 4, 8, 8));
		}

		int x = 44;
		g.setFont(Theme.small());
		FontMetrics small = g.getFontMetrics();
		int badgeW = badge == null || badge.isEmpty() ? 0 : small.stringWidth(badge) + 12;
		int room = w - x - badgeW - 8;

		g.setFont(Theme.body());
		FontMetrics body = g.getFontMetrics();
		Ui.shadowed(g, Ui.clip(title, body, room), x, 17, dim ? Theme.TEXT_MUTED : Theme.TEXT);
		if (sub != null && !sub.isEmpty())
		{
			g.setFont(Theme.small());
			Ui.shadowed(g, Ui.clip(sub, small, room), x, 31, Theme.TEXT_FAINT);
		}

		if (badgeW > 0)
		{
			g.setFont(Theme.small());
			int bx = w - badgeW - 6;
			int by = h / 2 - 8;
			RoundRectangle2D pill = new RoundRectangle2D.Float(bx, by, badgeW, 15, 8, 8);
			g.setColor(Theme.alpha(badgeColor, 34));
			g.fill(pill);
			g.setColor(Theme.alpha(badgeColor, 150));
			g.draw(pill);
			Ui.shadowed(g, badge, bx + 6, by + 11, badgeColor);
		}
		g.dispose();
	}
}
