---
navigation:
  parent: ufo_intro/qmf.md
  title: Structure and Auto-build
  icon: ufo:quantum_hyper_mechanical_casing
  position: 10
---

# QMF Structure and Auto-build

The Quantum Matter Fabricator occupies a fixed **15×7×7** volume. The scene is
generated from the same schema used by scan, JEI and auto-build. Drag to rotate.

<GameScene zoom="4" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/quantum_matter_fabricator.snbt" />
  <DiamondAnnotation pos="7.5 1.5 1.5" color="#80c6ff">QMF Controller</DiamondAnnotation>
  <IsometricCamera yaw="215" pitch="25" />
</GameScene>

## Material List

| Component | Count | Rule |
|---|---:|---|
| <ItemLink id="ufo:quantum_hyper_mechanical_casing" /> | 245 | Any one casing may become the Pattern Hatch |
| <ItemLink id="ae2:quartz_vibrant_glass" /> | 26 | Fixed observation shell |
| <ItemLink id="ae2:quartz_block" /> | 10 | Fixed |
| <ItemLink id="ae2:fluix_block" /> | 3 | Fixed |
| <ItemLink id="ufo:stellar_field_generator_t1" /> or better | 52 | All generators must use one tier |
| <ItemLink id="ufo:quantum_pattern_hatch" /> | 1 | Replace exactly one casing |
| <ItemLink id="ufo:quantum_matter_fabricator_controller" /> | 1 | Place first; not consumed by auto-build |

Air channels are intentional. Quartz Block, Fluix Block and Vibrant Glass are
strict positions; only Quantum Hyper Mechanical Casing may be replaced by the
Pattern Hatch or ME Massive Fluid Hatch.

## Using Auto-build

1. Place the Controller facing away from the volume reserved for the machine.
2. Put all required placeable blocks in the player inventory.
3. Open the controller and press **Auto-build**.
4. Wait while the server places one block per tick, then run **Scan**.

Auto-build preserves every already-valid block. It never replaces a wrong
occupied block. If preflight reports a blocked position, clear that coordinate
and restart. It reads only the player inventory, not ME storage or containers.

## Tier Variants

Every `F` position accepts MK1, MK2 or MK3 Stellar Field Generators. Use JEI
**Alternatives** to preview valid substitutions. Mixing tiers does not create an
average: the structure requires a coherent field tier.
