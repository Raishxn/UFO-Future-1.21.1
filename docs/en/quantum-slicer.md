# Quantum Slicer

The **Quantum Slicer** is the printed-component preparation multiblock. It handles the cutting stage for large processor and circuit pipelines.

## Core Behavior

- Converts bulk source materials into printed parts
- Uses the universal multiblock recipe system
- Supports up to **27 parallel jobs** in standard mode
- Drops to **9 parallel jobs** in Safe Mode
- Integrates with AE2 through a **Quantum Pattern Buffer** or linked **Quantum Pattern Proxy**
- Uses direct ME network pull and push for ingredients and outputs

## Pattern Buffer and Proxy

- Exactly one Buffer or Proxy is required in the structure.
- A Buffer stores **72 encoded patterns**; a linked Proxy shares another Buffer.
- AE2 can dispatch jobs without manual inventory handling.

## Typical Factory Position

1. Bulk source blocks or materials enter the **Quantum Slicer**
2. Printed outputs move into the ME network
3. AE2 requests finished processors from the **Quantum Processor Assembler**

The result is a clean split between printed-part generation and processor assembly.
