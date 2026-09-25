# UFO Future 3.0.0-beta.3

This beta improves the Stellar Nexus recipe and controller screens and includes fixes to Reality Anchor rescue, wireless range defaults, and configuration migration.

## Highlights

- Rebuilt the Stellar Nexus controller and JEI/EMI recipe screens. The recipe display now has room for up to 81 item outputs and 18 fluid outputs.
- The simulation name appears in the EMI header. Field tier, duration, and AE cost appear on the recipe's upper border.
- The EMI recipe frame now fits the full texture, and the controller workstation icon sits beside it.
- Fixed coolant tank rendering and label alignment in the controller screen.
- Fixed Reality Anchor rescue after falling into the void.
- Wireless Tool ranges now follow `config/ufo/wireless.toml`, including existing links that still used the old 32-block default. Explicitly chosen ranges remain unchanged.
- Moved UFO configuration files under `config/ufo/` with automatic migration of existing values.
- Updated the wiki across four languages, including restored Simplified Chinese pages.

## Compatibility and verification

- Stellar Nexus KubeJS recipe JSON remains unchanged. A development GameTest loaded a scripted recipe with 81 item outputs and 18 fluid outputs.
- The release build passed 41 required UFO GameTests both with and without Mekanism, 14 Core GameTests, and the two required short soak tests.
- Built against RaishxCore 0.1.0-beta.2; no new Core release is required.
- Minecraft 1.21.1, NeoForge 21.1.x, and Applied Energistics 2 19.2.17 or compatible 19.x versions remain required. Mekanism remains optional.
