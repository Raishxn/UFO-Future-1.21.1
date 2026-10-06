# UFO Future 3.0.0-beta.8

## Improved

- Increased Quantum Pattern Fabrication Matrix throughput. The Matrix now supports up to **16 simultaneous recipe batches with MK1 Fields, 32 with MK2, and 64 with MK3**, instead of waiting for one batch to finish before accepting another.
- The Matrix can return up to **16 / 32 / 64 recipe batches to ME storage per tick**, depending on Field tier. Identical crafts continue to run in bulk rather than requiring a separate execution for every item.
- Single-copy crafting tasks now use the aggregate scheduler as well, allowing them to progress alongside bulk crafting tasks.

## Fixed

- Fixed the Quantum Grid Link losing its controller reference when loading saved data. Matrix batch delivery now retains its tier-based throughput after reloading the world.
- Pending crafting outputs remain saved and are retried when ME storage is full or accepts only part of a batch.

## Validation

- Added regression tests for all three Matrix tiers, million-copy batches, full output storage, partial delivery, saved batch recovery, and exact ingredient/output accounting.

## Requirements

- Minecraft **1.21.1**, NeoForge **21.1.x**, Applied Energistics 2 **19.2.17 or compatible 19.x**, and RaishxCore **0.2 or compatible 0.x** (`[0.2, 1.0)`).
- This remains a **beta** release. No recipe or progression changes from beta.7.
- Actual crafting throughput depends on crafting CPU operations, available ingredients, network energy, and output storage space.
