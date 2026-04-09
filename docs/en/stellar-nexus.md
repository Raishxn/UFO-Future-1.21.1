# Stellar Nexus

The **Stellar Nexus** is the final simulation multiblock in UFO Future. It consumes immense AE power, rare fluids and high-tier materials to produce extreme-scale outputs over long cycle times.

This page focuses on the machine logic and control model rather than fixed recipe lists.

## Machine Identity

- Massive assembled multiblock
- Reads items and fluids from the ME network
- Charges a very large internal AE buffer while idle
- Consumes fuel on start and coolant during runtime
- Uses heat, safe mode and overclock as core balancing mechanics

## Field Generator Tiers

The four field positions must all be the same tier:

- **MK1**
- **MK2**
- **MK3**

Mixed field tiers invalidate the structure. The field tier decides recipe access and affects charging and cooling performance.

## Coolant Ladder

The Stellar Nexus now follows the intended coolant progression:

- **Gelid Cryotheum** = low efficiency
- **Stable Coolant** = medium efficiency
- **Temporal Fluid** = extreme efficiency

The machine no longer treats water as a meaningful normal progression coolant. The design is now built around moving up this ladder.

## Safe Mode

Safe Mode is the reliable automation option.

When enabled, the machine:

- consumes more AE
- consumes more fuel
- consumes more coolant
- shuts down instead of detonating at maximum heat

This is expensive by design. Safety is not free.

## Overclock Mode

The controller already supports **Overclock Mode**.

Effects:

- **5x faster** recipe completion
- **10x** AE cost
- **5x** fuel use
- **5x** heat generation
- **5x** coolant use

This behavior applies to custom Stellar Nexus recipes too, including KubeJS recipes, because it is handled in machine logic instead of per-recipe special casing.

## Catastrophic Explosion

If Safe Mode is disabled and the machine reaches full heat, the Stellar Nexus enters a real destruction sequence instead of a light cosmetic blast.

The current implementation:

- expands in shells instead of one instant lag spike
- destroys actual blocks around the controller
- converts the inner core area into lava
- ignites the outer blast zone
- damages nearby entities repeatedly as the wave propagates

The goal is to feel closer to a reactor-class failure than to a normal vanilla explosion.

## Recommended Position In Progression

- **DMA** handles flexible advanced crafting
- **Quantum multiblocks** handle bulk AE2 production chains
- **Stellar Nexus** handles extreme-scale simulations and post-endgame generation

## See Also

- [Dimensional Matter Assembler](dma.md)
- [Quantum Matter Fabricator](quantum-matter-fabricator.md)
- [Multiblock Tiers](multiblock-tiers.md)
