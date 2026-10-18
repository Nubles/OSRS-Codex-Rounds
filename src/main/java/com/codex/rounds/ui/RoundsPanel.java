package com.codex.rounds.ui;

import com.codex.rounds.core.Board;
import com.codex.rounds.core.Format;
import com.codex.rounds.core.Kingdom;
import com.codex.rounds.core.Leprechaun;
import com.codex.rounds.core.Need;
import com.codex.rounds.core.Route;
import com.codex.rounds.core.Settings;
import com.codex.rounds.core.Spot;
import com.codex.rounds.core.Status;
import com.codex.rounds.core.Step;
import com.codex.rounds.core.Stop;
import com.codex.rounds.core.Task;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.PluginPanel;

/**
 * The Rounds side panel: the crest with how many chores are ready, then two pages. Round is
 * the next step, the stops in order and the pull list; Board is every patch, bird house,
 * daily and the kingdom with where each one stands.
 */
public class RoundsPanel extends PluginPanel
{
	static final int WIDTH = 200;
	private static final String[] TABS = {"round", "board"};

	private final ItemIcons icons;
	private final Actions actions;
	private final Crest crest = new Crest();
	private final Segmented tabs = new Segmented(true, "Round", "Board");
	private final CardLayout cards = new CardLayout();
	private final JPanel content = new JPanel(cards);
	private final JScrollPane round = scroller();
	private final JScrollPane board = scroller();
	private Predicate<Task> done = t -> false;

	private View view = View.empty();

	public RoundsPanel(ItemIcons icons, Actions actions)
	{
		super(false);
		this.icons = icons;
		this.actions = actions;
		setLayout(new BorderLayout());
		setBackground(Theme.INK);

		JPanel north = new JPanel(new BorderLayout());
		north.setOpaque(false);
		north.add(crest, BorderLayout.NORTH);
		JPanel tabHolder = new JPanel(new BorderLayout());
		tabHolder.setOpaque(false);
		tabHolder.setBorder(new EmptyBorder(8, 8, 2, 8));
		tabHolder.add(tabs, BorderLayout.CENTER);
		north.add(tabHolder, BorderLayout.SOUTH);
		add(north, BorderLayout.NORTH);

		content.setOpaque(false);
		content.add(round, TABS[0]);
		content.add(board, TABS[1]);
		add(content, BorderLayout.CENTER);
		tabs.onChange(this::showTab);
		rebuild();
	}

	private static JScrollPane scroller()
	{
		JScrollPane scroll = new JScrollPane();
		scroll.setBorder(null);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		return scroll;
	}

	/**
	 * Called on the Swing thread whenever anything shown changes.
	 *
	 * @param done tells which chores of the round are finished
	 */
	public void update(View view, Predicate<Task> done)
	{
		this.view = view;
		this.done = done;
		updateCrest();
		rebuild();
	}

	public void showTab(int index)
	{
		tabs.setSelected(index, false);
		cards.show(content, TABS[index]);
	}

	private void updateCrest()
	{
		if (!view.known)
		{
			crest.update("READY NOW", null, false, view.loggedIn ? "Reading your account" : "Log in to read your patches", null, Theme.TEXT_MUTED, Theme.GOLD);
			return;
		}
		Board b = view.board;
		int ready = (int) b.count(Status.READY);
		String status;
		String brief = null;
		Color color = Theme.TEXT_MUTED;
		if (view.running && view.step != null && view.step.getTotal() > 0)
		{
			status = view.step.getDone() + " of " + view.step.getTotal() + " done, " + Format.clock(view.now - view.startedAt);
			color = Theme.GOLD;
		}
		else
		{
			Board.Entry next = b.nextUp();
			if (next != null)
			{
				status = "Next: " + next.getTitle() + " in " + Format.shortDuration(next.getReadyAt() - view.now);
				brief = "Next in " + Format.shortDuration(next.getReadyAt() - view.now);
			}
			else
			{
				status = ready > 0 ? "Ready for a round" : "Nothing growing";
			}
		}
		crest.update(view.running ? "ROUND UNDER WAY" : "READY NOW", ready, false, status, brief, color, ready > 0 ? Theme.GREEN : Theme.GOLD);
	}

