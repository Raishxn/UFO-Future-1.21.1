---
navigation:
  parent: ufo_intro/quantum_cryoforge.md
  title: Structure and Auto-build
  icon: ufo:quantum_hyper_mechanical_casing
  position: 10
---

# Quantum Cryoforge Structure

The Cryoforge occupies an asymmetric **6×7×7** volume. Rotate the generated scene
to inspect its Blue Ice chamber and field-generator lattice.

<GameScene zoom="3.2" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/quantum_cryoforge.snbt" />
  <DiamondAnnotation pos="5.5 1.5 3.5" color="#80c6ff">Cryoforge Controller</DiamondAnnotation>
  <DiamondAnnotation pos="4.5 2.5 2.5" color="#7ae7f2">Blue Ice chamber</DiamondAnnotation>
  <IsometricCamera yaw="215" pitch="25" />
</GameScene>

## Default Auto-build Materials

| Component | Count | Placement rule |
|---|---:|---|
| <ItemLink id="ufo:quantum_hyper_mechanical_casing" /> | 158 | Casing positions; valid universal hatches may replace allowed slots manually |
| <ItemLink id="ae2:quartz_vibrant_glass" /> | 38 | Fixed glass positions |
| <ItemLink id="ufo:stellar_field_generator_t1" /> or one uniform higher tier | 24 | Determines machine tier |
| <ItemLink id="minecraft:blue_ice" /> | 9 | Fixed cold chamber |
| <ItemLink id="ufo:quantum_cryoforge_controller" /> | 1 | Place first |

Auto-build uses the default casing in flexible `B` positions. Install required
universal hatches in valid casing slots before the final scan. Use JEI
Alternatives to inspect accepted substitutions.
