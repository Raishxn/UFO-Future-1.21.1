---
navigation:
  parent: ufo_intro/machines.md
  title: Quantum Slicer
  position: 43
item_ids:
  - ufo:quantum_slicer_controller
---

# Quantum Slicer

<BlockImage id="ufo:quantum_slicer_controller" scale="4"></BlockImage>

The **Quantum Slicer** prepares printed components for large processor and circuit pipelines.

<SubPages />

## What It Does

- Uses the universal multiblock recipe system
- Supports up to **27 parallel jobs** in standard mode
- Drops to **9 parallel jobs** in Safe Mode
- Accepts AE2 encoded patterns through a **Quantum Pattern Buffer or Proxy**
- Pulls ingredients from ME and returns outputs automatically

## Pattern Buffer / Proxy

- A local **Quantum Pattern Buffer** stores **72 encoded patterns**
- A **Quantum Pattern Proxy** uses the patterns held by its linked Buffer
- Either part links to the controller after assembly
- AE2 can push jobs without manual inventory handling

## Production-Line Role

Encode the Slicer output as the input used by the Quantum Processor Assembler.
Start with one copy and verify that AE2 returns the printed component before
requesting a large processor batch. A blocked output remains saved in its thread
and is not produced twice.

## Thermal Profile

- Base heat generation: **1 HU per active thread per tick**.
- Overclock heat generation: **5 HU per active thread per tick**.
- Idle passive cooling: **-1 HU every 40 ticks**.
- Coolant tank values use the shared universal multiblock ladder:
- Gelid Cryotheum removes **1 HU per 120 mB**, up to **1000 mB/tick**.
- Stable Coolant removes **50 HU per mB**, up to **10 mB/tick**.
- Temporal Fluid removes **100 HU per mB**, up to **10 mB/tick**.

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
