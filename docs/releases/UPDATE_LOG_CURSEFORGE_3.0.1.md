# UFO Future 3.0.1

New per-family storage components, a performance pass for servers and clients, and RaishxCore 0.2.2.

## New content

- **Per-family storage components.** White Dwarf, Neutron Star and Pulsar now have their own 40M, 100M, 250M, 750M and Infinity storage components with animated textures. The first component combines the Phase Shift Component Matrix with the family's fragment ingot; each later tier combines that tier's matrix with the previous component of the same family. A cell is housing + component, and disassembling it returns both (Pulsar Chambers can now be disassembled too, with Mekanism). Existing cells keep working.
- New cell housing textures for White Dwarf, Neutron Star and Pulsar.
- Stable Coolant is now an MK2 recipe in the Quantum Cryoforge and the QMF (was MK3), available when MK2 machines start asking for it. Guide and KubeJS examples updated.
- Cryotheum Bee for Productive Bees (only when that mod is installed).

## Fixes

- Requires RaishxCore **0.2.2**: autocrafting no longer fails with `RaishxCore planning stopped ... status=TIMED_OUT` when the network is short of a material. The exact planner is now bounded and returns the plan with its shortage instead of running out of time.
- The Stellar Nexus model cache is cleared after a resource reload (F3+T) instead of keeping the previous models.

## Server performance (TPS)

- Parallel multiblocks (QMF, Cryoforge, Slicer, Processor Assembler) no longer walk their whole structure every tick to find the Pattern Hatch.
- With a busy Quantum Computation Nexus, RaishxCore no longer forces AE2 to rebuild its list of items being crafted on every tick.
- Nexus modules, crafting CPUs on networks without a Grid Link, Stellar Nexus auto-start, the Quantum Interface, energy cells and armor modules do less work per tick.
- RaishxCore 0.2.2 plans large crafting requests much faster.

## Client performance (FPS)

- The structure hologram is drawn from a GPU buffer: on the Stellar Nexus, from 7.15 to 0.83 ms per frame.
- The Quantum Wireless tool no longer copies the item's data every frame.

## Known limitation

The timeout reported when requesting more than three Enhancement Cores or Infinity Templates has not been reproduced with the affected network. RaishxCore 0.2.2 removes the timeout on short networks, the most likely cause; if it persists, send the new log and `/raishxcore planner` output right after the failure.

## Requirements

Minecraft **1.21.1**, NeoForge **21.1.216+**, AE2 **19.2.17 or 19.2.18** (`[19.2.17, 20)`), AE2 Addon Lib **1.0.3-1.21.1+** and RaishxCore **0.2.2** (`[0.2.2, 1.0)`). Replace both old UFO and RaishxCore JARs.
