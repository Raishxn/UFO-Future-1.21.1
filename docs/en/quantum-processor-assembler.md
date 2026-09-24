# Quantum Processor Assembler

The **Quantum Processor Assembler** is the finishing multiblock for processor lines. It takes printed circuits and support materials, then assembles final processors at endgame scale.

## Main Features

- Up to **27 parallel jobs** in standard mode
- **9 parallel jobs** while Safe Mode is enabled
- Universal multiblock recipe support
- AE2 autocrafting integration through a **Quantum Pattern Buffer** or linked **Quantum Pattern Proxy**
- Direct network pull for ingredients and direct network push for outputs

## Pattern Buffer and Proxy

- Exactly one Buffer or Proxy is required in the structure.
- A Buffer stores **72 encoded patterns**; a linked Proxy shares another Buffer.
- Patterns reserve free threads instead of forcing single-job execution.

## Factory Chain

- **Quantum Slicer** makes the printed parts
- **Quantum Processor Assembler** turns them into final processors
- **QMF** covers large DMA-style matter recipes and other heavy conversions
