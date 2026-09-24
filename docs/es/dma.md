# Ensamblador de Materia Dimensional (DMA)

El **DMA** es la primera maquina avanzada del mod. Procesa recetas de items sin forma en una cuadrícula de nueve espacios, acepta fluidos de receta y usa energia de la red AE2. Inicia la progresion antes de las maquinas multibloque.

## Entradas y refrigeracion

- El **tanque de fluido de receta** suministra el fluido que la receta consume.
- El **tanque de coolant** sirve para controlar el calor y es independiente del fluido de receta.
- Los **cuatro espacios de catalizador** cambian velocidad, energia, calor y posibilidades de resultados adicionales. Consulta [Catalizadores](catalysts.md).

Empieza con Gelid Cryotheum y avanza hacia Stable Coolant y Temporal Fluid cuando aumente la carga. La maquina genera calor al trabajar; a partir del 50% entra en zona de peligro y puede dañar a jugadores sin [armadura termica completa](armor.md). A temperatura maxima comienza una cuenta regresiva de cinco segundos antes del fallo destructivo.

## Siguiente etapa

Usa el [QMF](quantum-matter-fabricator.md) para procesamiento DMA en lote y las otras maquinas de la [linea multibloque](multiblock-line.md) para produccion industrial. Las recetas DMA usan `ufo:dimensional_assembly`; la [guia KubeJS](kubejs-recipes.md) explica su formato. Consulta JEI/EMI para los ingredientes y costos actuales.
