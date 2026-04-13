# Quantum Matter Fabricator

El **Quantum Matter Fabricator (QMF)** es la evolucion multibloque del DMA. Existe para procesamiento masivo, paralelismo e integracion real con AE2.

## Comportamiento Base

- Hasta **27 hilos paralelos** en modo normal
- **9 hilos paralelos** con Safe Mode
- Acepta recetas nativas de **QMF** y recetas de **DMA**
- Extrae ingredientes directamente de la ME
- Devuelve los outputs al almacenamiento ME
- Usa el **Quantum Pattern Hatch** para automatizacion

## Quantum Pattern Hatch

- Guarda hasta **72 encoded patterns**
- Se vincula al controller cuando la estructura se ensambla
- Expone el multibloque a AE2 como maquina de crafting
- Permite que AE2 despache trabajos directo a los hilos libres

## Modelo Paralelo

Cada hilo libre puede ejecutar una copia de receta.

- Un pattern enviado por AE2 reserva un hilo
- Los hilos ociosos aun pueden iniciar trabajos validos desde la red
- Items, fluidos y AE llegan directo desde la ME
