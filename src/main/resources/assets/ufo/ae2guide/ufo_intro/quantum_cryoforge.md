---
navigation:
  parent: ufo_intro/machines.md
  title: Quantum Cryoforge
  position: 44
item_ids:
  - ufo:quantum_cryoforge_controller
---

# Quantum Cryoforge

<BlockImage id="ufo:quantum_cryoforge_controller" scale="4"></BlockImage>

The **Quantum Cryoforge** is the universal multiblock dedicated to large-scale coolant production.

<SubPages />

- Shares the same controller flow as the other universal multiblocks.
- Accepts universal hatches in valid casing positions.
- Focuses on coolant and thermal-fluid throughput.
- Machine tier comes from the installed Stellar Field Generators.

## Thermal Profile

- Heat generation is reduced to **50%** of the normal universal multiblock rate.
- Base heat generation: **ceil(active threads x 0.5) HU/tick**.
- Overclock heat generation: **ceil(active threads x 5 x 0.5) HU/tick**.
- Idle passive cooling: **-1 HU every 40 ticks**.

## Coolant Tank

- **Gelid Cryotheum** removes **1 HU per 120 mB**, up to **1000 mB/tick**.
- **Stable Coolant** removes **50 HU per mB**, up to **10 mB/tick**.
- **Temporal Fluid** removes **100 HU per mB**, up to **10 mB/tick**.

## Stable Coolant Gate

Stable Coolant is a **MK3 Quantum Cryoforge** recipe.

Replace every field generator position with **MK3 Stellar Field Generators** before expecting the Stable Coolant recipe to run. Gelid Cryotheum remains the early coolant path.

Use JEI Alternatives when placing hatches and fields. The structure scanner can
identify a single lower-tier or invalid field that silently reduces/blocks the
expected recipe tier.

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
