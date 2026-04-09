# Dimensional Matter Assembler

The **Dimensional Matter Assembler (DMA)** is the first advanced machine in UFO Future. It is still the entry point for high-end crafting, but it now sits at the start of a larger multiblock progression instead of trying to carry every extreme-scale recipe by itself.

## Core Behavior

- Shapeless 9-slot item crafting
- Optional recipe fluid input
- Dedicated coolant tank for heat management
- Catalyst upgrades for speed, efficiency, stability and advanced output behavior
- AE2-powered operation

The DMA is best seen as the flexible single-block machine for early and mid progression, recipe prototyping and lower-scale automation.

## Tank Separation

The DMA has two distinct fluid responsibilities:

- **Base fluid tank**: consumed only when the recipe explicitly asks for a fluid input
- **Coolant tank**: used only to remove heat from the machine

Coolants are not supposed to appear in the normal recipe fluid input for DMA processing. They belong in the coolant system.

## Coolant Progression

The intended coolant ladder is:

1. **Gelid Cryotheum** for early setups
2. **Stable Coolant** for sustained mid-to-late automation
3. **Temporal Fluid** for extreme endgame workloads

This progression matters now because the higher-performance setups are balanced around moving past Gelid Cryotheum instead of treating it as a forever solution.

## Heat and Failure

The DMA generates heat while active and cools down only slowly when idle. The machine moves through three thermal states:

- **Safe zone** below 50%
- **Hazard zone** above 50%, with warning effects and nearby damage
- **Meltdown** at 100%, followed by a countdown and destructive failure if cooling does not recover

If you stack aggressive catalyst combinations without upgrading coolant quality, the DMA will eventually punish that decision.

## Where DMA Ends and Multiblocks Begin

Very large endgame jobs are no longer meant to be solved by inflating the DMA into a million-stack singleblock. That role is now covered by the universal multiblock line:

- [Quantum Matter Fabricator](quantum-matter-fabricator.md)
- [Quantum Slicer](quantum-slicer.md)
- [Quantum Processor Assembler](quantum-processor-assembler.md)

This keeps the DMA useful without forcing it to become the answer to every industrial-scale problem.

## Recipe JSON Format

DMA recipes still use the `ufo:dimensional_assembly` type.

The exact recipe schema and scripting examples are documented on the [KubeJS Recipes](kubejs-recipes.md) page. This guide focuses on machine behavior and progression, while the KubeJS page remains the reference for the raw JSON fields.

## Related Systems

- [Catalysts](catalysts.md)
- [Multiblock Tiers](multiblock-tiers.md)
- [Crafting Progression](progression.md)
