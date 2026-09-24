# Quantum Slicer

El **Quantum Slicer** prepara componentes impresos en gran volumen para lineas de procesadores y circuitos.

## Comportamiento Base

- Convierte materiales base en partes impresas
- Usa el sistema universal de recetas de multibloque
- Soporta hasta **27 trabajos paralelos** en modo normal
- Baja a **9 trabajos paralelos** con Safe Mode
- Se integra con AE2 mediante un **Quantum Pattern Buffer** o **Quantum Pattern Proxy** vinculado
- Extrae y devuelve ingredientes y outputs directamente por la ME

## Pattern Buffer y Proxy

- La estructura requiere exactamente un Buffer o Proxy.
- El Buffer almacena **72 patrones codificados**; un Proxy vinculado comparte otro Buffer.
- AE2 puede enviar trabajos sin inventario manual.

## Posicion En La Fabrica

1. Los materiales entran al **Quantum Slicer**
2. Las partes impresas vuelven a la ME
3. AE2 pide los procesadores finales al **Quantum Processor Assembler**
