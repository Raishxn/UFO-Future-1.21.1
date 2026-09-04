# UFO Future 2.1-fix1

This hotfix focuses on player-reported localization, guide text, and JEI display issues after the 2.1 release.

## Fixes

- Fixed the black/pink missing-texture background in the `Stellar Nexus` JEI recipe category.
- The Stellar Nexus JEI page now uses the standard GUI texture path and draws its recipe background explicitly.
- Updated the AE2 guide page opened with `G` so the mega crafting storages and mega co-processors no longer show old names.
- Replaced old guide names such as `Quantum Drive Matrix`, `Tesseract Unit`, `Dimensional Storage Cube`, `Singularity Accelerator`, and `Qubit Co-Processor` with the current 2.1 names.
- Tightened the `Entropic Assembler Matrix` pattern check so only molecular-assembler-compatible patterns enter that machine path.

## Localization

- Added Simplified Chinese localization: `zh_cn.json`.
- The Chinese localization now matches the English language file key-for-key.
- Updated Chinese names for the mega crafting storages and mega co-processors to match the current naming scheme.

## Compatibility

- Added optional Applied Flux processing recipes:
  - `Charged Redstone Block` to `Printed Energy Processor`
  - `Printed Energy Processor` to `Energy Processor`
- Added optional runtime dependency entries for Applied Flux and MEGA Cells so release builds include those integrations correctly when present.

## Notes

- The reported AE2 crafting storage/co-processor reconnect issue was audited against the current code path. No additional code change was required in this hotfix.
- This release is intended as a small compatibility and presentation fix for 2.1.

## Validation

- `compileJava` completed successfully.
- `zh_cn.json` was validated against `en_us.json` with no missing localization keys.
- The AE2 guide was checked for the old mega storage/co-processor names.
