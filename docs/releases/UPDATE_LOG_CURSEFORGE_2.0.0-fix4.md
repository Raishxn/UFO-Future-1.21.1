# UFO Future 2.0.0-fix4

This version wraps up the changes since `2.0.0-fix3`, focused on endgame progression, multiblock stability, recipes, and AE2 integration.

## Main Additions

- Added the **Infinity Genesis Cell**, an AE2 endgame cell that learns any inserted resource and then provides it infinitely to the network.
- The Infinity Genesis Cell supports compatible AE resources, including **items, fluids, and chemicals**.
- Added the **Astral Nexus** endgame line, including a full armor set, wing rendering, and set bonuses.
- The Astral Nexus set grants damage/death immunity, extreme damage reflection, creative flight, night vision, water breathing, step assist, and Mekanism radiation cleanup when available.
- Added the **Reality Ripper**, an endgame weapon designed to handle extremely resistant targets.
- **UFO Energy Cell** and **Quantum Energy Cell** now automatically export FE to adjacent blocks and expose energy capability support for external cables and machines.

## Recipes, Balance And Progression

- Fixed the **Dimensional Matter Assembler** matcher so it prioritizes more specific recipes and updates its cached recipe when inputs change.
- Rebalanced the DMA bootstrap flow, making **Gelid Cryotheum** more useful as an early coolant.
- Adjusted time, output, and costs for DMA/QMF progression recipes.
- Adjusted automatic DMA recipe mirroring into QMF bulk recipes, allowing mirrors to be disabled when progression needs a separate route.
- Removed invalid bulk mirrors for the **Thermal Resistor Exosuit** and **UFO Staff**.
- Updated **White Dwarf** and **Neutron Star** recipes for dusts, rods, and matter items.
- **Quantum Cryoforge** now generates less base heat, reducing its thermal pressure.

## Fixes And Stability

- Fixed thermal failure behavior for the DMA, universal parallel multiblocks, and Stellar Nexus to avoid endlessly repeating explosion effects.
- Fixed recipe/JEI issues involving **Quantum Cryoforge Controller**, **Quantum Pattern Provider**, and **Quantum Energy Cell**.
- Adjusted QMF component matrix batch recipe output to improve progression.
- Fixed progress synchronization for parallel multiblocks, preventing screens from appearing stuck while the server continues processing.
- Improved AE2 connectivity for entropic multiblocks with real AE casings, a multiblock node, and a shared channel across the structure.
- Reduced validation cost for entropic multiblocks during load/unload and structure changes.
- Fixed crashes and recursive validation cases in **Entropic Convergence** during initialization and revalidation.

## Guide, JEI And Data

- AE2 Guide now includes thermal profile notes for **Quantum Processor Assembler** and **Quantum Slicer**.
- Guide pages were expanded for DMA, QMF, Quantum Cryoforge, and Stellar Nexus.
- Stellar Nexus JEI now displays coolant as MK1/MK2/MK3 and fuel in a shortened format, while keeping details in tooltips.
- `runData` was validated to ensure generated resources are up to date.

## Validation

- `runData` completed successfully.
- `build` completed successfully.

