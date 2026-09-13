# Synthetic legacy payloads

Origin: UFO tag `v2.1-fix7`, commit
`db7a369060b2c15b4611147d0a0ed3b709456b75`.

These are hand-authored examples of that revision's serializer output, not an
exported player world. Payloads deliberately contain values above 32-bit limits;
the cell contains 2^80 items to exercise signed BigInteger bytes and saturated
AE2 long views. Empty optional inventories/parts do not model a built structure.

Source contracts at that revision:

- `block/entity/AbstractSimpleMultiblockControllerBE.java`: primitive fields,
  parts, upgrades, displayedRecipes; no runtimeState.
- `block/entity/AbstractParallelMultiblockControllerBE.java`: processStates,
  thermalTicker, overloadTimer.
- `block/entity/processing/ParallelProcessState.java`: recipeId, energyBuffer,
  three long arrays, progress, patternPushed; no transactional ledgers, batch
  scale, pause state or output policy version.
- `block/entity/StellarNexusControllerBE.java`: energyBuffer/capacity and active
  recipe/thermal/simulation flags; no prepared output ledger.
- `item/custom/cell/AEBigIntegerCellData.java`: inventory/entries/key/amount
  byte arrays, error_entries; SavedData key `ae_universal_cell_data/<UUID>`.
- `init/OCDataComponents.java`: persistent `ufo:cell_uuid` UUID codec. The 3.0
  Java owner changed to ModDataComponents; the serialized identifier did not.
- `init/ModBlockEntities.java`: legacy controller type identifiers used by tests.

AE2 key format: 19.2.17 AEKey.CODEC (`#t`) and AEItemKey.CODEC (`id`).
The registry-enabled GameTests deserialize these literal tags, round-trip through
3.0 and check quantities/defaults. They do not prove upgrading a whole 2.x world,
old topology formation, recipe compatibility, or resource recovery from an
unknown legacy recipe. Those remain explicitly covered by the human save test.
