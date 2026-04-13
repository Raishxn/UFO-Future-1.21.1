# Quantum Matter Fabricator

The **Quantum Matter Fabricator (QMF)** is the multiblock evolution of the DMA. It is built for bulk matter processing, parallel production and AE2 autocrafting integration.

## Core Behavior

- Up to **27 parallel threads** in standard mode
- **9 parallel threads** while Safe Mode is enabled
- Accepts both **QMF-native** recipes and **DMA** recipes
- Pulls ingredients from the ME network automatically
- Pushes outputs back into ME storage automatically
- Supports AE2 automation through the **Quantum Pattern Hatch**

## Quantum Pattern Hatch

The **Quantum Pattern Hatch** is the multiblock-facing pattern provider for this machine family.

- Stores up to **72 encoded patterns**
- Links itself to the controller when the structure assembles
- Exposes the controller to AE2 as a crafting machine
- Lets AE2 dispatch work directly into the multiblock threads

## Parallel Model

Each free thread can run one recipe copy.

- A pushed AE2 pattern reserves one free thread
- Idle threads can still auto-start valid work from the ME network
- Items, fluids and AE are all pulled directly from the network

## Tier Rules

- Recipes can require **MK1**, **MK2** or **MK3**
- Higher machine tiers can run lower-tier recipes faster and cheaper
- Safe Mode trades throughput for thermal stability
