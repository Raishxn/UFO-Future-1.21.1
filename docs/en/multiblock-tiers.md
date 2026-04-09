# Multiblock Tiers

The universal multiblock line uses a shared **MK1 / MK2 / MK3** system for gating and scaling.

## Recipe Access

- **MK1** machine: MK1 recipes only
- **MK2** machine: MK1 and MK2 recipes
- **MK3** machine: MK1, MK2 and MK3 recipes

If the machine tier is below the recipe tier, the controller will not start the recipe.

## Tier Bonus

Running an older recipe on a better machine grants an automatic efficiency bonus:

- **Time** is halved for each tier above the recipe
- **Energy** is reduced to **75%** for each tier above the recipe

Examples:

- MK2 machine on MK1 recipe = **2x faster** and **25% less AE**
- MK3 machine on MK1 recipe = **4x faster** and **43.75% less AE**

## Why The System Matters

This makes upgrades valuable even before you touch brand-new recipes. A stronger tier does not only unlock content; it also compresses your old production chains into background throughput.

That is especially important once AE2 begins dispatching many jobs into the same controller through the Quantum Pattern Hatch.
