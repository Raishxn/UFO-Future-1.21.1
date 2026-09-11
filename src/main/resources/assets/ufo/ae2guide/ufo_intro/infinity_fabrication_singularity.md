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
copy-by-copy crafting machines. It turns a homogeneous AE2 crafting task into one
aggregate route, so a request for billions of identical items does not execute a
billion machine cycles.

It combines two services inside one multiblock:

- its own field-scaled crafting-pattern library;
- its own aggregate fabrication executor for massive crafting requests.

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
- 52 Quantum Hyper-Mechanical Casings;
- 25 Stellar Field Generators, in any mixture of MK1, MK2 and MK3;
- 36 Quartz Vibrant Glass;
- 12 Quartz Blocks;
- 12 Fluix Blocks;
- 8 Graviton-Plated Casings.

The remaining 196 positions are ignored by validation and may be air. Connect an
AE2 cable to the outward face of the Quantum Grid Link. The Link consumes one
channel and at least 32 AE/t.

## Pattern pages

Open the controller to insert encoded patterns directly into the nine-slot row,
using the arrow buttons to change pages. Every field position adds pages according
to the installed tier: **MK1 adds 1 page**, **MK2 adds 2 pages**, and **MK3 adds 4
pages**. With 25 fields, total capacity ranges from 25 pages / 225 patterns to 100
pages / 900 patterns. Every field position must use the same tier; mixing tiers
invalidates the structure.

If fields are downgraded, patterns above the new capacity are preserved and remain
accessible for removal; no encoded pattern is deleted automatically.

## Aggregate crafting

For each eligible crafting step, the Singularity validates one recipe copy, then
extracts all available copies in bulk and promises the corresponding outputs to
the AE2 crafting CPU in one bounded operation. Amounts use 64-bit counters, so
requests in the billions remain a single route rather than an item-by-item loop.

Up to **128 routes** can be pending at once. Routes and their promised outputs are
persisted across world reloads. Smithing and stonecutting crafting patterns are
supported alongside normal crafting patterns; processing patterns continue to
use their actual processing machines.

Duplicate patterns stored on different pages are published as one deduplicated
crafting route.

Enabling a pattern also starts continuous automatic fabrication, like the Quantum
Crafter: while ingredients remain in the network, the Singularity repeats the
recipe. Unlike the original machine, each round consumes every currently
available copy at once and creates one aggregate route instead of iterating one
copy at a time. Disabling the pattern stops new rounds without discarding outputs
that are already waiting for network space.

## Operating modes

| Mode | Route budget | AE cost | Use case |
| --- | ---: | ---: | --- |
| Balanced | 64/tick | 1× | General operation |
| Speed | 128/tick | 2× | Maximum burst throughput |
| Efficiency | 32/tick | 0.5× | Power-constrained networks |

The fields select the mode automatically: **Mk.I uses Efficiency**, **Mk.II uses
Balanced**, and **Mk.III uses Speed**. Mixed structures use their lowest installed
tier, so Speed requires all 25 fields to be Mk.III. Replacing a field affects new
dispatches immediately without cancelling jobs or discarding queued outputs.

## Dashboard and troubleshooting

The interface shows the pattern pages plus structure, grid, active-job and
queued-route status. **Scan Structure** highlights mismatched blocks;
**Auto-build** places only missing blocks for which the player has materials.

- **Structure incomplete:** repair the position highlighted by the scanner.
- **ME grid offline:** power the network, free one channel, and cable the Grid Link's outward face.
- **Waiting for patterns:** insert a supported encoded pattern into one of the controller's pages.
- **Routes remain queued:** ensure the ME network has storage space for the promised outputs.
