---
navigation:
  parent: ufo_intro/quantum_processor_assembler.md
  title: Controls and Thermal Budget
  icon: ufo:quantum_pattern_hatch
  position: 30
---

# Assembler Controls and Thermal Budget

The required ME Massive Fluid Hatch receives coolant from external fluid pipes.
Place it in any casing position; the assembly preview is an example. Only its local tank is consumed;
there is no automatic ME storage fallback.
The single Quantum Pattern Hatch may also move between casing positions; neither
hatch may replace Vibrant Glass.

Normal operation generates 1 HU per active thread each tick. Overclock multiplies
progress and base heat by five. Safe Mode caps concurrency at nine and prevents
progress at the thermal ceiling.

| Cooling source | Cooling value | Flow cap |
|---|---:|---:|
| Gelid Cryotheum | 1 HU / 120 mB | 1000 mB/t |
| Stable Coolant | 50 HU / mB | 10 mB/t |
| Temporal Fluid | 100 HU / mB | 10 mB/t |

Use per-thread pause when diagnosing one recipe without stopping unrelated
work. Scan only concerns structure state; it does not cancel or reconstruct
process ledgers. If output is blocked, expand the correct ME storage/partition
instead of re-encoding the pattern.
