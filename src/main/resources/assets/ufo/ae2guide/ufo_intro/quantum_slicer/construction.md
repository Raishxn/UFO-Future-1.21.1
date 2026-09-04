---
navigation:
  parent: ufo_intro/quantum_slicer.md
  title: Structure and Auto-build
  icon: ufo:quantum_hyper_mechanical_casing
  position: 10
---

# Quantum Slicer Structure

The Slicer uses a fixed **13×5×5** asymmetric shell. The open air channels visible
in the scene are intentional. Drag to inspect every layer.

<GameScene zoom="4" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/quantum_slicer.snbt" />
  <DiamondAnnotation pos="6.5 2.5 0.5" color="#80c6ff">Slicer Controller</DiamondAnnotation>
  <IsometricCamera yaw="215" pitch="25" />
</GameScene>

## Material List

| Component | Count |
|---|---:|
| <ItemLink id="ufo:quantum_hyper_mechanical_casing" /> | 143 (one becomes Pattern Hatch) |
| <ItemLink id="ae2:quartz_vibrant_glass" /> | 36 (fixed) |
| <ItemLink id="ae2:quartz_block" /> | 24 (fixed) |
| <ItemLink id="ae2:fluix_block" /> | 12 (fixed) |
| <ItemLink id="ufo:stellar_field_generator_t1" /> or one uniform higher tier | 61 |
| <ItemLink id="ufo:quantum_pattern_hatch" /> | 1 (replace a casing) |
| <ItemLink id="ufo:quantum_slicer_controller" /> | 1 |

Auto-build performs a complete preflight, preserves matching blocks and refuses
to overwrite occupied mistakes. Materials must be in the player inventory.
After completion, use Scan to identify any manually substituted invalid field.
