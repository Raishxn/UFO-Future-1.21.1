<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=for-the-badge&logo=mojang-studios&logoColor=white" alt="Minecraft 1.21.1"/>
  <img src="https://img.shields.io/badge/NeoForge-21.1.216-orange?style=for-the-badge" alt="NeoForge 21.1.216"/>
  <img src="https://img.shields.io/badge/AE2-19.2.17+-5C7CFA?style=for-the-badge" alt="AE2 addon"/>
  <img src="https://img.shields.io/badge/version-3.0.0--alpha.1-8E44AD?style=for-the-badge" alt="Version 3.0.0-alpha.1"/>
  <img src="https://img.shields.io/badge/License-LGPLv3%20%2F%20CC%20BY--NC--SA%203.0-blue?style=for-the-badge" alt="License"/>
</p>

<h1 align="center">🛸 UFO Future</h1>

<p align="center">
  <strong>An endgame Applied Energistics 2 addon for Minecraft 1.21.1 (NeoForge)</strong><br/>
  Seven multiblocks · ME-native hatches · Stellar materials · Transformable energy tools · Tiered armor · BigInteger storage
</p>

<p align="center">
  <a href="https://raishxn.github.io/UFO-Future-1.21.1/">📖 Wiki</a> ·
  <a href="#-features">✨ Features</a> ·
  <a href="#-getting-started">🚀 Getting Started</a> ·
  <a href="#-progression-overview">🗺️ Progression</a> ·
  <a href="#-credits">🙏 Credits</a>
</p>

---

## ✨ Features

### 🏗️ The Multiblock Line

UFO Future 3.0 is built around seven multiblock machines that share one recipe
system, one **MK1 / MK2 / MK3** tier ladder and one set of ME-native ports.

| Machine | Role | Scale |
|---------|------|-------|
| **Quantum Matter Fabricator** | Multiblock evolution of the DMA; runs both QMF-native and DMA recipes | 27 parallel threads (9 in Safe Mode) |
| **Quantum Slicer** | Printed-component cutting stage for circuit and processor lines | 27 / 9 threads |
| **Quantum Processor Assembler** | Finishing stage that turns printed parts into processors | 27 / 9 threads |
| **Quantum Cryoforge** | Cryogenic processing stage of the universal recipe line | 27 / 9 threads |
| **Stellar Nexus** | Endgame simulation multiblock: 200B AE buffer, fuel on start, coolant while running, heat as a real constraint | 1 job, 5 with Overclock |
| **Entropic Convergence Engine** | Capstone AE2 crafting CPU — tiered crafting storage plus co-processors | MK1: 4.6 EB + 250M co-processors → MK3: effectively unlimited |
| **Entropic Assembler Matrix** | Capstone crafting machine that accepts AE2 patterns directly | tiered by structure fill and field tier |

### 🔌 Shared Multiblock Infrastructure

- **ME Massive Input / Output Hatch** — pull ingredients from and push results
  into the ME network directly, with no intermediate inventory
- **ME Massive Fluid Hatch** — fluid output plus the external coolant intake
- **AE Energy Input Hatch** — feeds the controller's internal AE buffer
- **Quantum Pattern Hatch** — stores **72 encoded patterns** and exposes the
  controller to AE2 autocrafting as a real crafting machine. Outside a multiblock,
  it works as a standalone pattern provider; pipes can return items and fluids
  through the standard AE2 return buffer
- **Stellar Field Generator Mk.I / Mk.II / Mk.III** — the field blocks that set
  a structure's tier; mixed tiers invalidate the structure
- **Tier bonus** — running a lower-tier recipe on a higher-tier machine halves
  the time and cuts energy to 75% per tier of headroom
- **Safe Mode / Overclock** — trade throughput against heat and stability
- **Coolant ladder** — Gelid Cryotheum → Stable Coolant → Temporal Fluid
- **Hologram preview and auto-build** — sneak + right-click an unformed
  controller with an empty hand to project the whole structure in world, then use
  the in-GUI quick-build button to place the blocks that are still missing
