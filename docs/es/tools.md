# Herramientas y Armas

UFO Future introduce un **sistema de multi-herramienta energética transformable**. Todas las herramientas comparten un único ítem alimentado por RF que puede transformarse entre diferentes tipos usando el ciclo de la rueda del ratón.

---

## El Sistema Multi-Herramienta

- **Alimentadas por RF**: Consume energía RF por uso en lugar de durabilidad
- **Nombre Arcoíris**: Efecto animado de color arcoíris
- **Transformable**: Mantén la herramienta y rota (Shift+Scroll) para ciclar entre tipos
- **Energía Persistente**: La energía se preserva al transformar (usa `transmuteCopy`)
- **Barra de Energía**: Barra dinámica de energía en el ítem

---

## Orden del Ciclo

| # | Herramienta | Tipo | Característica Especial |
|---|------------|------|------------------------|
| 1 | Bastón UFO | Cuerpo a cuerpo | Herramienta base — punto de entrada |
| 2 | Espada UFO | Espada | Combate estándar |
| 3 | Pico UFO | Pico | Toggle Auto-Fundición (Shift+Clic Derecho) |
| 4 | Hacha UFO | Hacha | Tala de árboles |
| 5 | Pala UFO | Pala | Creación de caminos |
| 6 | Azada UFO | Azada | Auto-replanteo de cultivos (área 3x3) |
| 7 | Martillo UFO | Pico+ | Minería en área (1×1 / 3×3 / 5×5 / 7×7) |
| 8 | Gran Espada UFO | Espada | Daño pesado, más lenta |
| 9 | Caña de Pescar UFO | Caña | Pesca alimentada por energía |
| 10 | Arco UFO | Arco | Toggle modo tiro rápido |

---

## Detalles Individuales

### Bastón UFO
- **Tipo**: Arma cuerpo a cuerpo · **ID**: `ufo:ufo_staff`

### Espada UFO
- **Ataque**: +5 daño, −2.4 velocidad · **ID**: `ufo:ufo_sword`

### Pico UFO
- **Ataque**: +1 daño, −2.8 velocidad
- **Especial**: **Auto-Fundición** — `Shift+Clic Derecho`. Minerales minados se funden automáticamente.
- **Energía**: 50 RF/bloque · **ID**: `ufo:ufo_pickaxe`

### Hacha UFO
- **Ataque**: +6 daño, −3.2 velocidad · **ID**: `ufo:ufo_axe`

### Pala UFO
- **Ataque**: +1.5 daño, −3.0 velocidad · **ID**: `ufo:ufo_shovel`

### Azada UFO
- **Especial**: Labra un **área 3×3**. Auto-replantea cultivos.
- **ID**: `ufo:ufo_hoe`

### Martillo UFO ⭐
- **Ataque**: +7 daño, −3.4 velocidad
- **Energía**: 50 RF **por bloque** · Modos: 1×1, 3×3, 5×5, 7×7
- **Auto-Fundición**: Compatible · **Energía Inteligente**: Se detiene sin energía
- **ID**: `ufo:ufo_hammer`

### Gran Espada UFO
- **Ataque**: +8 daño, −3.0 velocidad · **ID**: `ufo:ufo_greatsword`

### Caña de Pescar UFO
- **Durabilidad**: 500 · **ID**: `ufo:ufo_fishing_rod`

### Arco UFO
- **Durabilidad**: 5000 · **Especial**: Modo **Tiro Rápido**
- **ID**: `ufo:ufo_bow`

---

## Fabricación

### Bastón UFO (Receta Primaria — DMA)
- **Entradas**: Esfera de Neutronio Enriquecida Cargada ×1, Anomalía Cuántica ×2, Proto Materia ×2, Lingote de Netherite ×4, Bola de Materia AE2 ×16
- **Fluido**: 2,000 mB Luz Estelar Líquida
- **Energía**: 12,000,000 AE · **Tiempo**: 2,400 ticks (2 min)

### Bastón UFO (Receta Alternativa — DMA)
- **Entradas**: Espada/Pico/Hacha/Azada de Netherite + Lingote Estrella de Neutrones
- **Energía**: 500,000 AE · **Tiempo**: 600 ticks (30s)

---

*Ver también: [Armadura](armor.md) · [Progresión](progression.md)*
