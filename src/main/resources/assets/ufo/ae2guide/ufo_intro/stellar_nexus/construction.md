---
navigation:
  parent: ufo_intro/stellar_nexus.md
  title: Structure and Field Core
  icon: ufo:entropy_singularity_casing
  position: 10
---

# Stellar Nexus Structure and Field Core

The current Nexus topology spans **35×34×35**. The scene is generated directly
from the production pattern; no conceptual mockup is used. Large scenes may take
a moment to initialize. Drag to rotate and zoom.

<GameScene zoom="1.25" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/stellar_nexus.snbt" />
  <DiamondAnnotation pos="18.5 17.5 1.5" color="#80c6ff">Stellar Nexus Controller</DiamondAnnotation>
  <IsometricCamera yaw="215" pitch="25" />
</GameScene>

## Canonical Default Composition

| Component | Count |
|---|---:|
| <ItemLink id="ufo:entropy_singularity_casing" /> | 980 |
| <ItemLink id="ufo:entropy_assembler_core_casing" /> | 534 |
| <ItemLink id="ufo:entropy_computer_condensation_matrix" /> | 168 |
| <ItemLink id="ufo:stellar_field_generator_t1" /> or one uniform higher tier | 138 |
| <ItemLink id="ufo:stellar_nexus_controller" /> | 1 |

Some casing positions accept the required hatch roles instead of the default
Singularity Casing. The Nexus is not part of the new universal 5×5 family; use
its own JEI viewer/scan result and do not transplant another topology.