- **Connected textures** — casings use a compact 32x32 CTM sheet resolved at
  chunk rebuild, with a plain 16x16 fallback for resource packs
- **Recipe-viewer previews** — JEI and EMI both get UFO recipe categories and
  an interactive, layer-by-layer structure preview for every multiblock

### 🔧 Dimensional Matter Assembler (DMA)

The single-block machine that starts the chain and still backs the QMF recipe set:

- **9-slot shapeless input grid** with fluid inputs and dual output slots
- **Dynamic thermal system** with heat zones (Safe → Hazard → Meltdown)
- **13 catalyst upgrades** across 4 families affecting speed, energy, stability and bonus drops
- **Meltdown mechanics** with a 5-second countdown, alarm sound, red warnings and
  a global chat alert broadcasting the explosion coordinates
- **Passive and active cooling** via multiple coolant fluids with different efficiencies
- **Recipe-viewer integration** with a custom GUI and K/M/G energy formatting
- **KubeJS support** for fully scripted recipe creation and modification

### 💾 Storage and AE2 Infrastructure

**BigInteger storage cells** with capacities far beyond vanilla limits:

| Series | Type | Tiers |
|--------|------|-------|
| **White Dwarf** | Items | Echo (40M) → Beacon (100M) → Nexus (250M) → Core (750M) → Singularity (∞) |
| **Neutron Star** | Fluids | Same tier progression |
| **Infinity Cells** | Single-resource | Unlimited storage for 30+ resources (vanilla, AE2, Mekanism, dyes) |

All cells render as proper 3D models inside ME Drives.

**Event Horizon Energy Cell** stores AE energy and accepts FE from external
cables, including Mekanism Universal Cables. It also supplies adjacent FE
consumers, using AE2's configured FE/AE conversion in both directions.

**Mega crafting storages and co-processors** extend AE2 autocrafting for extreme
jobs: Event Horizon, Quantum Drive Matrix, Tesseract Unit, Dimensional Storage
Cube and Hyper-Dense Stellar crafting storages.

### ⚔️ Transformable Energy Multi-Tool

A single RF-powered tool that morphs between **10 tool types** via scroll cycling:

- Staff → Sword → Pickaxe → Axe → Shovel → Hoe → **Hammer** → Greatsword → Fishing Rod → Bow
- **Auto-Smelt** toggle on Pickaxe and Hammer
- **Area Mining** (1x1 / 3x3 / 5x5 / 7x7) on Hammer with smart energy management
- **Fast-Draw** mode on Bow
- **3x3 tilling** with auto-replant on Hoe
- Animated rainbow name effect on all tools

### 🛡️ Tiered Armor System

| Set | Tier | Key Abilities |
|-----|------|---------------|
| **Thermal Resistor Exosuit** | Mid-game | Fire/lava immunity, +15% mining speed, DMA heat protection |
| **UFO Armor** | Endgame | Resistance X, creative flight, night vision, +20 hearts (RF-powered) |

### 🧪 13 Custom Fluids

Liquid Starlight · Gelid Cryotheum · Stable Coolant · Temporal Fluid · Spatial Fluid · Primordial Matter · Raw Star Matter Plasma · Transcending Matter · UU Matter · UU Amplifier · White Dwarf/Neutron Star/Pulsar Fragment Fluids

### 🌐 Languages

- **In game:** 🇬🇧 English · 🇧🇷 Português · 🇨🇳 中文
- **Wiki:** 🇬🇧 English · 🇧🇷 Português · 🇪🇸 Español · 🇨🇳 中文

---

## 🚀 Getting Started

### Requirements

