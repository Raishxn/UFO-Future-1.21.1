# KubeJS Custom Recipes — DMA & Stellar Nexus

This page documents how to create, modify, and remove recipes for the **Dimensional Matter Assembler (DMA)** and the **Stellar Nexus** multiblock using [KubeJS](https://kubejs.com/) for Minecraft 1.21.1 (NeoForge).

---

## Recipe Type

```
ufo:dimensional_assembly
```

---

## Basic Recipe Structure

Every DMA recipe consists of:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `item_inputs` | Array of `IngredientStack.Item` | ✅ | Item ingredients with counts |
| `fluid_inputs` | Array of `IngredientStack.Fluid` | ❌ | Fluid ingredients with amounts (mB) |
| `item_outputs` | Array of `GenericStack` | ❌ | Item outputs with amounts |
| `fluid_outputs` | Array of `GenericStack` | ❌ | Fluid outputs with amounts |
| `energy` | Integer | ✅ | Total AE energy consumed for this recipe |
| `time` | Integer | ✅ | Processing time in ticks (20 ticks = 1 second) |

> At least one output (item or fluid) must be specified.

---

## Creating Recipes with KubeJS

### Adding a Simple Item Recipe

```js
// kubejs/server_scripts/dma_recipes.js

ServerEvents.recipes(event => {
  // Create a custom DMA recipe
  event.custom({
    type: 'ufo:dimensional_assembly',
    item_inputs: [
      {
        ingredient: { item: 'minecraft:diamond' },
        count: 4
      },
      {
        ingredient: { item: 'minecraft:netherite_ingot' },
        count: 1
      }
    ],
    fluid_inputs: [
      {
        ingredient: { fluid: 'ufo:source_liquid_starlight_fluid' },
        amount: 500
      }
    ],
    item_outputs: [
      {
        id: 'ufo:quantum_anomaly',
        amount: 1
      }
    ],
    fluid_outputs: [],
    energy: 1000000,
    time: 400
  }).id('kubejs:custom_quantum_anomaly')
})
```

### Adding a Fluid Output Recipe

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:dimensional_assembly',
    item_inputs: [
      {
        ingredient: { item: 'ufo:white_dwarf_fragment_ingot' },
        count: 8
      }
    ],
    fluid_inputs: [
      {
        ingredient: { fluid: 'ufo:source_liquid_starlight_fluid' },
        amount: 1000
      }
    ],
    item_outputs: [],
    fluid_outputs: [
      {
        id: 'ufo:source_white_dwarf_fragment_fluid',
        amount: 2000
      }
    ],
    energy: 80000,
    time: 200
  }).id('kubejs:custom_white_dwarf_fluid')
})
```

### Mixed Item + Fluid Outputs

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:dimensional_assembly',
    item_inputs: [
      {
        ingredient: { item: 'ufo:obsidian_matrix' },
        count: 4
      },
      {
        ingredient: { item: 'minecraft:nether_star' },
        count: 16
      }
    ],
    fluid_inputs: [
      {
        ingredient: { fluid: 'ufo:source_uu_amplifier_fluid' },
        amount: 100
      }
    ],
    item_outputs: [
      {
        id: 'ufo:scrap',
        amount: 8
      }
    ],
    fluid_outputs: [
      {
        id: 'ufo:source_liquid_starlight_fluid',
        amount: 10000
      }
    ],
    energy: 4000000,
    time: 1000
  }).id('kubejs:starlight_and_scrap')
})
```

---

## Using Tags as Inputs

You can use item tags instead of specific items:

```js
event.custom({
  type: 'ufo:dimensional_assembly',
  item_inputs: [
    {
      ingredient: { tag: 'c:ingots/iron' },
      count: 16
    },
    {
      ingredient: { item: 'minecraft:ender_pearl' },
      count: 4
    }
  ],
  fluid_inputs: [],
  item_outputs: [
    {
      id: 'ufo:obsidian_matrix',
      amount: 2
    }
  ],
  fluid_outputs: [],
  energy: 50000,
  time: 100
}).id('kubejs:tagged_obsidian_matrix')
```

