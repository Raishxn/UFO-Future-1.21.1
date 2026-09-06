---
navigation:
  parent: ufo_intro/machines.md
  title: Quantum Matter Fabricator
  position: 42
item_ids:
  - ufo:quantum_matter_fabricator_controller
---

# Quantum Matter Fabricator

<BlockImage id="ufo:quantum_matter_fabricator_controller" scale="4"></BlockImage>

The **QMF** is the multiblock evolution of the DMA for heavy automation, bulk crafting and AE2 pattern workflows.

<SubPages />

## Main Benefits

- Up to **27 parallel threads** in standard mode
- **9 parallel threads** in Safe Mode
- Accepts both native **QMF recipes** and **DMA recipes**
- Supports AE2 autocrafting through the **Quantum Pattern Hatch**
- Reads ingredients directly from the connected ME network
- Pushes outputs back into ME automatically

## Quantum Pattern Hatch

<BlockImage id="ufo:quantum_pattern_hatch" scale="3"></BlockImage>

- Stores up to **72 encoded patterns**
- Links to the controller when the structure is assembled
- Exposes the multiblock as a crafting machine to AE2
- Lets the controller run multiple jobs in parallel

## First Automated Job

1. Confirm the controller reports `IDLE`, not `UNFORMED` or `PAUSED_NO_GRID`.
2. Encode one processing pattern with the deterministic base output shown by JEI.
3. Insert it in the Quantum Pattern Hatch and request one craft from an ME terminal.
4. Confirm one process cell appears in the controller GUI.
5. Scale to 27 jobs only after input, coolant, energy and output paths are stable.

Pattern-delivered items are not extracted from ME twice. Any output rejected by
storage remains persistently buffered and changes the controller state to
`OUTPUT_BLOCKED` until capacity returns.

## Thermal Profile

- Base heat generation: **1 HU per active thread per tick**.
- Overclock heat generation: **5 HU per active thread per tick**.
- Idle passive cooling: **-1 HU every 40 ticks**.
- Coolant tank values use the shared universal multiblock ladder:
- Gelid Cryotheum removes **1 HU per 120 mB**, up to **1000 mB/tick**.
- Stable Coolant removes **50 HU per mB**, up to **10 mB/tick**.
- Temporal Fluid removes **100 HU per mB**, up to **10 mB/tick**.

Safe Mode prevents progress at the thermal ceiling and limits parallel work to
9 threads. Overclock multiplies progress and base heat by five.

## Dedicated supply hatches

The assembly requires an **ME Massive Fluid Hatch** and an **AE Energy Input
Hatch** in any compatible casing positions. The preview shows one example
layout, not mandatory hatch locations. At least one of each is required.

Coolant is consumed **only from the fluid hatch's local tank** (16,000,000 mB).
Supply Gelid Cryotheum, Stable Coolant or Temporal Fluid using external fluid
pipes or an explicitly configured export device. Controllers never fetch coolant
from ME storage automatically.

The energy hatch accepts **external FE and AE2 grid power**. FE enters a persistent
1,000,000,000 AE-equivalent buffer using AE2's configured conversion ratio.
Buffered external energy is used first; the connected AE2 grid supplies any
remainder. Both paths respect AE2's configured consumption multiplier.
ME connections are still needed for recipe automation.
