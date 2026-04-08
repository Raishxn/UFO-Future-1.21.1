---
navigation:
  parent: ufo_intro/index.md
  title: Stellar Nexus
  position: 50
item_ids:
  - ufo:stellar_nexus_controller
---

# Stellar Nexus — The Ultimate Multiblock Machine

<BlockImage id="ufo:stellar_nexus_controller" scale="4"></BlockImage>

**The Stellar Nexus** is the pinnacle of UFO Future technology. This massive **35×34×35** multiblock machine simulates stellar phenomena — neutron star fusion, supernova harvesting, and exotic matter synthesis — to produce resources at a scale measured in **millions**.

When active, the machine renders a stunning 3D scene: a central star surrounded by orbiting dimension spheres inside a space shell.

> ⚠️ **Endgame Content**: The Stellar Nexus requires enormous amounts of energy (150M – 1G AE per cycle) and rare materials. It is meant for late-game/post-endgame progression.

---

## Structure Requirements

The Stellar Nexus is a massive multiblock structure that must be precisely assembled. Use the **Structure Scanner** item to verify your build and highlight missing blocks.

### Required Blocks

| Block | Purpose | Quantity |
|-------|---------|----------|
| Entropy Singularity Casing | Main structural frame | ~800+ |
| Entropy Assembler Core Casing | Inner core shell | ~200+ |
| Entropy Computer Condensation Matrix | Processing nodes | 4 |
| Stellar Field Generator (Mk.I/II/III) | Enables simulations of increasing tier | 4 |
| ME Massive Output Hatch | Item output to ME network | Exactly 1 |
| ME Massive Fluid Hatch | Fluid output to ME network | Exactly 1 |
| ME Massive Input Hatch | Input items/fluids from ME network | Exactly 1 |
| AE Energy Input Hatch | Energy charging from ME network | Exactly 1 |
| Stellar Nexus Controller | The brain of the machine | 1 (placed last) |

### Hatch Rules
- You must have **exactly one** of each hatch type.
- Hatches go in the `B` (Singularity Casing) positions — they can replace any Singularity Casing block.
- All hatches connect to your ME network via AE2 Dense Smart Cables.
- **Fuel and Coolant** are consumed directly from the ME network storage, no separate hatch needed.

---

## How It Works

### 1. Assembly
Build the 35×34×35 structure and right-click the **Controller** with a **Structure Scanner**. If successful, the controller lights up and displays "Assembled". A star will appear at the center of the structure.

### 2. Select a Simulation Program
Open the Controller GUI and use the `◀` / `▶` arrows to cycle through available simulation programs (recipes). Each program has different requirements:

- **Item Inputs**: Consumed from your ME network on start.
- **Fluid Inputs**: Consumed from your ME network on start.
- **Fuel Fluid**: A specific fuel (e.g., Mekanism Hydrogen) consumed from ME storage on start.
- **AE Energy**: Charged over time from the AE Energy Input Hatch into the global buffer.
- **Field Tier**: Minimum Stellar Field Generator tier required (Mk.I, Mk.II, or Mk.III).
- **Cooling Level**: Thermal stress level (determines how fast heat builds up).

### 3. Energy Charging
The AE Energy Input Hatch continuously draws AE power from your ME grid to fill the **global energy buffer** (max 20 billion AE). The charging rate depends on your Field Generator tier:

| Field Generator | Charge Rate | Time to Full (20B AE) |
|----------------|-------------|----------------------|
| Mk.I | 500,000 AE/tick | ~11 hours |
| Mk.II | 1,000,000 AE/tick | ~5.5 hours |
| Mk.III | 2,000,000 AE/tick | ~2.7 hours |

> 💡 **Tip**: Keep the machine assembled and idle to charge energy passively. The buffer persists between operations!

### 4. Start the Simulation
Click **▶ START** in the Controller GUI. The machine will:
1. Verify all inputs are available in the ME network.
2. Verify the energy buffer has enough AE power.
3. Consume the inputs, fuel, and energy.
4. Begin processing for the specified duration.

If any requirement is missing, the GUI shows **specific error messages** telling you exactly what's wrong (e.g., "Missing: 3/4x Enriched Neutronium Sphere").

### 5. Thermal Management
During operation, the machine generates **heat** based on the recipe's cooling level. A **coolant fluid** is consumed automatically from your ME network every tick to reduce heat.

Each recipe consumes coolant directly from the ME network. The intended progression is:

| Coolant Fluid | Cooling Efficiency |
|---------------|-------------------|
| Gelid Cryotheum | Low (1x) |
| Stable Coolant | Medium (4x) |
| Temporal Fluid | Extreme (8x) |
| Water (fallback) | Emergency only (1x) |

The thermal gauge in the GUI shows current heat (0.0% — 100.0%).

### 6. Safe Mode & Overclock

