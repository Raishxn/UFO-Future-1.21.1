# Migrating saves from UFO Future 2.1 to 3.0

This document describes what changes when an existing 2.1 (or 2.1-fix1..fix7) world
is first loaded with UFO Future 3.0.0-alpha.x. The retained block/item IDs and legacy NBT reader contracts are described below.
A complete player-world upgrade still requires testing on a copy: the automated
fixtures cover serialized fields, not every old structure, recipe or modpack.
Normal Minecraft saves rewrite loaded data; this is not a zero-rewrite migration.

## 1. New and changed required dependencies

3.0 declares hard dependencies in `neoforge.mods.toml` and refuses to load without:

- **Applied Energistics 2** 19.2.17+ (as before).
- **RaishxCore** (`raishxcore-0.1.0-beta.2.jar` or compatible) — new shared core mod.
  If you previously ran the 3.0 alphas with the old `ufocore` jar, replace it: the
  core mod id changed from `ufocore` to `raishxcore` and the old jar no longer
  satisfies the dependency. The core registers no content, so there is nothing to
  migrate on the save side.
- **AE2 Addon Lib** 1.0.3 for Minecraft 1.21.1 or compatible.

**Mekanism** 10.7.x remains optional and enables chemical storage/processing.
Chemical-dependent recipes require it. UFO no longer requires GeckoLib; keep
GeckoLib installed when another mod, such as AdvancedAE, depends on it. The
experimental Apocalypse Type-A entity was removed.

JEI, EMI, Applied Flux, Applied Mekanistics and KubeJS remain optional integrations.

The experimental `ufo:apocalypse_type_a` entity and
`ufo:apocalypse_type_a_spawn_egg` item are no longer registered. Existing copies
of that entity and its spawn egg do not carry over to this build.

## 2. Removed: Entropic Assembler Matrix

The legacy `Entropic Assembler Matrix` multiblock and its casing were retired and
their IDs (`ufo:entropic_assembler_matrix`, `ufo:entropic_assembler_casing`) were
deleted from the mod together with their recipes, loot tables and models.

On first 3.0 load:

- Existing matrix/casing blocks in the world disappear (they load as air). There is
  no drop and no alias.
- Any matrix or casing items stored in chests, machines or inventories are deleted
  the first time those stacks load.
- Patterns stored for the matrix remain valid AE2 patterns; re-home them as
  described below.

