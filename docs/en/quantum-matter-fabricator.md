# Quantum Matter Fabricator

The **Quantum Matter Fabricator (QMF)** is the multiblock evolution of the DMA. It is built for bulk matter processing, parallel production and AE2 autocrafting integration.

## Core Behavior

- Up to **27 parallel threads** in standard mode
- **9 parallel threads** while Safe Mode is enabled
- Accepts both **QMF-native** recipes and **DMA** recipes
- Pulls ingredients from the ME network automatically
- Pushes outputs back into ME storage automatically
- Supports AE2 automation through a **Quantum Pattern Buffer** or linked **Quantum Pattern Proxy**

## Pattern Buffer and Proxy

The controller requires exactly one **Quantum Pattern Buffer** or **Quantum Pattern Proxy**.

- The Buffer stores up to **72 encoded patterns** and serves its own controller.
- A linked Proxy lets this controller share another Buffer.
- AE2 can dispatch work directly into free multiblock threads.
- The older **Quantum Pattern Hatch** is for the single-block DMA workflow.

## Parallel Model

Each free thread can run one recipe copy.

- A pushed AE2 pattern reserves one free thread
- Idle threads can still auto-start valid work from the ME network
- Recipe items and fluids come from ME. Supply coolant externally through the ME Massive Fluid Hatch and FE through the FE Energy Input Hatch.

## Tier Rules

- Recipes can require **MK1**, **MK2** or **MK3**
- Higher machine tiers can run lower-tier recipes faster and cheaper
- Safe Mode trades throughput for thermal stability
