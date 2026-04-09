---
navigation:
  parent: ufo_intro/index.md
  title: Quantum Processor Assembler
  position: 44
item_ids:
  - ufo:quantum_processor_assembler_controller
  - ufo:quantum_pattern_hatch
---

# Quantum Processor Assembler

<BlockImage id="ufo:quantum_processor_assembler_controller" scale="4"></BlockImage>

The **Quantum Processor Assembler** is the bulk processor-finishing multiblock. It consumes printed circuits plus support materials and assembles final processors at endgame throughput.

## Features
- Up to **8 parallel crafting threads**.
- AE2 pattern support through the **Quantum Pattern Hatch**.
- Universal multiblock recipe support for processor families from multiple mods.
- Direct ME network pull/push behavior for items and fluids.

## In The Factory Chain
- **Quantum Slicer** prepares printed circuits.
- **Quantum Processor Assembler** turns them into final processors.
- **QMF** covers large DMA-style matter recipes and other heavy conversions.

## Parallelism
Parallel jobs do not need to be identical. One controller can finish multiple processor lines at the same time as long as threads, power and materials are available.

*See also: [Quantum Slicer](quantum_slicer.md) · [Multiblock Tiers](multiblock_tiers.md)*
