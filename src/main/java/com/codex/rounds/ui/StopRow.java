package com.codex.rounds.ui;

import com.codex.rounds.core.Stop;
import com.codex.rounds.core.Task;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Predicate;
import javax.swing.JComponent;

/**
 * One stop of the round: a numbered seal, the place, how to get there, and its chores with a
 * tick against each one done. The current stop is lit in gold; finished stops fade back.
 */
public class StopRow extends JComponent
{
	private static final int HEAD = 34;
	private static final int LINE = 14;

	private final int number;
	private final Stop stop;
	private final boolean current;
	private final Predicate<Task> done;

	public StopRow(int number, Stop stop, boolean current, Predicate<Task> done)
	{
		this.number = number;
		this.stop = stop;
		this.current = current;
		this.done = done;
		setOpaque(false);
		setPreferredSize(new Dimension(10, HEAD + stop.getTasks().size() * LINE + 6));
	}

	private boolean allDone()
	{
		for (Task t : stop.getTasks())
		{
			if (!done.test(t))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		Theme.smooth(g);
		int w = getWidth();
		int h = getHeight();
		boolean finished = allDone();

		RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 2, 9, 9);
		Color top = current ? Theme.mix(Theme.CARD_TOP, Theme.GOLD_DIM, 0.18) : finished ? Theme.mix(Theme.CARD, Theme.INK, 0.5) : Theme.CARD_TOP;
		g.setPaint(new GradientPaint(0, 0, top, 0, h, Theme.CARD));
		g.fill(shape);
		g.setColor(current ? Theme.alpha(Theme.GOLD, 160) : Theme.STROKE_SOFT);
		g.draw(shape);

		// The seal: a numbered disc, gold for the current stop, green with a tick when finished.
		float cx = 17;
		float cy = 17;
		float r = 10;
		Color seal = finished ? Theme.GREEN : current ? Theme.GOLD : Theme.GOLD_DIM;
		g.setPaint(new GradientPaint(0, cy - r, Theme.mix(seal, Color.WHITE, 0.2), 0, cy + r, Theme.mix(seal, Theme.INK_DEEP, 0.35)));
		g.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
		g.setColor(Theme.INK_DEEP);
		g.draw(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
		if (finished)
		{
			tick(g, cx - 4.5f, cy - 0.5f, 1.1f, Theme.INK_DEEP);
		}
		else
		{
			g.setFont(Theme.title());
			FontMetrics fm = g.getFontMetrics();
			String n = String.valueOf(number);
			g.setColor(Theme.INK_DEEP);
			g.drawString(n, Math.round(cx - fm.stringWidth(n) / 2f), Math.round(cy + fm.getAscent() / 2f - 2));
		}

		int x = 34;
		int room = w - x - 8;
		g.setFont(Theme.body());
		FontMetrics body = g.getFontMetrics();
		Ui.shadowed(g, Ui.clip(stop.getPlace().getLabel(), body, room), x, 15, finished ? Theme.TEXT_MUTED : current ? Theme.GOLD_BRIGHT : Theme.TEXT);
		g.setFont(Theme.small());
		FontMetrics small = g.getFontMetrics();
		Ui.shadowed(g, Ui.clip(stop.getWay().describe(), small, room), x, 28, Theme.TEXT_FAINT);

		List<Task> tasks = stop.getTasks();
		int y = HEAD + 8;
		for (Task t : tasks)
		{
			boolean d = done.test(t);
			if (d)
			{
				tick(g, x + 1, y - 4, 0.75f, Theme.GREEN);
			}
			else
			{
				g.setColor(current ? Theme.GOLD : Theme.GOLD_DIM);
				Path2D diamond = new Path2D.Float();
				diamond.moveTo(x + 3, y - 7);
				diamond.lineTo(x + 6, y - 4);
				diamond.lineTo(x + 3, y - 1);
				diamond.lineTo(x, y - 4);
				diamond.closePath();
				g.fill(diamond);
			}
			String line = t.getSpot() != null ? t.getSpot().kindName() + ", " + t.getSpot().getLabel() : t.getTitle();
			Ui.shadowed(g, Ui.clip(line, small, room - 12), x + 11, y, d ? Theme.TEXT_FAINT : Theme.TEXT_MUTED);
			y += LINE;
		}
		g.dispose();
	}

	static void tick(Graphics2D g, float x, float y, float scale, Color color)
	{
		Path2D p = new Path2D.Float();
		p.moveTo(x, y);
		p.lineTo(x + 3 * scale, y + 3 * scale);
		p.lineTo(x + 9 * scale, y - 4 * scale);
		g.setStroke(new BasicStroke(1.8f * scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(color);
		g.draw(p);
		g.setStroke(new BasicStroke(1));
	}
}
