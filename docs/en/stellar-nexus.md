# Stellar Nexus — Complete Guide

The **Stellar Nexus** is the pinnacle of UFO Future technology. This massive **35×34×35 block** multiblock machine simulates stellar phenomena to produce endgame resources at a scale measured in **millions**.

> ⚡ **Endgame Content** — Requires 150M to 1G AE per cycle, rare materials, and a dedicated cooling infrastructure.

---

## Table of Contents

- [Overview](#overview)
- [Structure & Assembly](#structure--assembly)
- [The Controller GUI](#the-controller-gui)
- [Energy System](#energy-system)
- [Fuel & Coolant System](#fuel--coolant-system)
- [Thermal Management & Safe Mode](#thermal-management--safe-mode)
- [Catastrophic Explosion](#catastrophic-explosion)
- [Simulation Programs](#simulation-programs)
- [Visual Rendering](#visual-rendering)
- [KubeJS Integration](#kubejs-integration)
- [Troubleshooting](#troubleshooting)

---

## Overview

The Stellar Nexus is the most powerful machine in UFO Future. It operates by simulating stellar-scale events (star fusion, supernovae, exotic matter synthesis) inside a containment field. The machine:

1. **Charges** AE energy passively from your ME network over time.
2. **Consumes** items, fluids, fuel, and energy to start a simulation.
3. **Generates heat** during operation that must be managed with coolant.
4. **Produces** massive quantities of resources injected directly into your ME network.

The process cycle takes **20 to 40 minutes** per run, but yields millions of items per cycle.

---

## Structure & Assembly

### Dimensions
- **Width**: 35 blocks
- **Height**: 34 blocks
- **Depth**: 35 blocks
- **Total volume**: ~41,650 blocks

### Required Blocks

| Block | Purpose | Count |
|-------|---------|-------|
| Entropy Singularity Casing | Main structural frame | ~800+ |
| Entropy Assembler Core Casing | Inner core shell | ~200+ |
| Entropy Computer Condensation Matrix | Processing nodes | 4 |
| Stellar Field Generator (Mk.I, II, or III) | Tier gate for simulations | 4 |
| ME Massive Output Hatch | Injects item outputs into ME | 1 |
| ME Massive Fluid Hatch | Injects fluid outputs into ME | 1 |
| ME Massive Input Hatch | Extracts inputs from ME | 1 |
| AE Energy Input Hatch | Charges the energy buffer | 1 |
| Stellar Nexus Controller | The brain — placed last | 1 |

### Assembly Steps

1. Build the 35×34×35 structure following the pattern.
2. Place all 4 hatches in any `B` (Singularity Casing) position.
3. Connect hatches to your ME network with Dense Smart Cables.
4. Place the **Controller** last.
5. Right-click the Controller with a **Structure Scanner** to verify.
6. If valid, the Controller displays "Assembled" and a star appears at the center.

### Structure Scanner

| Action | Result |
|--------|--------|
| Right-Click Controller | Shows missing/misplaced blocks (up to 10 in chat) |
| Shift + Right-Click (Creative) | Auto-builds the entire structure instantly |

---

## The Controller GUI

The Controller GUI provides complete monitoring and control:

| Element | Description |
|---------|-------------|
| **Simulation Selector** | `◀` / `▶` arrows to browse available programs |
| **Progress Bar** | Shows simulation completion percentage |
| **Energy Gauge** | Global energy buffer (max 20 billion AE) |
| **Heat Gauge** | Current temperature (0.0% — 100.0%) |
| **Safe Mode Toggle** | Toggles automatic overheat protection |
| **▶ START Button** | Begins the simulation after validating all requirements |
| **Error Messages** | Red text showing exactly what's missing (e.g., "Missing: 3/4x Enriched Neutronium Sphere") |

---

## Energy System

The Stellar Nexus uses a **global energy buffer** of **20 billion AE** that charges passively from your ME network when the machine is idle.

### Charging Rates

| Field Generator | AE per Tick | Time to Full |
|----------------|-------------|--------------|
| Mk.I | 500,000 | ~11.1 hours |
| Mk.II | 1,000,000 | ~5.5 hours |
| Mk.III | 2,000,000 | ~2.7 hours |

> 💡 **Tip**: The energy buffer persists between simulations. Keep the machine idle and assembled to charge continuously.

### Energy Consumption
- Energy is consumed **instantly** when you click START.
- If Safe Mode is ON, effective energy cost is **2.5× the recipe value**.

---

## Fuel & Coolant System

Each simulation recipe specifies a **fuel fluid** and a **coolant fluid**. Both are consumed from your **ME network storage** (not from separate tanks).

### Fuel
- Consumed **on start** along with energy and item/fluid inputs.
- Common fuels: `mekanism:hydrogen`, `mekanism:ethene`
- Typical amounts: 20,000 mB per cycle.
- In Safe Mode, fuel consumption is **2.5× higher**.

### Coolant
- Consumed **every tick** during operation to reduce heat.
- Common coolants: `ufo:source_gelid_cryotheum`, `ufo:source_temporal_fluid`
- If the coolant runs out during operation, heat rises unchecked.

---

## Thermal Management & Safe Mode

### Heat Generation
During operation, heat increases every tick based on the recipe's `cooling_level + 1`:

| Cooling Level | Heat per Tick |
|---------------|---------------|
| 0 | +1 |
| 1 | +2 |
| 2 | +3 |
| 3 | +4 |

### Safe Mode (Default: ON)

| | Safe Mode ON | Safe Mode OFF |
|--|-------------|---------------|
| **Resource Cost** | 2.5× fuel, energy, coolant | 1× (normal) |
| **At 100% Heat** | Auto-shutdown + 30min cooldown | ⚠️ Catastrophic explosion |
| **Risk Level** | None | Extreme |
| **Recommended** | ✅ Always | ❌ Only if you're confident |

> ⚠️ **Safe Mode uses 2.5× resources** but prevents destruction. Consider it an insurance premium.

---

## Catastrophic Explosion

If Safe Mode is **OFF** and heat reaches 100%, the Stellar Nexus undergoes a **catastrophic stellar explosion**:

### Explosion Details—
- **Radius**: 50 blocks from the Controller
- **Duration**: 3 seconds (60 ticks), spread across multiple ticks to reduce lag
- **Inner zone** (0–20 blocks): All blocks replaced with **lava**
- **Outer zone** (20–50 blocks): Blocks destroyed, **fire** placed above
- **Entity damage**: Up to 200 HP based on distance, plus 30 seconds of fire
- **Sub-explosions**: Vanilla explosions every 5 ticks for dramatic effect
- **Machine destruction**: The multiblock disassembles completely

### After the Explosion
- The machine is fully disassembled.
- The energy buffer is wiped to 0.
- All progress is lost.
- The surrounding area is devastated.

---

## Simulation Programs

### Tier 1 — Mk.I Field Generator

#### Iron Core Fusion
| | |
|--|--|
| **Energy** | 150,000,000 AE |
| **Duration** | 20 minutes (24,000 ticks) |
| **Fuel** | 20,000 mB Hydrogen |
| **Coolant** | 10,000 mB Gelid Cryotheum |
| **Cooling Level** | 1 |
| **Item Inputs** | 4x Enriched Neutronium Sphere |
| **Fluid Inputs** | 100,000 mB Raw Star Matter Plasma |
| **Outputs** | 15M Iron Ingot, 10M Gold Ingot, 10M Copper Ingot |

#### Red Giant Collapse
| | |
|--|--|
| **Energy** | 150,000,000 AE |
| **Duration** | 20 minutes (24,000 ticks) |
| **Fuel** | 20,000 mB Hydrogen |
| **Coolant** | 10,000 mB Gelid Cryotheum |
| **Cooling Level** | 1 |
| **Item Inputs** | 4x Enriched Neutronium Sphere |
| **Fluid Inputs** | 100,000 mB Raw Star Matter Plasma |
| **Outputs** | 15M Redstone, 10M Lapis Lazuli, 10M Glowstone Dust |

---

### Tier 2 — Mk.II Field Generator

#### Diamond Pressure
| | |
|--|--|
| **Energy** | 350,000,000 AE |
| **Duration** | 25 minutes (30,000 ticks) |
| **Fuel** | 20,000 mB Hydrogen |
| **Coolant** | 25,000 mB Gelid Cryotheum |
| **Cooling Level** | 2 |
| **Item Inputs** | 8x Enriched Neutronium Sphere |
| **Fluid Inputs** | 200,000 mB Raw Star Matter Plasma |
| **Outputs** | 10M Diamond, 10M Emerald, 10M Amethyst Shard |

#### Neutron Bombardment
| | |
|--|--|
| **Energy** | 350,000,000 AE |
| **Duration** | 25 minutes (30,000 ticks) |
| **Fuel** | 20,000 mB Hydrogen |
| **Coolant** | 25,000 mB Gelid Cryotheum |
| **Cooling Level** | 2 |
| **Item Inputs** | 8x Enriched Neutronium Sphere |
| **Fluid Inputs** | 250,000 mB Raw Star Matter Plasma |
| **Outputs** | 10M Netherite Scrap, 10M Quartz, 10M Ender Pearl |

---

### Tier 3 — Mk.III Field Generator

#### Supernova Harvest
| | |
|--|--|
| **Energy** | 750,000,000 AE |
| **Duration** | 30 minutes (36,000 ticks) |
| **Fuel** | 20,000 mB Ethene |
| **Coolant** | 50,000 mB Temporal Fluid |
| **Cooling Level** | 3 |
| **Item Inputs** | 16x Charged Enriched Neutronium Sphere, 4x Aether Containment Capsule |
| **Fluid Inputs** | 500,000 mB Raw Star Matter Plasma |
| **Outputs** | 15M Iron Ingot, 12M Gold Ingot, 12M Diamond, 10M Netherite Ingot, 5M Nether Star |

#### Stellar Synthesis
| | |
|--|--|
| **Energy** | 1,000,000,000 AE (1 Billion) |
| **Duration** | 40 minutes (48,000 ticks) |
| **Fuel** | 20,000 mB Ethene |
| **Coolant** | 50,000 mB Temporal Fluid |
| **Cooling Level** | 3 |
| **Item Inputs** | 16x Charged Enriched Neutronium Sphere, 8x Aether Containment Capsule, 4x Dark Matter |
| **Fluid Inputs** | 500,000 mB Raw Star Matter Plasma, 250,000 mB Transcending Matter |
| **Outputs** | 10M Pulsar Dust, 5M White Dwarf Matter, 2.5M Neutron Star Matter, 250K Liquid Starlight |

---

## Visual Rendering

When assembled, the Stellar Nexus renders a stunning 3D scene inside the multiblock:

| Layer | Model | Description |
|-------|-------|-------------|
| 🌌 Space Shell | `space.obj` | Large translucent sphere wrapping the entire structure, slowly rotating |
| ⭐ Central Star | `star.obj` | Bright rotating star at the center, pulsing during operation |
| 🌍🔥🟣 Orbiting Dimensions | `star.obj` + dim textures | Three small spheres (Overworld, Nether, End) orbiting the star at different speeds |

The star model changes based on the active simulation:
- **Default**: Regular yellow star
- **Neutron Bombardment**: Blue star
- **Stellar Synthesis**: Neutron star (white-blue)

---

## KubeJS Integration

Modpack developers can add custom Stellar Nexus simulation programs using KubeJS.

### Recipe Type

```
ufo:stellar_simulation
```

### Complete Recipe Example

```js
// kubejs/server_scripts/stellar_recipes.js

ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:stellar_simulation',

    // Display name shown in the Controller GUI
    simulation_name: 'Void Harvester',

    // Item inputs — consumed from ME network on start
    item_inputs: [
      {
        amount: 8,
        ingredient: { item: 'ufo:enriched_neutronium_sphere' }
      },
      {
        amount: 4,
        ingredient: { item: 'minecraft:nether_star' }
      }
    ],

    // Fluid inputs — consumed from ME network on start
    fluid_inputs: [
      {
        amount: 200000,
        ingredient: { fluid: 'ufo:raw_star_matter_plasma' }
      }
    ],

    // Item outputs — AE2 GenericStack format
    // '#' = amount, '#t' = 'ae2:i' (item) or 'ae2:f' (fluid), 'id' = item/fluid ID
    item_outputs: [
      { '#': 5000000, '#t': 'ae2:i', id: 'minecraft:ender_pearl' },
      { '#': 2000000, '#t': 'ae2:i', id: 'minecraft:blaze_rod' }
    ],

    // Fluid outputs — same GenericStack format with '#t': 'ae2:f'
    fluid_outputs: [
      { '#': 100000, '#t': 'ae2:f', id: 'ufo:liquid_starlight' }
    ],

    // AE energy consumed on start (150M = 150,000,000)
    energy: 300000000,

    // Duration in ticks (20 ticks = 1 second, 24000 = 20 minutes)
    time: 30000,

    // Thermal stress level (0-3). Higher = more heat per tick
    cooling_level: 2,

    // Minimum Stellar Field Generator tier required (1-3)
    field_tier: 2,

    // Fuel fluid consumed from ME storage on start
    fuel_fluid: 'mekanism:hydrogen',
    fuel_amount: 20000,

    // Coolant fluid consumed every tick during operation
    coolant_fluid: 'ufo:source_gelid_cryotheum',
    coolant_amount: 25000
  }).id('kubejs:void_harvester_simulation')
})
```

### Recipe Fields Reference

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `type` | String | ✅ | Must be `ufo:stellar_simulation` |
| `simulation_name` | String | ✅ | Name displayed in the Controller GUI |
| `item_inputs` | Array | ✅ | Items consumed from ME network |
| `fluid_inputs` | Array | ❌ | Fluids consumed from ME network |
| `item_outputs` | Array | ❌ | Items injected into ME (AE2 GenericStack format) |
| `fluid_outputs` | Array | ❌ | Fluids injected into ME (AE2 GenericStack format) |
| `energy` | Integer | ✅ | Total AE energy cost |
| `time` | Integer | ✅ | Duration in ticks (20 ticks = 1 second) |
| `cooling_level` | Integer | ✅ | Thermal stress (0–3) |
| `field_tier` | Integer | ✅ | Minimum Field Generator tier (1–3) |
| `fuel_fluid` | String | ❌ | Fuel fluid registry ID (e.g., `mekanism:hydrogen`) |
| `fuel_amount` | Integer | ❌ | Fuel amount in mB |
| `coolant_fluid` | String | ❌ | Coolant fluid registry ID |
| `coolant_amount` | Integer | ❌ | Coolant amount in mB |

### AE2 GenericStack Output Format

Outputs use AE2's GenericStack format:

```json
{ "#": 15000000, "#t": "ae2:i", "id": "minecraft:iron_ingot" }
```

| Key | Meaning |
|-----|---------|
| `#` | Amount (supports values above Integer.MAX_VALUE) |
| `#t` | Type: `ae2:i` for items, `ae2:f` for fluids |
| `id` | Registry ID of the item or fluid |

### Removing / Modifying Recipes

```js
ServerEvents.recipes(event => {
  // Remove a specific simulation
  event.remove({ id: 'ufo:stellar_simulation/iron_core_fusion' })

  // Remove ALL stellar simulations
  event.remove({ type: 'ufo:stellar_simulation' })

  // To modify: remove original, then re-add with changes
  event.remove({ id: 'ufo:stellar_simulation/diamond_pressure' })
  event.custom({
    type: 'ufo:stellar_simulation',
    simulation_name: 'Diamond Pressure (Modified)',
    // ... your modified recipe fields ...
  }).id('ufo:stellar_simulation/diamond_pressure')
})
```

### Tips for Custom Recipes

1. **Energy scaling**: T1 recipes use 150M AE, T2 use 350M, T3 use 750M–1G.
2. **Time**: 24,000 ticks = 20 minutes. Most recipes run 20–40 minutes.
3. **Cooling level**: Higher cooling_level means heat builds faster. Match with appropriate coolant.
4. **Safe Mode penalty**: Players using Safe Mode consume 2.5× your specified fuel/energy amounts.
5. **Fuel fluids**: Any registered fluid works. Mekanism gases (Hydrogen, Ethene) are commonly used.
6. **Output amounts**: The `#` field supports very large numbers (millions). This is why the Stellar Nexus exists.

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Machine won't assemble | Use Structure Scanner to find missing blocks |
| "No ME network connection" | Ensure hatches are connected via Dense Smart Cables |
| "Energy insufficient" | Wait for the energy buffer to charge (check gauge in GUI) |
| "Missing X" errors | Items/fluids must be in your ME network storage |
| Heat rising too fast | Ensure coolant fluid is stocked in ME network |
| Pink/black textures | Texture rendering issue — ensure mod is up to date |
| Star not visible | Machine must be assembled; star only renders when assembled |

---

*See also: [DMA](dma.md) · [KubeJS Recipes](kubejs-recipes.md) · [Materials & Fluids](materials.md) · [Crafting Progression](progression.md)*
