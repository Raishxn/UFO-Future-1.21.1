---
navigation:
  parent: ufo_intro/machines.md
  title: Quantum Processor Assembler
  position: 44
item_ids:
  - ufo:quantum_processor_assembler_controller
---

# Quantum Processor Assembler

<BlockImage id="ufo:quantum_processor_assembler_controller" scale="4"></BlockImage>

The **Quantum Processor Assembler** is the bulk processor-finishing multiblock.

<SubPages />

## Features

- Up to **27 parallel jobs** in standard mode
- **9 parallel jobs** in Safe Mode
- AE2 pattern support through a **Quantum Pattern Buffer or Proxy**
- Universal multiblock recipe support for processor families
- Direct ME network pull and push behavior for items and fluids

## Pattern Buffer / Proxy

- A local **Quantum Pattern Buffer** stores **72 encoded patterns**
- A **Quantum Pattern Proxy** uses the patterns held by its linked Buffer
- Either part exposes its controller to the Buffer as a crafting machine
- Each pushed pattern reserves one free thread

## Production-Line Role

Keep Slicer patterns and final-assembly patterns visible to the same crafting
network. AE2 can then request printed components as dependencies and dispatch
the final processor step here. If all 27 threads are reserved, new jobs wait
until a process completes and its output is accepted.

## Thermal Profile

- Base heat generation: **1 HU per active thread per tick**.
- Overclock heat generation: **5 HU per active thread per tick**.
- Idle passive cooling: **-1 HU every 40 ticks**.
- Coolant tank values use the shared universal multiblock ladder:
- Gelid Cryotheum removes **1 HU per 120 mB**, up to **1000 mB/tick**.
- Stable Coolant removes **50 HU per mB**, up to **10 mB/tick**.
- Bose-Einstein Condensate removes **200 HU per mB**, up to **10 mB/tick**.

## Dedicated supply hatches

The assembly requires an **ME Massive Fluid Hatch** and an **FE Energy Input
Hatch** in any compatible casing positions. The preview shows one example
layout, not mandatory hatch locations. At least one of each is required.

Supply FE through an energy cable to the energy hatch's local reservoir.
Processing never draws its fuel from AE2 grid power.

Coolant is consumed **only from the fluid hatch's local tank** (16,000,000 mB).
Supply Gelid Cryotheum, Stable Coolant or Bose-Einstein Condensate using external fluid
pipes or an explicitly configured export device. Controllers never fetch coolant
from ME storage automatically.

The energy hatch accepts **external FE only**. FE enters a persistent
1,000,000,000 AE-equivalent buffer using AE2's configured conversion ratio.
Adding more energy hatches does not multiply the controller's total charging
limit. ME connections and their own power are still needed for item and fluid
automation.
