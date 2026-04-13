# Quantum Matter Fabricator

The **Quantum Matter Fabricator (QMF)** is the multiblock evolution of the DMA for bulk processing, parallel jobs and AE2 autocrafting.

## Core Behavior

- Up to **27 parallel threads** in standard mode
- **9 parallel threads** in Safe Mode
- Accepts both **QMF** recipes and **DMA** recipes
- Pulls ingredients directly from the ME network
- Pushes outputs back into ME storage
- Uses the **Quantum Pattern Hatch** for automation

## Quantum Pattern Hatch

- Stores up to **72 encoded patterns**
- Links to the controller when the structure assembles
- Exposes the multiblock to AE2 as a crafting machine
- Lets AE2 dispatch work to free threads

## Parallel Model

Each free thread can run one recipe copy.

- One AE2 pattern reserves one free thread
- Idle threads can still auto-start valid work
- Items, fluids and AE all come from the ME network
