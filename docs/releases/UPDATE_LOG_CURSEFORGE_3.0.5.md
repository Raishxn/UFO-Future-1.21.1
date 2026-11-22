# UFO Future 3.0.5

Fixes heavy AE2 grid lag from Stellar Nexus casings.

## Bug fixes

- **Stellar Nexus grid lag.** Entropy Singularity Casings and Entropy Assembler Core Casings no longer create AE2 grid nodes. On a server with 15 Stellar Nexus, AE2's `Grid.onLevelEndTick` dropped from about 160 ms to about 5 ms per tick.
- **Behaviour change:** the casings no longer carry ME network connections. Connect your ME network through the Nexus hatches.
- Casings now correctly restore their controller link when the chunk loads. Existing worlds keep working without changes.

## Requirements

Minecraft **1.21.1**, NeoForge **21.1.216+**, AE2 **19.2.17 or 19.2.18** (`[19.2.17, 20)`), AE2 Addon Lib **1.0.3-1.21.1+** and RaishxCore **0.2.3** (`[0.2.3, 1.0)`). Replace the old UFO JAR.

Thanks to Immortalis202 for the fix (#35).
