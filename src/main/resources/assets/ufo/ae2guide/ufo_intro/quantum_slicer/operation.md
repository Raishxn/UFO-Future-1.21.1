---
navigation:
  parent: ufo_intro/quantum_slicer.md
  title: Production Pipeline
  icon: ufo:quantum_slicer_controller
  position: 20
---

# Quantum Slicer Production Pipeline

The Slicer turns upstream materials into printed and sliced components consumed
by processor recipes. Treat it as the first provider in a two-machine AE2 chain.

1. Encode a Slicer processing pattern with the exact JEI input and base output.
2. Store it in this machine's Quantum Pattern Buffer, or in the Buffer linked to its Proxy.
3. Encode the corresponding final processor recipe for the
   [Quantum Processor Assembler](../quantum_processor_assembler.md).
4. Request a single final processor and verify both providers are scheduled.
5. Increase batch size only after both output paths return to ME reliably.

The controller offers 27 persistent threads, or 9 in Safe Mode. Partial output
acceptance is recorded per thread; the rejected remainder stays buffered rather
than being rolled again or lost.

## Common Pipeline Faults

- **Final pattern unavailable:** verify the Assembler is formed and online.
- **Printed component accumulates:** check the final recipe and its pattern.
- **Crafting CPU waits:** inspect `OUTPUT_BLOCKED`, grid power and item capacity.
- **Wrong recipe tier:** install one uniform, sufficient field-generator tier.
