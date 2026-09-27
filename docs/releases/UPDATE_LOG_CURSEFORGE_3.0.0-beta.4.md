# UFO Future 3.0.0-beta.4

This beta makes the Structure Terminal easier to craft and safer to use, expands GUI localization, and fixes startup without Applied Flux.

## Highlights

- The Structure Terminal now crafts from one Dimensional Processor surrounded by eight books.
- Scan is the default terminal mode. Build, replace, and dismantle are explicit choices. Dismantle targets matching structural blocks while leaving unrelated blocks and the controller untouched.
- Build operations check permissions at each edited position. Replace mode preserves blocks with persistent data, and material extraction can use an active AE grid node.
- More screens, tooltips, JEI text, and structure diagnostics have English and Simplified Chinese translations.
- Fixed startup when Applied Flux is absent, centered the first Stellar Nexus input, and updated the Dwarf cell LED.

## Compatibility and verification

- Minecraft 1.21.1, NeoForge 21.1.x, and Applied Energistics 2 19.2.17 or compatible 19.x versions remain required. Mekanism and Applied Flux remain optional.
- Built against RaishxCore 0.1.0-beta.2. No new Core release is required.
- The release workflow builds the JAR and runs GameTests with and without Mekanism before publishing the GitHub assets.
