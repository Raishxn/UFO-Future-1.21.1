# Current Multiblock Line

UFO Future has eight multiblock machines. The first five process resources and share the MK1–MK3 field tier system; the last three provide large-scale AE2 crafting infrastructure.

| Machine | Main role |
|---|---|
| [Quantum Matter Fabricator](quantum-matter-fabricator.md) | Bulk DMA and QMF recipes, with 27 parallel threads (9 in Safe Mode) |
| [Quantum Slicer](quantum-slicer.md) | Bulk printed-component preparation |
| [Quantum Processor Assembler](quantum-processor-assembler.md) | Final processor assembly |
| [Quantum Cryoforge](quantum-cryoforge.md) | Cryogenic processing, including the stable-coolant line |
| [Stellar Nexus](stellar-nexus.md) | Endgame stellar simulations with a 200-billion-AE buffer |
| [Quantum Computation Nexus](quantum-computation-nexus.md) | Pools installed UFO crafting-storage and co-processor modules into virtual AE2 CPUs |
| [Quantum Pattern Fabrication Matrix](quantum-pattern-fabrication-matrix.md) | Searchable, field-scaled pattern library and virtual assembler for crafting, smithing and stonecutting |
| [Infinity Fabrication Singularity](infinity-fabrication-singularity.md) | Capstone crafter with up to 128 persistent pattern routes |

## Patterns and network access

Each universal processing multiblock requires exactly one **Quantum Pattern Buffer** or **Quantum Pattern Proxy**. A Buffer holds **72 encoded patterns** and serves its own controller. A linked Proxy lets another controller use the same Buffer. The older **Quantum Pattern Hatch** belongs to the single-block DMA workflow.

The processing machines pull recipe ingredients from ME and return outputs through their ME hatches. Supply coolant externally to the **ME Massive Fluid Hatch** and FE to the **FE Energy Input Hatch**. ME network energy does not automatically power these controllers. Use the [multiblock tiers](multiblock-tiers.md) page for recipe access and efficiency bonuses. The [KubeJS guide](kubejs-recipes.md) documents custom processing recipes.

For current installation requirements and a broader feature list, see the [project README](https://github.com/Raishxn/UFO-Future-1.21.1#readme).