---

## Removing Existing Recipes

```js
ServerEvents.recipes(event => {
  // Remove by recipe ID
  event.remove({ id: 'ufo:dma/quantum_anomaly' })
  
  // Remove all DMA recipes that output a specific item
  event.remove({ type: 'ufo:dimensional_assembly', output: 'ufo:quantum_anomaly' })
  
  // Remove all DMA recipes entirely (use with caution!)
  event.remove({ type: 'ufo:dimensional_assembly' })
})
```

---

## Modifying Existing Recipes

KubeJS does not support direct recipe modification for custom types. The recommended approach is:

1. **Remove** the original recipe
2. **Re-add** it with modified values

```js
ServerEvents.recipes(event => {
  // Remove original recipe
  event.remove({ id: 'ufo:dma/quantum_anomaly' })
  
  // Re-add with modified energy and time
  event.custom({
    type: 'ufo:dimensional_assembly',
    item_inputs: [
      {
        ingredient: { item: 'ufo:pulsar_fragment_dust' },
        count: 6  // reduced from 12
      },
      {
        ingredient: { item: 'ufo:obsidian_matrix' },
        count: 1
      }
    ],
    fluid_inputs: [
      {
        ingredient: { fluid: 'ufo:source_pulsar_fragment_fluid' },
        amount: 4000  // reduced from 8000
      }
    ],
    item_outputs: [
      {
        id: 'ufo:quantum_anomaly',
        amount: 1
      }
    ],
    fluid_outputs: [],
    energy: 3000000,  // reduced from 6000000
    time: 600  // reduced from 1200
  }).id('ufo:dma/quantum_anomaly')
})
```

---

## Item & Fluid ID Reference

### Common Item IDs

| Item | ID |
|------|-----|
| Obsidian Matrix | `ufo:obsidian_matrix` |
| Dimensional Processor | `ufo:dimensional_processor` |
| Printed Dimensional Processor | `ufo:printed_dimensional_processor` |
| Dimensional Processor Press | `ufo:dimensional_processor_press` |
| White Dwarf Fragment Ingot | `ufo:white_dwarf_fragment_ingot` |
| Neutron Star Fragment Ingot | `ufo:neutron_star_fragment_ingot` |
| Pulsar Fragment Ingot | `ufo:pulsar_fragment_ingot` |
| Proto Matter | `ufo:proto_matter` |
| Corporeal Matter | `ufo:corporeal_matter` |
| White Dwarf Matter | `ufo:white_dwarf_matter` |
| Neutron Star Matter | `ufo:neutron_star_matter` |
| Pulsar Matter | `ufo:pulsar_matter` |
| Dark Matter | `ufo:dark_matter` |
| Quantum Anomaly | `ufo:quantum_anomaly` |
| Nuclear Star | `ufo:nuclear_star` |
| Neutronium Sphere | `ufo:neutronium_sphere` |
| Enriched Neutronium Sphere | `ufo:enriched_neutronium_sphere` |
| Charged Enriched Neutronium Sphere | `ufo:charged_enriched_neutronium_sphere` |
| UU Matter Crystal | `ufo:uu_matter_crystal` |
| Scrap | `ufo:scrap` |
| Scrap Box | `ufo:scrap_box` |
| Unstable White Hole Matter | `ufo:unstable_white_hole_matter` |
| Safe Containment Matter | `ufo:safe_containment_matter` |
| Aether Containment Capsule | `ufo:aether_containment_capsule` |

### Component Matrices

| Component | ID |
|-----------|-----|
| Phase Shift Component Matrix | `ufo:phase_shift_component_matrix` |
| Hyper Dense Component Matrix | `ufo:hyper_dense_component_matrix` |
| Tesseract Component Matrix | `ufo:tesseract_component_matrix` |
| Event Horizon Component Matrix | `ufo:event_horizon_component_matrix` |
| Cosmic String Component Matrix | `ufo:cosmic_string_component_matrix` |

