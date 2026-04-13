# Quantum Processor Assembler

The **Quantum Processor Assembler** is the finishing multiblock for processor production. It takes printed circuits and support materials, then assembles final processors at endgame scale.

## Main Features

- Up to **27 parallel jobs** in standard mode
- **9 parallel jobs** in Safe Mode
- Universal multiblock recipe support
- AE2 autocrafting through the **Quantum Pattern Hatch**
- Direct network pull for ingredients and direct push for outputs

## Pattern Hatch

- The **Quantum Pattern Hatch** stores **72 encoded patterns**
- It exposes the controller to AE2 as a crafting machine
- Each pattern uses one free thread instead of blocking the whole machine

## Factory Chain

- **Quantum Slicer** makes printed parts
- **Quantum Processor Assembler** finishes processors
- **QMF** handles heavy DMA-style conversions
