# Quantum Pattern Fabrication Matrix

La **Quantum Pattern Fabrication Matrix** es una biblioteca de patrones AE2 con busqueda y un ensamblador virtual para crafting, smithing y stonecutting.

## Capacidad y uso

- Sus **22 posiciones de campo** deben usar el mismo nivel MK.
- Segun el nivel de los campos, almacena de **5632 a 22 528 patrones**.
- Busca y administra patrones desde la interfaz del controlador.
- Conecta la maquina a la red ME para automatizacion por patrones.

La vista previa del juego muestra la distribucion de los campos. El [resumen de multibloques](multiblock-line.md) explica su lugar en la linea de crafting.

La Matrix ejecuta crafteos iguales por lotes y admite hasta **16 / 32 / 64 lotes de recetas pendientes** con Fields MK1 / MK2 / MK3. Puede devolver esa cantidad de lotes al almacenamiento ME por tick. El rendimiento depende de las operaciones de la CPU, los ingredientes, la energía de la red y el espacio para los resultados.
