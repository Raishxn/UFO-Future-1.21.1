# Catalizadores

El **DMA, QMF, Quantum Slicer, Quantum Processor Assembler y Quantum Cryoforge** aceptan catalizadores en cuatro espacios de mejora. Hay cuatro familias con tres niveles cada una, ademas del Catalizador Dimensional.

| Familia | Efecto principal | T1 / T2 / T3 |
|---|---|---|
| Matterflow | Reduce el costo de energia de la receta | −10% / −25% / −50% por carta |
| Chrono | Acelera el procesamiento | +25% / +62,5% / +125% de velocidad por carta |
| Quantum | Probabilidad de resultado adicional | +10% / +25% / +50% por carta |
| Overflux | Modificador termico | Calor estatico negativo; depende del controlador |

Matterflow y Chrono tambien multiplican el buffer de energia por **10 / 100 / 1000** en T1 / T2 / T3. Combinar catalizadores potentes puede elevar mucho el calor. Cuatro cartas identicas activan una sinergia de su familia y una penalizacion termica adicional.

## Overflux y calor

La antigua wiki afirmaba que Overflux reducia una probabilidad de fallo y enfriaba activamente el DMA. El codigo actual no contiene esa mecanica de probabilidad de fallo. El DMA limita cada contribucion negativa de calor a cero; por eso Overflux **no** enfria activamente el DMA. Los multibloques paralelos incluyen el valor negativo en su perfil termico combinado; cuatro cartas Overflux reducen mas ese perfil.

## Catalizador Dimensional

Este objeto se **fabrica en el QMF MK3**, a pesar del antiguo nombre de “catalizador creativo” en su tooltip. Acelera mucho el proceso, elimina los costos de energia y calor, y garantiza salida adicional. **Los ingredientes de la receta siguen consumiendose.** Consulta la receta actual en JEI/EMI.

Usa el panel de estado del controlador para revisar los efectos combinados de velocidad, energia y calor antes de automatizar continuamente.

*Ver tambien: [DMA](dma.md) · [Linea de multibloques](multiblock-line.md)*
