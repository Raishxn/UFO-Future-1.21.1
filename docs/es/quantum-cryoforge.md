# Quantum Cryoforge

El **Quantum Cryoforge** es la etapa criogenica de las recetas universales. Produce materiales como Stable Coolant para las maquinas posteriores.

## Operacion

- Ejecuta hasta **27 trabajos paralelos**, o **9** en Safe Mode.
- Usa requisitos MK1–MK3 y la [bonificacion por nivel](multiblock-tiers.md).
- Acepta recetas `ufo:universal_multiblock` con `machine: 'quantum_cryoforge'`; consulta [recetas KubeJS](kubejs-recipes.md).
- Requiere un Quantum Pattern Buffer o Proxy vinculado para automatizar patrones AE2.
- Extrae ingredientes de ME y devuelve los resultados. Suministra coolant externo al **ME Massive Fluid Hatch** y FE al **FE Energy Input Hatch**.

Usa la vista previa de la estructura o el Structure Scanner para encontrar las posiciones de los hatches.
