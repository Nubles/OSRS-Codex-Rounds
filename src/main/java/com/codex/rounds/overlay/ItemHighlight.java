package com.codex.rounds.overlay;

import com.codex.rounds.CodexRoundsConfig;
import com.codex.rounds.ui.Theme;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.util.ImageUtil;

/** Lights up the items the current step uses, in the pack, worn gear and the bank. */
public class ItemHighlight extends WidgetItemOverlay
{
	private final ItemManager itemManager;
	private final CodexRoundsConfig config;
	private volatile Set<Integer> items = Collections.emptySet();

	@Inject
	ItemHighlight(ItemManager itemManager, CodexRoundsConfig config)
	{
		this.itemManager = itemManager;
		this.config = config;
		showOnInventory();
		showOnEquipment();
		showOnBank();
	}

	public void setItems(Set<Integer> ids)
	{
		items = ids;
	}

	@Override
	public void renderItemOverlay(Graphics2D g, int itemId, WidgetItem widgetItem)
	{
		if (!config.highlightItems() || !items.contains(itemManager.canonicalize(itemId)) && !items.contains(itemId))
		{
			return;
		}
		Color color = config.highlightColor();
		Rectangle bounds = widgetItem.getCanvasBounds();
		BufferedImage outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), color);
		g.drawImage(outline, (int) bounds.getX(), (int) bounds.getY(), null);
		BufferedImage image = itemManager.getImage(itemId, widgetItem.getQuantity(), false);
		if (image != null)
		{
			g.drawImage(ImageUtil.fillImage(image, Theme.alpha(color, 60)), (int) bounds.getX(), (int) bounds.getY(), null);
		}
	}
}
