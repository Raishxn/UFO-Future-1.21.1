# UFO Future 3.0.1

A performance release for servers and clients, with RaishxCore 0.2.2.

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
