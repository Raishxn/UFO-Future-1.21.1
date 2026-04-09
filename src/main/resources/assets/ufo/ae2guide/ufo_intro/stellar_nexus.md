---
navigation:
  parent: ufo_intro/index.md
  title: Stellar Nexus
  position: 50
item_ids:
  - ufo:stellar_nexus_controller
---

# Stellar Nexus

<BlockImage id="ufo:stellar_nexus_controller" scale="4"></BlockImage>

The **Stellar Nexus** is the endgame stellar simulation multiblock. It converts huge AE power, fluids and rare materials into large-scale outputs.

## Core Mechanics
- Reads inputs directly from the ME network.
- Charges a large shared AE buffer while assembled.
- Consumes fuel on start.
- Consumes coolant while running.
- Generates heat continuously during operation.

## Field Tiers
The four field generator positions must all match the same tier:
- **MK1**
- **MK2**
- **MK3**

Mixed tiers invalidate the structure.

## Coolant Ladder
The Stellar Nexus now follows the intended progression:
- **Gelid Cryotheum**: low efficiency
- **Stable Coolant**: medium efficiency
- **Temporal Fluid**: extreme efficiency

Higher field tiers can still use lower coolants, but the machine is designed to reward proper progression.

## Safe Mode
With **Safe Mode ON**, the Nexus spends more resources to protect itself:
- extra AE cost
- extra fuel usage
- extra coolant usage
- auto shutdown at maximum heat

This is the correct mode for routine automation.

## Overclock Mode
The **Overclock** toggle is an intentional risk/reward option:
- **5x faster recipes**
- **10x AE usage**
- **5x fuel usage**
- **5x heat generation**
- **5x coolant consumption**

It applies to every Stellar Nexus recipe, including recipes added by KubeJS.

## Catastrophic Failure
With Safe Mode disabled, max heat triggers a large-scale explosion wave that actually destroys the surrounding area shell by shell. It is meant to feel like a reactor-class detonation, not just a cosmetic burst.

## What The Nexus Is For
- absurd-scale resource generation
- endgame fluids and stellar materials
- long-cycle, high-cost simulations
- the final industrial layer above DMA and universal multiblocks

*See also: [Quantum Matter Fabricator](qmf.md) · [Multiblock Tiers](multiblock_tiers.md)*
