# Quantum Slicer

The **Quantum Slicer** is the printed-component preparation multiblock. It handles the “cutting” stage for large processor and circuit pipelines.

## Machine Role

- Converts bulk source materials into printed parts
- Uses the universal multiblock recipe system
- Supports up to **8 parallel jobs**
- Integrates with AE2 through the **Quantum Pattern Hatch**

This machine is built for throughput, not one-off hand-fed recipes.

## Typical Factory Position

The normal chain is:

1. Bulk source blocks or materials enter the **Quantum Slicer**
2. Printed outputs move into the ME network
3. AE2 requests finished processors from the **Quantum Processor Assembler**

The result is a clean split between printed-part generation and processor assembly.

## Automation Notes

- Encoded patterns live in the hatch, not the controller
- The controller consumes directly from the ME network
- Parallel threads let one slicer handle multiple circuit families at once

## See Also

- [Quantum Processor Assembler](quantum-processor-assembler.md)
- [Quantum Matter Fabricator](quantum-matter-fabricator.md)
