# UFO Future 3.0.0-beta.6

This beta makes UFO Future compatible with the stable RaishxCore 0.2 release and with later 0.x versions.

## Highlights

- **RaishxCore 0.2 support:** UFO Future now declares the dependency range `[0.2, 1.0)`, so the stable 0.2 release and future 0.x builds load without the dependency error that rejected 0.2. No gameplay code changed; RaishxCore 0.2 is a planner bug fix over 0.1.0-beta.3.
- The exact companion revision used by the build and release workflows now points at the commit tagged `v0.2`.

## Compatibility and verification

- Minecraft 1.21.1, NeoForge 21.1.x, and Applied Energistics 2 19.2.17 or compatible 19.x versions remain required. RaishxCore `[0.2, 1.0)` is the companion range.
- Local release checks passed: the JUnit suite, including the generated mod-metadata contract test, and the distribution JAR build.
- Saves, recipes, and progression are unchanged from 3.0.0-beta.5.
