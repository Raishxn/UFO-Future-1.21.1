# Stellar Nexus

The **Stellar Nexus** is the final simulation multiblock in UFO Future. It consumes immense AE power, rare fluids and high-tier materials to produce extreme-scale outputs in short, high-throughput cycles: up to three minutes for Mk2 programs and four minutes for Mk3 programs.

## Machine Identity

- Massive assembled multiblock
- Reads items and fluids from the ME network
- Charges a **200B AE** internal buffer while idle
- Consumes fuel on start and coolant during runtime
- Uses heat, safe mode and overclock as core balancing mechanics
- Requires the designated item input/output hatches, an ME Massive Fluid Hatch for external coolant, and an FE Energy Input Hatch for external power

## Field Generator Tiers

The four field positions must all be the same tier:

- **MK1**
- **MK2**
- **MK3**

Mixed field tiers invalidate the structure. The field tier decides recipe access and affects charging and cooling performance.

## Coolant Ladder

- **Gelid Cryotheum** = low efficiency
- **Stable Coolant** = medium efficiency
- **Temporal Fluid** = extreme efficiency

The intended setup is to climb this ladder instead of brute-forcing the machine with weak coolant forever.

## Safe Mode

Safe Mode is the reliable automation option.

- **2x** AE cost
- **2x** fuel use
- **2.5x** coolant use
- Automatic shutdown instead of detonation at maximum heat

## Overclock Mode

- **5x faster** recipe completion
- **8x** AE cost
- **5x** fuel use
- **5x** heat generation
- **5x** coolant use

This behavior also applies to custom Stellar Nexus recipes, because it is implemented in machine logic.

## Overheat policy

With Safe Mode off, maximum heat causes a local failure with damage and visual effects. **Block destruction is disabled by default.** Server operators can explicitly enable a bounded destructive wave in `config/ufo-server.toml` (`stellar.explosion.enableBlockGrief`), with limits for radius, blocks per tick, total blocks and allowed dimensions. Lava creation and secondary explosions have separate opt-in settings.
