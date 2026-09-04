# UFO Future 2.1-fix2

This hotfix targets the remaining JEI rendering issue in the `Stellar Nexus` recipe category.

## Fixes

- Fixed the `Stellar Nexus` JEI recipe page still showing a black/pink missing-texture background for some players.
- The Stellar Nexus JEI category no longer depends on an external PNG for its recipe background.
- The recipe layout is now drawn directly in code, matching the same slot positions while avoiding broken resource lookup or stale texture-cache issues.

## Notes

- This only changes the Stellar Nexus JEI recipe display.
- Recipes, machine behavior, outputs, inputs, and progression are unchanged.

## Validation

- `compileJava` completed successfully.
