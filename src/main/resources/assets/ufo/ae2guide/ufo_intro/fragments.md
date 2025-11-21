---
navigation:
  parent: ufo_intro/index.md
  title: Advanced Star Matter
  position: 20
---

# Advanced Star Matter (Ingot Guide)

The advanced materials used across UFO Future are **manufactured** — they are not mined directly from the world. These high-density ingots are produced in the **Dimensional Matter Assembler (DMA)** and form the backbone of late-game tech (processors, mega-storage, armor alloys, etc.).

This page documents the primary star-matter ingots, how to obtain them, and their typical uses.

---

## White Dwarf Ingot
**Role:** Base high-density ingot used in many mid-to-late-game components.

### Acquisition
White Dwarf Ingots are **crafted in the DMA** from compressed precursor materials and reactive fluids. Use the DMA recipe for `white_dwarf_ingot` to convert base components into this ingot.

<recipe id="ufo:dma/ingot/white_dwarf_ingot" />

### Common uses
- Parts for advanced machines
- Early-tier star-matter alloys
- Component for Thermal Resistor Exosuit pieces

---

## Neutron Star Ingot
**Role:** Denser and more energetic than White Dwarf; used for high-tier components.

### Acquisition
Produced in the DMA using more exotic inputs (higher energy cost and special fluids/catalysts).

<recipe id="ufo:dma/ingot/neutron_star_ingot" />

### Common uses
- Mega Co-Processors
- Advanced storage matrices
- High-tier armor components and tools

---

## Pulsar Ingot
**Role:** An advanced, high-yield ingot intended for endgame builds.

### Acquisition
Crafted in the DMA with top-tier inputs and typically requires strong catalysts and premium coolant (e.g., Transcending Matter Fluid).

<recipe id="ufo:dma/ingot/pulsar_ingot" />

### Common uses
- Top-tier processors and matrices
- Endgame armor upgrades and components
- High-value trade/gate items in progression-focused packs

---

## Other notable materials
The mod also includes several related high-density items and transitional materials used in ingot production:

- **UU-Matter Crystal** — a solid form used in certain DMA recipes and catalyst tiers. (See `uu_matter_crystal` recipes.) :contentReference[oaicite:3]{index=3}  
  <recipe id="ufo:dma/uu_matter_crystal" />

- **Dark Matter / Proto Matter / Transcending matter** — special-tier materials used as inputs for the very highest tier recipes or as fluids for coolant/processing steps. These typically appear later in the DMA recipe tree. :contentReference[oaicite:4]{index=4}

---

## Notes & best practices
- **DMA only:** These ingots are *not* mined — they must be assembled in the DMA. Make sure you have sufficient energy, catalysts, and coolant before attempting advanced recipes. :contentReference[oaicite:5]{index=5}
- **Catalyst & coolant guidance:** High-tier ingots commonly require T2/T3 catalysts (Matterflow/Overflux/Quantum) and premium coolants (Transcending Matter Fluid or Gelid Cryotheum). Use Overflux to reduce failure risk and Matterflow to reduce energy cost. :contentReference[oaicite:6]{index=6}
- **Recipe complexity rule:** Some DMA recipes enforce `ItemInputs + FluidInputs <= 4`. If a DMA recipe fails validation, check the number of distinct item/fluid inputs. :contentReference[oaicite:7]{index=7}
- **Server admins:** Consider gating Pulsar and Transcending-tier recipes behind progression (quests, boss drops, or research) to avoid early-game imbalance.

---

## Troubleshooting
- Recipe isn't showing in DMA: verify the recipe ID and that the DMA `data/` JSON is enabled in your datapack/mod.
- Frequent failures: add Overflux catalysts and stronger coolant, or reduce Chrono (speed) catalysts that increase heat. :contentReference[oaicite:8]{index=8}
- High energy drain: add Matterflow T2/T3 to reduce energy cost.

---

*If you want, I can:*
- Replace the `<recipe id="...">` placeholders with actual JSON examples (I+F <= 4 compliant).
- Add item icons and images for each ingot.
- Generate a corresponding Portuguese version.

