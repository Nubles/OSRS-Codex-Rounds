package com.codex.rounds.ui;

import com.codex.rounds.core.Board;
import com.codex.rounds.core.Format;
import com.codex.rounds.core.Herbs;
import com.codex.rounds.core.Kingdom;
import com.codex.rounds.core.Leprechaun;
import com.codex.rounds.core.Need;
import com.codex.rounds.core.Route;
import com.codex.rounds.core.Settings;
import com.codex.rounds.core.Spot;
import com.codex.rounds.core.Status;
import com.codex.rounds.core.Step;
import com.codex.rounds.core.Supplies;
import com.codex.rounds.core.Stop;
import com.codex.rounds.core.Sulliuscep;
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
		boolean cepHere = view.cep != null && (view.cep.running || view.inSwamp);

		if (cepHere && !view.running)
		{
			addCep(list);
			list.space(2);
		}
		list.add(new SectionHeader(view.running ? "Next step" : "Your round", view.running && step.getTotal() > 0 ? step.getDone() + "/" + step.getTotal() : null));
		list.add(stepCard(route, step));
		if (cepHere && view.running)
		{
			list.space(2);
			addCep(list);
		}

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
			if (route.tripCount() > 1)
			{
				list.add(note("Too much for one pack, so this round banks " + (route.tripCount() - 1)
					+ (route.tripCount() == 2 ? " time" : " times") + " on the way. Each trip's pull list is below."));
				for (int t = 0; t < route.tripCount(); t++)
				{
					int first = route.tripStart(t) + 1;
					int last = t + 1 < route.tripCount() ? route.tripStart(t + 1) : route.getStops().size();
					list.add(sub("Trip " + (t + 1) + " (stops " + first + (last > first ? " to " + last : "") + ")"));
					for (Need n : route.getTripPulls(t))
					{
						list.add(pullRow(n));
					}
				}
			}
			else
			{
				if (view.slots > Supplies.PACK_SLOTS)
				{
					list.add(note("This takes about " + view.slots + " slots, more than a full pack. Take what the first stops need, "
						+ "then stop at a bank on the way, or turn some chores off for this round."));
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
		else if (!view.ironman && (n.getKind() == Need.Kind.PLANT || n.getKind() == Need.Kind.PAYMENT))
		{
			// Seeds, saplings and payments the account is short of: a main can pick them up at the Grand Exchange.
			badge = "Buy";
			color = Theme.BLOOD;
		}
		else
		{
			badge = "Short";
			color = Theme.BLOOD;
		}
		String title = (n.getQuantity() > 1 ? Format.stack(n.getQuantity()) + " x " : "") + n.getLabel();
		String why = "Buy".equals(badge) ? n.getWhy() + " · short " + Format.number(n.getQuantity() - carried - banked) + ", buy on the GE" : n.getWhy();
		return new Row(icons, n.getItemId(), title, why, badge, color, false);
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
			if (group == Spot.Group.HERBS && view.herbs != null)
			{
				addHerbLedger(list, view.herbs);
			}
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
			list.add(new SectionHeader("Dailies and weeklies"));
			list.add(note("Dailies and Tears of Guthix show once you log in."));
		}
		else
		{
			addGroup(list, "Dailies and weeklies", dailies);
		}
		list.add(new SectionHeader("Kingdom of Miscellania"));
		list.add(kingdomCard(b.getKingdom(), kingdom));
		if (view.cep != null)
		{
			Sulliuscep.Shown c = view.cep;
			list.add(new SectionHeader("Sulliuscep runs", c.best != null ? "best " + Format.clock(c.best.seconds) : null));
			list.add(cepRecordCard(c));
		}
		return list;
	}

	/** What your own herb patches give you: yield against the wiki, the last run, the herb that pays best and each patch's record. */
	private void addHerbLedger(StackPanel list, Herbs.Shown h)
	{
		int width = WIDTH - 40;
		list.add(new SectionHeader("Herb ledger", h.harvests > 0 ? h.harvests + (h.harvests == 1 ? " harvest" : " harvests") : null));
		Card card = new Card(new BorderLayout()).accent(Theme.GREEN);
		StackPanel body = new StackPanel(4, 0);
		if (h.harvests == 0)
		{
			body.add(Ui.wrapped("Pick a herb patch and Rounds counts what it gives, with its compost, against the wiki's expected yield for your level "
				+ "and boosts. Your own yield and profit build up here, one harvest at a time.", Theme.small(), Theme.TEXT_MUTED, width));
		}
		else
		{
			double mean = 0;
			int n = 0;
			for (Herbs.HerbStat st : h.herbs)
			{
				mean += st.mean * st.harvests;
				n += st.harvests;
			}
			mean = n == 0 ? 0 : mean / n;
			body.add(Ui.wrapped(Herbs.one(mean) + " herbs a patch", Theme.title(), Theme.GOLD_BRIGHT, width));
			if (h.luck != null)
			{
				double own = h.luck;
				Color c = own >= 1 ? Theme.GREEN : Theme.EMBER;
				double top = Math.max(own, 1) * 1.25;
				body.add(new Meter("Yours against the wiki", Math.round((own - 1) * 100) >= 0 ? "+" + Math.round((own - 1) * 100) + "%" : Math.round((own - 1) * 100) + "%",
					own / top, c, 1 / top));
				body.add(Ui.wrapped(Character.toUpperCase(Herbs.luckText(own).charAt(0)) + Herbs.luckText(own).substring(1) + ", over "
					+ h.compared + (h.compared == 1 ? " harvest" : " harvests") + " whose compost Rounds saw." + (h.compared < 10 ? " Herb yields swing a lot, so this settles with more." : ""),
					Theme.small(), Theme.TEXT_MUTED, width));
			}
			if (h.last != null)
			{
				int k = h.last.crops.size();
				String died = h.last.died() > 0 ? " " + h.last.died() + " died." : "";
				body.add(Ui.wrapped("Last run: " + k + (k == 1 ? " patch, " : " patches, ") + h.last.herbs() + " herbs, " + Herbs.signedCompact(h.lastProfit)
					+ " at today's prices after seeds and compost." + died, Theme.small(), Theme.TEXT, width));
			}
			if (h.deathRate != null)
			{
				body.add(Ui.wrapped("Lost to disease: " + Math.round(h.deathRate * 100) + "% of your crops. Troll Stronghold, Weiss and Harmony Island never catch it.",
					Theme.small(), Theme.TEXT_FAINT, width));
			}
		}
		card.add(body, BorderLayout.CENTER);
		list.add(card);

		if (!h.picks.isEmpty())
		{
			list.add(new SectionHeader("Pays best now", "a patch"));
			for (int i = 0; i < Math.min(3, h.picks.size()); i++)
			{
				Herbs.Pick p = h.picks.get(i);
				String sub = Herbs.one(p.yield) + (p.own ? " yours" : " wiki") + " · " + Herbs.compact(p.herbPrice) + " ea";
				list.add(new Row(icons, p.herb.getGrimyId(), p.herb.getName(), sub, Herbs.signedCompact(p.value), p.value >= 0 ? Theme.GREEN : Theme.BLOOD, false));
			}
			list.add(note("Expected coins a patch after the seed and " + (h.usual == null || h.usual == Herbs.Compost.NONE ? "no compost" : h.usual.getLabel())
				+ " at today's prices, with " + (h.deathRate != null ? "your own" : "the wiki's") + " chance of losing the crop."));
		}

		if (!h.herbs.isEmpty())
		{
			list.add(new SectionHeader("Your herbs"));
			for (Herbs.HerbStat st : h.herbs)
			{
				String sub = st.harvests + (st.harvests == 1 ? " patch" : " patches") + (st.compared > 0 ? " · wiki " + Herbs.one(st.expectedMean) : "");
				String badge = null;
				Color color = Theme.TEXT_MUTED;
				if (st.compared > 0 && st.expectedMean > 0)
				{
					long pct = Math.round((st.comparedMean / st.expectedMean - 1) * 100);
					badge = (pct >= 0 ? "+" : "") + pct + "%";
					color = pct >= 0 ? Theme.GREEN : Theme.EMBER;
				}
				list.add(new Row(icons, st.herb.getGrimyId(), st.herb.getName() + " · " + Herbs.one(st.mean), sub, badge, color, false));
			}
		}

		if (!h.patches.isEmpty())
		{
			list.add(new SectionHeader("Your patches"));
			for (Herbs.PatchStat ps : h.patches)
			{
				String sub = ps.crops + (ps.crops == 1 ? " crop" : " crops") + (ps.harvests > 0 ? " · " + Herbs.one(ps.mean()) + " each" : "")
					+ (ps.diseased > 0 ? " · " + ps.diseased + " sick" : "");
				String badge;
				Color color;
				if (ps.died > 0)
				{
					badge = ps.died + " died";
					color = Theme.BLOOD;
				}
				else if (ps.diseaseFree)
				{
					badge = "Disease-free";
					color = Theme.TEAL;
				}
				else
				{
					badge = "None lost";
					color = Theme.GREEN;
				}
				list.add(new Row(icons, -1, ps.spot.getLabel(), sub, badge, color, false));
			}
		}
	}

	/** The sulliuscep run under way: the six caps, the one to cut, the clock against your best and the haul. */
	private void addCep(StackPanel list)
	{
		Sulliuscep.Shown c = view.cep;
		list.add(new SectionHeader("Sulliuscep run", c.fell.size() + "/" + Sulliuscep.CAPS));
		Card card = new Card(new BorderLayout()).accent(Theme.GREEN).highlighted(c.running);
		StackPanel body = new StackPanel(4, 0);
		int width = WIDTH - 40;
		body.add(new CepTrail(c.order, c.fell, c.active));
		body.add(Ui.wrapped(c.title, Theme.title(), Theme.GOLD_BRIGHT, width));
		body.add(Ui.wrapped(c.detail, Theme.small(), Theme.TEXT, width));
		if (c.running)
		{
			Double mark = c.best == null ? null : c.ghostFell / (double) Sulliuscep.CAPS;
			Color pace = c.lastDiff == null ? Theme.GOLD : c.lastDiff <= 0 ? Theme.GREEN : Theme.EMBER;
			body.add(new Meter("Run " + Format.clock(c.elapsed), c.best == null ? "first full run" : "best " + Format.clock(c.best.seconds),
				c.fell.size() / (double) Sulliuscep.CAPS, pace, mark));
			String ghost = Sulliuscep.ghostText(c);
			if (ghost != null)
			{
				body.add(Ui.wrapped(ghost, Theme.small(), pace, width));
			}
			String haul = Sulliuscep.haulText(c.haul);
			body.add(Ui.wrapped(haul == null ? "Nothing in the pack from this run yet." : "This run: " + haul + ".", Theme.small(), Theme.TEXT_MUTED, width));
			if (c.xp > 0)
			{
				body.add(Ui.wrapped(Format.number(c.xp) + " Woodcutting xp this run" + rateText(c, "; "), Theme.small(), Theme.TEXT_MUTED, width));
			}
		}
		else
		{
			body.add(Ui.wrapped(c.last != null ? "Your run starts with the first chop. Last run: " + lastText(c.last) + "."
				: "Your run starts with the first chop. Rounds times it and counts what it gives.", Theme.small(), Theme.TEXT_MUTED, width));
		}
		if (!c.orderLearned)
		{
			body.add(Ui.wrapped("Caps go by the game's own numbering until Rounds has seen you cut a whole lap.", Theme.small(), Theme.TEXT_FAINT, width));
		}
		card.add(body, BorderLayout.CENTER);
		list.add(card);
	}

	private JComponent cepRecordCard(Sulliuscep.Shown c)
	{
		Card card = new Card(new BorderLayout()).accent(Theme.GREEN);
		StackPanel body = new StackPanel(4, 0);
		int width = WIDTH - 40;
		if (c.last == null)
		{
			body.add(Ui.wrapped("Chop a sulliuscep in Fossil Island's Tar Swamp (65 Woodcutting, after Bone Voyage) and Rounds times the run, "
				+ "names the next cap and keeps your best.", Theme.small(), Theme.TEXT_MUTED, width));
		}
		else
		{
			if (c.xpHour > 0)
			{
				body.add(Ui.wrapped(Format.number(c.xpHour) + " xp/hr", Theme.title(), Theme.GOLD_BRIGHT, width));
				body.add(Ui.wrapped("Woodcutting, from your last " + (c.rateRuns == 1 ? "run" : c.rateRuns + " runs") + ".", Theme.small(), Theme.TEXT_MUTED, width));
			}
			if (c.best != null)
			{
				body.add(new Meter("Fastest lap", Format.clock(c.best.seconds), 1, Theme.GREEN, null));
			}
			String per = Sulliuscep.haulText(c.perRun);
			if (per != null)
			{
				body.add(Ui.wrapped("A full run brings about " + per + ".", Theme.small(), Theme.TEXT, width));
			}
			body.add(Ui.wrapped("Last run: " + lastText(c.last) + ".", Theme.small(), Theme.TEXT_FAINT, width));
		}
		card.add(body, BorderLayout.CENTER);
		return card;
	}

	private static String rateText(Sulliuscep.Shown c, String sep)
	{
		return c.xpHour > 0 ? sep + Format.number(c.xpHour) + " an hour over your runs." : ".";
	}

	private static String lastText(Sulliuscep.RunRecord r)
	{
		return Format.clock(r.seconds) + ", " + r.fell.size() + (r.fell.size() == 1 ? " cap down" : " caps down") + (r.complete ? "" : " (left early)");
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
				if (e.getType() == Board.Type.DAILY && !e.getDaily().resetsDaily() && e.getDaily().isRemembered())
				{
					// Weekly: count down to Juna's day, or say what she is waiting for.
					badge = e.getReadyAt() > view.now ? Format.shortDuration(e.getReadyAt() - view.now) : "Waiting";
				}
				else
				{
					badge = e.getType() == Board.Type.DAILY ? "Claimed" : "Done";
				}
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