### Catalyst IDs

| Catalyst | ID |
|----------|-----|
| Matterflow T1 | `ufo:matterflow_catalyst_t1` |
| Matterflow T2 | `ufo:matterflow_catalyst_t2` |
| Matterflow T3 | `ufo:matterflow_catalyst_t3` |
| Chrono T1 | `ufo:chrono_catalyst_t1` |
| Chrono T2 | `ufo:chrono_catalyst_t2` |
| Chrono T3 | `ufo:chrono_catalyst_t3` |
| Overflux T1 | `ufo:overflux_catalyst_t1` |
| Overflux T2 | `ufo:overflux_catalyst_t2` |
| Overflux T3 | `ufo:overflux_catalyst_t3` |
| Quantum T1 | `ufo:quantum_catalyst_t1` |
| Quantum T2 | `ufo:quantum_catalyst_t2` |
| Quantum T3 | `ufo:quantum_catalyst_t3` |
| Dimensional (Creative) | `ufo:dimensional_catalyst` |

### Fluid IDs

| Fluid | ID |
|-------|-----|
| Liquid Starlight | `ufo:source_liquid_starlight_fluid` |
| Gelid Cryotheum | `ufo:source_gelid_cryotheum` |
| Temporal Fluid | `ufo:source_temporal_fluid` |
| Spatial Fluid | `ufo:source_spatial_fluid` |
| Primordial Matter | `ufo:source_primordial_matter_fluid` |
| Raw Star Matter Plasma | `ufo:raw_star_matter_plasma` |
| Transcending Matter | `ufo:transcending_matter` |
| UU Matter | `ufo:uu_matter` |
| UU Amplifier | `ufo:source_uu_amplifier_fluid` |
| White Dwarf Fragment Fluid | `ufo:source_white_dwarf_fragment_fluid` |
| Neutron Star Fragment Fluid | `ufo:source_neutron_star_fragment_fluid` |
| Pulsar Fragment Fluid | `ufo:source_pulsar_fragment_fluid` |

---

## Tips & Best Practices

1. **Energy scaling**: Most basic recipes use 50K–500K AE. Mid-game recipes use 1M–10M. Endgame recipes can use 50M–500M AE.
2. **Time vs Speed**: Base time is affected by Chrono catalysts. A `time: 200` recipe takes 10 seconds base (200 ticks / 20).
3. **Minimum time cap**: The DMA enforces a minimum processing time of **1 second** (20 ticks) regardless of Chrono catalyst stacking.
4. **Shapeless inputs**: All DMA recipes are shapeless — item placement order in the 9 slots does not matter.
5. **Max 9 item inputs**: The DMA has 9 input slots, so recipes cannot exceed 9 distinct item types.
6. **Fluid inputs go to Slot 3**: Recipe-required fluids are consumed from the base fluid tank (Slot 3), not the coolant tank (Slot 2).
7. **Coolant is NOT part of recipes**: Coolant is player-managed for thermal control and is completely independent of recipe definitions.

---

## Datapack Alternative (JSON)

If you prefer datapacks over KubeJS, place recipe JSON files in:

```
data/<your_namespace>/recipe/<recipe_name>.json
```

