# Materials & Fluids

## Stellar Fragment Materials

### White Dwarf Fragments
Base stellar material. Forms: Ingot, Rod, Nugget, Dust, Block.
- **ID prefix**: `ufo:white_dwarf_fragment_*`
- **Crafting** (DMA): 2x Netherite Ingot + 4x Blue Ice + 4x AE2 Sky Stone + 250mB Gelid Cryotheum = 100K AE

### Neutron Star Fragments
Mid-tier. Forms: Ingot, Rod, Nugget, Dust, Block.
- **ID prefix**: `ufo:neutron_star_fragment_*`
- **Crafting** (DMA): 4x WD Ingot + Nether Star + Obsidian Matrix + 500mB Gelid Cryotheum = 500K AE

### Pulsar Fragments
High-tier. Forms: Ingot, Nugget, Dust, Block.
- **ID prefix**: `ufo:pulsar_fragment_*`
- **Crafting** (DMA): 2x NS Ingot + 2x Lodestone + 4x Lightning Rod + 1000mB Gelid Cryotheum = 1M AE

---

## Matter Progression

| Matter | Key Inputs | Energy | ID |
|--------|-----------|--------|-----|
| Neutronium Sphere | 9x NS Ingot | 1M | `ufo:neutronium_sphere` |
| Enriched Neutronium Sphere | Neutronium Sphere + 2x Quantum Anomaly | 8M | `ufo:enriched_neutronium_sphere` |
| Proto Matter | Enriched NS Sphere + UU Matter | 2M | `ufo:proto_matter` |
| Corporeal Matter | Proto Matter + 64x Iron Block | 5M | `ufo:corporeal_matter` |
| UU Matter Crystal | Amethyst Shard + 10K mB UU Matter | 20M | `ufo:uu_matter_crystal` |
| White Dwarf Matter | Corporeal + UU Crystal + WD Block/Fluid | 7M | `ufo:white_dwarf_matter` |
| Neutron Star Matter | Corporeal + UU Crystal + NS Block/Fluid | 9.5M | `ufo:neutron_star_matter` |
| Pulsar Matter | Corporeal + UU Crystal + Pulsar Block/Fluid | 12M | `ufo:pulsar_matter` |
| Dark Matter | 16x each WD/NS/Pulsar Matter + Transcending | 200M | `ufo:dark_matter` |

---

## Custom Fluids

| Fluid | Use | ID |
|-------|-----|-----|
| Liquid Starlight | Base fluid for most recipes | `ufo:source_liquid_starlight_fluid` |
| Gelid Cryotheum | Coolant + crafting | `ufo:source_gelid_cryotheum` |
| Temporal Fluid | Best coolant (100 HU/mB) | `ufo:source_temporal_fluid` |
| Spatial Fluid | Quantum catalysts | `ufo:source_spatial_fluid` |
| Primordial Matter | Component crafting | `ufo:source_primordial_matter_fluid` |
| Raw Star Matter Plasma | Stellar processing | `ufo:raw_star_matter_plasma` |
| Transcending Matter | Endgame recipes | `ufo:transcending_matter` |
| UU Matter | Replication | `ufo:uu_matter` |
| UU Amplifier | UU precursor | `ufo:source_uu_amplifier_fluid` |
| White Dwarf Fluid | WD Matter crafting | `ufo:source_white_dwarf_fragment_fluid` |
| Neutron Star Fluid | NS Matter crafting | `ufo:source_neutron_star_fragment_fluid` |
| Pulsar Fluid | Pulsar Matter crafting | `ufo:source_pulsar_fragment_fluid` |

### Bootstrap Chain (No Circular Dependencies)
1. Water + Snowball + Packed Ice → Dust Blizz
2. Dust Blizz + Snowball → Dust Cryotheum
3. Dust Blizz + Water → Gelid Cryotheum
4. Obsidian Matrix + Nether Star + UU Amplifier → Liquid Starlight

---

*See also: [Progression](progression.md) · [DMA](dma.md)*