#### Safe Mode ON (Default — Recommended)
- Machine **safely shuts down** at 100% heat.
- Enters a **30-minute cooldown** before it can be restarted.
- Resources are consumed at **2.5× rate** (Safe Mode penalty).

> 💡 Safe Mode uses 2.5× more fuel, coolant, and energy. This is the price of safety.

#### Safe Mode OFF (Dangerous!)
- **No safety shutdown** — the machine keeps running at 100% heat.
- If heat reaches maximum: **🔥 CATASTROPHIC STELLAR EXPLOSION! 🔥**
- Tier-based destruction radius (**30 blocks** for Mk.I, **50 blocks** for Mk.II, **100 blocks** for Mk.III):
  - Inner core (40% of radius): Replaced with **lava**
  - Outer ring: Blocks destroyed, **fire** placed
  - All nearby entities take massive damage and burn
  - Sub-explosions every 5 ticks for dramatic effect
  - The explosion spreads across 3 seconds to reduce lag

> ⚠️ **WARNING**: Disabling Safe Mode with insufficient coolant WILL destroy your base. The explosion is comparable to a nuclear meltdown.

#### Overclock Mode
- Activate via the **⚡ O.C.** button in the GUI.
- **Benefits**: 5× processing speed.
- **Costs**: 10× AE Energy, 5× Heat, and 5× Fuel requirement.
- **Penalty**: If the machine overheats while Overclocked in Safe Mode, the cooldown period jumps to **2 hours** instead of 30 minutes. Use it carefully.

### 7. Completion
When the simulation finishes (typically 20–40 minutes), the outputs are automatically injected into your ME network through the ME Massive Output Hatch.

---

## Simulation Programs

### Tier 1 — Mk.I Field Generator

| Program | Energy | Duration | Fuel | Coolant | Key Inputs | Outputs |
|---------|--------|----------|------|---------|------------|---------|
| Iron Core Fusion | 150M AE | 20m | 20K Hydrogen | 10K Gelid Cryotheum | 4x Enriched Neutronium Sphere, 100K mB Raw Star Matter Plasma | 15M Iron, 10M Gold, 10M Copper |
| Red Giant Collapse | 150M AE | 20m | 20K Hydrogen | 10K Gelid Cryotheum | 4x Enriched Neutronium Sphere, 100K mB Raw Star Matter Plasma | 15M Redstone, 10M Lapis, 10M Glowstone |

### Tier 2 — Mk.II Field Generator

| Program | Energy | Duration | Fuel | Coolant | Key Inputs | Outputs |
|---------|--------|----------|------|---------|------------|---------|
| Diamond Pressure | 350M AE | 25m | 20K Hydrogen | 25K Gelid Cryotheum | 8x Enriched Neutronium Sphere, 200K mB Raw Star Matter Plasma | 10M Diamond, 10M Emerald, 10M Amethyst |
| Neutron Bombardment | 350M AE | 25m | 20K Hydrogen | 25K Gelid Cryotheum | 8x Enriched Neutronium Sphere, 250K mB Raw Star Matter Plasma | 10M Netherite Scrap, 10M Quartz, 10M Ender Pearl |

### Tier 3 — Mk.III Field Generator

| Program | Energy | Duration | Fuel | Coolant | Key Inputs | Outputs |
|---------|--------|----------|------|---------|------------|---------|
| Supernova Harvest | 750M AE | 30m | 20K Ethene | 50K Temporal Fluid | 16x Charged Enriched Neutronium Sphere, 4x Aether Containment, 500K mB Plasma | 15M Iron, 12M Gold, 12M Diamond, 10M Netherite, 5M Nether Star |
| Stellar Synthesis | 1G AE | 40m | 20K Ethene | 50K Temporal Fluid | 16x Charged Enriched Neutronium Sphere, 8x Aether Capsule, 4x Dark Matter, 500K Plasma + 250K Transcending Matter | 10M Pulsar Dust, 5M White Dwarf Matter, 2.5M Neutron Star Matter, 250K Liquid Starlight |

---

## The Structure Scanner

<ItemImage id="ufo:structure_scanner" scale="2"></ItemImage>

A diagnostic tool for the Stellar Nexus:
- **Right-Click** on the Controller → Shows all missing or misplaced blocks (up to 10 in chat, 50 highlighted in-world with red boxes).
- **Shift + Right-Click** (Creative Mode) → Instantly auto-builds the entire structure.

---

## Visual Rendering

When assembled, the Stellar Nexus renders a stunning 3D scene inside the structure:

- **🌌 Space Shell**: A large translucent sphere representing outer space, slowly rotating.
- **⭐ Central Star**: A bright rotating star at the center. The model changes based on which simulation is active (regular star, blue star, or neutron star).
- **🌍🔥🟣 Orbiting Dimensions**: When active, three dimension spheres (Overworld, Nether, End) orbit the central star at different speeds and distances.

The star pulses gently during operation and the entire scene is visible from up to 256 blocks away.

---

*End of Stellar Nexus guide.*
