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
- **The round.** Press **Start** and the ready chores (plus anything due within the next ten minutes, adjustable) become an ordered list of stops. The order comes from the teleports you actually own: jewellery, capes, diaries' items, spellbook teleports for your book and level, fairy rings if you can use them, and short walks where a walk beats a teleport (Miscellania to Etceteria, Varrock to the Champions' Guild, the Fossil Island houses to the seabed). It starts with the nearest next stop and is then improved until no swap or reversal saves time.
- **What to bring.** One pull list for the whole round: teleports, tools (spade, dibber, secateurs, diving gear), seeds and saplings for what you will replant, compost, gardener payments, logs and hop seeds for the bird houses, flax and bones for the dailies and coins for the kingdom. Each row is marked as packed, in your bank, optional or short, so you see what is missing before you leave.
- **Next step.** At every moment the panel and a small plaque in the game view say the one thing to do next: the teleport to use, the patch to check, the house to rebuild, the advisor to pay. The target is outlined in the world, a hint arrow points at it, the item to use is highlighted in your inventory and bank, and a marker sits on the world map. Steps tick themselves off as the game confirms them, and **Skip** moves past anything you want to leave.
- **Replanting.** For each patch type it picks the best seed you own that your Farming level allows (or the one you set), and reminds you to compost and pay the gardener if you want that.
- **Diary dailies.** Zaff's battlestaves, Cromperty's noted essence, the Flax keeper, Rantz's arrows, Robin's bone exchange, Thirus's runes, Bert's sand, the Nightmare Zone herb boxes and Lundail's runes, each with the amount your diary tier gives and whether you have claimed it today. The day resets at midnight UTC, as in the game.
- **Kingdom of Miscellania.** Your coffer and approval estimated day by day from when you last checked, with the coffer you should top up to for a full payout and how many days until approval drops below your target. The kingdom joins the round when it needs a visit.
- **Round history.** When a round ends, a line in the chat box sums it up (chores done, time taken) and the panel keeps your best time.
- **Ready alert.** Optionally, a notification when enough chores are ready to make a round worth it (four by default).

<p align="center">
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
