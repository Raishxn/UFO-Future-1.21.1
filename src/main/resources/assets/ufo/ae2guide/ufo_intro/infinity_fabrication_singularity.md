---
navigation:
  parent: ufo_intro/infrastructure.md
  title: Infinity Fabrication Singularity
  position: 57
item_ids:
  - ufo:infinity_fabrication_singularity_controller
---

# Infinity Fabrication Singularity

<BlockImage id="ufo:infinity_fabrication_singularity_controller" scale="4"></BlockImage>

The **Infinity Fabrication Singularity** is UFO's endgame replacement for
copy-by-copy crafting machines. It turns a homogeneous recipe into one aggregate
route, so fabricating billions of identical items does not execute a billion
machine cycles.

It combines two services inside one multiblock:

- its own field-scaled crafting-pattern library;
- its own aggregate fabrication executor that keeps the network stocked.

It does not require a separate Quantum Computation Nexus. Only the formed
Singularity and its Quantum Grid Link need to be connected to the ME network.

<GameScene zoom="1.75" background="transparent" interactive={true}>
  <ImportStructure src="../assets/assemblies/infinity_fabrication_singularity.snbt" />
  <IsometricCamera yaw="35" pitch="25" />
</GameScene>

## Exact structure

The structure is a **7 × 7 × 7** cube. It requires:

- 1 Infinity Fabrication Singularity Controller;
- 1 Quantum Grid Link;
- 51 Quantum Hyper-Mechanical Casings;
- 1 FE Energy Input Hatch, replacing the casing next to the controller marked by the scanner;
- 25 Stellar Field Generators, all of the same tier: MK1, MK2 or MK3;
- 36 Quartz Vibrant Glass;
- 12 Quartz Blocks;
- 12 Fluix Blocks;
- 8 Graviton-Plated Casings.

The remaining 196 positions are ignored by validation and may be air. Connect an
AE2 cable to the outward face of the Quantum Grid Link. The Link consumes one
channel and at least 32 AE/t.

Supply the energy hatch with an FE cable on any face. Its local reservoir is the
only fabrication fuel source: the Singularity never draws crafting energy from
AE2 grid power or Applied Flux storage. Additional energy hatches may replace
Quantum Hyper-Mechanical Casings for more local storage and supply connections.
An empty reservoir pauses new rounds until externally recharged; queued outputs
are retained. Existing structures must replace the casing highlighted by Scan
Structure with the required hatch.

## Pattern pages

Open the controller to insert encoded patterns directly into the nine-slot row,
using the arrow buttons to change pages. Every field position adds pages according
to the installed tier: **MK1 adds 1 page**, **MK2 adds 2 pages**, and **MK3 adds 4
pages**. With 25 fields, total capacity ranges from 25 pages / 225 patterns to 100
pages / 900 patterns. Every field position must use the same tier; mixing tiers
invalidates the structure.

If fields are downgraded, patterns above the new capacity are preserved and remain
accessible for removal; no encoded pattern is deleted automatically.

## Aggregate fabrication

The Singularity is a stock producer, not a crafting target: its patterns are
never published to the AE2 crafting service, so they do not appear as craftable
in the terminal and cannot steal planning from your recipes. On-demand crafting
for these items keeps flowing through your normal autocrafting chain.

For each enabled pattern the Singularity validates one recipe copy, extracts
as many copies as its ingredients and locally stored FE permit in bulk, and inserts the results
as one bounded route. Amounts use 64-bit counters, so a single route can cover
billions of copies without an item-by-item loop. Up to **128 routes** can be
pending at once; routes and their outputs are persisted across world reloads.
Smithing and stonecutting crafting patterns are supported alongside normal
crafting patterns; processing patterns continue to use their actual processing
machines.

Ingredients returned with the exact same item and components, such as the Master
Infusion Crystal, are reserved once for the entire batch and returned once. A
single reusable crystal can therefore process a full essence batch. Consumed
ingredients and changed remainders, such as empty buckets or damaged tools, are
still counted for every recipe copy.

Duplicate patterns stored on different pages collapse into one deduplicated
route.

While a pattern is enabled, the Singularity repeats it continuously, like the
Quantum Crafter: as long as ingredients remain in the network, each round
consumes every copy affordable by its local FE supply at once instead of iterating one copy at a time.
Disabling the pattern stops new rounds without discarding outputs that are
already waiting for network space.

## Operating modes

| Mode | Route budget | Fuel cost (AE equivalent) | Use case |
| --- | ---: | ---: | --- |
| Balanced | 64/tick | 1× | General operation |
| Speed | 128/tick | 2× | Maximum burst throughput |
| Efficiency | 32/tick | 0.5× | Power-constrained networks |

The fields select the mode automatically: **Mk.I uses Efficiency**, **Mk.II uses
Balanced**, and **Mk.III uses Speed**. Mixed tiers invalidate formation,
so Speed requires all 25 fields to be Mk.III. Replacing fields affects new
dispatches immediately without cancelling jobs or discarding queued outputs.

## Dashboard and troubleshooting

The interface shows the pattern pages plus structure, grid, active-job and
queued-route status. **Scan Structure** highlights mismatched blocks;
**Auto-build** places only missing blocks for which the player has materials.

- **Structure incomplete:** repair the position highlighted by the scanner.
- **ME grid offline:** power the network, free one channel, and cable the Grid Link's outward face.
- **Waiting for patterns:** insert a supported encoded pattern into one of the controller's pages.
- **Patterns enabled but no new rounds:** check the FE stored in the energy hatch and its external energy supply.
- **Routes remain queued:** ensure the ME network has storage space for the promised outputs.
