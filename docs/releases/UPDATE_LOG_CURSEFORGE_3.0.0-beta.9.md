# UFO Future 3.0.0-beta.9

## Fixed

- Fixed the Quantum Computation Nexus being slower than equivalent stand-alone AE2 crafting CPUs. Installed co-processor lanes are now shared fairly between all active jobs instead of being capped at a fixed 2,048-operation window for the whole pool. With four jobs and 16,384 lanes the Nexus matches four equivalent AE2 CPUs instead of running at about one eighth of their throughput.
- The low-buffer energy throttle is now opt-in. By default the Nexus behaves like AE2 and CPU addons, which do not reduce dispatch when the stored buffer is low. Server operators can re-enable it with `nexus.energyThrottle`.
- Nexus crafting no longer consumes the network's stored energy by default: jobs only need the Grid Link powered (32 AE/t). AE2's native per-pattern energy cost can be restored with `nexus.ignorePatternEnergy=false`.

## Added

- `nexus.maxPatternDispatchesPerTick` server setting (default 16,384) caps the Nexus dispatch budget per tick and protects server TPS on very large pools. Jobs loaded from disk ramp up over four ticks, so a resumed craft cannot burst-drain a full network buffer.
- New scheduler TPS load test in the release pipeline.

## Validation

- Automated regression GameTests compare the Nexus against four equivalent stand-alone CPUs: exact parity (32,776 dispatches over eight rounds) with full power and with a simulated 1% energy buffer.
- The new scheduler TPS load test runs 16 jobs on 1,638,400 lanes through the real RaishxCore tick path, dispatching its full budget every tick (~16,465 dispatches per tick, 16.5 million over 1,000 ticks). Average tick time stayed at ~23 ms against the 50 ms budget on the development machine.
- Unit tests, 57 GameTests, the active-load test and the release pipeline (GameTests with and without Mekanism, datagen, idle soak, Nexus TPS load) pass.

## Requirements

- Minecraft **1.21.1**, NeoForge 21.1.x, Applied Energistics 2 **19.2.17 or compatible 19.x**, and RaishxCore **0.2 or compatible 0.x** (`[0.2, 1.0)`).
- This remains a **beta** release. No recipe or progression changes from beta.8.
- Actual throughput depends on the installed modules, pattern providers, available ingredients and output storage.
