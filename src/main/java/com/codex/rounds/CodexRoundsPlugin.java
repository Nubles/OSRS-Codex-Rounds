package com.codex.rounds;

import com.codex.rounds.core.Ability;
import com.codex.rounds.core.BankTab;
import com.codex.rounds.core.Board;
import com.codex.rounds.core.Daily;
import com.codex.rounds.core.DailyState;
import com.codex.rounds.core.Format;
import com.codex.rounds.core.Growth;
import com.codex.rounds.core.Guide;
import com.codex.rounds.core.Kingdom;
import com.codex.rounds.core.Leprechaun;
import com.codex.rounds.core.Memory;
import com.codex.rounds.core.Planner;
import com.codex.rounds.core.Point;
import com.codex.rounds.core.Progress;
import com.codex.rounds.core.Reading;
import com.codex.rounds.core.Route;
import com.codex.rounds.core.Seen;
import com.codex.rounds.core.Settings;
import com.codex.rounds.core.Spot;
import com.codex.rounds.core.Status;
import com.codex.rounds.core.Step;
import com.codex.rounds.core.TravelTimer;
import com.codex.rounds.core.Stop;
import com.codex.rounds.core.Task;
import com.codex.rounds.core.Teleports;
import com.codex.rounds.core.Vars;
import com.codex.rounds.core.farming.CropState;
import com.codex.rounds.core.farming.PatchState;
import com.codex.rounds.core.farming.Produce;
import com.codex.rounds.overlay.ItemHighlight;
import com.codex.rounds.overlay.StepPlaque;
import com.codex.rounds.overlay.TargetOverlay;
import com.codex.rounds.ui.Actions;
import com.codex.rounds.ui.ItemIcons;
import com.codex.rounds.ui.RoundsPanel;
import com.codex.rounds.ui.Sigil;
import com.codex.rounds.ui.View;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.inject.Provides;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Scene;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.banktags.BankTagsPlugin;
import net.runelite.client.plugins.banktags.BankTagsService;
import net.runelite.client.plugins.banktags.TagManager;
import net.runelite.client.plugins.banktags.tabs.Layout;
import net.runelite.client.plugins.banktags.tabs.LayoutManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.AsyncBufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
	name = "Codex Rounds",
	description = "Your bird houses, seaweed, special trees and patches, diary dailies and Kingdom of Miscellania in one guided round",
	tags = {"farming", "birdhouse", "seaweed", "hardwood", "calquat", "spirit tree", "daily", "miscellania", "kingdom", "route", "codex"}
)
@PluginDependency(BankTagsPlugin.class)
public class CodexRoundsPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(CodexRoundsPlugin.class);
	private static final String MEMORY_KEY = "memory";
	private static final String TIME_TRACKING = "timetracking";
	private static final String KINGDOM = "kingdomofmiscellania";
	private static final int SAVE_TICKS = 50;
	private static final int ACCOUNT_TICKS = 5;
	private static final int QUEST_TICKS = 100;
	private static final int PUBLISH_TICKS = 10;
	private static final int SCAN_RADIUS = 10;
	/** The bank tab holding the round's pull list. */
	static final String BANK_TAG = "codex rounds";

	private static final Pattern COMPOST = Pattern.compile(
		"(You treat the .+ with (ultra|super|)compost\\.)|(The .+ has been treated with (ultra|super|)compost.*)"
			+ "|(This .+ has already been (treated|fertilised) with (ultra|super|)compost.*)");
	private static final String[] PAID = {
		"Leave it with me", "look after that nursery", "protects your tree for you",
	};

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private Notifier notifier;

	@Inject
	private Gson gson;

	@Inject
	private CodexRoundsConfig config;

	@Inject
	private BankTagsService bankTags;

	@Inject
	private TagManager tagManager;

	@Inject
	private LayoutManager layoutManager;

	@Inject
	private TargetOverlay targetOverlay;

	@Inject
	private ItemHighlight itemHighlight;

	private final StepPlaque plaque = new StepPlaque();
	private final Progress progress = new Progress();
	private final TravelTimer timer = new TravelTimer();

	private RoundsPanel panel;
	private NavigationButton navButton;

	private String profile;
	private Memory memory;
	private Settings settings = Settings.defaults();
	private Growth.Offset offset = Growth.Offset.NONE;
	private Board board;
	private Route route = Route.EMPTY;
	private Step step = Step.IDLE;
	private Map<Integer, Integer> carried = Collections.emptyMap();
	private volatile Set<Integer> tabItems = Collections.emptySet();
	private boolean tabOpened;
	private boolean tabWanted;
	private boolean dirty;
	private boolean unsaved;
	private boolean replan = true;
	private int ticks;
	private String published;
	private boolean notified;
	private WorldMapPoint mapPoint;
	private String arrowKey;

	@Provides
	CodexRoundsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CodexRoundsConfig.class);
	}

	@Override
	protected void startUp()
	{
		ItemIcons icons = (itemId, onLoaded) ->
		{
			AsyncBufferedImage image = itemManager.getImage(itemId);
			image.onLoaded(onLoaded);
			return image;
		};
		panel = new RoundsPanel(icons, new PanelActions());
		navButton = NavigationButton.builder()
			.tooltip("Codex Rounds")
			.icon(Sigil.icon(16))
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		overlayManager.add(plaque);
		overlayManager.add(targetOverlay);
		overlayManager.add(itemHighlight);
		tagManager.registerTag(BANK_TAG, id -> tabItems.contains(id));
		settings = readSettings();
		clientThread.invokeLater(() ->
		{
			loadProfile();
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				carried = readCarried();
			}
			refresh(true);
		});
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(plaque);
		overlayManager.remove(targetOverlay);
		overlayManager.remove(itemHighlight);
		clientToolbar.removeNavigation(navButton);
		clientThread.invokeLater(() ->
		{
			if (BANK_TAG.equals(bankTags.getActiveTag()))
			{
				bankTags.closeBankTag();
			}
			tagManager.unregisterTag(BANK_TAG);
			layoutManager.removeLayout(BANK_TAG);
			save();
			clearGuidance();
			memory = null;
			profile = null;
			progress.stop();
			timer.reset();
		});
		panel = null;
	}

	// ---- Events ----

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != InterfaceID.BANKMAIN)
		{
			return;
		}
		tabOpened = false;
		// Opening the bank mid-way through gathering supplies goes straight to the round's tab.
		if (tabWanted || config.bankTab() && progress.isStarted() && step.getKind() == Step.Kind.SUPPLIES && step.getTarget() == null)
		{
			tabWanted = false;
			clientThread.invokeLater(this::openBankTab);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			save();
			clearGuidance();
		}
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			ticks = 0;
			replan = true;
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		save();
		progress.stop();
		timer.reset();
		loadProfile();
		replan = true;
		refresh(true);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!CodexRoundsConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}
		settings = readSettings();
		replan = true;
		clientThread.invokeLater(() -> refresh(true));
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int id = event.getContainerId();
		if (memory == null)
		{
			return;
		}
		if (id == InventoryID.BANK)
		{
			memory.bank = contents(event.getItemContainer());
			memory.bankTime = Instant.now().getEpochSecond();
			unsaved = true;
			dirty = true;
		}
		else if (id == InventoryID.INV || id == InventoryID.WORN)
		{
			carried = readCarried();
			memory.carried = new HashMap<>(carried);
			dirty = true;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}
		String msg = event.getMessage();
		if (msg.contains("compost") && COMPOST.matcher(msg).matches())
		{
			Spot spot = spotBeside();
			if (spot != null)
			{
				progress.composted(spot, Instant.now().getEpochSecond());
				dirty = true;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (memory == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		Player me = client.getLocalPlayer();
		if (me == null)
		{
			return;
		}
		long now = Instant.now().getEpochSecond();
		WorldPoint wp = me.getWorldLocation();
		Point loc = Point.of(wp.getX(), wp.getY(), wp.getPlane());

		readSpots(loc, now);
		if (ticks % ACCOUNT_TICKS == 0)
		{
			readAccount(now, ticks % QUEST_TICKS == 0);
		}
		readPayment();
		ticks++;

		refresh(false);

		if (unsaved && ticks % SAVE_TICKS == 0)
		{
			save();
		}
	}

	// ---- Reading the game ----

	private void readSpots(Point loc, long now)
	{
		if (client.isMenuOpen())
		{
			return;
		}
		int removals = 0;
		Map<Spot, Integer> houses = new EnumMap<>(Spot.class);
		for (Spot spot : Spot.values())
		{
			if (!spot.readableFrom(loc))
			{
				continue;
			}
			if (spot.isBirdhouse())
			{
				int v = client.getVarpValue(spot.getVarp());
				Seen old = memory.seen(spot);
				if (old == null || old.getValue() != v)
				{
					houses.put(spot, v);
					if (v <= 0 && old != null && old.getValue() > 0)
					{
						removals++;
					}
				}
				continue;
			}
			int v = client.getVarbitValue(spot.getVarbit());
			PatchState now0 = spot.getKind().forVarbitValue(v);
			if (now0 == null)
			{
				continue;
			}
			Seen old = memory.seen(spot);
			if (old == null || old.getValue() != v)
			{
				PatchState before = old == null ? null : spot.getKind().forVarbitValue(old.getValue());
				memory.put(spot, new Seen(v, now));
				if ((before == null || before.isEmpty()) && !now0.isEmpty() && now0.getCropState() == CropState.GROWING && now0.getStage() == 0)
				{
					progress.planted(spot, now);
				}
				dirty = true;
				unsaved = true;
			}
			else if (old.getTime() + 300 < now)
			{
				// Same state still: refresh the time so predictions run from the latest look.
				memory.put(spot, new Seen(v, now));
				unsaved = true;
			}
		}
		// The bird house varps can read zero for a moment after arriving; four at once dropping to
		// nothing is not something a player can do (RuneLite's bird house tracker uses the same guard).
		if (removals <= 2 && !houses.isEmpty())
		{
			houses.forEach((s, v) -> memory.put(s, new Seen(v, now)));
			dirty = true;
			unsaved = true;
		}
	}

	private final Vars vars = new Vars()
	{
		@Override
		public int varbit(int id)
		{
			return client.getVarbitValue(id);
		}

		@Override
		public int varp(int id)
		{
			return client.getVarpValue(id);
		}
	};

	private void readAccount(long now, boolean quests)
	{
		int farming = client.getRealSkillLevel(Skill.FARMING);
		int hunter = client.getRealSkillLevel(Skill.HUNTER);
		int crafting = client.getRealSkillLevel(Skill.CRAFTING);
		int magic = client.getRealSkillLevel(Skill.MAGIC);
		int book = client.getVarbitValue(VarbitID.SPELLBOOK);
		if (farming != memory.farming || hunter != memory.hunter || crafting != memory.crafting || magic != memory.magic || book != memory.spellbook)
		{
			memory.farming = farming;
			memory.hunter = hunter;
			memory.crafting = crafting;
			memory.magic = magic;
			memory.spellbook = book;
			dirty = true;
			unsaved = true;
		}

		Map<Integer, Integer> stored = Leprechaun.read(vars);
		if (!stored.equals(memory.stored))
		{
			memory.stored = stored;
			dirty = true;
			unsaved = true;
		}

		for (Daily d : Daily.values())
		{
			DailyState st = DailyState.of(d, vars);
			Memory.DailySnap old = memory.dailies == null ? null : memory.dailies.get(d.name());
			if (old == null || old.claimable != st.isClaimable() || old.unlocked != st.isUnlocked() || now / 86400 != old.seenAt / 86400
				|| !st.getReward().equals(old.reward))
			{
				memory.snapDaily(st, now);
				dirty = true;
				unsaved = true;
			}
		}

		boolean quest = client.getVarpValue(VarPlayerID.MISC_QUEST) > 0;
		if (quest != memory.kingdomQuest)
		{
			memory.kingdomQuest = quest;
			dirty = true;
			unsaved = true;
		}
		if (quest)
		{
			int coffer = client.getVarbitValue(VarbitID.MISC_COFFERS);
			int approval = client.getVarbitValue(VarbitID.MISC_APPROVAL);
			boolean inKingdom = false;
			Player me = client.getLocalPlayer();
			if (me != null)
			{
				int region = me.getWorldLocation().getRegionID();
				for (int r : Kingdom.REGIONS)
				{
					inKingdom |= r == region;
				}
			}
			if ((inKingdom || coffer > 0 && approval > 0)
				&& (memory.kingdomCoffer == null || memory.kingdomCoffer != coffer || memory.kingdomApproval == null || memory.kingdomApproval != approval))
			{
				memory.kingdomCoffer = coffer;
				memory.kingdomApproval = approval;
				memory.kingdomTime = now;
				dirty = true;
				unsaved = true;
			}
		}

		if (quests)
		{
			boolean rt = Quest.ROYAL_TROUBLE.getState(client) == QuestState.FINISHED;
			boolean fairy = Quest.FAIRYTALE_II__CURE_A_QUEEN.getState(client) != QuestState.NOT_STARTED
				&& (client.getVarbitValue(VarbitID.LUMBRIDGE_DIARY_ELITE_COMPLETE) == 1 || memory.ability().owns(Teleports.FAIRY_STAFFS));
			if (rt != memory.royalTrouble || fairy != memory.fairyRings)
			{
				memory.royalTrouble = rt;
				memory.fairyRings = fairy;
				dirty = true;
				unsaved = true;
			}
		}
	}

	/** Watches the gardener's chat box for the line that confirms a crop is protected. */
	private void readPayment()
	{
		if (!progress.isStarted() || step.getTask() == null || step.getTask().getSpot() == null || !step.getTask().isProtect())
		{
			return;
		}
		Widget text = client.getWidget(InterfaceID.ChatLeft.TEXT);
		if (text == null || text.isHidden() || text.getText() == null)
		{
			return;
		}
		String t = text.getText();
		for (String p : PAID)
		{
			if (t.contains(p))
			{
				Spot spot = step.getTask().getSpot();
				if (!progress.isPaid(spot))
				{
					progress.paid(spot, Instant.now().getEpochSecond());
					dirty = true;
				}
				return;
			}
		}
	}

	/** The spot the player is standing beside: the current step's if close, else any close one. */
	private Spot spotBeside()
	{
		Player me = client.getLocalPlayer();
		if (me == null)
		{
			return null;
		}
		WorldPoint wp = me.getWorldLocation();
		Point p = Point.of(wp.getX(), wp.getY(), wp.getPlane());
		if (step.getTask() != null && step.getTask().getSpot() != null && step.getTask().getSpot().getPoint().distance(p) <= 4)
		{
			return step.getTask().getSpot();
		}
		Spot best = null;
		int bestD = 5;
		for (Spot s : Spot.values())
		{
			if (!s.isBirdhouse() && s.getPoint().sameMap(p) && s.getPoint().distance(p) < bestD)
			{
				best = s;
				bestD = s.getPoint().distance(p);
			}
		}
		return best;
	}

	private Map<Integer, Integer> readCarried()
	{
		Map<Integer, Integer> out = new HashMap<>();
		for (int id : new int[]{InventoryID.INV, InventoryID.WORN})
		{
			ItemContainer c = client.getItemContainer(id);
			if (c != null)
			{
				contents(c).forEach((k, v) -> out.merge(k, v, Integer::sum));
			}
		}
		return out;
	}

	private Map<Integer, Integer> contents(ItemContainer c)
	{
		Map<Integer, Integer> out = new HashMap<>();
		if (c == null)
		{
			return out;
		}
		for (Item item : c.getItems())
		{
			if (item.getId() >= 0 && item.getQuantity() > 0)
			{
				out.merge(item.getId(), item.getQuantity(), Integer::sum);
			}
		}
		return out;
	}

	// ---- Planning and guiding ----

	private Map<Spot, Reading> readings(long now)
	{
		Map<Spot, Reading> out = new EnumMap<>(Spot.class);
		Map<Spot, Seen> seen = memory.seenMap();
		for (Spot s : Spot.values())
		{
			out.put(s, Growth.read(s, seen.get(s), now, offset));
		}
		return out;
	}

	private List<DailyState> dailies(long now)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			List<DailyState> out = new ArrayList<>();
			for (Daily d : Daily.values())
			{
				out.add(DailyState.of(d, vars));
			}
			return out;
		}
		return memory.dailyStates(now);
	}

	/** Rebuilds the board, the round and the step, then pushes them to the panel and overlays. */
	private void refresh(boolean force)
	{
		if (memory == null)
		{
			publish(force);
			return;
		}
		long now = Instant.now().getEpochSecond();
		Ability ability = memory.ability();
		List<DailyState> dailies = dailies(now);
		Kingdom kingdom = memory.kingdom(now);
		if (force || dirty || ticks % ACCOUNT_TICKS == 0)
		{
			board = Board.build(memory.seenMap(), dailies, kingdom, settings, ability, now, offset);
			if (!progress.isStarted() && (replan || ticks % 50 == 0 || dirty))
			{
				route = Planner.plan(board, dailies, settings, ability, here(), now);
				replan = false;
			}
			else if (progress.isStarted() && replan)
			{
				route = Planner.plan(board, dailies, settings, ability, here(), now);
				replan = false;
			}
			dirty = false;
			notifyIfReady();
		}

		Map<Daily, DailyState> dailyMap = new EnumMap<>(Daily.class);
		for (DailyState d : dailies)
		{
			dailyMap.put(d.getDaily(), d);
		}
		Guide.World world = new Guide.World(here(), readings(now), dailyMap, kingdom, carried, ability, now);
		Step next = progress.isStarted() ? Guide.next(route, world, progress, settings) : Guide.next(route, world, new Progress(), settings);
		step = next;

		if (progress.isStarted())
		{
			if (!route.isEmpty() && route.getStops().get(0).getPlace().contains(here()))
			{
				progress.suppliesDone();
			}
			TravelTimer.Lap lap = timer.update(next, route, here(), ability, System.currentTimeMillis());
			if (lap != null)
			{
				memory.lap(lap.getKey(), lap.getSeconds());
				unsaved = true;
			}
			if (next.getKind() == Step.Kind.IDLE && next.getTotal() > 0 && next.getDone() >= next.getTotal())
			{
				finishRound(now);
			}
		}
		guide();
		publish(force);
	}

	private Point here()
	{
		Player me = client.getLocalPlayer();
		if (me == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return null;
		}
		WorldPoint wp = me.getWorldLocation();
		return Point.of(wp.getX(), wp.getY(), wp.getPlane());
	}

	/** Lays the pull list out as a bank tab and opens it; only while the bank is open. */
	private void openBankTab()
	{
		Widget bank = client.getWidget(InterfaceID.Bankmain.UNIVERSE);
		if (bank == null || bank.isHidden() || route.isEmpty() || tabOpened)
		{
			return;
		}
		tabOpened = true;
		// The tab is drawn by RuneLite's Bank Tags plugin, which can be switched off.
		if ("false".equals(configManager.getConfiguration(RuneLiteConfig.GROUP_NAME, "banktagsplugin")))
		{
			chat("turn on RuneLite's Bank Tags plugin to see the round as a bank tab.");
			return;
		}
		Map<Integer, Integer> held = memory == null || memory.bank == null ? Collections.emptyMap() : memory.bank;
		tabItems = BankTab.items(route.getPulls());
		layoutManager.saveLayout(new Layout(BANK_TAG, BankTab.layout(route.getPulls(), held)));
		bankTags.openBankTag(BANK_TAG, 0);
	}

	/** "Wait 14 min and 5 more join: giant seaweed and bird houses." */
	private String forecastText(long now)
	{
		if (board == null)
		{
			return null;
		}
		Board.Forecast f = board.forecast(now + settings.getLookaheadMinutes() * 60L);
		if (f == null)
		{
			return null;
		}
		List<String> g = f.getGroups();
		String what = g.size() == 1 ? g.get(0) : String.join(", ", g.subList(0, g.size() - 1)) + " and " + g.get(g.size() - 1);
		return (route.isEmpty() ? "Wait " + Format.duration(f.getWaitSeconds()) + " and " + f.getMore() + " come ready: "
			: "Wait " + Format.duration(f.getWaitSeconds()) + " and " + f.getMore() + " more join: ") + what + ".";
	}

	private void chat(String text)
	{
		String msg = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append("Codex Rounds: ")
			.append(ChatColorType.NORMAL)
			.append(text)
			.build();
		chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(msg).build());
	}

	private void finishRound(long now)
	{
		int seconds = (int) (now - progress.getStartedAt());
		int chores = route.taskCount();
		memory.record(new Memory.RoundRecord(now, seconds, chores, route.getStops().size()));
		unsaved = true;
		progress.stop();
		timer.reset();
		replan = true;
		if (config.chatSummary())
		{
			String msg = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append("Codex Rounds: ")
				.append(ChatColorType.NORMAL)
				.append("round complete in " + Format.clock(seconds) + ", " + chores + (chores == 1 ? " chore at " : " chores at ")
					+ route.getStops().size() + (route.getStops().size() == 1 ? " stop." : " stops."))
				.build();
			chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(msg).build());
		}
	}

	private void notifyIfReady()
	{
		if (board == null || progress.isStarted())
		{
			return;
		}
		long ready = board.count(Status.READY);
		if (ready >= config.notifyAt())
		{
			if (!notified && config.notifyReady() && client.getGameState() == GameState.LOGGED_IN && ticks > 10)
			{
				notifier.notify("Codex Rounds: " + ready + " chores are ready for a round.");
			}
			notified = true;
		}
		else
		{
			notified = false;
		}
	}

	/** Points the way: plaque, outline, hint arrow, item highlights and the map marker. */
	private void guide()
	{
		boolean on = progress.isStarted() && step.getKind() != Step.Kind.IDLE;
		if (!on)
		{
			clearGuidance();
			return;
		}
		String over;
		if (step.getKind() == Step.Kind.SUPPLIES)
		{
			over = "Before you set out";
		}
		else
		{
			Stop stop = route.getStops().get(Math.max(0, step.getStopIndex()));
			over = "Stop " + (step.getStopIndex() + 1) + " of " + route.getStops().size() + ": " + stop.getPlace().getLabel();
		}
		long now = Instant.now().getEpochSecond();
		plaque.show(config.plaque() ? new StepPlaque.Shown(over, step.getTitle(), step.getDetail(),
			Format.clock(now - progress.getStartedAt()), step.getDone(), step.getTotal()) : null);

		Set<Integer> items = new HashSet<>();
		for (int id : step.getItems())
		{
			items.add(id);
		}
		if (step.getKind() == Step.Kind.SUPPLIES)
		{
			items.addAll(step.getItems());
		}
		itemHighlight.setItems(items);

		resolveTarget();
		placeMapPoint();
	}

	private void clearGuidance()
	{
		plaque.show(null);
		itemHighlight.setItems(Collections.emptySet());
		targetOverlay.setResolved(null);
		if (arrowKey != null)
		{
			client.clearHintArrow();
			arrowKey = null;
		}
		if (mapPoint != null)
		{
			worldMapPointManager.remove(mapPoint);
			mapPoint = null;
		}
	}

	private void resolveTarget()
	{
		Step.Target target = step.getTarget();
		if (target == null || client.getLocalPlayer() == null)
		{
			targetOverlay.setResolved(null);
			setArrow(null, null, null);
			return;
		}
		WorldView wv = client.getTopLevelWorldView();
		WorldPoint at = new WorldPoint(target.getPoint().getX(), target.getPoint().getY(), target.getPoint().getPlane());
		List<TileObject> objects = new ArrayList<>();
		NPC npc = null;
		switch (target.getType())
		{
			case SPOT:
			case OBJECT:
				objects = findObjects(wv, at, target);
				break;
			case NPC:
				int best = Integer.MAX_VALUE;
				for (NPC n : wv.npcs())
				{
					if (n != null && target.matches(n.getId()))
					{
						int d = n.getWorldLocation().distanceTo2D(at);
						if (d < best)
						{
							best = d;
							npc = n;
						}
					}
				}
				break;
			default:
				break;
		}
		targetOverlay.setResolved(new TargetOverlay.Resolved(objects, npc, at, step.getTitle()));
		setArrow(npc, objects.isEmpty() ? at : objects.get(0).getWorldLocation(), npc != null ? "npc" + npc.getIndex() : "pt" + at);
	}

	private List<TileObject> findObjects(WorldView wv, WorldPoint at, Step.Target target)
	{
		List<TileObject> out = new ArrayList<>();
		LocalPoint lp = LocalPoint.fromWorld(wv, at);
		if (lp == null)
		{
			return out;
		}
		Scene scene = wv.getScene();
		Tile[][][] tiles = scene.getTiles();
		int plane = at.getPlane();
		if (plane < 0 || plane >= tiles.length)
		{
			return out;
		}
		Spot spot = target.getSpot();
		int sx = lp.getSceneX();
		int sy = lp.getSceneY();
		for (int x = Math.max(0, sx - SCAN_RADIUS); x <= Math.min(tiles[plane].length - 1, sx + SCAN_RADIUS); x++)
		{
			for (int y = Math.max(0, sy - SCAN_RADIUS); y <= Math.min(tiles[plane][x].length - 1, sy + SCAN_RADIUS); y++)
			{
				Tile tile = tiles[plane][x][y];
				if (tile == null)
				{
					continue;
				}
				for (GameObject go : tile.getGameObjects())
				{
					if (go == null || go.getSceneMinLocation() == null || !go.getSceneMinLocation().equals(tile.getSceneLocation()))
					{
						continue;
					}
					if (spot != null)
					{
						ObjectComposition def = client.getObjectDefinition(go.getId());
						if (def != null && (spot.isBirdhouse() ? def.getVarPlayerId() == spot.getVarp() : def.getVarbitId() == spot.getVarbit()))
						{
							out.add(go);
						}
					}
					else if (target.matches(go.getId()))
					{
						out.add(go);
					}
				}
			}
		}
		// Several patches can share a varbit in one scene; keep the one nearest the spot.
		if (out.size() > 1)
		{
			out.sort((a, b) -> Integer.compare(a.getWorldLocation().distanceTo2D(at), b.getWorldLocation().distanceTo2D(at)));
			return out.subList(0, 1);
		}
		return out;
	}

	private void setArrow(NPC npc, WorldPoint point, String key)
	{
		if (!config.hintArrow() || (npc == null && point == null))
		{
			if (arrowKey != null)
			{
				client.clearHintArrow();
				arrowKey = null;
			}
			return;
		}
		if (key.equals(arrowKey))
		{
			return;
		}
		if (npc != null)
		{
			client.setHintArrow(npc);
		}
		else
		{
			client.setHintArrow(point);
		}
		arrowKey = key;
	}

	private void placeMapPoint()
	{
		Point p = step.getMapPoint();
		if (!config.mapMarker() || p == null)
		{
			if (mapPoint != null)
			{
				worldMapPointManager.remove(mapPoint);
				mapPoint = null;
			}
			return;
		}
		WorldPoint wp = new WorldPoint(p.getX(), p.getY(), p.getPlane());
		if (mapPoint != null && wp.equals(mapPoint.getWorldPoint()))
		{
			return;
		}
		if (mapPoint != null)
		{
			worldMapPointManager.remove(mapPoint);
		}
		mapPoint = WorldMapPoint.builder()
			.worldPoint(wp)
			.image(Sigil.icon(20))
			.name("Codex Rounds")
			.tooltip("Codex Rounds: " + step.getTitle())
			.snapToEdge(true)
			.jumpOnClick(true)
			.build();
		worldMapPointManager.add(mapPoint);
	}

	private void publish(boolean force)
	{
		if (panel == null)
		{
			return;
		}
		long now = Instant.now().getEpochSecond();
		boolean loggedIn = client.getGameState() == GameState.LOGGED_IN;
		String sig = step.getTitle() + step.getDone() + "/" + step.getTotal() + "|" + route.taskCount() + "|" + route.getBuiltAt()
			+ "|" + (board == null ? -1 : board.count(Status.READY)) + "|" + progress.isStarted() + "|" + loggedIn + "|" + carried.hashCode();
		if (!force && sig.equals(published) && ticks % PUBLISH_TICKS != 0)
		{
			return;
		}
		published = sig;
		boolean known = memory != null && board != null && (loggedIn || !memory.seenMap().isEmpty());
		String last = null;
		if (memory != null && memory.rounds != null && !memory.rounds.isEmpty())
		{
			Memory.RoundRecord r = memory.rounds.get(memory.rounds.size() - 1);
			Memory.RoundRecord best = memory.best();
			last = "Last round: " + Format.clock(r.seconds) + " for " + r.chores + " chores." + (best != null && best != r ? " Fastest: " + Format.clock(best.seconds) + "." : "");
		}
		Map<Integer, Integer> bank = memory == null || memory.bank == null ? Collections.emptyMap() : new HashMap<>(memory.bank);
		View view = new View(loggedIn, known, board, route, step, progress.isStarted(), progress.getStartedAt(), now,
			memory != null && memory.bankTime > 0, new HashMap<>(carried), bank, last,
			memory == null || memory.stored == null ? Collections.emptyMap() : new HashMap<>(memory.stored),
			!route.isEmpty() && Leprechaun.PLACES.contains(route.getStops().get(0).getPlace()),
			forecastText(now));
		Progress snapshot = progress;
		Route r = route;
		Map<Spot, Reading> live = memory == null ? Collections.emptyMap() : readings(now);
		Map<Daily, DailyState> dailyMap = new EnumMap<>(Daily.class);
		if (memory != null)
		{
			for (DailyState d : dailies(now))
			{
				dailyMap.put(d.getDaily(), d);
			}
		}
		Guide.World world = new Guide.World(here(), live, dailyMap, memory == null ? null : memory.kingdom(now), carried,
			memory == null ? Ability.unknown() : memory.ability(), now);
		Set<String> doneKeys = new HashSet<>();
		long since = progress.isStarted() ? progress.getStartedAt() : r.getBuiltAt();
		for (Stop s : r.getStops())
		{
			for (Task t : s.getTasks())
			{
				if (snapshot.isStarted() && Guide.isDone(t, world, snapshot, settings, since))
				{
					doneKeys.add(Progress.key(t));
				}
			}
		}
		RoundsPanel p = panel;
		SwingUtilities.invokeLater(() -> p.update(view, t -> doneKeys.contains(Progress.key(t))));
	}

	// ---- Saved data ----

	private void loadProfile()
	{
		profile = configManager.getRSProfileKey();
		memory = null;
		if (profile == null)
		{
			return;
		}
		String saved = configManager.getRSProfileConfiguration(CodexRoundsConfig.GROUP, MEMORY_KEY);
		if (saved != null)
		{
			try
			{
				memory = gson.fromJson(saved, Memory.class);
			}
			catch (JsonParseException e)
			{
				log.warn("Could not read saved rounds data", e);
			}
		}
		if (memory == null)
		{
			memory = new Memory();
		}
		adoptOutside();
		dirty = true;
	}

	/** Reads RuneLite's own Time Tracking and Kingdom plugins' saved data, without changing it. */
	private void adoptOutside()
	{
		for (Spot s : Spot.values())
		{
			String stored = configManager.getRSProfileConfiguration(TIME_TRACKING, s.timeTrackingKey());
			if (memory.adopt(s, Seen.parse(stored)))
			{
				unsaved = true;
			}
		}
		Integer offsetMins = configManager.getRSProfileConfiguration(TIME_TRACKING, "farmTickOffset", int.class);
		Integer precision = configManager.getRSProfileConfiguration(TIME_TRACKING, "farmTickOffsetPrecision", int.class);
		offset = new Growth.Offset(offsetMins, precision);

		Instant changed = configManager.getRSProfileConfiguration(KINGDOM, "lastChanged", Instant.class);
		Integer coffer = configManager.getRSProfileConfiguration(KINGDOM, "coffer", int.class);
		Integer approval = configManager.getRSProfileConfiguration(KINGDOM, "approval", int.class);
		if (changed != null && coffer != null && approval != null && changed.getEpochSecond() > memory.kingdomTime)
		{
			memory.kingdomTime = changed.getEpochSecond();
			memory.kingdomCoffer = coffer;
			memory.kingdomApproval = approval;
			memory.kingdomQuest = true;
			unsaved = true;
		}
	}

	private void save()
	{
		if (profile == null || memory == null || !unsaved)
		{
			return;
		}
		configManager.setConfiguration(CodexRoundsConfig.GROUP, profile, MEMORY_KEY, gson.toJson(memory));
		unsaved = false;
	}

	private Settings readSettings()
	{
		Settings s = Settings.defaults()
			.group(Spot.Group.BIRDHOUSES, config.birdhouses())
			.group(Spot.Group.SEAWEED, config.seaweed())
			.group(Spot.Group.HARDWOODS, config.hardwoods())
			.group(Spot.Group.TREES, config.specialTrees())
			.group(Spot.Group.SPIRIT_TREES, config.spiritTrees())
			.group(Spot.Group.PATCHES, config.specialPatches())
			.daily(Daily.ZAFF, config.zaff())
			.daily(Daily.CROMPERTY, config.cromperty())
			.daily(Daily.FLAX, config.flax())
			.daily(Daily.RANTZ, config.rantz())
			.daily(Daily.ROBIN, config.robin())
			.daily(Daily.THIRUS, config.thirus())
			.daily(Daily.BERT, config.bert())
			.daily(Daily.HERB_BOXES, config.herbBoxes())
			.daily(Daily.LUNDAIL, config.lundail())
			.kingdom(config.kingdom())
			.kingdomTargets(config.approvalTarget(), config.cofferTarget(), config.topUpSlack())
			.compost(config.compost())
			.protect(config.protect())
			.keepSpiritTrees(config.keepSpiritTrees())
			.includeUnseen(config.includeUnseen())
			.lookahead(config.lookahead());
		s.crop(com.codex.rounds.core.farming.PatchKind.HARDWOOD_TREE, produce(config.hardwood().name()));
		s.crop(com.codex.rounds.core.farming.PatchKind.BUSH, produce(config.bush().name()));
		s.crop(com.codex.rounds.core.farming.PatchKind.CACTUS, produce(config.cactus().name()));
		s.crop(com.codex.rounds.core.farming.PatchKind.ANIMA, produce(config.anima().name()));
		return s;
	}

	private static Produce produce(String name)
	{
		if ("BEST".equals(name))
		{
			return null;
		}
		try
		{
			return Produce.valueOf(name);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	private class PanelActions implements Actions
	{
		@Override
		public void start()
		{
			clientThread.invokeLater(() ->
			{
				progress.start(Instant.now().getEpochSecond());
				timer.reset();
				replan = true;
				refresh(true);
			});
		}

		@Override
		public void stop()
		{
			clientThread.invokeLater(() ->
			{
				progress.stop();
				timer.reset();
				replan = true;
				refresh(true);
			});
		}

		@Override
		public void skip()
		{
			clientThread.invokeLater(() ->
			{
				if (step.getTask() != null)
				{
					progress.skip(step.getTask());
				}
				else if (step.getKind() == Step.Kind.SUPPLIES && step.getTarget() != null)
				{
					progress.leprechaunDone();
				}
				else if (step.getKind() == Step.Kind.SUPPLIES)
				{
					progress.suppliesDone();
				}
				refresh(true);
			});
		}

		@Override
		public void bankTab()
		{
			clientThread.invokeLater(() ->
			{
				tabOpened = false;
				Widget bank = client.getWidget(InterfaceID.Bankmain.UNIVERSE);
				if (bank != null && !bank.isHidden())
				{
					openBankTab();
				}
				else
				{
					tabWanted = true;
					chat("your round's bank tab opens the next time you open your bank.");
				}
			});
		}

		@Override
		public void replan()
		{
			clientThread.invokeLater(() ->
			{
				replan = true;
				dirty = true;
				refresh(true);
			});
		}
	}
}
