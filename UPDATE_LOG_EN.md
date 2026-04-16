# UFO Future 2.0.0 - Update Log

## From 1.6.1 to 2.0.0

This update covers everything that changed since the last public release, `1.6.1`.

Back in `1.6.1`, UFO Future was still at a very early stage. The mod only had the `DMA`, that DMA was buggy, it could not even be built properly, the heat system did not really work, the cells were bugged, and most of the mod was still in a first-mod prototype state.

`2.0.0` is the point where UFO Future stops feeling like an early experiment and starts feeling like a real late-game tech mod with structure, progression, automation, and identity.

## What Changed at a High Level

- UFO Future now has multiple real endgame systems instead of only the early DMA concept.
- The mod now includes several new blocks, multiblocks, AE2 integrations, energy components, recipes, UI improvements, JEI support upgrades, and internal system rewrites.
- Large parts of the mod were rebuilt to make the gameplay clearer, more stable, and much more rewarding for players.

## New Blocks and Components

Compared to `1.6.1`, the mod now includes a much larger block lineup, especially for endgame progression and automation.

### New Energy and Infrastructure Blocks

- `UFO Energy Cell`
- `Quantum Energy Cell`
- `AE Energy Input Hatch`
- `ME Massive Input Hatch`
- `ME Massive Output Hatch`
- `ME Massive Fluid Hatch`

### New Quantum and Endgame Structural Blocks

- `Quantum Entropy Casing`
- `Quantum Hyper Mechanical Casing`
- `Entropy Assembler Core Casing`
- `Entropy Singularity Casing`
- `Entropy Computer Condensation Matrix`
- `Quantum Lattice Frame`
- `Graviton Plated Casing`

### New Stellar and Matter Blocks

- `Stellar Field Generator Mk.I`
- `Stellar Field Generator Mk.II`
- `Stellar Field Generator Mk.III`
- `White Dwarf Fragment Block`
- `Neutron Star Fragment Block`
- `Pulsar Fragment Block`

## New Multiblocks

This is one of the biggest differences from `1.6.1`.

At the time of the last public update, the mod was basically centered on the DMA. Now UFO Future has a real multiblock ecosystem:

- `Dimensional Matter Assembler`
- `Stellar Nexus`
- `Quantum Matter Fabricator`
- `Quantum Slicer`
- `Quantum Processor Assembler`
- `Quantum Cryoforge`

That means the mod has moved from one unstable early machine into a broader late-game progression chain with specialized multiblocks, different structure requirements, AE2 interaction, recipe scaling, and machine identity.

## AE2 and Automation Progress

One of the biggest leaps since `1.6.1` is how much stronger the AE2 side of the mod has become.

- Added dedicated AE2-connected hatches for large-scale item, fluid, and energy handling.
- Added `Quantum Pattern Hatch`.
- Added `Quantum Pattern Provider` part integration.
- Improved multiblock interaction with AE2 crafting and network-based automation.
- Improved scaling for large outputs and high-throughput automation.
- Improved crafting flow for quantum pattern handling and controller communication.

This matters because the mod is no longer just a concept with recipes. It now supports the kind of automation and throughput players expect from a real endgame AE2 addon-style progression.

## JEI, UI, and Player Experience

The player-facing experience is dramatically better than it was in `1.6.1`.

- Added JEI support for multiblock recipes and machine categories.
- Added 3D JEI multiblock previews.
- Migrated multiblock JEI previews to `LDLib2` for a cleaner and more robust preview pipeline.
- Improved multiblock controller screens.
- Improved machine summaries, status display, parallel info, energy display, and active recipe visibility.
- Improved the `Quantum Pattern Hatch` UI layout and clarity.
- Expanded in-game and external documentation across multiple languages.

In older builds, players had to fight the mod just to understand what was happening. Now the mod does a much better job of explaining itself.

## Major Bug Fixes Since 1.6.1

This release is also important because it fixes core problems that made the old version feel unfinished.

- Fixed the DMA so it can actually be built and used as intended.
- Fixed the broken or incomplete heat behavior that existed in early versions.
- Fixed multiple cell-related issues.
- Fixed multiblock recipe presentation problems.
- Fixed multiblock fluid packet encoding issues.
- Fixed large-output handling for multiblock processing.
- Fixed `Quantum Pattern Hatch` crafting flow issues.
- Fixed controller HUD and UI behavior.
- Fixed `Stellar Nexus` coolant and explosion logic.
- Fixed recipe inconsistencies and balance issues in several endgame chains.

## Refactor and System Rewrite

`2.0.0` is not just "more content". A large part of the mod was rebuilt internally.

- Refactored the multiblock backend into a shared parallel processing architecture.
- Added a more consistent controller framework for heat, overclocking, safe mode, progress display, and client sync.
- Reworked recipe handling for universal multiblocks.
- Improved multiblock pattern definitions and structure validation support.
- Reworked generated assets, loot tables, blockstates, recipes, and supporting data.
- Cleaned up the technical base so future machines and systems are much easier to add.

This is the part that really separates the old version from the current one. `1.6.1` was an early first-mod foundation. `2.0.0` is a rebuilt and much more serious base.

## New Content Beyond Machines

- Added the foundation for the `Apocalypse Type-A` boss, including entity, renderer, model, animation, texture, and spawn egg support.
- Expanded recipe sets for catalysts, matter tiers, processor chains, energy cells, and universal multiblock batches.
- Expanded wiki and GuideMe documentation across locales.
- Added planning and documentation groundwork for future endgame content lines.

## Short Public Summary

From `1.6.1` to `2.0.0`, UFO Future evolved from a very early DMA-only prototype into a real endgame tech mod.

The old version had only one machine, and even that machine was still buggy, incomplete, and hard to use. The new version adds multiple quantum multiblocks, new energy cells, massive AE2 hatches, better automation, better JEI support, better UI, a working heat system, major bug fixes, and a full internal refactor focused on stability, scalability, and player experience.

## One-Line Release Version

`2.0.0` is the update where UFO Future stops being an early first-mod experiment and becomes a real large-scale endgame automation experience.
