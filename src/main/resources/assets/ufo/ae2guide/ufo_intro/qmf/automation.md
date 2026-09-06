---
navigation:
  parent: ufo_intro/qmf.md
  title: AE2 Automation and Thermals
  icon: ufo:quantum_pattern_hatch
  position: 30
---

# QMF AE2 Automation and Thermals

## Pattern Contract

The Quantum Pattern Hatch stores up to **72 encoded processing patterns**. The
QMF advertises those patterns only while formed and connected. AE2 remains the
owner of pattern-delivered ingredients; the controller does not extract them a
second time.

Encode the deterministic base output displayed by JEI. Bonus/byproduct rolls are
not part of the promise to the crafting CPU and return separately when produced.

## Network Checklist

- Connect the hatch/node on its supported face and verify network power.
- Ensure the crafting CPU can store the requested result.
- Keep every chunk in the 15×7×7 footprint loaded during operation.
- If a job stalls, inspect its individual cell before sending another batch.

## Heat and Cooling

Install the required ME Massive Fluid Hatch in any casing position
and connect a NeoForge-compatible fluid pipe. Coolant is consumed only
from its local tank; ME storage is never used as an automatic fallback. The Quantum Pattern Hatch may likewise occupy any casing
position, but exactly one is required; neither hatch is valid in Vibrant Glass.

Base load is **1 HU per active thread per tick**; Overclock raises the base to
**5 HU per thread per tick**. Idle dissipation removes 1 HU every 40 ticks.

| Coolant | Conversion | Maximum flow |
|---|---:|---:|
| Gelid Cryotheum | 1 HU / 120 mB | 1000 mB/t |
| Stable Coolant | 50 HU / mB | 10 mB/t |
| Temporal Fluid | 100 HU / mB | 10 mB/t |

Safe Mode stops progress at the thermal ceiling and limits concurrency to nine.
Catalysts can alter speed, AE cost, bonus output and heat; consult
[Catalysts](../catalysts.md) before combining four high-tier catalysts.
