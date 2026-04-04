# Tools & Weapons

UFO Future introduces a **transformable energy multi-tool system**. All tools share a single RF-powered item that can transform between different tool types using scroll-wheel cycling.

---

## The Multi-Tool System

All UFO tools implement the `IEnergyTool` interface and share these features:

- **RF-Powered**: Each tool consumes RF energy per use instead of durability
- **Rainbow Name**: Tool names display with an animated rainbow color effect
- **Transformable**: Hold the tool and scroll (Shift+Scroll) to cycle between all tool types
- **Persistent Energy**: Energy is preserved when transforming between tool types (uses `transmuteCopy`)
- **Energy Bar**: A dynamic energy bar is displayed on the item

---

## Tool Cycle Order

The transformation cycle goes through these tools in order:

| # | Tool | Type | Special Feature |
|---|------|------|----------------|
| 1 | UFO Staff | Melee | Base tool — entry point |
| 2 | UFO Sword | Sword | Standard combat |
| 3 | UFO Pickaxe | Pickaxe | Auto-Smelt toggle (Shift+Right Click) |
| 4 | UFO Axe | Axe | Tree-felling |
| 5 | UFO Shovel | Shovel | Path creation |
| 6 | UFO Hoe | Hoe | Crop auto-replant (3x3 area) |
| 7 | UFO Hammer | Pickaxe+ | Area mining (1×1 / 3×3 / 5×5 / 7×7) |
| 8 | UFO Greatsword | Sword | Heavy damage, slower |
| 9 | UFO Fishing Rod | Fishing Rod | Energy-powered fishing |
| 10 | UFO Bow | Bow | Fast-draw mode toggle |

---

## Individual Tool Details

### UFO Staff
- **Type**: Melee weapon
- **Attack**: Standard UFO tier damage
- **ID**: `ufo:ufo_staff`

### UFO Sword
- **Type**: Sword
- **Attack**: +5 attack damage, −2.4 attack speed
- **Energy per use**: Standard
- **ID**: `ufo:ufo_sword`

### UFO Pickaxe
- **Type**: Pickaxe
- **Attack**: +1 attack damage, −2.8 attack speed
- **Special**: **Auto-Smelt** mode — toggle with `Shift+Right Click`. When active, mined ores are automatically smelted into ingots.
- **Energy per use**: 50 RF/block
- **ID**: `ufo:ufo_pickaxe`

### UFO Axe
- **Type**: Axe
- **Attack**: +6 attack damage, −3.2 attack speed
- **Energy per use**: Standard
- **ID**: `ufo:ufo_axe`

### UFO Shovel
- **Type**: Shovel
- **Attack**: +1.5 attack damage, −3.0 attack speed
- **Energy per use**: Standard
- **ID**: `ufo:ufo_shovel`

### UFO Hoe
- **Type**: Hoe
- **Attack**: +0 attack damage, −3.0 attack speed
- **Special**: Tills a **3×3 area** when used. Auto-replants crops if applicable.
- **ID**: `ufo:ufo_hoe`

### UFO Hammer ⭐
- **Type**: Area mining tool (Pickaxe-based)
- **Attack**: +7 attack damage, −3.4 attack speed
- **Energy per use**: 50 RF **per block** (including area blocks)
- **Area Modes**: Cycle with mode key
  - 1×1 (default)
  - 3×3
  - 5×5
  - 7×7
- **Auto-Smelt**: Supports auto-smelt toggle — all area blocks are smelted if active
- **Smart Energy**: Stops breaking area blocks when energy runs out
- **ID**: `ufo:ufo_hammer`

### UFO Greatsword
- **Type**: Heavy sword
- **Attack**: +8 attack damage, −3.0 attack speed
- **ID**: `ufo:ufo_greatsword`

### UFO Fishing Rod
- **Type**: Fishing rod
- **Durability**: 500 (still uses durability)
- **ID**: `ufo:ufo_fishing_rod`

### UFO Bow
- **Type**: Bow
- **Durability**: 5000
- **Special**: **Fast-Draw** mode toggle — allows instant bow drawing
- **ID**: `ufo:ufo_bow`

---

## Crafting

The UFO Staff is the entry point for the tool system. All other forms are accessed via transformation.

### UFO Staff (Primary Recipe)
**DMA Recipe**:
- **Inputs**: Charged Enriched Neutronium Sphere ×1, Quantum Anomaly ×2, Proto Matter ×2, Netherite Ingot ×4, AE2 Matter Ball ×16
- **Fluid**: 2,000 mB Liquid Starlight
- **Energy**: 12,000,000 AE
- **Time**: 2,400 ticks (2 minutes)

### UFO Staff (Alternate Recipe)
**DMA Recipe**:
- **Inputs**: Netherite Sword, Netherite Pickaxe, Netherite Axe, Netherite Hoe, Neutron Star Fragment Ingot
- **Energy**: 500,000 AE
- **Time**: 600 ticks (30 seconds)

---

## Tool Item IDs

| Tool | Registry ID |
|------|-------------|
| UFO Staff | `ufo:ufo_staff` |
| UFO Sword | `ufo:ufo_sword` |
| UFO Pickaxe | `ufo:ufo_pickaxe` |
| UFO Axe | `ufo:ufo_axe` |
| UFO Shovel | `ufo:ufo_shovel` |
| UFO Hoe | `ufo:ufo_hoe` |
| UFO Hammer | `ufo:ufo_hammer` |
| UFO Greatsword | `ufo:ufo_greatsword` |
| UFO Fishing Rod | `ufo:ufo_fishing_rod` |
| UFO Bow | `ufo:ufo_bow` |

---

*See also: [Armor](armor.md) · [Progression](progression.md)*
