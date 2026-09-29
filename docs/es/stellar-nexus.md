# Stellar Nexus

El **Stellar Nexus** es el multibloque final de simulacion de UFO Future. Consume AE, fluidos raros y materiales de alto tier para producir outputs en escala extrema.

## Identidad De La Maquina

- Multibloque masivo
- Lee items y fluidos directamente desde la ME
- Carga un buffer interno de **200B AE**
- Consume combustible al iniciar y coolant durante la operacion
- Usa calor, safe mode y overclock como ejes principales
- Requiere los hatches de entrada y salida de items, un ME Massive Fluid Hatch para coolant externo y un FE Energy Input Hatch para energia externa

## Programas de simulacion

Beta 5 conserva las **13 simulaciones originales** y sus tipos de recursos, pero aumenta la producción a **280 millones–1,625 mil millones de items** y **107–375 millones de mB** por ciclo. Se eliminan los bloques de mineral de las salidas; los metales se entregan procesados.

La nueva simulación **Stellar Omnibus (MK3)** reúne los recursos de esas recetas, con dos sustituciones en **64 salidas de items y 17 de fluidos**. Requiere Advanced AE, ExtendedAE, Mega Cells, Mekanism, Mekanism Generators y Applied Flux para disponer de todos los recursos opcionales. En Omnibus, la piedra y la obsidiana se sustituyen por los tres lingotes de UFO Future, materiales avanzados y fluidos de fusión. Ethylene y Sky Steel siguen disponibles en MK2.

En JEI, las salidas llenan primero los tres espacios de cada panel de izquierda a derecha y luego bajan a la siguiente fila. Las selecciones del catálogo experimental se reasignan a una receta original al cargar el mundo.

## Tiers De Field Generator

Las cuatro posiciones de field generator deben ser del mismo tier:

- **MK1**
- **MK2**
- **MK3**

Mezclar tiers invalida la estructura.

## Escalera De Coolant

- **Gelid Cryotheum** = baja eficiencia
- **Stable Coolant** = eficiencia media
- **Temporal Fluid** = eficiencia extrema

## Safe Mode

El Safe Mode es la opcion segura para automatizacion.

- **2x** costo de AE
- **2x** consumo de combustible
- **2.5x** consumo de coolant
- Apagado automatico en vez de explosion

## Overclock

- **5x** mas velocidad
- **8x** costo de AE
- **5x** combustible
- **5x** calor
- **5x** coolant

Tambien afecta a las recetas personalizadas.

## Politica de sobrecalentamiento

Con Safe Mode desactivado, el calor maximo causa un fallo local con dano y efectos visuales. **La destruccion de bloques esta desactivada por defecto.** El administrador puede habilitar una onda destructiva limitada en `config/ufo/server.toml` (`stellar.explosion.enableBlockGrief`), con limites de radio, bloques por tick, total de bloques y dimensiones permitidas. Lava y explosiones secundarias tienen opciones separadas.
