---
navigation:
  parent: ufo_intro/infrastructure.md
  title: Quantum Hatches
  position: 46
item_ids:
  - ufo:quantum_pattern_hatch
  - ufo:quantum_pattern_provider_part
  - ufo:me_massive_output_hatch
  - ufo:me_massive_fluid_hatch
  - ufo:me_massive_input_hatch
  - ufo:ae_energy_input_hatch
---

# Quantum Hatches

<BlockImage id="ufo:quantum_pattern_hatch" scale="3"></BlockImage>

The **Quantum Pattern Hatch** exposes universal multiblocks to AE2 autocrafting and stores up to **72 patterns**.

<ItemImage id="ufo:quantum_pattern_provider_part" scale="3" />

The **Quantum Pattern Provider** is the cable-bus part version of the expanded provider.

- **ME Massive Output Hatch** returns finished items to the ME network.
- **ME Massive Fluid Hatch** is also the shared coolant input: it combines a
  **16,000,000 mB local tank** for external pipes with its existing ME fluid access.
- **ME Massive Input Hatch** feeds bulk item throughput into multiblocks.
- **AE Energy Input Hatch** is the dedicated AE power hatch.

## AE2 Connection Rules

Massive hatches remain **ME cable ports**. The Massive Fluid Hatch additionally
exposes a native NeoForge fluid input capability for coolant automation.

- Connect the ME cable to the face indicated by the hatch orientation. The other
  five faces are intentionally isolated from the ME grid.
- Fluid pipes may fill the Massive Fluid Hatch from any face, including the face
  used by an ME cable. Pipe access is input-only and accepts Gelid Cryotheum,
  Stable Coolant or Temporal Fluid; it cannot drain the tank or mix coolants.
- Put item and fluid inputs in ME storage.
- Finished item and fluid outputs return to ME storage through the matching hatch.
- The AE Energy Input Hatch lets the multiblock draw AE from the connected grid.
- Use the hatch role the structure asks for: item input, item output, fluid output or AE energy.

The controller consumes the Massive Fluid Hatch's local tank first, then falls
back to the existing ME storage path. Old structures and AE-only automation keep
working without conversion. In QMF, QPA, Slicer and Cryoforge, both this hatch
and the Quantum Pattern Hatch may replace casing positions only; Vibrant Glass
positions stay glass-only.

Right-click a massive hatch with an empty hand to see whether it is online and
linked to a controller. A neighboring hatch does not create an invisible cable
connection through the multiblock shell.

## Pattern Ownership

When AE2 pushes a processing pattern, the items delivered with that pattern are
owned by the process immediately. The controller does not charge those item
requirements a second time. Missing fluids or supported chemicals may still be
pulled through their configured storage paths.
