---
navigation:
  parent: ufo_intro/stellar_nexus.md
  title: Simulation Operation
  icon: ufo:stellar_nexus_controller
  position: 30
---

# Stellar Simulation Operation

## Safe First Cycle

1. Scan until the structure reports formed and one uniform field tier.
2. Verify all four hatch roles and their external ME connections.
3. Wait for the internal AE buffer to charge.
4. Select a recipe and confirm its field, input, output, energy and coolant gates.
5. Keep Safe Mode enabled and Overclock disabled.
6. Start one cycle; monitor stored AE, heat and output until completion.

Starting commits the required fuel and effective energy cost. Runtime then builds
heat and consumes coolant. Temporary network loss pauses progress; promised
output remains buffered if ME storage refuses it.

## Coolant Ladder

The base active request is **100 mB/t**. Safe Mode raises it to 250 mB/t and
Overclock multiplies the result by five.

| Fluid | Default efficiency |
|---|---:|
| Gelid Cryotheum | 1 cooling / mB |
| Stable Coolant | 4 cooling / mB |
| Temporal Fluid | 8 cooling / mB |

Field tier multiplies effective cooling. Server configuration may change each
efficiency or disable a fluid by setting it to zero.
