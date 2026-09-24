# Quantum Processor Assembler

El **Quantum Processor Assembler** es el multibloque de acabado para la linea de procesadores. Recibe circuitos impresos y materiales de soporte para ensamblar procesadores finales a escala endgame.

## Funciones Principales

- Hasta **27 trabajos paralelos** en modo normal
- **9 trabajos paralelos** con Safe Mode
- Soporte para recetas universales de multibloque
- Integracion de autocrafting AE2 mediante un **Quantum Pattern Buffer** o **Quantum Pattern Proxy** vinculado
- Pull de ingredientes y push de outputs directo por la red

## Pattern Buffer y Proxy

- La estructura requiere exactamente un Buffer o Proxy.
- El Buffer almacena **72 patrones codificados**; un Proxy vinculado comparte otro Buffer.
- Cada patron usa un hilo libre en vez de bloquear toda la maquina.

## Cadena De Fabrica

- **Quantum Slicer** crea las partes impresas
- **Quantum Processor Assembler** termina los procesadores
- **QMF** cubre conversiones pesadas del estilo DMA
