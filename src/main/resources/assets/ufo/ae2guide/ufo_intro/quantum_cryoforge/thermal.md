---
navigation:
  parent: ufo_intro/quantum_cryoforge.md
  title: Thermal Efficiency and Diagnosis
  icon: ufo:stellar_field_generator_t1
  position: 30
---

# Cryoforge Thermal Efficiency

The ME Massive Fluid Hatch and Quantum Pattern Buffer/Proxy are valid only where the
template expects a casing/universal-hatch position. Glass positions remain
strict. The fluid hatch accepts the three thermal coolants from external pipes,
uses its local tank before ME storage, and does not change the Cryoforge's shape.

The Cryoforge generates **50%** of the common universal-machine heat load:
`ceil(active threads × base heat × 0.5)`. Base heat is 1 normally and 5 while
overclocked. Idle dissipation removes 1 HU every 40 ticks.

Safe Mode reduces the active admission limit from 27 to 9 and stops progress at
the thermal ceiling. It does not delete already-reserved resources. Catalysts
still modify heat, speed, AE cost and bonus behavior after the machine-specific
half-heat factor.

## When a Coolant Recipe Will Not Start

1. Confirm formation and grid connection.
2. Verify all 24 field generators are the same tier.
3. Compare the displayed MK tier with the recipe's JEI requirement.
4. Check exact input fluid/item availability and output capacity.
5. Inspect paused threads and the global temperature.

Do not confuse the Cryoforge's own cooling requirement with the coolant it is
manufacturing: produced fluid still needs a valid output destination.
