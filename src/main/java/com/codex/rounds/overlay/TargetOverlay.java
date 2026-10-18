package com.codex.rounds.overlay;

import com.codex.rounds.CodexRoundsConfig;
import com.codex.rounds.ui.Theme;
import com.codex.rounds.ui.Ui;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

/** Outlines what the current step uses in the world and labels it, so the next click is obvious. */
public class TargetOverlay extends Overlay
{
	private final Client client;
	private final ModelOutlineRenderer outlines;
	private final CodexRoundsConfig config;

	private volatile Resolved resolved;

	/** What the plugin found in the scene for the current step. */
	public static final class Resolved
	{
		final List<TileObject> objects;
		final NPC npc;
		final WorldPoint tile;
		final String label;

		public Resolved(List<TileObject> objects, NPC npc, WorldPoint tile, String label)
		{
			this.objects = objects == null ? Collections.emptyList() : objects;
			this.npc = npc;
			this.tile = tile;
			this.label = label;
		}
	}

	@Inject
	TargetOverlay(Client client, ModelOutlineRenderer outlines, CodexRoundsConfig config)
	{
		this.client = client;
		this.outlines = outlines;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	public void setResolved(Resolved r)
	{
		resolved = r;
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		Resolved r = resolved;
		if (r == null || !config.outline())
		{
			return null;
		}
		Color color = config.highlightColor();
		Point labelAt = null;
		for (TileObject o : r.objects)
		{
			outlines.drawOutline(o, 2, color, 4);
			if (labelAt == null)
			{
				labelAt = o.getCanvasTextLocation(g, r.label, 120);
			}
		}
		if (r.npc != null)
		{
			outlines.drawOutline(r.npc, 2, color, 4);
			if (labelAt == null)
			{
				labelAt = r.npc.getCanvasTextLocation(g, r.label, r.npc.getLogicalHeight() + 40);
			}
		}
		if (r.objects.isEmpty() && r.npc == null && r.tile != null)
		{
			LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(), r.tile);
			if (lp != null)
			{
				Polygon poly = Perspective.getCanvasTilePoly(client, lp);
				if (poly != null)
				{
					g.setStroke(new BasicStroke(2));
					g.setColor(color);
					g.draw(poly);
					g.setColor(Theme.alpha(color, 40));
					g.fill(poly);
				}
				labelAt = Perspective.getCanvasTextLocation(client, g, lp, r.label, 60);
			}
		}
		if (labelAt != null && r.label != null)
		{
			g.setFont(Theme.small());
			FontMetrics fm = g.getFontMetrics();
			int w = fm.stringWidth(r.label) + 12;
			int h = fm.getHeight() + 4;
			int x = labelAt.getX() - 6;
			int y = labelAt.getY() - fm.getAscent() - 3;
			Theme.smooth(g);
			g.setColor(Theme.alpha(Theme.INK_DEEP, 210));
			g.fillRoundRect(x, y, w, h, 8, 8);
			g.setColor(Theme.alpha(Theme.GOLD, 160));
			g.drawRoundRect(x, y, w, h, 8, 8);
			Ui.shadowed(g, r.label, labelAt.getX(), labelAt.getY(), Theme.GOLD_BRIGHT);
		}
		return null;
	}
}
