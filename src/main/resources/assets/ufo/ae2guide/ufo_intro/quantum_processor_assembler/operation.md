---
navigation:
  parent: ufo_intro/quantum_processor_assembler.md
  title: Processor Assembly
  icon: ufo:quantum_processor_assembler_controller
  position: 20
---

# Processor Assembly

This is the finishing provider for supported processor families. It accepts
only recipes whose inputs, outputs and required tier match the encoded pattern.

## Recommended AE2 Chain

1. Publish every prerequisite printed-component pattern from the Quantum Slicer.
2. Publish the final assembly pattern from this machine's Pattern Hatch.
3. Keep both providers on the same powered crafting network.
4. Request one result and inspect the crafting plan before requesting bulk work.

Up to 27 jobs can be reserved simultaneously, reduced to 9 in Safe Mode. Jobs
that already own inputs remain persistent across reload and temporary grid loss.
Output is inserted in deterministic batches; rejected quantities remain attached
to their original process.

## Recipe Diagnosis

If AE2 does not select this provider, check the encoded base output, exact input
amounts, machine MK tier, free thread count and network connection. Chemical
ingredients require their supported port path and fail closed if delivered as an
unknown key.