Using the same JSON format shown in the [DMA page](dma.md#recipe-json-format).

---

*See also: [DMA](dma.md) · [Catalysts](catalysts.md) · [Materials & Fluids](materials.md)*

---

# Stellar Nexus — KubeJS Recipes

## Recipe Type

```
ufo:stellar_simulation
```

## Complete Recipe Example

```js
// kubejs/server_scripts/stellar_nexus_recipes.js

ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:stellar_simulation',

    // Name displayed in the Controller GUI
    simulation_name: 'Custom Void Harvest',

    // Item inputs — consumed from ME network when START is pressed
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

    // Fluid inputs — consumed from ME network when START is pressed
    fluid_inputs: [
      {
        amount: 200000,  // 200 buckets of Raw Star Matter Plasma
        ingredient: { fluid: 'ufo:raw_star_matter_plasma' }
      }
    ],

    // Item outputs — AE2 GenericStack format
    // '#' = amount, '#t' = 'ae2:i' (item), 'id' = registry ID
    item_outputs: [
      { '#': 5000000, '#t': 'ae2:i', id: 'minecraft:ender_pearl' },
      { '#': 2000000, '#t': 'ae2:i', id: 'minecraft:blaze_rod' }
    ],

    // Fluid outputs — '#t' = 'ae2:f' for fluids
    fluid_outputs: [
      { '#': 100000, '#t': 'ae2:f', id: 'ufo:liquid_starlight' }
    ],

    // Total AE energy consumed instantly on start
    energy: 300000000,  // 300 million AE

    // Duration in ticks (20 ticks = 1 second)
    time: 30000,  // 25 minutes

    // Thermal stress (0-3). Higher = more heat per tick
    cooling_level: 2,

    // Minimum Stellar Field Generator tier (1-3)
    field_tier: 2,

    // Fuel fluid consumed from ME storage on start
    fuel_fluid: 'mekanism:hydrogen',
    fuel_amount: 20000,  // 20 buckets

    // Coolant fluid consumed every tick during operation
    coolant_fluid: 'ufo:source_gelid_cryotheum',
    coolant_amount: 25000  // 25 buckets
  }).id('kubejs:custom_void_harvest')
})
```

## Field Reference

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `type` | String | ✅ | Must be `ufo:stellar_simulation` |
| `simulation_name` | String | ✅ | Display name in Controller GUI |
| `item_inputs` | Array | ✅ | Items consumed from ME network |
| `fluid_inputs` | Array | ❌ | Fluids consumed from ME network |
| `item_outputs` | Array | ❌ | Items produced (AE2 GenericStack) |
| `fluid_outputs` | Array | ❌ | Fluids produced (AE2 GenericStack) |
| `energy` | Integer | ✅ | Total AE energy cost |
| `time` | Integer | ✅ | Duration in ticks |
| `cooling_level` | Integer | ✅ | Heat stress level (0–3) |
| `field_tier` | Integer | ✅ | Min Field Generator tier (1–3) |
| `fuel_fluid` | String | ❌ | Fuel fluid registry ID |
| `fuel_amount` | Integer | ❌ | Fuel amount in mB |
| `coolant_fluid` | String | ❌ | Coolant fluid registry ID |
| `coolant_amount` | Integer | ❌ | Coolant amount in mB |

## AE2 GenericStack Output Format

```json
{ "#": 15000000, "#t": "ae2:i", "id": "minecraft:iron_ingot" }
```

- `#` — Amount (supports very large numbers, even above Integer.MAX_VALUE)
- `#t` — Type key: `ae2:i` = item, `ae2:f` = fluid
- `id` — Full registry ID

## Removing / Modifying Stellar Nexus Recipes

```js
ServerEvents.recipes(event => {
  // Remove a specific simulation by ID
  event.remove({ id: 'ufo:stellar_simulation/iron_core_fusion' })

  // Remove ALL stellar simulation recipes
  event.remove({ type: 'ufo:stellar_simulation' })
})
```

## Scaling Tips

| Tier | Energy Range | Duration | Fuel |
|------|-------------|----------|------|
| T1 (Mk.I) | 150M AE | 20 min | Hydrogen 20K mB |
| T2 (Mk.II) | 350M AE | 25 min | Hydrogen 20K mB |
| T3 (Mk.III) | 750M–1G AE | 30–40 min | Ethene 20K mB |

> ⚠️ **Safe Mode Penalty**: Players with Safe Mode ON consume **2.5×** your specified fuel, energy, and coolant amounts. Design recipes with this in mind.

---

*See also: [Stellar Nexus](stellar-nexus.md) · [DMA](dma.md) · [Catalysts](catalysts.md) · [Materials & Fluids](materials.md)*
