---
navigation:
  parent: ufo_intro/quantum_hatches.md
  title: Wireless Network and Bonuses
  position: 1
---

# Wireless Network and Bonuses

For multiblocks, connect a Quantum Pattern Buffer to a powered ME grid and
install processing patterns. The Buffer serves its own multiblock and links to
Pattern Proxies installed in additional multiblocks. For DMAs, use the legacy
Quantum Pattern Hatch and link it directly to the machines. Request recipes
through the ME terminal; outputs still need a return path to ME storage.

The tool shows the selected source and new-link preview in yellow; saved connections and faces are blue. Destinations must be loaded, in the same dimension and within range. Wireless does not power machines, load chunks or bridge separate ME grids.

Use a [Quantum Interface](quantum_interface.md) with configured coolant and Auto
Export for supplies; link its destinations separately. The **Pattern Buffer
grants multiblock buffs**, while the legacy **Pattern Hatch grants DMA buffs**.
The interface provides logistics only.

## Adjustable range

Every hatch/interface starts at **32 blocks**. Use its range gear: click adds one block, Shift-click removes one. The server maximum bounds the setting. Each source saves its own range.

Range applies to linking and transfers. Reducing it suspends distant destinations without deleting links; increasing it resumes them. The server checks distance on every resolution.

## Automatic recipe bonuses

Configure **config/ufo/wireless.toml** once on the instance/server. There are no profiles or point allocations. Speed, energy discount and productive heat discount apply together, with independent limits for DMA and multiblocks.

Default maxima are **+20% speed, −10% energy per recipe and −15% productive heat per recipe**. One eligible machine gets neutral factors; strength increases linearly to the maximum at **10 DMAs** or **4 multiblocks**. Families are counted separately. Two multiblocks receive approximately +6.67% speed, −3.33% energy and −5% heat; three receive +13.33%, −6.67% and −10%.

Only distinct loaded, linked, non-creative machines that advanced recipe processing in the last **20 ticks (one second)** count. Patterns may differ, and inputs may arrive through wireless, cables or manually. Being linked, receiving coolant or waiting for ingredients/energy does not count as productive activity. Multiblocks must be formed; several faces or parallel threads count as one machine. The source must have wireless enabled and an active ME node. Separate sources do not stack bonuses: the strongest applicable source wins.

The Pattern Hatch panel shows only DMA activity and factors; the Pattern Buffer
panel shows only multiblock activity and factors. These are the factors available
for new recipes. Each machine locks its factors when starting a recipe (before
energy reservation in multiblocks) and keeps them until completion, including
across saves. The first wave after idle may have no bonus; subsequent recipes
benefit as activity builds. Activity itself starts empty after world reload.

## Configuration

```toml
[wireless]
range = 128
maxLinks = 1024

[wireless.buffs]
enabled = true

[wireless.buffs.dma]
saturationMachines = 10
maxSpeedBonus = 0.20
maxEnergyDiscount = 0.10
maxHeatDiscount = 0.15

[wireless.buffs.multiblock]
saturationMachines = 4
maxSpeedBonus = 0.20
maxEnergyDiscount = 0.10
maxHeatDiscount = 0.15
```

range limits local range settings; zero removes that server cap only. Sources start at 32. A value of 0.20 means 20%; zero disables an individual effect. Restart the instance/server after editing for predictable application. Multiplayer processing uses server values.

Speed preserves total energy before the chosen discount and may increase power per tick. Productive heat is compensated for speed before the heat discount. Passive cooling and coolant efficiency remain unchanged. One-tick recipes cannot become shorter.

Supported: DMA, Quantum Matter Fabricator, Quantum Slicer, Quantum Processor Assembler and Quantum Cryoforge. Stellar Nexus and other mods' machines do not receive these buffs.

The old shared wireless.buffs.saturationMachines setting has been replaced by the two family settings. Move any customized old threshold into the desired family sections; the new defaults are 10 and 4.

The old activityTicks setting and per-pattern delivery history are no longer used. Keep your family thresholds and effect caps; no new config setting is required.