**Replacement path:** the autocrafting endgame is now the
[trio pipeline](https://github.com/Raishxn/UFO-Future-1.21.1#readme) — Quantum
Computation Nexus (shared crafting CPUs), Quantum Pattern Fabrication Matrix
(crafting/smithing/stonecutting pattern library) and Infinity Fabrication
Singularity (aggregate endgame fabricator). Build the Singularity where the matrix
used to stand; encoded crafting patterns from the old machine insert directly into
its pattern pages.

## 3. Existing multiblocks need supply hatches (do this first)

Independent processing machines now require explicit external supply — the
controller no longer pulls coolant or energy from the ME network by itself. This
applies to every already-built **Stellar Nexus, QMF, QPA, Quantum Slicer and
Quantum Cryoforge**:

- Replace **two casing positions adjacent to the controller** with one
  `ME Massive Fluid Hatch` and one `FE Energy Input Hatch` (registry ID remains
  `ufo:ae_energy_input_hatch`). The structure does not
  form until both are present, and the structure preview / Scan Structure highlight
  exactly which two positions to swap.
- **Coolant** is inserted into the fluid hatch tank from outside (export bus,
  bucket, or another pipe). The network itself is no longer a coolant source.
- The **energy hatch** accepts FE on any face and consumes only its saved local
  reservoir. Supply it continuously with an FE cable; neither AE2 grid energy nor
  Applied Flux storage is used as an automatic fallback. The ME network still
  needs its own power supply for nodes and crafting CPUs.

Existing **Infinity Fabrication Singularity** structures from earlier 3.0 alphas
also need one FE Energy Input Hatch in the casing position highlighted by the
scanner next to the controller. Connect the outward face of its Quantum Grid Link
to the ME network; supplying the hatch alone does not connect the machine to ME.

Until the two hatches are placed, affected machines report `ESTRUTURA INCOMPLETA`
/ incomplete structure. This is expected after the update, not a corruption.

## 4. Stellar Nexus: free cells no longer become ports

Positions in a Stellar Nexus that accept any block are no longer scanned as part
of the machine. A hatch that previously worked because it sat in a free cell is no
longer discovered as a port; move hatches into the designated hatch positions
(highlighted by the preview and listed by Scan Structure). Machines that never
relied on that quirk are unaffected, and scans of large structures are
significantly cheaper after this change.

## 5. IDs that were deliberately kept

- The registry typos in some big storage cell IDs (the `..._beaco` family) are kept
  as-is: no alias, no rename, no save migration. They display under their proper
  names.
- The DMA recipe id `dma/event_horizon_energy_cell` was renamed to
  `dma/ufo_energy_cell`. AE2 processing patterns describe
  inputs/outputs rather than this DMA recipe ID. This rename alone needs no pattern
  conversion. Other controllers persist active recipe IDs; recipe changes still
  need the separate compatibility check below.

## 6. Recipe rebalance

3.0 rebalanced the DMA and QMF recipe families (F0–F3): component matrix cascades,
field generator costs, catalysts, fragment chain parity, infinity cell pricing and
the restored stellar matter energies. If you had autocrafting patterns for these
families, open them once in JEI and re-encode where inputs or outputs changed.
Existing patterns keep working mechanically; they may simply no longer match the
current recipe.

## 7. New optional content

- **Productive Bees integration** (optional): three custom bees (Matter Ball, Scrap,
  Scrap Box) whose only acquisition path is a QMF recipe. Without Productive Bees in
  the pack nothing bee-related loads.
- Quantum Pattern Matrix buffer and wireless crafting resume are additive
  content. For an existing 3.0-alpha Singularity, follow the hatch and Grid Link
  instructions in section 3.

## Quick checklist for an existing world

1. Make a complete copy of the existing world, including its `data` directory
   and `data/ae_universal_cell_data` UUID files. Test the copy first.
2. Update mods: add `raishxcore`, keep AE2 versions in range, optionally add Mekanism,
   replace the UFO jar.
3. Load the copied world; expect removed Entropic Assembler blocks/items to be gone.
4. For each pre-existing processing multiblock, swap the two highlighted casings for
   the fluid and energy hatches; fill the coolant tank.
5. Move any Stellar Nexus hatches that lived in free cells into real hatch slots.
6. Re-encode autocraft patterns for rebalanced recipes where JEI shows changes.
7. Optionally add Productive Bees to start the bee loop via the QMF.

No NBT edits, commands or manual save file surgery are required at any step.


## Automated evidence and remaining world test

`LegacySaveMigrationGameTests` loads literal, synthetic payloads matching the
serializer contracts in tag `v2.1-fix7` (`db7a369`); fixture provenance is in
`src/gameTest/resources/migration/v2.1-fix7/README.md`.

- Four parallel-controller identifiers still resolve and retain long buffers,
  process progress and unversioned/full-batch defaults through a 3.0 rewrite.
- Stellar retains energy/thermal/recipe fields, reads older fuel aliases and
  gives modern energy fields precedence; no pending outputs are invented.
- The retained `white_dwarf_cell_beaco` item and `cell_uuid` component resolve;
  the UUID opens a legacy-format SavedData file from disk. A 2^80-item balance
  remains exact through extraction and reserialization while AE2's long view
  saturates.

These tests do not execute the historical recipe or import a whole world. For
final release QA, open a copy of an actual 2.x save, check UUID-backed cell
balances and inventories, replace the designated supply hatches, then process
and save/reload representative machines. Check completed/pending resources and
any in-flight old recipe before resuming it; recipe rebalance and untracked
legacy buffers require world-specific verification. Keep the original 2.x
world available until that comparison passes.

The complete remaining human procedure is in [Release QA](RELEASE_QA_3.0.md).
