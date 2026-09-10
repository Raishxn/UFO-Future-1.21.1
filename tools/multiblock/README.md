# Multiblock authoring

`multis.pyw` converts pasted structure data and NBT/JSON sources into forms used
while authoring UFO multiblock patterns. The files in `sources/` are historical
material-count exports; they are not runtime resources.

Always validate a generated pattern against the compiled definition and the JEI
viewer before replacing production code.

## Endgame AE layouts

The three layouts approved on 2026-09-09 were imported from the Copy/Paste
Gadget exports in `MineProjects/printUI` and normalized with the same 90-degree
conversion used by `multis.pyw`:

| Export | Runtime schema | Normalized size | Provisional controller |
| --- | --- | --- | --- |
| `quantum computation nexus structure.txt` | `QuantumComputationNexusTopologySchema` | 18 x 5 x 12 | H at 8,2,7 |
| `quantum pattern fabrication Matrix structure.txt` | `QuantumPatternFabricationMatrixTopologySchema` | 7 x 5 x 9 | H at 1,0,6 |
| `infinity fabrication singularity structure.txt` | `InfinityFabricationSingularityTopologySchema` | 7 x 7 x 7 | H at 0,3,3 |

The exported `ufo:quantum_slicer_controller` is only an `H` position marker.
Each pattern factory must bind `H` to its own final controller block. Air from
the exports is intentionally represented by `A`/`MultiblockPattern.ANY`, so
internal compute and crafting modules can be installed without invalidating the
outer structure.
