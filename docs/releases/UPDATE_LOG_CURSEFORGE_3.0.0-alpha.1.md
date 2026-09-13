# UFO Future 3.0.0-alpha.1

The first public 3.0 alpha brings a new endgame fabrication trio, dedicated energy and coolant supply, modular armor, and improvements to automation, stability and performance.

**Before upgrading a 2.1 or 2.1-fix1–fix7 world, read the migration section:** some structures need manual adjustments, and the old Entropic Assembler Matrix has been removed.

## Dependencies

- **New required dependency:** [RaishxCore](https://github.com/Raishxn/RaishxCore/releases/tag/v0.1.0-alpha.2) `0.1.0-alpha.2` or a compatible version, downloaded from its own repository. Players using earlier alphas with the old `ufocore` JAR must replace it.
- **Applied Energistics 2:** 19.2.17 or newer within the 19.x line.
- **AE2 Addon Lib:** 1.0.3 for Minecraft 1.21.1 or a compatible 1.x version.
- **Mekanism is now optional:** chemical storage and processing are available when it is installed.
- **GeckoLib is no longer required by UFO.** Keep it if another mod in your pack, such as AdvancedAE, needs it.

## Added

- **New endgame fabrication trio**, replacing the Entropic Assembler Matrix:
  - **Quantum Computation Nexus:** crafting CPUs with shared resources across the ME grid.
  - **Quantum Pattern Fabrication Matrix:** crafting, smithing and stonecutting pattern library, with multiblock pattern buffer automation.
  - **Infinity Fabrication Singularity:** aggregate batch fabrication, connected to the grid through the Quantum Grid Link, with reuse of intact catalysts.
- **Infinite Nexus mode** for the Stellar Nexus.
- **Modular UFO armor** with quantum upgrades.
- **Optional Productive Bees integration:** Matter Ball, Scrap and Scrap Box bees, with spawn eggs obtainable exclusively through Quantum Matter Fabricator (QMF) recipes. Bee content does not load without Productive Bees.
- **Auto-build in Stellar Nexus and universal controller interfaces:** uses inventory materials and preserves mismatched blocks already placed.
- **Native 3D multiblock previews in JEI**, with layers, rotation, zoom and block details; available in EMI through the JEI/EMI bridge, with JEI installed.
- **GuideME guide reorganized around progression**, with updated structure scenes. The prototype tutorial/Ponder system was removed.
- **Native connected textures on Quantum and Entropy casings**, without an external CTM dependency.
- **In-game diagnostics:** `/ufo debug machine` and `/ufo debug perf`, including processing, scan, storage and synchronization metrics, with export for analysis.

## Changes and balance

- **Dedicated coolant and energy supply:** Stellar Nexus, QMF, Quantum Processor Assembler (QPA), Quantum Slicer and Quantum Cryoforge now require an ME Massive Fluid Hatch and an FE Energy Input Hatch in their designated structure positions. Fill the coolant tank externally and connect an FE source to the energy hatch.
- **The FE Energy Input Hatch consumes only its local reservoir:** it does not automatically draw energy from the ME grid or Applied Flux storage. The Singularity also requires its own FE hatch; the ME grid and crafting CPUs still need a separate power supply.
- **Stellar Nexus Safe Mode and Overclock** rebalanced to **2x and 8x** energy costs, respectively.
- **DMA/QMF recipes rebalanced:** costs, energy, duration and batches for component matrices, field generators, catalysts, fragments and infinity cells were revised. Check recipes and re-encode affected patterns.
- **Stellar Nexus:** coolant consumption proportional to heat, with cooling while idle.
- **Quantum Computation Nexus:** crafting parallelism adjusts to stored grid energy, reducing demand when the buffer is low to avoid repeated power outages.
- **Crafting planner moved to RaishxCore**, with a per-instance `planner.enabled` setting. Disabling it uses AE2's native planner for new requests; calculations already submitted remain in progress.
- Removed the experimental **Apocalypse Type-A** mob and its spawn egg.

## Fixed

- **Transactional parallel processing:** resources and progress are preserved for recovery after full storage, world reload or machine break, fixing losses and duplication in these paths.
- **Autocrafting with catalysts:** patterns promise only guaranteed output. Bonuses are delivered separately and persist when blocked or after reload, without rerolling or leaving the CPU waiting for a probabilistic result.
- **BigInteger storage cells:** fixed partition/inverter/fuzzy modes, byte counting, limits and NBT reading, with full Cell Workbench support.
- **Dimensional Matter Assembler (DMA):** recipes with multiple fluid requirements reserve and consume the correct total; coolant identification was also fixed.
- **Stellar Nexus:** outputs wait for grid space. Destructive thermal failure effects are disabled by default and can be enabled in configuration.
- **Quantum Grid Link:** pending outputs persist through physical chunk unload/reload. Breaking the link returns what the grid accepts and drops the remaining balance in AE2 packets, preserving quantities and components.
- **Endgame trio:** structures automatically reform and reconnect after full chunk unload/reload; retained members are correctly detached during chunk deactivation.
- Fixed crafting CPU deadlocks when aggregate providers are busy.
- Singularity patterns no longer incorrectly appear as craftable in the terminal.
- Wireless crafting now resumes correctly after a save/reload.
- Restored UFO recipes and categories with **JEI + EMI**, including Mekanism chemical recipes through the viewer bridge.
- Fixed **BiggerAE2** compatibility, the **Applied Flux** induction card, **Quantum Pattern Hatch** upgrades and **Stellar ANY** cells.
- Restored FE input for **UFO/Quantum Energy Cells** and pattern hatch item returns.
- Removed interference with AE2's loading lifecycle that affected drives and UFO cell upgrades.
- Fixed UFO armor incorrectly removing flight granted by Creative, Spectator or other mods when changing armor.
- Fixed crafting cube geometry loading; the DMA now displays coolant and upgrade status.
- Pulsar chemical storage and its housing are hidden from creative/JEI/EMI without Mekanism, while keeping their IDs for existing saves.
- Machine interface actions received additional server validation, including open menu, distance and packet frequency.

## Performance

- **Event-driven structure scans**, without periodic checks of idle structures. Block breaks, chunk changes, explosions and pistons invalidate structures when needed.
- Cross-chunk block lookups use already loaded data on the server thread, without forcing chunk loads or adding chunk tickets.
- Less work per tick for idle controllers and armor effects; hot DMA particles and entity searches run at controlled intervals.
- More compact interface synchronization, avoiding transmission of full persistent NBT on every update.
- Identical AE2 storage transfers within the same tick are batched by resource, preserving operation order and consistency.

## Migration from 2.1

1. **Back up the complete world, including its `data` directory, and test on a copy.** This version is an alpha.
2. Install the required dependencies and update UFO.
3. **Entropic Assembler Matrix and its casing were removed:** old blocks and items disappear when loaded, without automatic conversion or drops. Their crafting patterns can be transferred to the new trio's Singularity.
4. On existing Stellar Nexus, QMF, QPA, Quantum Slicer and Quantum Cryoforge structures, replace the two casing positions shown by the preview with fluid and energy hatches. **These structures only form after the adjustment.** Supply coolant and FE externally.
5. Move Stellar Nexus hatches previously placed in free positions into designated hatch slots.
6. Check rebalanced recipes in JEI and re-encode patterns whose ingredients or outputs changed. Also check old in-flight recipes before resuming them.

Players upgrading from earlier 3.0 alphas should also check the Singularity's FE hatch and the Quantum Grid Link's external connection to the ME grid.

Full guide: [migration from 2.1 to 3.0](https://github.com/Raishxn/UFO-Future-1.21.1/blob/main/docs/releases/MIGRATION_2.1-to-3.0.md).

## Validation and alpha limits

- **26 GameTests**, covering lifecycle, recovery and legacy data reading, passed with and without Mekanism, alongside CI regression suites.
- **Idle test:** 102 real structures over 10,000 ticks, checking the absence of periodic scans after initialization.
- **Active load test:** 102 Singularities over 10,000 active ticks, with initially blocked outputs, storage reopening and physical ME power disconnection/reconnection. Exact material and energy checks, with no pending outputs at the end.
- Automated fixtures check controller buffers, Stellar energy fields and UUID-backed cells using the 2.1-fix7 format.
- UFO/RaishxCore builds passed with zero javac warnings; warnings fail the build except for the documented NeoForge registration `this-escape` category.

**A complete 2.x world upgrade and performance measurements in the actual modpack with players and 100+ active machines still need human validation.** Save fixtures and load tests without players do not establish those results. Procedure: [3.0 release QA](https://github.com/Raishxn/UFO-Future-1.21.1/blob/main/docs/releases/RELEASE_QA_3.0.md).
