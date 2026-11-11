package com.codex.rounds.overlay;

import com.codex.rounds.CodexRoundsConfig;
import com.codex.rounds.ui.Card;
import com.codex.rounds.ui.Theme;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collections;
import net.runelite.api.Client;
import net.runelite.api.TileObject;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * RuneLite's overlay renderer compares the clip after each overlay and fails every frame if an
 * overlay left it null. Every overlay and painted card must hand the graphics back as it found them.
 */
public class GraphicsStateTest
{
	/** The state a renderer would check, taken before and after drawing. */
	private static final class State
	{
		final Shape clip;
		final Font font;
		final Stroke stroke;
		final AffineTransform transform;
		final Object antialias;
		final Color color;

		State(Graphics2D g)
		{
			clip = g.getClip();
			font = g.getFont();
			stroke = g.getStroke();
			transform = g.getTransform();
			antialias = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			color = g.getColor();
		}

		void assertSame(Graphics2D g)
		{
			assertNotNull("the clip was cleared", g.getClip());
			assertEquals(clip.getBounds(), g.getClip().getBounds());
			assertEquals(font, g.getFont());
			assertEquals(stroke, g.getStroke());
			assertEquals(transform, g.getTransform());
			assertEquals(antialias, g.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
			assertEquals(color, g.getColor());
		}
	}

	private static Graphics2D canvas()
	{
		Graphics2D g = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB).createGraphics();
		g.setClip(new Rectangle(4, 4, 380, 260));
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		g.setStroke(new BasicStroke(1));
		g.setColor(Color.MAGENTA);
		return g;
	}

	@Test
	public void stepPlaqueLeavesTheGraphicsAsItFoundThem()
	{
		StepPlaque plaque = new StepPlaque();
		plaque.show(new StepPlaque.Shown("Round 3 of 9", "Check the tree's health", "Then clear it and plant a magic sapling.", "4:12", 3, 9));
		Graphics2D g = canvas();
		State before = new State(g);
		assertNotNull(plaque.render(g));
		before.assertSame(g);
	}

	@Test
	public void targetOutlineLeavesTheGraphicsAsItFoundThem()
	{
		CodexRoundsConfig config = mock(CodexRoundsConfig.class);
		when(config.outline()).thenReturn(true);
		when(config.highlightColor()).thenReturn(Theme.GOLD);
		TileObject patch = mock(TileObject.class);
		when(patch.getCanvasTextLocation(any(), anyString(), anyInt())).thenReturn(new net.runelite.api.Point(120, 90));
		TargetOverlay overlay = new TargetOverlay(mock(Client.class), mock(ModelOutlineRenderer.class), config);
		overlay.setResolved(new TargetOverlay.Resolved(Collections.singletonList(patch), null, null, "Rake the weeds"));
		Graphics2D g = canvas();
		State before = new State(g);
		overlay.render(g);
		before.assertSame(g);
	}

	@Test
	public void anAccentedCardPutsTheClipBack()
	{
		Card card = new Card(new BorderLayout()).accent(Theme.TEAL);
		card.setSize(180, 60);
		Graphics2D g = canvas();
		Shape clip = g.getClip();
		card.paint(g);
		assertNotNull(g.getClip());
		assertEquals(clip.getBounds(), g.getClip().getBounds());
	}
}
