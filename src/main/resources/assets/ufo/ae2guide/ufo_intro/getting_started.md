---
navigation:
  title: Getting Started
  icon: ufo:dimensional_matter_assembler
  parent: ufo_intro/index.md
  position: 10
---

# Getting Started

This tutorial takes you from an existing AE2 network to the first automated UFO
production line.

## Prerequisites

Prepare a stable ME network with storage, crafting CPUs, processing-pattern
support and enough AE generation for late-game recipes. Install JEI: the UFO
recipe categories are the authoritative view of inputs, fluids, outputs, time,
energy and required machine tier.

Keep a <ItemLink id="ufo:structure_scanner" /> nearby. Right-clicking a supported
controller validates its structure and highlights incorrect positions. In
Creative mode, Shift + Right Click can auto-build the approved shell using
blocks from the player inventory.

## Step 1: Build and Power the DMA

Craft a <ItemLink id="ufo:dimensional_matter_assembler" />, connect it to AE2 and
open its interface.

1. Put shapeless item ingredients in the nine input slots.
2. Put recipe fluid in the base-fluid tank only when JEI lists one.
3. Put coolant in the dedicated coolant tank.
4. Supply enough AE for the complete recipe.
5. Add catalysts only after the basic recipe works reliably.

The two tanks have different ownership: recipe fluid is consumed as an
ingredient; coolant is consumed only to remove heat. See [DMA](dma.md) and
[Catalysts](catalysts.md).

## Step 2: Establish the Stellar-Matter Chain

Use JEI to progress through White Dwarf, Neutron Star and Pulsar materials.
These feed component matrices, large storage cells, casings and controllers.
Do not copy fixed costs from screenshots: a modpack can override recipes.

## Step 3: Build a Universal Multiblock

Choose the machine for the job:

- <ItemLink id="ufo:quantum_matter_fabricator_controller" /> for QMF-native and
  compatible DMA-style bulk work.
- <ItemLink id="ufo:quantum_slicer_controller" /> for printed components.
- <ItemLink id="ufo:quantum_processor_assembler_controller" /> for finished
  processors.
- <ItemLink id="ufo:quantum_cryoforge_controller" /> for coolant production.

Open the controller's JEI usage page to inspect its structure. Use Alternatives
to choose valid hatches or field generators, then use the viewer's quick-build
pattern or the Structure Scanner in Creative when appropriate.

## Step 4: Connect Automation

1. Place every required hatch in a valid structure position.
2. Connect the indicated face of the AE hatches to the same powered ME network.
3. Insert encoded processing patterns into the Quantum Pattern Hatch.
4. Ensure inputs and output capacity exist in ME storage.
5. Send one job first, then scale to the controller's 27-thread limit.

Normal mode supports up to **27 jobs**. Safe Mode limits execution to **9 jobs**
and prevents unsafe thermal operation. Each thread can be paused and resumed
without discarding its persistent buffers.

## Step 5: Upgrade the Tier

The lowest valid field-generator tier determines the universal machine tier.
MK2 and MK3 unlock their own recipes and improve lower-tier processing. The
Stellar Nexus is stricter: all four field generators must match.

## Step 6: Prepare the Stellar Nexus

Before starting a simulation, verify all of the following:

- the structure is formed and every required massive hatch is connected on its
  indicated face;
- the internal AE buffer is charged;
- recipe inputs, fuel and coolant are available;
- output storage has enough room;
- Safe Mode and Overclock match the cooling capacity of the installation.

See [Stellar Nexus](stellar_nexus.md) before disabling Safe Mode.
