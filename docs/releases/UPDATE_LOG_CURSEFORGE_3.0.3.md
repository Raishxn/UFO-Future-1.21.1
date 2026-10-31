# UFO Future 3.0.3

Fixes server freezes on large crafting jobs with mega co-processors.

## Bug fixes

- **Server freeze on large crafting jobs.** Crafting CPUs with mega co-processors could spend an entire server tick pushing patterns into slow targets such as Mekanism machines, until the watchdog shut the server down. All AE2 crafting CPUs now share a time budget per tick; remaining work continues on the next tick, and every CPU still gets a few pushes per tick. Jobs that push into slow targets may take longer to finish.
- **New server option** `performance.craftingCpuTickBudgetMillis` (default **10**, range 1-1000). Lower values favour TPS, higher values favour crafting speed.
- **JEI:** multiblock structure pages can now be encoded in AE2 as processing patterns, with the structure blocks as inputs and the controller as output.

## Requirements

Minecraft **1.21.1**, NeoForge **21.1.216+**, AE2 **19.2.17 or 19.2.18** (`[19.2.17, 20)`), AE2 Addon Lib **1.0.3-1.21.1+** and RaishxCore **0.2.3** (`[0.2.3, 1.0)`). Replace the old UFO JAR and update RaishxCore to 0.2.3, which also carries the AE2 Lightning Tech planner fix.
