---
navigation:
  title: Machines
  icon: ufo:quantum_matter_fabricator_controller
  parent: ufo_intro/index.md
  position: 20
---

# Machines

UFO production is split into machines with distinct responsibilities. The DMA
is flexible and compact; universal multiblocks add AE2 pattern dispatch and
parallelism; the Stellar Nexus handles long, expensive endgame simulations.

<SubPages />

## Universal Controller Controls

- **Scan** validates the structure and reports missing or invalid positions.
- **Safe Mode** caps parallel execution at 9 and stops work at the thermal limit.
- **Overclock** increases progress per tick while generating substantially more
  heat.
- The process pages show up to 27 independent jobs. Pause affects only the
  selected process and does not erase its inputs, energy or progress.

## Common Runtime States

| State | Meaning | First check |
|---|---|---|
| `UNFORMED` | Structure does not match | Run Scan and inspect highlighted blocks |
| `PAUSED_NO_GRID` | No usable powered AE2 node | Cable face, channel and grid power |
| `IDLE` | Formed, connected, no runnable work | Pattern and recipe tier |
| `RUNNING` | At least one thread is progressing | Temperature and output capacity |
| `OUTPUT_BLOCKED` | Finished output cannot enter storage | Free ME storage or fix output access |
| `INVALID_RECIPE` | Saved process no longer resolves | Datapack/reload and refund path |
