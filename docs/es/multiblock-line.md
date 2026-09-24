# Linea actual de multibloques

UFO Future tiene ocho maquinas multibloque. Las primeras cinco procesan recursos y comparten los niveles de campo MK1–MK3; las otras tres ofrecen infraestructura de autocrafting AE2 a gran escala.

| Maquina | Funcion principal |
|---|---|
| [Quantum Matter Fabricator](quantum-matter-fabricator.md) | Recetas DMA y QMF en lote, con 27 hilos paralelos (9 en Safe Mode) |
| [Quantum Slicer](quantum-slicer.md) | Produccion masiva de componentes impresos |
| [Quantum Processor Assembler](quantum-processor-assembler.md) | Ensamblaje final de procesadores |
| [Quantum Cryoforge](quantum-cryoforge.md) | Procesamiento criogenico, incluida la linea de Stable Coolant |
| [Stellar Nexus](stellar-nexus.md) | Simulaciones estelares finales con un buffer de 200 mil millones de AE |
| [Quantum Computation Nexus](quantum-computation-nexus.md) | Combina modulos UFO de almacenamiento de crafting y coprocesadores en CPU virtuales AE2 |
| [Quantum Pattern Fabrication Matrix](quantum-pattern-fabrication-matrix.md) | Biblioteca de patrones con busqueda y ensamblador virtual para crafting, smithing y stonecutting |
| [Infinity Fabrication Singularity](infinity-fabrication-singularity.md) | Maquina final de crafting con hasta 128 rutas persistentes de patrones |

## Patrones y acceso a la red

Cada multibloque universal de procesamiento requiere exactamente un **Quantum Pattern Buffer** o **Quantum Pattern Proxy**. El Buffer almacena **72 patrones codificados** y sirve a su propio controlador. Un Proxy vinculado permite que otro controlador utilice el mismo Buffer. El antiguo **Quantum Pattern Hatch** corresponde al flujo del DMA de un solo bloque.

Las maquinas de procesamiento extraen ingredientes de las recetas desde ME y devuelven los resultados mediante sus hatches ME. Suministra coolant externo al **ME Massive Fluid Hatch** y FE al **FE Energy Input Hatch**. La energia de la red ME no alimenta estos controladores automaticamente. Consulta los [niveles de multibloque](multiblock-tiers.md) para acceso a recetas y bonificaciones. La [guia KubeJS](kubejs-recipes.md) documenta recetas personalizadas.

Para requisitos de instalacion y la lista completa de funciones, consulta el [README del proyecto](https://github.com/Raishxn/UFO-Future-1.21.1#readme).