| Mod | Type | Version | Why |
|-----|------|---------|-----|
| **Minecraft** | required | 1.21.1 | — |
| **NeoForge** | required | 21.1.216+ | — |
| **[Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2)** | required | `[19.2.17, 20)` | The network, storage and autocrafting UFO builds on |
| **[UFO Core](https://github.com/Raishxn/UFO-Core)** | required | `[0.1.0-alpha.1, 0.2)` | Multiblock, exact-amount and GUI foundations |
| **[AE2 Addon Lib](https://github.com/pedroksl/AE2AddonLib)** | required | `[1.0.3-1.21.1, 2)` | Recipes, registries, menus and widgets |
| **[GeckoLib](https://github.com/bernie-g/geckolib)** | required | `[4.8.2, 5)` | Apocalypse Type-A entity and renderer |
| **[Mekanism](https://github.com/mekanism/Mekanism)** | required | `[10.7.18, 11)` | Mekanism-backed cells and chemical integration |
| **[JEI](https://www.curseforge.com/minecraft/mc-mods/jei)** | optional | — | UFO recipe categories and multiblock previews |
| **[EMI](https://github.com/emilyploszaj/emi)** | optional | — | Keeps the ingredient sidebar clear of attached UFO widgets |
| **[Applied Mekanistics](https://www.curseforge.com/minecraft/mc-mods/applied-mekanistics)** | optional | — | Chemical keys when available |
| **[Applied Flux](https://www.curseforge.com/minecraft/mc-mods/applied-flux)** | optional | — | FE/RF energy integration when available |

### Installation

1. Download the latest build from the [Releases page](https://github.com/Raishxn/UFO-Future-1.21.1/releases)
2. Drop `ufo-<version>.jar` **and** `ufocore-<version>.jar` into your `mods/` folder
3. Make sure AE2, AE2 Addon Lib, GeckoLib and Mekanism are installed too
4. Launch the game

### Your First Steps

1. Craft an **Obsidian Matrix** (8x Obsidian + 1x Ender Eye)
2. Build a **Graviton Plated Casing**
3. Craft the **Dimensional Matter Assembler** and connect it to your AE2 network
4. Start with **Gelid Cryotheum** as your first coolant
5. Progress through White Dwarf → Neutron Star → Pulsar materials
6. Once the single-block chain is running, move up to the **Quantum Matter
   Fabricator** and the rest of the multiblock line

---

## 🧭 Progression Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        FOUNDATION                                │
│  Obsidian Matrix → Graviton Casing → DMA                         │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────────┐
│                     FLUIDS & COOLANTS                            │
│  Dust Blizz → Gelid Cryotheum → Stable Coolant → Temporal Fluid  │
│  Scrap → UU Amplifier → Liquid Starlight (base fluid)            │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────────┐
│                   STELLAR MATERIALS                              │
│  White Dwarf Fragment → Neutron Star Fragment → Pulsar Fragment  │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────────┐
│                PROCESSORS & COMPONENTS                           │
│  Dimensional Processor → Component Matrices (5 tiers)            │
│  Phase Shift → Hyper Dense → Tesseract → Event Horizon → Cosmic  │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────────┐
│                   MATTER CHAIN                                   │
│  Neutronium Sphere → Proto Matter → Corporeal Matter             │
│  → WD/NS/Pulsar Matter → Dark Matter                             │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────────┐
│                    MULTIBLOCK LINE                               │
│  Quantum Matter Fabricator → Slicer → Processor Assembler        │
│  → Cryoforge → Stellar Nexus (Field Mk.I → Mk.III)               │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────────┐
│                     ENDGAME                                      │
│  Entropic Convergence Engine · Entropic Assembler Matrix         │
│  UFO Armor · UFO Staff · Infinity Cells · Dimensional Catalyst   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📖 Documentation

Full documentation lives on the **[UFO Future Wiki](https://raishxn.github.io/UFO-Future-1.21.1/)**, available in 4 languages.

| Page | Description |
|------|-------------|
| [Stellar Nexus](https://raishxn.github.io/UFO-Future-1.21.1/en/stellar-nexus/) | Endgame simulation multiblock, field tiers, coolant, heat |
| [Quantum Matter Fabricator](https://raishxn.github.io/UFO-Future-1.21.1/en/quantum-matter-fabricator/) | Bulk matter processing and parallel threads |
| [Quantum Slicer](https://raishxn.github.io/UFO-Future-1.21.1/en/quantum-slicer/) | Printed-component cutting stage |
| [Quantum Processor Assembler](https://raishxn.github.io/UFO-Future-1.21.1/en/quantum-processor-assembler/) | Final processor assembly |
| [Multiblock Tiers](https://raishxn.github.io/UFO-Future-1.21.1/en/multiblock-tiers/) | MK1/MK2/MK3 gating and the tier bonus |
| [Dimensional Matter Assembler](https://raishxn.github.io/UFO-Future-1.21.1/en/dma/) | Core machine mechanics, GUI, thermal system |
| [Catalysts](https://raishxn.github.io/UFO-Future-1.21.1/en/catalysts/) | All 13 upgrade cards — families, tiers, stacking rules |
| [Mega Storage](https://raishxn.github.io/UFO-Future-1.21.1/en/mega-storage/) | Mega crafting storages and co-processors |
| [Containment](https://raishxn.github.io/UFO-Future-1.21.1/en/containment/) | Safe Containment Matter and Aether Containment Capsule |
| [KubeJS Recipes](https://raishxn.github.io/UFO-Future-1.21.1/en/kubejs-recipes/) | Full scripted recipe API documentation |
| [Tools & Weapons](https://raishxn.github.io/UFO-Future-1.21.1/en/tools/) | Transformable multi-tool system |
| [Armor Sets](https://raishxn.github.io/UFO-Future-1.21.1/en/armor/) | Thermal Resistor & UFO Armor |
| [Storage Cells](https://raishxn.github.io/UFO-Future-1.21.1/en/storage-cells/) | BigInteger cells & Infinity Cells |
| [Materials & Fluids](https://raishxn.github.io/UFO-Future-1.21.1/en/materials/) | All stellar materials and custom fluids |
| [Crafting Progression](https://raishxn.github.io/UFO-Future-1.21.1/en/progression/) | Step-by-step progression guide |
| [Recipe Balance Audit](https://raishxn.github.io/UFO-Future-1.21.1/en/recipe-balance-audit/) | Cost and throughput review of the recipe set |

---

## 🔧 For Modpack Developers

### KubeJS Integration

UFO Future exposes its recipe types for full KubeJS scripting — add, remove or
replace recipes, use tags as ingredients, and script the multiblock line as well
as the DMA. See the [KubeJS documentation](https://raishxn.github.io/UFO-Future-1.21.1/en/kubejs-recipes/) for examples.

### Configuration

Machine behaviour, energy values and thermal thresholds are tunable through
`ufo-common.toml`.

---

## 🛠️ Building From Source

UFO Future is a Gradle composite build: it pulls **UFO Core** from a sibling
directory instead of a published artifact.

```bash
git clone https://github.com/Raishxn/UFO-Core.git         UFO-Core-1.21.1
git clone https://github.com/Raishxn/UFO-Future-1.21.1.git UFO-Future-1.21.1
cd UFO-Future-1.21.1
./gradlew build      # compiles, runs datagen and the unit test suite
./gradlew runClient  # launches a dev client with the test modpack
```

Both checkouts must sit side by side — `settings.gradle` includes
`../UFO-Core-1.21.1` and substitutes the `com.raishxn.ufocore:ufocore`
dependency with it.

---

## 🤝 Contributing

Contributions, bug reports and suggestions are welcome — please open an issue or
a pull request on this repository. If a change touches textures or models, note
the source of any external asset so the credits below stay accurate.

---

## 🙏 Credits

UFO Future is built on other people's work. If anything below is attributed
incorrectly or is missing, please open an issue and it will be fixed.

### Built on

| Project | Role |
|---------|------|
| **[Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2)** — AppliedEnergistics team | The network, storage and autocrafting foundation. UFO Future follows AE2's own license split (LGPLv3 code / CC BY-NC-SA 3.0 assets). |
| **[GuideME](https://github.com/AppliedEnergistics/guideme)** — AppliedEnergistics team | In-game guide pages and 3D structure previews |
| **[NeoForge](https://github.com/neoforged/NeoForge)** | Mod loader. The project was bootstrapped from the NeoForge MDK, still covered by its MIT notice in [`TEMPLATE_LICENSE.txt`](TEMPLATE_LICENSE.txt). |
| **[ParchmentMC](https://parchmentmc.org/)** | Parameter names and javadoc mappings used at build time |
| **[AE2 Addon Lib](https://github.com/pedroksl/AE2AddonLib)** — pedroksl and contributors | Recipes, registries, menus and GUI widgets |
| **[GeckoLib](https://github.com/bernie-g/geckolib)** — bernie-g and contributors | Animated entity and renderer support |
| **[Mekanism](https://github.com/mekanism/Mekanism)** — Mekanism team | Chemical integration and Mekanism-backed cells |
| **[UFO Core](https://github.com/Raishxn/UFO-Core)** | Companion library: multiblock, exact-amount and GUI foundations |

### Optional integrations

- **[JEI](https://www.curseforge.com/minecraft/mc-mods/jei)** — mezz
- **[EMI](https://github.com/emilyploszaj/emi)** — Emi (emilyploszaj)
- **[KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs)** and Rhino — the KubeJS team
- **[Applied Mekanistics](https://www.curseforge.com/minecraft/mc-mods/applied-mekanistics)**
- **[Applied Flux](https://www.curseforge.com/minecraft/mc-mods/applied-flux)**

### Adapted code and assets

- **[AE2 Lightning Tech](https://github.com/ae2lt/AE2-Lightning-Tech)** — its
  interactive multiblock preview, auto-build architecture and compact
  connected-texture geometry were ported and adapted for UFO Future under
  **LGPL-3.0**. The `quick_build.png` toolbar icon is redistributed under
  **CC BY-NC-SA 3.0** with attribution to the AE2 Lightning Tech contributors.
- **[AE2 Crystal Science](https://github.com/Frostbite-time/AE2-Crystal-Science)** —
  several UFO textures are based on or adapted from this project.
- **[GT New Horizons Modpack](https://github.com/GTNewHorizons/GT-New-Horizons-Modpack)** —
  texture basis and visual reference for the industrial material line.
- **[GTO Project](https://github.com/GregTech-Odyssey)** — reference for the
  compact connected-texture casing sheets (`entropy_singularity_casing_ctm.png`,
  `quantum_hyper_mechanical_casing_ctm.png`). The Stellar Nexus renderer is
  based on GTO Core's `EyeOfHarmonyRenderer`, as credited in its source.
- **Applied Energistics 2** — the multiblock supply panel reuses AE2's own
  `ae2:textures/guis/resourcesrequirementswidget.png` at its original size.

### Documentation tooling

- **[MkDocs](https://www.mkdocs.org/)** with **[Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)**
  and the static i18n plugin power the 4-language wiki.

### Author

**Raishxn** — design, code, textures and documentation.

The author credit covers UFO Future's original contributions; adapted work
remains credited to its respective authors above. Dependencies and development
test mods listed in `build.gradle` are separate projects, not bundled UFO code.

---

## 📜 License

Code is licensed under the **GNU Lesser General Public License v3.0 or later (LGPLv3+)**.

Art, textures, models, sounds and other visual assets are licensed under
**Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0)**.

This split follows the licensing model used by Applied Energistics 2, since UFO
Future is an AE2 addon and may derive from, interoperate with or visually
reference AE2 code and assets. Third-party notices for adapted code and assets
are listed in [LICENSE.md](LICENSE.md) alongside the full project notice.

---

<p align="center">
  <sub>Built with ❤️ by <strong>Raishxn</strong> · Powered by AE2 &amp; NeoForge</sub>
</p>
