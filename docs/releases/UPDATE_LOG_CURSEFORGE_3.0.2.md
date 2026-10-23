# UFO Future 3.0.2

Stellar fragment bees, lighter power requirements for crafting and early-game server performance fixes.

## New content

- **Stellar fragment bees for Productive Bees** (only when that mod is installed). White Dwarf, Neutron Star and Pulsar bees come from a conversion chain: feed a fragment ingot to a Netherite Bee for the White Dwarf Bee, to a White Dwarf Bee for the Neutron Star Bee, and to a Neutron Star Bee for the Pulsar Bee. They do not breed among themselves, need the fragment block as their flower, and their combs give fragment nuggets at a low chance.
- **Configurable bee chances.** `config/ufo/common.toml`, section `compat.productivebees.<family>`: `conversionChance`, `nuggetChance` and `nuggetMax`. Edit the file and run `/reload`; data packs and KubeJS can still override the same recipe ids.

| Bee | Conversion chance per ingot | Nugget chance per comb |
|---|---:|---:|
| White Dwarf | 8% | 15% |
| Neutron Star | 5% | 10% |
| Pulsar | 3% | 6% |

## Balance

- **Power matters less for crafting.** New server option `machines.recipeEnergyMultiplier` (default **0.25**) scales the AE cost of Dimensional Matter Assembler recipes and of parallel multiblocks (QMF, Cryoforge, Slicer, Processor Assembler). JEI and the guide show the scaled cost. Set it to `1.0` to restore the previous cost.

## Server performance (TPS)

- **DMA with Applied Flux:** every working DMA extracted `Integer.MAX_VALUE` FE from the ME network and inserted the remainder back on every tick (two full storage operations per DMA per tick). It now pulls only what its buffer can take and leaves the network alone when the buffer is full.
- **DMA without power** no longer polls the network every tick; it slows its tick down (up to once per second) and returns to full speed on the first tick with power.
- New automated early-game load test: a fleet of 64 DMAs running a real recipe on every build, which fails if an unpowered DMA polls every tick or if full DMAs touch Applied Flux FE.

## Requirements

Minecraft **1.21.1**, NeoForge **21.1.216+**, AE2 **19.2.17 or 19.2.18** (`[19.2.17, 20)`), AE2 Addon Lib **1.0.3-1.21.1+** and RaishxCore **0.2.2** (`[0.2.2, 1.0)`). Replace the old UFO JAR; RaishxCore 0.2.2 is unchanged.
