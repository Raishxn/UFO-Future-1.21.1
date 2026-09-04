# UFO Future 2.0.0-fix4

Public changelog for all changes since `2.0.0-fix3`.

## Highlights

- Added the `Infinity Genesis Cell`, an AE2 endgame cell that learns any inserted resource and then provides that resource infinitely to the network, covering items, fluids, and AE-compatible chemicals.
- `UFO Energy Cell` and `Quantum Energy Cell` now automatically export FE to adjacent blocks and expose an energy capability for external cables and machines.
- Added the `Astral Nexus` endgame line, including armor set, wing rendering, absolute damage/death immunity, creative flight, night vision, water breathing, step assist, and Mekanism radiation cleanup when Mekanism is present.
- Added the `Reality Ripper`, an endgame sword with extreme damage and forced removal behavior for highly resistant targets.

## Balance And Recipes

- Fixed the `Dimensional Matter Assembler` matcher so it prioritizes more specific recipes and swaps its cached recipe when inputs change.
- Rebalanced the DMA bootstrap flow, making `Gelid Cryotheum` a stronger early coolant and adjusting progression recipe time/output.
- Reduced the `Dimensional Matter Assembler` block resistance so it is easier to move at the stage where it appears.
- Adjusted DMA recipe mirroring into QMF bulk recipes, allowing specific recipes to opt out when progression needs a separate route.
- Updated QMF/DMA recipes for `White Dwarf` and `Neutron Star` dusts, rods, and matter items.
- Removed invalid bulk mirrors for the `Thermal Resistor Exosuit` and `UFO Staff` where those crafts should not follow the automatic DMA route.
- `Quantum Cryoforge` now generates half of its base heat, making its thermal profile less punishing.

## Fixes And Stability

- Fixed thermal failure behavior for the DMA, universal parallel multiblocks, and `Stellar Nexus`: explosions now remove the controller and avoid repeating effects forever.
- Fixed pending recipe/JEI issues: visible recipe for `Quantum Cryoforge Controller`, visible `Quantum Pattern Provider` conversion through the hatch, and updated `Quantum Energy Cell` recipe.
- Adjusted QMF component matrix batch recipes to output `24`.
- Fixed parallel multiblock progress synchronization so the UI no longer appears frozen while the server keeps processing.
- Fixed AE2 connectivity for entropic multiblocks using real AE casings, a multiblock node, and a shared channel across the structure.
- Reduced entropic multiblock validation cost during load/unload and structure changes.
- Fixed crashes and recursive validation cases in `Entropic Convergence`, including proper `BlockEntityType` separation and safer `onReady` behavior.

## Guide, JEI And Data

- AE2 Guide now includes thermal profile notes for `Quantum Processor Assembler` and `Quantum Slicer`.
- Guide pages were expanded for DMA, QMF, Quantum Cryoforge, and Stellar Nexus, including HU values, coolant ladder, and thermal behavior.
- Stellar Nexus JEI now shows coolant as `MK1`, `MK2`, or `MK3`, and fuel in a shortened format while keeping full names in tooltips.
- `runData` is now validated by CI to prevent outdated generated resources from reaching published artifacts.
- Added a tag-based release workflow for publishing GitHub Actions artifacts.

