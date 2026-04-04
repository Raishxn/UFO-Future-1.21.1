# Dimensional Matter Assembler (DMA)

The **Dimensional Matter Assembler** is the core machine of UFO Future. Every advanced item, fluid, material, and component in the mod is crafted through the DMA. It is an AE2-powered machine with shapeless item inputs, fluid inputs, a coolant system, and support for catalyst upgrades.

---

## Crafting the DMA

```
O P O
C M C
O E O
```

| Symbol | Item |
|--------|------|
| O | Obsidian Matrix |
| P | AE2 Engineering Processor |
| C | Graviton Plated Casing |
| M | AE2 Controller Block |
| E | Ender Eye |

> **Prerequisite**: You need access to AE2 and a Graviton Plated Casing first.

---

## GUI Overview

The DMA GUI has the following layout:

| Section | Description |
|---------|-------------|
| **9-slot Input Grid** | Shapeless item inputs (order does not matter) |
| **2 Output Slots** | Item/Fluid outputs appear here |
| **Coolant Tank** (Slot 2) | Player-managed coolant fluid — NOT consumed by recipes |
| **Base Fluid Tank** (Slot 3) | Recipe-required fluid input — consumed by recipes |
| **4 Upgrade Slots** | For Catalyst upgrade cards |
| **Progress Bar** | Shows processing progress |
| **Energy Display** | Shows current AE power stored and consumption rate |
| **Temperature Gauge** | Shows current heat level vs max capacity |

---

## How It Works

1. **Place items** in the 9-slot input grid. **Order does not matter** — all recipes are shapeless.
2. **Add base fluid** to the right fluid tank (Slot 3) if the recipe requires it.
3. **Add coolant** to the left fluid tank (Slot 2) to prevent overheating.
4. **Insert catalysts** into the 4 upgrade slots to modify performance.
5. **Connect AE2 power** — the DMA is an AE2 networked block entity. It draws power from the ME network or from internal buffer.
6. Machine starts processing when a valid recipe match is found and enough power is available.

---

## Thermal System

The DMA generates heat while processing and must be cooled to prevent catastrophic failure.

### Heat Zones

| Zone | Threshold | Effects |
|------|-----------|---------|
| **Safe** | < 50% capacity | Normal operation |
| **Hazard** | ≥ 50% capacity | Spinning flame particle rings, burns nearby players without thermal armor |
| **Meltdown** | 100% capacity | 5-second countdown, then **explosion** destroying the machine and surrounding blocks |

### Heat Generation
- **While working**: +1 HU every 2 ticks (≈ +10 HU/s base), multiplied by catalyst heat modifiers
- **While idle**: Passive cooling −1 HU every 40 ticks (≈ −0.5 HU/s)

### Coolant Effectiveness

| Coolant | Efficiency | Notes |
|---------|-----------|-------|
| **Temporal Fluid** | 100 HU per 1 mB | Most efficient — endgame coolant |
| **Liquid Starlight** | 30 HU per 1 mB | Good mid-game option |
| **Default fluids** | 15 HU per 1 mB | Any unrecognized fluid |
| **Gelid Cryotheum** | 1 HU per 200 mB | Volume-based — needs bulk pumping |

### Meltdown Explosion
When temperature reaches 100% of max capacity:
1. A **5-second countdown** begins (100 ticks)
2. **Alarm sound** plays every second
3. **Red actionbar warning** appears to nearby players: `"CRITICAL OVERLOAD IN X SECONDS!"`
4. At 0, the machine **explodes** (power 10.0, block-breaking)
5. A **global chat alert** broadcasts the explosion coordinates to all players on the server

> **Thermal Resistor Exosuit** provides full immunity to DMA heat damage. See [Armor](armor.md).

---

## Recipe JSON Format

All DMA recipes use the type `ufo:dimensional_assembly`. The JSON format is:

```json
{
  "type": "ufo:dimensional_assembly",
  "item_inputs": [
    {
      "ingredient": { "item": "minecraft:netherite_ingot" },
      "count": 2
    },
    {
      "ingredient": { "item": "minecraft:blue_ice" },
      "count": 4
    }
  ],
  "fluid_inputs": [
    {
      "ingredient": { "fluid": "ufo:source_liquid_starlight_fluid" },
      "amount": 250
    }
  ],
  "item_outputs": [
    {
      "id": "ufo:white_dwarf_fragment_ingot",
      "amount": 1
    }
  ],
  "fluid_outputs": [],
  "energy": 100000,
  "time": 200
}
```

See the [KubeJS Recipes](kubejs-recipes.md) page for scripted recipe creation.

---

## Power Consumption

- Base internal buffer: **500,000 AE**
- **Matterflow catalysts** can increase the buffer by 10x/100x/1000x per catalyst
- Power consumption is calculated per tick based on `energy / (recipe_time / speed_factor)`
- **Power Multiplier** from catalysts reduces effective cost

---

## Auto-Export

The DMA supports **auto-export** via the config manager setting. When enabled, finished outputs are automatically pushed to adjacent inventories/ME storage.

---

*See also: [Catalysts](catalysts.md) · [KubeJS Recipes](kubejs-recipes.md) · [Progression](progression.md)*
