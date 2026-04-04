# Ensamblador de Materia Dimensional (DMA)

El **Ensamblador de Materia Dimensional** es la máquina principal de UFO Future. Todos los ítems avanzados, fluidos, materiales y componentes del mod se fabrican a través del DMA. Es una máquina alimentada por AE2 con entradas de ítems sin forma, entradas de fluidos, sistema de refrigeración y soporte para upgrades de catalizador.

---

## Fabricación del DMA

```
O P O
C M C
O E O
```

| Símbolo | Ítem |
|---------|------|
| O | Matriz de Obsidiana |
| P | Procesador de Ingeniería AE2 |
| C | Carcasa Revestida de Gravitón |
| M | Bloque Controlador AE2 |
| E | Ojo de Ender |

> **Prerrequisito**: Necesitas acceso a AE2 y una Carcasa Revestida de Gravitón primero.

---

## Descripción de la GUI

| Sección | Descripción |
|---------|-------------|
| **Cuadrícula de 9 Slots** | Entradas de ítems sin forma (el orden no importa) |
| **2 Slots de Salida** | Las salidas de ítems/fluidos aparecen aquí |
| **Tanque de Refrigerante** (Slot 2) | Fluido refrigerante gestionado por el jugador — NO consumido por recetas |
| **Tanque de Fluido Base** (Slot 3) | Entrada de fluido requerido por la receta — consumido por recetas |
| **4 Slots de Upgrade** | Para tarjetas de upgrade de Catalizador |
| **Barra de Progreso** | Muestra el progreso del procesamiento |
| **Display de Energía** | Muestra la energía AE almacenada y tasa de consumo |
| **Medidor de Temperatura** | Muestra el nivel de calor actual vs capacidad máxima |

---

## Cómo Funciona

1. **Coloca ítems** en la cuadrícula de 9 slots. **El orden no importa** — todas las recetas son sin forma.
2. **Añade fluido base** al tanque derecho (Slot 3) si la receta lo requiere.
3. **Añade refrigerante** al tanque izquierdo (Slot 2) para prevenir sobrecalentamiento.
4. **Inserta catalizadores** en los 4 slots de upgrade para modificar el rendimiento.
5. **Conecta energía AE2** — el DMA es una entidad de bloque conectada a la red AE2.
6. La máquina comienza a procesar cuando se encuentra una receta válida y hay suficiente energía.

---

## Sistema Térmico

El DMA genera calor durante el procesamiento y debe enfriarse para prevenir falla catastrófica.

### Zonas de Calor

| Zona | Umbral | Efectos |
|------|--------|---------|
| **Segura** | < 50% capacidad | Operación normal |
| **Peligro** | ≥ 50% capacidad | Anillos de partículas de llama, quema jugadores cercanos sin armadura térmica |
| **Colapso** | 100% capacidad | Cuenta regresiva de 5 segundos, luego **explosión** |

### Generación de Calor
- **Trabajando**: +1 HU cada 2 ticks (≈ +10 HU/s base), multiplicado por modificadores de catalizadores
- **Inactiva**: Enfriamiento pasivo −1 HU cada 40 ticks (≈ −0.5 HU/s)

### Efectividad del Refrigerante

| Refrigerante | Eficiencia | Notas |
|-------------|-----------|-------|
| **Fluido Temporal** | 100 HU por 1 mB | Más eficiente — refrigerante endgame |
| **Luz Estelar Líquida** | 30 HU por 1 mB | Buena opción mid-game |
| **Fluidos estándar** | 15 HU por 1 mB | Cualquier fluido no reconocido |
| **Criotheum Gélido** | 1 HU por 200 mB | Basado en volumen — necesita bombeo masivo |

### Explosión de Colapso
Cuando la temperatura alcanza 100% de la capacidad máxima:
1. Una **cuenta regresiva de 5 segundos** comienza (100 ticks)
2. **Sonido de alarma** cada segundo
3. **Advertencia roja en la barra de acción**: `"¡SOBRECARGA CRÍTICA EN X SEGUNDOS!"`
4. La máquina **explota** (poder 10.0, destruye bloques)
5. Un **alerta global en el chat** transmite las coordenadas a todos los jugadores

> **Exotraje Resistor Térmico** proporciona inmunidad total al daño térmico del DMA. Ver [Armadura](armor.md).

---

## Formato JSON de Receta

Todas las recetas del DMA usan el tipo `ufo:dimensional_assembly`. Ver la página [Recetas KubeJS](kubejs-recipes.md) para creación de recetas por script.

---

## Consumo de Energía

- Buffer interno base: **500,000 AE**
- **Catalizadores Matterflow** pueden aumentar el buffer en 10x/100x/1000x
- El consumo se calcula por tick: `energía / (tiempo_receta / factor_velocidad)`

---

## Auto-Exportación

El DMA soporta **auto-exportación** vía configuración. Las salidas finalizadas se envían automáticamente a inventarios adyacentes/almacenamiento ME.

---

*Ver también: [Catalizadores](catalysts.md) · [Recetas KubeJS](kubejs-recipes.md) · [Progresión](progression.md)*
