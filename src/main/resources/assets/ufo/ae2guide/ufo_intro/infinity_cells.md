---
navigation:
  parent: ufo_intro/index.md
  title: Infinity Cells — Reference & DMA Fabrication
  position: 40
---

# Infinity Cells — Reference & DMA Fabrication

**Purpose**
Infinity Cells are endgame storage/production artifacts that provide effectively infinite access to a single resource when placed into a compatible storage network (e.g., ME/AE2 style systems). They are expensive and typically crafted only in the DMA.

---

## Common Types
- **Fluid Infinity Cells** — e.g., Water, Lava, Liquid Starlight.
- **Item Infinity Cells** — base blocks like Cobblestone, Sand, Obsidian, Glass.
- **Advanced Infinity Cells** — antimatter pellets, star-matter, etc. (very high cost).

---

## How to use
1. Insert the cell into a compatible storage network slot or pedestal that recognizes Infinity Cells.
2. Configure access/permissions to prevent accidental extraction or duplication exploits.
3. Remember: Infinity Cells are powerful—consider server policies or gating.

---

## Fabrication (DMA-specific)
Infinity Cells are **crafted only in the DMA**. Typical recipe pattern:
- Item inputs (1–4) + fluid inputs (0–4) — obey DMA validation rules (many advanced DMA recipes enforce `ItemInputs + FluidInputs <= 4`).
- Catalysts: recipes generally require at least one catalyst. Common setups:
    - **Efficiency builds**: Matterflow T2/T3 to lower energy cost.
    - **Stability builds**: Overflux T2/T3 for safer runs (recommended for advanced Infinity Cells).
    - **Speed builds**: Chrono T2/T3 to speed up processing (ensure coolant reserve).
    - **Yield builds**: Quantum catalysts for chance at extra byproducts (rarely used for basic Infinity Cells).
- Coolant: Spatial Fluid for mid-tier, Transcending Matter Fluid for advanced Infinity Cells.

**Example (simplified)**:
- Inputs: 8×Glass (or recipe-specific inputs)
- Fluid: none or small special fluid
- Catalysts: Matterflow T2 + Overflux T1
- Coolant: Spatial Fluid
- Result: Infinity Glass Cell (example)

---

## Balancing & Server recommendations
- Infinity Cells often remove the need to farm certain resources—consider making advanced cells require multi-stage crafting or quest unlocks.
- You may wish to limit which Infinity Cells are craftable by default on multiplayer servers.

---

## Troubleshooting
- Recipe fails validation: check I+F rule (Item count + Fluid count).
- DMA fails or burns outputs: add Overflux, stronger coolant, or reduce Chrono.

---

*End of Infinity Cells reference.*
