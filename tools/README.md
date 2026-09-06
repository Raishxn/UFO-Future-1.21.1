# Development tools

This directory contains utilities and historical source material that are not
packaged in the UFO Future mod JAR.

- `multiblock/`: maintained converters and source exports used while authoring
  multiblock definitions.
- `diagnostics/stellar_balance_baseline.py`: prints theoretical Stellar energy
  and processing timings from generated recipes; run from any directory with
  Python 3. These estimates are not in-game measurements.
- `legacy/`: one-off migration scripts and old source snapshots kept only for
  reference. They must not be compiled or copied into `src/main`.

Runtime textures belong under `src/main/resources/assets/ufo/textures`. Editable
source artwork should live outside that runtime tree until it is exported to a
Minecraft-ready PNG.
