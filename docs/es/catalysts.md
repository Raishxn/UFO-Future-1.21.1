# Catalizadores

Los catalizadores son **tarjetas de upgrade AE2** usadas en los 4 slots de upgrade del **Ensamblador de Materia Dimensional**. Modifican el rendimiento de la máquina — velocidad, costo de energía, probabilidad de fallo y probabilidad de drop bonus — pero también afectan la generación de calor.

---

## Familias de Catalizadores

Hay **4 familias** con 3 niveles cada una (T1 → T3), más 1 catalizador creativo:

| Familia | Efecto Principal | Efecto en Calor |
|---------|-----------------|-----------------|
| **Matterflow** | Reduce costo de energía | Aumenta producción de calor |
| **Chrono** | Aumenta velocidad | Aumenta producción de calor (mayor) |
| **Overflux** | Reduce probabilidad de fallo | **Disminuye** producción de calor |
| **Quantum** | Aumenta probabilidad de drop bonus | Aumenta producción de calor |
| **Dimensional** | Todos los bonos, fabricación instantánea | Resetea temperatura (Solo Creativo) |

---

## Estadísticas Detalladas

### Matterflow — Eficiencia Energética

| Nivel | Efecto de Energía | Mult. de Calor | Mult. de Buffer |
|-------|------------------|----------------|-----------------|
| T1 | −10.0% Costo | ×1.5 calor | ×10 buffer |
| T2 | −25.0% Costo | ×2.0 calor | ×100 buffer |
| T3 | −50.0% Costo | ×3.0 calor | ×1000 buffer |

### Chrono — Velocidad

| Nivel | Efecto de Velocidad | Mult. de Calor | Mult. de Buffer |
|-------|-------------------|----------------|-----------------|
| T1 | +25.0% Velocidad | ×2.0 calor | ×10 buffer |
| T2 | +62.5% Velocidad | ×3.5 calor | ×100 buffer |
| T3 | +125.0% Velocidad | ×5.0 calor | ×1000 buffer |

> **Advertencia**: Los Chrono producen más calor. ¡Apilar 4× Chrono T3 generará calor extremo!

### Overflux — Estabilidad

| Nivel | Efecto de Fallo | Mult. de Calor | Mult. de Buffer |
|-------|----------------|----------------|-----------------|
| T1 | −10.0% Prob. de Fallo | ×0.5 calor (¡enfriamiento!) | ×1 buffer |
| T2 | −25.0% Prob. de Fallo | ×0.0 calor (¡sin calor!) | ×1 buffer |
| T3 | −50.0% Prob. de Fallo | ×-1.0 calor (¡enfriamiento activo!) | ×1 buffer |

> **Consejo**: Los Overflux son los únicos que _reducen_ la generación de calor. Se pueden combinar con Chrono para compensar penalizaciones térmicas.

### Quantum — Drops Bonus

| Nivel | Efecto de Bonus | Mult. de Calor | Mult. de Buffer |
|-------|----------------|----------------|-----------------|
| T1 | +10.0% Drop Bonus | ×1.75 calor | ×1 buffer |
| T2 | +25.0% Drop Bonus | ×2.5 calor | ×1 buffer |
| T3 | +50.0% Drop Bonus | ×4.0 calor | ×1 buffer |

---

## Reglas de Apilamiento

### Límite Suave

| Cantidad | Efectividad |
|----------|------------|
| 1× | 100% |
| 2× | 175% |
| 3× | 225% |
| 4× | 250% |

### Bonus de Sinergia 4-Stack

| Familia | Bonus de Sinergia | Penalización |
|---------|------------------|-------------|
| **Chrono 4×** | ×2.0 velocidad adicional | ×1.5 calor |
| **Matterflow 4×** | ×0.5 descuento de energía | ×1.5 calor |
| **Quantum 4×** | (Reservado) | ×1.5 calor |
| **Overflux 4×** | Apilamiento estándar | ×1.5 calor |

> **Protección anti-exploit**: Tiempo mínimo de procesamiento es **1 segundo** (20 ticks).

---

## Catalizador Dimensional (Creativo)

- ⚡ Fabricación Instantánea
- 💰 Sin Costo de Energía/Recursos
- 🎁 100% Drop Bonus
- ✅ Nunca Falla
- ❄️ Resetea Temperatura de la Máquina

**Energía**: 500,000,000 AE · **Tiempo**: 10,000 ticks (8.3 minutos)

---

## IDs de Catalizadores

| Catalizador | ID de Registro |
|-------------|---------------|
| Matterflow T1–T3 | `ufo:matterflow_catalyst_t1/t2/t3` |
| Chrono T1–T3 | `ufo:chrono_catalyst_t1/t2/t3` |
| Overflux T1–T3 | `ufo:overflux_catalyst_t1/t2/t3` |
| Quantum T1–T3 | `ufo:quantum_catalyst_t1/t2/t3` |
| Dimensional | `ufo:dimensional_catalyst` |

---

*Ver también: [DMA](dma.md) · [Progresión](progression.md)*
