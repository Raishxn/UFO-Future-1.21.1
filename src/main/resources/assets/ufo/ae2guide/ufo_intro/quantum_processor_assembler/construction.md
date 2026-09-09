---
navigation:
  parent: ufo_intro/quantum_processor_assembler.md
  title: Structure and Auto-build
  icon: ufo:quantum_hyper_mechanical_casing
  position: 10
---

# Processor Assembler Structure

The Assembler occupies **5×7×12**. Its internal field geometry differs from both
the QMF and Slicer; do not reuse another machine's layer plan.

<GameScene zoom="4" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/quantum_processor_assembler.snbt" />
  <DiamondAnnotation pos="2.5 3.5 0.5" color="#80c6ff">Processor Assembler Controller</DiamondAnnotation>
  <IsometricCamera yaw="215" pitch="25" />
</GameScene>

## Material List

| Component | Count |
|---|---:|
| <ItemLink id="ufo:quantum_hyper_mechanical_casing" /> | 80 (one becomes Pattern Buffer or Proxy) |
| <ItemLink id="ae2:quartz_vibrant_glass" /> | 73 (fixed) |
| <ItemLink id="ae2:quartz_block" /> | 16 (fixed) |
| <ItemLink id="ae2:fluix_block" /> | 12 (fixed) |
| <ItemLink id="ufo:stellar_field_generator_t1" /> or one uniform higher tier | 72 |
| <ItemLink id="ufo:quantum_pattern_hatch" /> | 1 (replace a casing) |
| <ItemLink id="ufo:quantum_processor_assembler_controller" /> | 1 |

Place the controller first, carry the remaining materials and use Auto-build.
Existing valid blocks are deducted. Any wrong occupied slot aborts preflight and
is left untouched; use Scan/highlights to locate it.
