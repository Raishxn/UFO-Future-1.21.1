---
navigation:
  parent: ufo_intro/quantum_cryoforge.md
  title: Structure and Auto-build
  icon: ufo:quantum_hyper_mechanical_casing
  position: 10
---

# Quantum Cryoforge Structure

The redesigned Cryoforge occupies a **9×14×9** volume. Rotate the generated scene
to inspect its Blue Ice chamber and field-generator lattice.

<GameScene zoom="3.2" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/quantum_cryoforge.snbt" />
  <DiamondAnnotation pos="4.5 1.5 0.5" color="#80c6ff">Cryoforge Controller</DiamondAnnotation>
  <DiamondAnnotation pos="4.5 1.5 4.5" color="#7ae7f2">Blue Ice chamber</DiamondAnnotation>
  <IsometricCamera yaw="215" pitch="25" />
</GameScene>

## Default Auto-build Materials

| Component | Count | Placement rule |
|---|---:|---|
| <ItemLink id="ufo:quantum_hyper_mechanical_casing" /> | 226 | Casing positions; valid universal hatches may replace allowed slots manually |
| <ItemLink id="ae2:quartz_vibrant_glass" /> | 16 | Fixed glass positions |
| <ItemLink id="ae2:quartz_block" /> | 86 | Fixed quartz framework |
| <ItemLink id="ae2:fluix_block" /> | 72 | Fixed fluix conduits |
| <ItemLink id="ufo:stellar_field_generator_t1" /> or one uniform higher tier | 45 | Determines machine tier |
| <ItemLink id="minecraft:blue_ice" /> | 40 | Fixed cold chamber |
| <ItemLink id="ufo:quantum_cryoforge_controller" /> | 1 | Place first |

Auto-build uses the default casing in flexible `B` positions. Install required
universal hatches in valid casing slots before the final scan. Use JEI
Alternatives to inspect accepted substitutions.
