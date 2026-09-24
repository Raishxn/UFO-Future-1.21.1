# Quantum Matter Fabricator

El **Quantum Matter Fabricator (QMF)** es la evolucion multibloque del DMA. Existe para procesamiento masivo, paralelismo e integracion real con AE2.

## Comportamiento Base

- Hasta **27 hilos paralelos** en modo normal
- **9 hilos paralelos** con Safe Mode
- Acepta recetas nativas de **QMF** y recetas de **DMA**
- Extrae ingredientes directamente de la ME
- Devuelve los outputs al almacenamiento ME
- Usa el **Quantum Pattern Buffer** o un **Quantum Pattern Proxy** vinculado para automatizacion

## Pattern Buffer y Proxy

- La estructura requiere exactamente un Buffer o Proxy.
- El Buffer almacena hasta **72 patrones codificados** y sirve a su propio controlador.
- Un Proxy vinculado comparte otro Buffer con este controlador.
- AE2 puede enviar trabajos a los hilos libres.
- El antiguo **Quantum Pattern Hatch** corresponde al DMA de un solo bloque.

## Modelo Paralelo

Cada hilo libre puede ejecutar una copia de receta.

- Un pattern enviado por AE2 reserva un hilo
- Los hilos ociosos aun pueden iniciar trabajos validos desde la red
- Los items y fluidos de la receta llegan desde ME. Suministra coolant externo al ME Massive Fluid Hatch y FE al FE Energy Input Hatch.
