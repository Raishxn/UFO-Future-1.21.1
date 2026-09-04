---
navigation:
  parent: ufo_intro/infrastructure.md
  title: Multiblock Components
  position: 15
  icon: ufo:quantum_hyper_mechanical_casing
item_ids:
  - ufo:quantum_hyper_mechanical_casing
  - ufo:entropy_singularity_casing
  - ufo:entropy_assembler_core_casing
  - ufo:entropy_computer_condensation_matrix
---

# Multiblock Components

These blocks define the shell, internal frame and computation layers of UFO
multiblocks. Their positions are structural: replacing one with a visually
similar block does not satisfy validation.

## Quantum Hyper Mechanical Casing

<BlockImage id="ufo:quantum_hyper_mechanical_casing" scale="3"></BlockImage>

The common structural shell of the Quantum Matter Fabricator, Quantum Slicer,
Quantum Processor Assembler and Quantum Cryoforge. Universal hatch substitutions
are accepted only at positions explicitly permitted by each controller.

<Recipe id="ufo:quantum_hyper_mechanical_casing" />

## Entropy Singularity Casing

<BlockImage id="ufo:entropy_singularity_casing" scale="3"></BlockImage>

The outer load-bearing shell of the Stellar Nexus. It cannot be exchanged for
Quantum Hyper Mechanical Casing or an arbitrary hatch.

<Recipe id="ufo:entropy_singularity_casing" />

## Entropy Assembler Core Casing

<BlockImage id="ufo:entropy_assembler_core_casing" scale="3"></BlockImage>

This casing forms the dense inner computation body of the Stellar Nexus. Keep
its internal positions exactly as shown by the interactive construction scene.

<Recipe id="ufo:entropy_assembler_core_casing" />

## Entropy Computer Condensation Matrix

<BlockImage id="ufo:entropy_computer_condensation_matrix" scale="3"></BlockImage>

The condensation matrices occupy fixed internal positions and must not be
replaced with shell blocks. They are part of the Nexus computation geometry.

<Recipe id="ufo:entropy_computer_condensation_matrix" />

## Construction Rules

- Build from the lowest layer upward and compare each layer against the scene.
- Treat controller, field and condensation positions as fixed.
- Add optional automation only through ports documented for that machine.
- If formation fails, verify orientation and the first mismatched block reported
  by the controller before rebuilding surrounding layers.
