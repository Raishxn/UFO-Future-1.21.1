---
navigation:
  parent: ufo_intro/index.md
  title: Quantum Slicer
  position: 43
item_ids:
  - ufo:quantum_slicer_controller
  - ufo:quantum_pattern_hatch
---

# Quantum Slicer

<BlockImage id="ufo:quantum_slicer_controller" scale="4"></BlockImage>

The **Quantum Slicer** is the bulk slicing multiblock for turning blocks and high-volume materials into printed circuits and related intermediate parts.

## What It Does
- Uses the universal multiblock recipe system.
- Executes up to **8 parallel jobs**.
- Accepts AE2 encoded patterns through the **Quantum Pattern Hatch**.
- Pulls ingredients from ME and returns outputs automatically.

## Why Use It
- Replaces slow single-recipe processor prep.
- Handles printed-circuit batches at endgame scale.
- Works well as the first stage in a fully automated processor chain.

## Recommended Flow
1. Encode slicing patterns in AE2.
2. Insert them into the Quantum Pattern Hatch.
3. Let AE2 request the printed outputs.
4. Feed those outputs into the **Quantum Processor Assembler**.

*See also: [Quantum Processor Assembler](quantum_processor_assembler.md) · [Quantum Matter Fabricator](qmf.md)*
