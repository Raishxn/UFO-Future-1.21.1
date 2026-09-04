---
navigation:
  parent: ufo_intro/quantum_slicer.md
  title: Controls, Heat, and Recovery
  icon: ufo:quantum_pattern_hatch
  position: 30
---

# Slicer Controls, Heat, and Recovery

An ME Massive Fluid Hatch may replace any casing and accept coolant from external
fluid pipes. Its local tank has priority over the existing ME storage source. The
single Quantum Pattern Hatch may move to any casing, while Vibrant Glass remains
strictly glass-only.

- **Scan** revalidates the canonical 13×5×5 topology and reports mismatches.
- **Safe Mode** limits admission to nine jobs and locks progress at maximum heat.
- **Overclock** multiplies progress and base heat by five.
- The control inside each process cell pauses/resumes only that persistent job.

Heat is 1 HU per active thread per tick normally and 5 HU while overclocked.
Coolant follows the common ladder: Gelid Cryotheum (1 HU/120 mB), Stable
Coolant (50 HU/mB) and Temporal Fluid (100 HU/mB). Idle cooling removes 1 HU
every 40 ticks.

Disconnecting AE2, reloading the world or blocking output is recoverable. Restore
the missing condition and leave the original thread intact; do not break the
controller merely to retry a craft.
