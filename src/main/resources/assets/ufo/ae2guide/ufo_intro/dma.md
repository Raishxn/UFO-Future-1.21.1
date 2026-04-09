---
navigation:
  parent: ufo_intro/index.md
  title: Dimensional Matter Assembler
  position: 30
item_ids:
  - ufo:dimensional_matter_assembler
---

# Dimensional Matter Assembler

<BlockImage id="ufo:dimensional_matter_assembler" scale="4"></BlockImage>

The **DMA** is the first advanced machine in UFO Future. It handles shapeless item recipes, optional base fluids, catalysts and heat management.

## Core Loop
1. Insert items into the 9-slot grid.
2. Fill the **base fluid tank** only when the recipe asks for a fluid input.
3. Fill the **coolant tank** with a valid coolant.
4. Add catalysts if you want speed, efficiency or yield changes.
5. Supply AE power and let the DMA process.

## Tanks
- **Coolant tank**: not part of the recipe cost. It only removes heat from the DMA.
- **Base fluid tank**: consumed by recipes that explicitly ask for fluid inputs.

DMA recipes should not ask for coolant in their normal fluid inputs. Coolants belong in the coolant tank only.

## Coolant Progression
- **Gelid Cryotheum**: low-efficiency starter coolant.
- **Stable Coolant**: mid-tier sustained coolant.
- **Temporal Fluid**: extreme endgame coolant.

The intended path is **Gelid -> Stable -> Temporal**. If you push Chrono-heavy catalyst setups too early, Gelid Cryotheum will not keep up for long.

## Heat and Meltdown
- Below 50% heat: stable operation.
- Above 50% heat: hazard zone, visual warning effects and nearby damage.
- At 100% heat: meltdown countdown, then a destructive explosion if the machine is not cooled in time.

## Catalysts
- **Matterflow**: improves efficiency.
- **Chrono**: increases speed, but spikes heat.
- **Overflux**: increases thermal stability.
- **Quantum**: focuses on advanced output behavior.
- **Dimensional Catalyst**: creative-tier override.

## Where It Fits Now
The DMA is the entry point and flexible single-machine crafter. Once your recipes become extremely expensive or need automated pattern-based throughput, progression moves into the multiblock line:

- **Quantum Matter Fabricator**
- **Quantum Slicer**
- **Quantum Processor Assembler**

*See also: [Catalysts System](catalysts.md) · [Quantum Matter Fabricator](qmf.md) · [Multiblock Tiers](multiblock_tiers.md)*
