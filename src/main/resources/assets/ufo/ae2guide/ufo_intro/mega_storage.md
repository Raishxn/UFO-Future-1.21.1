---
navigation:
  parent: ufo_intro/index.md
  title: Mega Processors & Storage
  position: 50
item_ids:
  - ufo:1b_mega_crafting_storage
  - ufo:50b_mega_crafting_storage
  - ufo:1t_mega_crafting_storage
  - ufo:250t_mega_crafting_storage
  - ufo:1qd_mega_crafting_storage
  - ufo:50m_mega_co_processor
  - ufo:150m_mega_co_processor
  - ufo:300m_mega_co_processor
  - ufo:750m_mega_co_processor
  - ufo:2b_mega_co_processor
---
# Mega Storage & Crafting Processors — Player Guide

<BlockImage id="ufo:1b_mega_crafting_storage" scale="3"></BlockImage>

**Overview**
Mega Storage modules and Crafting Processors implement extremely high-capacity storage and parallelized crafting capabilities. They integrate well with the DMA for fabricating the components required at late-game scale.

---

## Mega Storage Tiers
- **High-capacity tiers** (examples): 1M, 256M, 1B, 1T, 1QD.
- Higher tiers require more energy and more complex components (crafted in DMA).

---

## Crafting Processor vs Mega Co-Processor
- **Crafting Processor**: a modular component that speeds up and parallelizes crafting tasks in storage networks.
- **Mega Co-Processor**: an advanced companion to Crafting Processors that enables multi-threaded or batched recipe execution for extremely complex recipes (often required for mass-producing late-game items).

---

## DMA role in Mega Storage
- DMA crafts **processors**, **co-processors**, and **high-density storage matrices** that form the backbone of Mega Storage.
- DMA recipes for processors typically require:
    - star-matter ingots / UU-Matter / Transcending Matter Fluids
    - catalysts (often Matterflow + Overflux for efficiency and stability)
    - high-tier coolant and large energy budgets

---

## Best practices for large-scale production
- **Parallelize**: use multiple DMAs for simultaneous fabrication of processor components.
- **Use Matterflow** T2/T3 to reduce the energy overhead for continuous production.
- **Monitor coolant** and keep coolant buffers for each DMA in case Chrono catalysts increase heat generation.
- Use **Overflux** to reduce failure rates on complex recipes which would otherwise be lost.

---

## Example: Building a Mega Crafting Node
1. Build the storage network and allocate slots for Crafting Processors and Mega Co-Processors.
2. Fabricate processors with DMA using Matterflow and Overflux to stabilize and save energy.
3. Install processors into the network and configure parallel crafting threads.
4. Use a pool of DMAs as a "fabrication farm" for producing additional processors and co-processors as demand increases.

---

## Admin notes
- Production of high-end processors can quickly destabilize server performance if unregulated. Introduce gating: research, quests, or multi-step crafting (e.g., require several separate DMA passes).
- Consider limiting Transcending Matter Fluid or T3 catalysts behind progression systems.

---

*End of Mega Storage & Crafting Processor guide.*
