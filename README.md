# Codex Rounds

**Every timed chore the farm run leaves out, in one guided round.** Birdhouses, giant seaweed, hardwood, calquat, celastrus, redwood and crystal trees, spirit trees, cactus, bushes, mushrooms, belladonna, anima and Hespori, the diary dailies and the Kingdom of Miscellania. Codex Rounds shows what is ready, plans the fastest way round with the teleports you own, lists what to bring, and points at the next click at every stop.

It is part of the **Codex** suite, after [Codex Navigator](https://github.com/Nubles/OSRS-Codex-Navigator), [Codex Guardian](https://github.com/Nubles/OSRS-Codex-Guardian), [Codex Log Insight](https://github.com/Nubles/OSRS-Codex-Log-Insight), [Codex Gear Roadmap](https://github.com/Nubles/OSRS-Codex-Gear-Roadmap), [Codex Session Chronicle](https://github.com/Nubles/OSRS-Codex-Session-Chronicle), [Codex Ledger](https://github.com/Nubles/OSRS-Codex-Ledger) and [Codex Quartermaster](https://github.com/Nubles/OSRS-Codex-Quartermaster), and shares their look: warm ink, aged gold and the game's own fonts.

<p align="center">
  <img src="docs/screenshots/round.png" width="225" alt="The round">
  <img src="docs/screenshots/running.png" width="225" alt="A round under way">
  <img src="docs/screenshots/board.png" width="225" alt="The board">
</p>

## What it does

- **Board.** Every chore in one list, grouped as bird houses, giant seaweed, hardwood trees, special trees, spirit trees, special patches, dailies and the kingdom. Each row says what is waiting (full and ready to collect, ready for its health check, diseased, dead, growing) and a countdown to when it will be ready. Patches you have not unlocked say what they need, and patches you have not visited yet say so instead of guessing.
- **The round.** Press **Start** and the ready chores (plus anything due within the next ten minutes, adjustable) become an ordered list of stops. The order comes from the teleports you actually own: jewellery, capes, diaries' items, spellbook teleports for your book and level, fairy rings if you can use them, and only the ones whose quest or unlock you have finished, and short walks where a walk beats a teleport (Miscellania to Etceteria, Varrock to the Champions' Guild, the Fossil Island houses to the seabed). It starts with the nearest next stop and is then improved until no swap or reversal saves time.
- **What to bring.** One pull list for the whole round: teleports, tools (spade, dibber, secateurs, diving gear), seeds and saplings for what you will replant, compost, gardener payments, logs and hop seeds for the bird houses, flax and bones for the dailies and coins for the kingdom. Each row is marked as packed, in your bank, optional or short (or to buy, for seeds and payments on a main), so you see what is missing before you leave. A hammer, chisel, Imcando hammer or jeweller's chisel all count, and when the list is more than a full pack, Rounds says so. Ironmen are only sent to plant seeds and saplings they own. Each special tree (calquat, celastrus, redwood, crystal) can be turned off on its own, and on Fossil Island the bird houses go Verdant Valley first, then Mushroom Meadow, with a nudge to take the Magic Mushtree.
- **Tool Leprechaun aware.** Tools and compost stored with the tool leprechaun are read from the game, so when the round starts at a stop with a leprechaun (the Farming Guild, Fossil Island, the seabed, Prifddinas and most patch sites in between) they are marked as waiting there instead of asked for from the bank, and the first step at that stop is to take them out.
- **Round bank tab.** When you bank for a round, the bank opens on a tab holding exactly the pull list, one row for each kind of supply in the order of the list, so withdrawing is one sweep. **Lay it out in my bank** under the pull list does the same on demand. The tab uses RuneLite's Bank Tags plugin.
- **Next step.** At every moment the panel and a small plaque in the game view say the one thing to do next: the teleport to use, the patch to check, the house to rebuild, the advisor to pay. The target is outlined in the world, a hint arrow points at it, the item to use is highlighted in your inventory and bank, and a marker sits on the world map. Steps tick themselves off as the game confirms them, and **Skip** moves past anything you want to leave.
- **Replanting.** For each patch type it picks the best seed you own that your Farming level allows (or the one you set), and reminds you to compost and pay the gardener if you want that.
- **Diary dailies.** Zaff's battlestaves, Cromperty's noted essence, the Flax keeper, Rantz's arrows, Robin's bone exchange, Thirus's runes, Bert's sand, the Nightmare Zone herb boxes and Lundail's runes, each with the amount your diary tier gives and whether you have claimed it today. The day resets at midnight UTC, as in the game.
- **Farming contracts.** Guildmaster Jane's contract joins the round when it is done (hand it in and take the next) or when none is running. If the contract crop grows in a guild patch Rounds tends (a bush, cactus, celastrus or redwood), that patch is planted with it.
- **Quick loop.** When bird houses or seaweed are ready, **Quick loop** runs just those: the hourly Fossil Island trip, with the same guidance.
- **Kingdom of Miscellania.** Your coffer and approval estimated day by day from when you last checked, with the coffer you should top up to for a full payout and how many days until approval drops below your target. The kingdom joins the round when it needs a visit.
- **Learns your pace.** Every teleport leg is timed from the moment the guide says to travel to the moment you stand in the stop. After two laps a leg is planned with your own time instead of the estimate, so the order fits how you actually play. Detours, like a stop at the bank, are left out.
- **Worth the wait?** When a short wait brings several more chores in, the round card says so: "Wait 14 min and 3 more join: bird houses and special patches."
- **Round history.** When a round ends, a line in the chat box sums it up (chores done, time taken) and the panel keeps your best time.
- **Ready alert.** Optionally, a notification when enough chores are ready to make a round worth it (four by default).

<p align="center">
  <img src="docs/screenshots/leprechaun.png" width="225" alt="Tools waiting with the leprechaun">
  <img src="docs/screenshots/plaque.png" alt="Plaque">
</p>

## How it reads your chores

Patch states come from the same game values RuneLite's Time Tracking plugin uses, read whenever you are near a patch. Growth times are predicted the same way, including your farming tick offset, so the board keeps counting while you are elsewhere. If Time Tracking or Kingdom of Miscellania have already seen a patch or your kingdom, Codex Rounds starts from what they saw, without changing their data. Bird houses are read from their own values as soon as you are on Fossil Island. Your bank is remembered from the last time it was open, so the pull list works anywhere.

## Rules posture

Codex Rounds only reads what the game already shows you and draws highlights, like the farming and time tracking plugins do. It does not click, change menus, or send anything over the network.

## Development

```bash
./gradlew build   # compile and run tests
./gradlew run     # launch RuneLite with the plugin loaded
```

The board, route planner and step guide (`core/`) are plain Java with unit tests; the panel and overlays are in `ui/` and `overlay/`.

## Licence

BSD 2-Clause, see [LICENSE](LICENSE).
