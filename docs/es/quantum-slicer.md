# Quantum Slicer

El **Quantum Slicer** prepara componentes impresos en gran volumen para lineas de procesadores y circuitos.

## Comportamiento Base

- Convierte materiales base en partes impresas
- Usa el sistema universal de recetas de multibloque
- Soporta hasta **27 trabajos paralelos** en modo normal
- Baja a **9 trabajos paralelos** con Safe Mode
- Se integra con AE2 por medio del **Quantum Pattern Hatch**
- Extrae y devuelve ingredientes y outputs directamente por la ME

## Pattern Hatch

- El **Quantum Pattern Hatch** guarda **72 encoded patterns**
- Se vincula al controller despues del ensamblado
- AE2 puede empujar trabajos sin inventario manual

## Posicion En La Fabrica

1. Los materiales entran al **Quantum Slicer**
2. Las partes impresas vuelven a la ME
3. AE2 pide los procesadores finales al **Quantum Processor Assembler**
