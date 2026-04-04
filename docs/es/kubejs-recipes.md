# Recetas Personalizadas KubeJS — DMA

Esta página documenta cómo crear, modificar y eliminar recetas del **Ensamblador de Materia Dimensional (DMA)** usando [KubeJS](https://kubejs.com/) para Minecraft 1.21.1 (NeoForge).

---

## Tipo de Receta

```
ufo:dimensional_assembly
```

---

## Estructura Básica

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `item_inputs` | Array de `IngredientStack.Item` | ✅ | Ingredientes de ítems con cantidades |
| `fluid_inputs` | Array de `IngredientStack.Fluid` | ❌ | Ingredientes de fluidos con cantidades (mB) |
| `item_outputs` | Array de `GenericStack` | ❌ | Salidas de ítems |
| `fluid_outputs` | Array de `GenericStack` | ❌ | Salidas de fluidos |
| `energy` | Entero | ✅ | Total de energía AE consumida |
| `time` | Entero | ✅ | Tiempo de procesamiento en ticks (20 = 1 segundo) |

> Al menos una salida (ítem o fluido) debe especificarse.

---

## Creando Recetas

### Receta Simple de Ítem

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:dimensional_assembly',
    item_inputs: [
      { ingredient: { item: 'minecraft:diamond' }, count: 4 },
      { ingredient: { item: 'minecraft:netherite_ingot' }, count: 1 }
    ],
    fluid_inputs: [
      { ingredient: { fluid: 'ufo:source_liquid_starlight_fluid' }, amount: 500 }
    ],
    item_outputs: [{ id: 'ufo:quantum_anomaly', amount: 1 }],
    fluid_outputs: [],
    energy: 1000000,
    time: 400
  }).id('kubejs:custom_quantum_anomaly')
})
```

### Usando Tags como Entradas

```js
event.custom({
  type: 'ufo:dimensional_assembly',
  item_inputs: [
    { ingredient: { tag: 'c:ingots/iron' }, count: 16 },
    { ingredient: { item: 'minecraft:ender_pearl' }, count: 4 }
  ],
  fluid_inputs: [],
  item_outputs: [{ id: 'ufo:obsidian_matrix', amount: 2 }],
  fluid_outputs: [],
  energy: 50000,
  time: 100
}).id('kubejs:tagged_obsidian_matrix')
```

---

## Eliminando Recetas

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'ufo:dma/quantum_anomaly' })
  event.remove({ type: 'ufo:dimensional_assembly', output: 'ufo:quantum_anomaly' })
})
```

---

## Modificando Recetas

KubeJS no soporta modificación directa. Elimina y re-agrega:

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'ufo:dma/quantum_anomaly' })
  event.custom({
    type: 'ufo:dimensional_assembly',
    item_inputs: [
      { ingredient: { item: 'ufo:pulsar_fragment_dust' }, count: 6 },
      { ingredient: { item: 'ufo:obsidian_matrix' }, count: 1 }
    ],
    fluid_inputs: [
      { ingredient: { fluid: 'ufo:source_pulsar_fragment_fluid' }, amount: 4000 }
    ],
    item_outputs: [{ id: 'ufo:quantum_anomaly', amount: 1 }],
    fluid_outputs: [],
    energy: 3000000,
    time: 600
  }).id('ufo:dma/quantum_anomaly')
})
```

---

## Consejos y Buenas Prácticas

1. **Escala de energía**: Básicas 50K–500K AE. Mid-game 1M–10M. Endgame 50M–500M AE.
2. **Tiempo mínimo**: 1 segundo (20 ticks) independientemente de catalizadores Chrono.
3. **Entradas sin forma**: El orden de colocación no importa.
4. **Máximo 9 entradas de ítems**.
5. **Fluidos van al Slot 3**: El refrigerante (Slot 2) es independiente de las recetas.

---

## Alternativa Datapack (JSON)

Coloca los archivos JSON en: `data/<tu_namespace>/recipe/<nombre_receta>.json`

---

*Ver también: [DMA](dma.md) · [Catalizadores](catalysts.md) · [Materiales y Fluidos](materials.md)*
