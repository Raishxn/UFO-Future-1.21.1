# Quantum Matter Fabricator

The **Quantum Matter Fabricator (QMF)** is the multiblock evolution of the DMA. It exists for bulk matter processing, parallel production and proper AE2 autocrafting integration.

## What Makes It Different

- Up to **8 parallel processing threads**
- Accepts both **QMF-native** recipes and **DMA** recipes
- Pulls ingredients from the ME network automatically
- Pushes outputs back into ME storage automatically
- Supports AE2 automation through the **Quantum Pattern Hatch**

The QMF is the correct home for recipes that are too large, too expensive or too repetitive to keep on the DMA.

## Quantum Pattern Hatch

The new **Quantum Pattern Hatch** is the multiblock-facing pattern provider for this machine family.

- Stores up to **64 encoded patterns**
- Links itself to the controller when the structure assembles
- Exposes the controller to AE2 as a crafting machine
- Lets AE2 dispatch work directly into the multiblock threads

This removes the old gap where the machine could process recipes internally but could not participate cleanly in an AE2 autocrafting chain.

## Parallel Thread Model

Each controller has **8 processing threads**.

- Threads can run the same recipe or different recipes
- A pushed AE2 pattern reserves one free thread
- Idle threads can still auto-start valid jobs from ME storage
- Power, fluids and items are all pulled directly from the network

This means the machine scales by concurrency rather than by fake oversized slot stacks.

## Intended Use

Use the QMF for:

- bulk DMA-compatible recipes
- huge matter conversions
- long-running network-driven production
- expensive recipes with large ingredient counts

## See Also

- [Quantum Slicer](quantum-slicer.md)
- [Quantum Processor Assembler](quantum-processor-assembler.md)
- [Multiblock Tiers](multiblock-tiers.md)
