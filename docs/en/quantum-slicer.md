# Quantum Slicer

The **Quantum Slicer** is the printed-component preparation multiblock. It handles the cutting stage for large processor and circuit pipelines.

## Core Behavior

- Converts bulk source materials into printed parts
- Uses the universal multiblock recipe system
- Supports up to **27 parallel jobs** in standard mode
- Drops to **9 parallel jobs** in Safe Mode
- Integrates with AE2 through the **Quantum Pattern Hatch**
- Uses direct ME network pull and push for ingredients and outputs

## Pattern Hatch

- The **Quantum Pattern Hatch** stores **72 encoded patterns**
- It links itself to the controller after the structure assembles
- AE2 can push jobs directly into the slicer without manual inventory handling

## Typical Factory Position

1. Bulk source blocks or materials enter the **Quantum Slicer**
2. Printed outputs move into the ME network
3. AE2 requests finished processors from the **Quantum Processor Assembler**

The result is a clean split between printed-part generation and processor assembly.
