---
navigation:
  parent: ufo_intro/index.md
  title: Dimensional Matter Assembler
  position: 30
item_ids:
  - ufo:dimensional_matter_assembler
---

# Dimensional Matter Assembler (DMA) — Player Guide

<BlockImage id="ufo:dimensional_matter_assembler" scale="4"></BlockImage>

**Overview**
The Dimensional Matter Assembler (DMA) is the primary advanced crafting machine in UFO Future. It processes item and fluid inputs, uses catalysts to modify outcomes, and relies on a coolant/thermal system. 

---

## Quickstart
1. Provide **power** (DMA consumes very large amounts of energy).
2. Place item inputs into the 9-slot input grid. **Order does not matter (Shapeless)!**
3. Add **coolant** into the coolant tank (left side fluid inputs).
4. Insert **catalysts** into the upgrade slots.
5. Provide specific base fluids (like Liquid Starlight) into the base fluid tank if the recipe demands it.

---

## Catalysts & Multipliers

Catalysts modify the DMA performance. They belong to families:
- **Matterflow** — Decreases energy cost limits (e.g. 0.5x Power Cost).
- **Chrono** — Increases speed drastically (e.g. 2x Speed).
- **Overflux** / **Quantum** — Specialized modifiers.

### Heat Multiplier Penalty
- Catalysts now act as Heat Production Multipliers! Example: A Chrono T3 Catalyst applies a **x4 Heat Production** debuff!
- These effects stack. Stacking 4 of the same catalyst grants a massive Synergy bonus, but increases heat production significantly.
- **Exploit protection**: The DMA strictly limits any recipe to take at minimum 1 second (20 ticks). You cannot stack Chrono infinitely to bypass heat processing. Over-stacking Chrono will keep the time capped at 1s but the Heat Multiplier will be monstrous!

---

## Thermal System (Thermo)

Generate heat == Need Coolants!

### Zones
- **SAFE**: below **50% Heat Capacity**
- **HAZARD**: **≥50% Capacity** — Triggers the rotating 3D Flame rings. Burns players nearby!
- **MELTDOWN**: **100% Heat** — Triggers an alarm and 5-second countdown. If it reaches 0, the machine **explodes**, destroying blocks around it!

### Coolants Effectiveness
The DMA requires fluids for cooling, and each fluid provides a different cooling factor (Heat Units - HU):

- **Temporal Fluid** — Super efficient. **1 mB** cools **100 HU**. (Endgame)
- **Liquid Starlight** — Great cost-benefit. **1 mB** cools **30 HU**. (Midgame)
- **Gelid Cryotheum** — Very weak, requires intense volume. Needs a hefty **60 mB** to cool just **1 HU**.

---

*End of DMA guide.*
