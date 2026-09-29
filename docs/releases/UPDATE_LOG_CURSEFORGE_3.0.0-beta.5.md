# UFO Future 3.0.0-beta.5

This beta improves automatic multiblock construction and revises the Stellar Nexus simulation catalog based on player feedback.

## Highlights

- **Advanced AE compatibility:** the companion RaishxCore shared-CPU bridge no longer replaces AE2 return values that other CPU add-ons extend. With Advanced AE installed, the Quantum Computer appears in the CPU list again and receives crafted items, with the planner enabled or disabled. This release requires RaishxCore 0.1.0-beta.3.
- Automatic construction now recognizes already valid parts, places required service hatches when a compatible casing slot is available, and reports when the structure remains incomplete. Structure previews and JEI information were adjusted accordingly.
- Fixed the gene definitions of the Scrap, Scrap Box, and Matter Ball bees. Spawning one of these bees could crash the world when Productive Bees read its weather tolerance.
- The 13 original Stellar Nexus simulations return with their focused outputs. Each produces 10 times as many items and 50 times as much fluid as in beta.4; raw ore blocks are removed from their outputs.
- The new MK3 **Stellar Omnibus** combines the focused programs into 64 item and 17 fluid outputs. It replaces cobblestone and obsidian with UFO Future ingots and advanced materials, and adds Transcending Matter, Bose–Einstein Condensate, deuterium, tritium, and D-T fuel.
- JEI/EMI item outputs fill three slots in each panel from left to right before moving to the next row. Worlds that selected a recipe from the experimental consolidated catalog map it to an original simulation when loaded.

## Compatibility and verification

- The original focused simulations retain their optional-mod conditions. Stellar Omnibus requires Advanced AE, ExtendedAE, Mega Cells, Mekanism, Mekanism Generators, and Applied Flux because it includes resources from all of them.
- Minecraft 1.21.1, NeoForge 21.1.x, and Applied Energistics 2 19.2.17 or compatible 19.x versions remain required. RaishxCore 0.1.0-beta.3 is the companion build; its shared CPU bridge now composes with Advanced AE's Quantum Computer.
- Local release checks passed: distribution JAR build, 49 GameTests with the full fixture and 49 without Mekanism (including the bee spawn-egg regression on Productive Bees 13.13.5 and the shared-CPU-pool and Quantum Computer regression), two short idle soak tests, and one short active-load test. The release workflow repeats the distribution and compatibility gates.

This is a beta for balance and save-world feedback. Please report unexpected recipe availability, output handling, or automatic-construction behavior with the full game log and reproduction steps.
