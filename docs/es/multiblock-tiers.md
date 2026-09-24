# Niveles de multibloque

Los multibloques universales comparten los niveles **MK1 / MK2 / MK3**, que controlan el acceso a las recetas y las bonificaciones de eficiencia.

## Acceso a las recetas

- Maquina **MK1**: solo ejecuta recetas MK1.
- Maquina **MK2**: ejecuta recetas MK1 y MK2.
- Maquina **MK3**: ejecuta recetas MK1, MK2 y MK3.

El controlador no inicia una receta que requiere un nivel superior al de la maquina.

## Bonificacion por nivel

Al ejecutar una receta de nivel inferior, por cada nivel de diferencia:

- El **tiempo** se reduce a la mitad.
- El **consumo de AE** baja al **75%** del valor anterior.

Ejemplos:

- MK2 con receta MK1: **2x mas rapido** y **25% menos AE**.
- MK3 con receta MK1: **4x mas rapido** y **43,75% menos AE**.

## Por que mejorar la maquina?

La mejora acelera las recetas existentes incluso antes de desbloquear otras nuevas. Resulta util cuando AE2 distribuye muchos trabajos al mismo controlador mediante un Quantum Pattern Buffer o un Proxy vinculado.
