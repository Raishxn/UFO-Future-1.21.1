# UFO Future 3.0.0-beta.2

The second 3.0 beta focuses on player-tested endgame machines, UI clarity,
structure tooling, compatibility, and bounded performance under acceleration.

## Highlights

- Reworked the Quantum Pattern Fabrication Matrix and Quantum Computation Nexus
  screens for clearer status, installed-module and active-job information.
- Added configurable external tick acceleration, defaulting to 64x and capped at
  256x, for supported UFO controllers.
- Rebalanced Mega Crafting Storage and Co-Processor tiers while keeping every UFO
  tier above the supported AE2 addons and preserving registry IDs.
- Structure Scanner now supports AE2 material sourcing, MK1/MK2/MK3 field selection,
  formed-structure replacement and exact return of replaced blocks.
- Fixed Stellar Nexus scene placement and composition; the Nether orbit no longer
  intersects the central star.
- Memory Cards copy controller modes and upgrades without copying process state.
- Fixed Jade fluid reporting, Quantum Interface upgrade-card support and Infinity
  Genesis Cell advertised capacity.
- Large emergency machine balances remain in compact recovery packages; right-click
  an active ME network block with a package to insert its contents safely.

## Verification

- Unit tests and release build passed.
- All 40 required UFO GameTests passed.
- Player testing covered parallel QMF, Quantum Slicer, Quantum Processor Assembler
  and Quantum Cryoforge operation without a measurable TPS increase.

## Dependencies

- Minecraft 1.21.1 and NeoForge 21.1.x.
- Applied Energistics 2 19.2.17 or a compatible 19.x release.
- RaishxCore 0.1.0-beta.2 or newer in the 0.1.x line.
- Mekanism remains optional.

Back up old worlds before migration. A complete real 2.x-world migration remains
a final human gate before the stable 3.0.0 release.
