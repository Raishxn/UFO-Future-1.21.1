# Armor Sets

UFO Future adds two armor sets with unique mechanics: the **Thermal Resistor Exosuit** (mid-game, heat protection) and the **UFO Armor** (endgame, full god-mode with flight).

---

## Thermal Resistor Exosuit

A specialized armor set designed to protect against the DMA's hazard zone heat damage. This is a prerequisite for safely operating high-temperature DMA setups.

### Stats & Abilities (per piece)

| Feature | Value |
|---------|-------|
| Base Armor | Standard UFO Armor tier |
| Fire Immunity | ✅ Extinguishes fire instantly |
| +15% Mining Efficiency | Per piece (attribute modifier) |
| Fire Resistant Item | Cannot be destroyed by fire/lava |

### Full Set Bonus

When **all 4 pieces** (Helmet, Chestplate, Leggings, Boots) are equipped:

| Bonus | Description |
|-------|-------------|
| Resistance II | Permanent damage reduction (amplifier 1) |
| Fire/Lava Immunity | Complete immunity to fire and lava damage |
| DMA Heat Immunity | Safe from DMA hazard zone burn damage |
| Thermal Stability | Full thermal protection from all sources |

> The Thermal Resistor Exosuit is the **only** armor that protects from DMA heat damage in the hazard zone (≥50% heat).

### Crafting

#### Thermal Resistor Plating (Material)
**DMA Recipe**:
- Netherite Ingot ×1 + Obsidian Matrix ×1
- 500 mB Gelid Cryotheum
- 50,000 AE, 100 ticks

#### Armor Pieces (DMA Recipes)

| Piece | Plating Count | Energy | Time |
|-------|--------------|--------|------|
| Mask (Helmet) | 5 | 1,000,000 AE | 500 ticks |
| Chest | 8 | 1,500,000 AE | 500 ticks |
| Pants (Leggings) | 7 | 1,200,000 AE | 500 ticks |
| Boots | 4 | 800,000 AE | 500 ticks |

All armor pieces require 1,000 mB Gelid Cryotheum as fluid input.

### Item IDs

| Piece | Registry ID |
|-------|-------------|
| Thermal Resistor Plating | `ufo:thermal_resistor_plating` |
| Mask | `ufo:thermal_resistor_mask` |
| Chest | `ufo:thermal_resistor_chest` |
| Pants | `ufo:thermal_resistor_pants` |
| Boots | `ufo:thermal_resistor_boots` |

---

## UFO Armor (Endgame)

The ultimate armor set in UFO Future. Requires the Thermal Resistor Exosuit as a base and grants god-like powers when worn as a full set.

### Stats & Abilities

| Feature | Value |
|---------|-------|
| Base Armor | Highest tier in the mod |
| **RF-Powered** | Each piece stores and consumes RF energy |
| Energy Bar | Dynamic energy HUD on each piece |
| Rainbow Name | Animated rainbow item name effect |

### Full Set Bonus (When RF Available)

When **all 4 pieces** are equipped and have sufficient RF energy:

| Bonus | Description |
|-------|-------------|
| **Resistance X** | Near-invulnerability (amplifier 9) |
| **Night Vision** | Permanent night vision |
| **Creative Flight** | Full creative-mode flight ability |
| **+40 Max Health** | Adds 20 extra hearts (+40 HP) |

### Energy Consumption
- **400 RF per second** (20 RF/tick) per piece
- Drained every 20 ticks to avoid equip sound spam
- All effects are removed immediately when any piece runs out of energy
- Flight is disabled (dropped to ground) when energy depletes

### Crafting (DMA Recipes)

Each piece requires the corresponding Thermal Resistor piece as input:

| Piece | Inputs | Energy | Time |
|-------|--------|--------|------|
| Helmet | Thermal Resistor Mask + 2× Enriched Neutronium Sphere + Quantum Anomaly | 50,000,000 AE | 2,000 ticks |
| Chestplate | Thermal Resistor Chest + 2× Enriched Neutronium Sphere + Quantum Anomaly | 50,000,000 AE | 2,000 ticks |
| Leggings | Thermal Resistor Pants + 2× Enriched Neutronium Sphere + Quantum Anomaly | 50,000,000 AE | 2,000 ticks |
| Boots | Thermal Resistor Boots + 2× Enriched Neutronium Sphere + Quantum Anomaly | 50,000,000 AE | 2,000 ticks |

All pieces require 2,000 mB Transcending Matter as fluid input.

### Item IDs

| Piece | Registry ID |
|-------|-------------|
| Helmet | `ufo:ufo_helmet` |
| Chestplate | `ufo:ufo_chestplate` |
| Leggings | `ufo:ufo_leggings` |
| Boots | `ufo:ufo_boots` |

---

*See also: [Tools](tools.md) · [Progression](progression.md)*