	private void rebuild()
	{
		round.setViewportView(buildRound());
		board.setViewportView(buildBoard());
		revalidate();
		repaint();
	}

	private StackPanel buildRound()
	{
		StackPanel list = new StackPanel(6, 8);
		if (!view.known)
		{
			list.add(note(view.loggedIn ? "Reading your patches, dailies and kingdom."
				: "Log in and Rounds reads your patches, bird houses, dailies and kingdom. RuneLite's Time Tracking history is picked up too."));
			return list;
		}
		Route route = view.route;
		Step step = view.step;

		list.add(new SectionHeader(view.running ? "Next step" : "Your round", view.running && step.getTotal() > 0 ? step.getDone() + "/" + step.getTotal() : null));
		list.add(stepCard(route, step));

		if (!route.isEmpty())
		{
			list.space(2);
			list.add(new SectionHeader("The round", route.getStops().size() + (route.getStops().size() == 1 ? " stop" : " stops")));
			int i = 0;
			for (Stop s : route.getStops())
			{
				list.add(new StopRow(i + 1, s, view.running && step.getStopIndex() == i, done));
				i++;
			}

			list.space(2);
			list.add(new SectionHeader("Bring", "pull list"));
			if (!view.bankKnown)
			{
				list.add(note("Open your bank once so Rounds can plan with the teleports and supplies you own."));
			}
			Need.Kind kind = null;
			for (Need n : route.getPulls())
			{
				if (n.getKind() != kind)
				{
					kind = n.getKind();
					list.add(sub(kind.getLabel()));
				}
				list.add(pullRow(n));
			}
			if (!route.getPulls().isEmpty())
			{
				list.space(1);
				JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
				bar.setOpaque(false);
				bar.add(new CodexButton("Lay it out in my bank", false, actions::bankTab));
				list.add(bar);
			}
		}
		if (view.lastRound != null)
		{
			list.space(2);
			list.add(note(view.lastRound));
		}
		return list;
	}

