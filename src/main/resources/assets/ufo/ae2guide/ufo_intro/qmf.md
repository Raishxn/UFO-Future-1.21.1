---
navigation:
  parent: ufo_intro/index.md
  title: Quantum Matter Fabricator
  position: 42
item_ids:
  - ufo:quantum_matter_fabricator_controller
  - ufo:quantum_pattern_hatch
---

# Quantum Matter Fabricator

<BlockImage id="ufo:quantum_matter_fabricator_controller" scale="4"></BlockImage>

The **Quantum Matter Fabricator (QMF)** is the multiblock evolution of the DMA for heavy automation, bulk crafting and AE2 pattern workflows.

## Main Benefits
- Runs up to **8 parallel threads** at once.
- Accepts both native **QMF recipes** and **DMA recipes**.
- Supports AE2 autocrafting through the **Quantum Pattern Hatch**.
- Reads ingredients directly from the connected ME network.
- Pushes outputs back into ME automatically.

## Quantum Pattern Hatch

<BlockImage id="ufo:quantum_pattern_hatch" scale="3"></BlockImage>

The Pattern Hatch is the bridge between AE2 autocrafting and the multiblock.

- Stores up to **64 encoded patterns**.
- Links to the controller when the structure is assembled.
- Exposes the multiblock as a crafting machine to AE2.
- Lets the controller run multiple jobs in parallel instead of single-threading a whole chain.

## Thread Model
- Each free thread can accept one recipe job.
- Jobs can be equal or different.
- A pushed AE2 pattern reserves one thread until the job is finished.
- Idle threads can still auto-start valid recipes from the ME network.

## Tier Rules
- Recipes can require **MK1, MK2 or MK3**.
- Higher machine tiers can run lower-tier recipes faster and cheaper.
- Current scaling is **2x faster per tier above the recipe** and **25% less AE cost per extra tier**.

## Best Use Cases
- Massive DMA-compatible recipes
- Bulk matter conversion
- Expensive recipes with huge input counts
- AE2-driven automated production lines

*See also: [Dimensional Matter Assembler](dma.md) · [Multiblock Tiers](multiblock_tiers.md)*