	private JComponent stepCard(Route route, Step step)
	{
		Card card = new Card(new BorderLayout(0, 6)).accent(view.running ? Theme.GOLD : Theme.TEAL).highlighted(view.running);
		StackPanel body = new StackPanel(3, 0);
		int width = WIDTH - 40;
		String title;
		String detail;
		if (route.isEmpty())
		{
			title = "Nothing due";
			Board.Entry next = view.board.nextUp();
			detail = next == null ? "Every chore is growing or done." : next.getTitle() + " is ready in " + Format.duration(next.getReadyAt() - view.now) + ".";
		}
		else if (!view.running)
		{
			title = route.taskCount() + (route.taskCount() == 1 ? " chore, " : " chores, ") + route.getStops().size()
				+ (route.getStops().size() == 1 ? " stop" : " stops");
			detail = "About " + Format.duration(route.getSeconds()) + " with what you own. Start the round and Rounds lights the way.";
		}
		else
		{
			title = step.getTitle();
			detail = step.getDetail();
		}
		body.add(Ui.wrapped(title, Theme.title(), Theme.GOLD_BRIGHT, width));
		body.add(Ui.wrapped(detail, Theme.small(), Theme.TEXT, width));
		if (!view.running && view.forecast != null)
		{
			body.add(Ui.wrapped(view.forecast, Theme.small(), Theme.GREEN, width));
		}
		if (view.running && !step.getItems().isEmpty())
		{
			body.add(iconStrip(step.getItems()));
		}
		card.add(body, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		buttons.setOpaque(false);
		if (view.running)
		{
			buttons.add(new CodexButton("Skip", false, actions::skip));
			buttons.add(spacer());
			buttons.add(new CodexButton("Replan", false, actions::replan));
			buttons.add(spacer());
			buttons.add(new CodexButton("End", false, actions::stop));
		}
		else
		{
			// Two buttons fit the card: the quick Fossil Island loop takes Replan's place when it is on offer.
			if (!route.isEmpty())
			{
				buttons.add(new CodexButton("Start round", true, actions::start));
			}
			if (quickReady())
			{
				if (buttons.getComponentCount() > 0)
				{
					buttons.add(spacer());
				}
				buttons.add(new CodexButton("Quick loop", false, actions::startQuick));
			}
			else if (!route.isEmpty())
			{
				buttons.add(spacer());
				buttons.add(new CodexButton("Replan", false, actions::replan));
			}
		}
		if (buttons.getComponentCount() > 0)
		{
			card.add(buttons, BorderLayout.SOUTH);
		}
		return card;
	}

	private static JComponent spacer()
	{
		JPanel s = new JPanel();
		s.setOpaque(false);
		s.setPreferredSize(new Dimension(5, 1));
		return s;
	}

	private JComponent iconStrip(List<Integer> items)
	{
		JPanel strip = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		strip.setOpaque(false);
		int shown = 0;
		for (int id : items)
		{
			if (shown++ >= 4)
			{
				break;
			}
			BufferedImage[] holder = new BufferedImage[1];
			JComponent well = new JComponent()
			{
				@Override
				protected void paintComponent(java.awt.Graphics graphics)
				{
					java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
					Theme.smooth(g);
					Well.paint(g, 0, 0, holder[0], Theme.alpha(Theme.GOLD, 120));
					g.dispose();
				}
			};
			holder[0] = icons.icon(id, well::repaint);
			well.setPreferredSize(new Dimension(Well.SIZE + 5, Well.SIZE + 1));
			strip.add(well);
		}
		return strip;
	}

	/** Whether a bird house or seaweed patch is ready, so a quick Fossil Island loop is worth offering. */
	private boolean quickReady()
	{
		if (view.board == null)
		{
			return false;
		}
		for (Board.Entry e : view.board.getEntries())
		{
			if (e.getStatus() == Status.READY && e.getSpot() != null && Settings.QUICK.contains(e.getSpot().getGroup()))
			{
				return true;
			}
		}
		return false;
	}

	private Row pullRow(Need n)
	{
		int carried = view.carrying(n.getItems());
		int banked = view.banked(n.getItems());
		String badge;
		Color color;
		if (carried >= n.getQuantity())
		{
			badge = "Packed";
			color = Theme.GREEN;
		}
		else if (Leprechaun.holds(n) && view.leprechaunFirst && carried + view.stored(n.getItems()) >= n.getQuantity())
		{
			badge = "Leprechaun";
			color = Theme.GREEN;
		}
		else if (carried + banked >= n.getQuantity())
		{
			badge = "In bank";
			color = Theme.GOLD;
		}
		else if (n.isOptional())
		{
			badge = "Optional";
			color = Theme.TEXT_FAINT;
		}
		else if (!view.bankKnown)
		{
			badge = "Check";
			color = Theme.TEXT_MUTED;
		}
		else
		{
			badge = "Short";
			color = Theme.BLOOD;
		}
		String title = (n.getQuantity() > 1 ? Format.stack(n.getQuantity()) + " x " : "") + n.getLabel();
		return new Row(icons, n.getItemId(), title, n.getWhy(), badge, color, false);
	}

	private StackPanel buildBoard()
	{
		StackPanel list = new StackPanel(5, 8);
		if (!view.known)
		{
			list.add(note("Log in to see every patch, bird house and daily."));
			return list;
		}
		Board b = view.board;
		for (Spot.Group group : Spot.Group.values())
		{
			List<Board.Entry> rows = new ArrayList<>();
			for (Board.Entry e : b.getEntries())
			{
				if (e.getType() == Board.Type.SPOT && e.getSpot().getGroup() == group)
				{
					rows.add(e);
				}
			}
			addGroup(list, group.getLabel(), rows);
		}
		List<Board.Entry> dailies = new ArrayList<>();
		Board.Entry kingdom = null;
		for (Board.Entry e : b.getEntries())
		{
			if (e.getType() == Board.Type.DAILY)
			{
				dailies.add(e);
			}
			else if (e.getType() == Board.Type.KINGDOM)
			{
				kingdom = e;
			}
		}
		if (dailies.isEmpty())
		{
			list.add(new SectionHeader("Dailies"));
			list.add(note("Dailies show once you log in."));
		}
		else
		{
			addGroup(list, "Dailies", dailies);
		}
		list.add(new SectionHeader("Kingdom of Miscellania"));
		list.add(kingdomCard(b.getKingdom(), kingdom));
		return list;
	}

	private void addGroup(StackPanel list, String label, List<Board.Entry> rows)
	{
		if (rows.isEmpty())
		{
			return;
		}
		long ready = rows.stream().filter(e -> e.getStatus() == Status.READY).count();
		list.add(new SectionHeader(label, ready > 0 ? ready + " ready" : null));
		rows.sort(Comparator.comparingInt((Board.Entry e) -> order(e.getStatus())).thenComparingLong(Board.Entry::getReadyAt));
		for (Board.Entry e : rows)
		{
			list.add(boardRow(e));
		}
	}

	private static int order(Status s)
	{
		switch (s)
		{
			case READY:
				return 0;
			case GROWING:
				return 1;
			case UNSEEN:
				return 2;
			case DONE:
				return 3;
			case LOCKED:
				return 4;
			default:
				return 5;
		}
	}

	private Row boardRow(Board.Entry e)
	{
		String badge;
		switch (e.getStatus())
		{
			case READY:
				badge = "Ready";
				break;
			case GROWING:
				badge = e.getReadyAt() > view.now ? Format.shortDuration(e.getReadyAt() - view.now) : "Soon";
				break;
			case UNSEEN:
				badge = "Unseen";
				break;
			case DONE:
				badge = e.getType() == Board.Type.DAILY ? "Claimed" : "Done";
				break;
			case LOCKED:
				badge = "Locked";
				break;
			default:
				badge = "Off";
				break;
		}
		String title = e.getType() == Board.Type.SPOT ? e.getSpot().getLabel() : e.getTitle();
		String sub = e.getDetail();
		if (e.getType() == Board.Type.SPOT && e.getReading() != null && e.getReading().getProduce() != null)
		{
			sub = e.getReading().getProduce().getName() + ": " + sub.substring(0, 1).toLowerCase() + sub.substring(1);
		}
		boolean dim = e.getStatus() == Status.OFF || e.getStatus() == Status.LOCKED || e.getStatus() == Status.DONE;
		return new Row(icons, e.getItemId(), title, sub, badge, Theme.status(e.getStatus()), dim);
	}

	private JComponent kingdomCard(Kingdom k, Board.Entry entry)
	{
		Card card = new Card(new BorderLayout());
		StackPanel body = new StackPanel(4, 0);
		if (k == null)
		{
			body.add(Ui.wrapped("Visit Miscellania once after Throne of Miscellania and Rounds keeps watch on approval and the coffer.",
				Theme.small(), Theme.TEXT_MUTED, WIDTH - 40));
		}
		else
		{
			int pct = k.approvalPercent();
			body.add(new Meter("Approval", pct + "%", pct / 100.0, pct >= 90 ? Theme.GREEN : pct >= 60 ? Theme.GOLD : Theme.EMBER, null));
			double full = Kingdom.fullPayoutCoffer(k.isRoyalTrouble());
			body.add(new Meter("Coffer", Format.stack(k.getCoffer()), k.getCoffer() / full,
				k.getCoffer() >= full ? Theme.GREEN : Theme.GOLD, null));
			String state = entry == null ? "" : entry.getStatus() == Status.READY ? "Due a visit" : entry.getStatus() == Status.OFF ? "Left out of rounds" : "Looked after";
			int days = k.daysUntilApprovalBelow(60);
			String foot = state + (k.isEstimated() ? ", estimated" : "") + (days > 0 ? ". Approval stays above 60% for " + days + (days == 1 ? " day" : " days") : "");
			body.add(Ui.wrapped(foot, Theme.small(), Theme.TEXT_FAINT, WIDTH - 40));
		}
		card.add(body, BorderLayout.CENTER);
		return card;
	}

	private static JComponent note(String text)
	{
		Card card = new Card(new BorderLayout());
		card.add(Ui.wrapped(text, Theme.small(), Theme.TEXT_MUTED, WIDTH - 40), BorderLayout.CENTER);
		return card;
	}

	private static JComponent sub(String text)
	{
		JPanel p = new JPanel(new BorderLayout());
		p.setOpaque(false);
		p.setBorder(new EmptyBorder(2, 2, 0, 0));
		p.add(Ui.label(text, Theme.small(), Theme.TEXT_FAINT), BorderLayout.CENTER);
		return p;
	}
}
