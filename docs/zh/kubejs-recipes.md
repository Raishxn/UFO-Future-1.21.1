# KubeJS 自定义配方 — DMA

本页面记录如何使用 [KubeJS](https://kubejs.com/) 创建、修改和删除 **维度物质装配器 (DMA)** 配方，适用于 Minecraft 1.21.1 (NeoForge)。

---

## 配方类型

```
ufo:dimensional_assembly
```

---

## 基本结构

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| `item_inputs` | `IngredientStack.Item` 数组 | ✅ | 物品原料及数量 |
| `fluid_inputs` | `IngredientStack.Fluid` 数组 | ❌ | 流体原料及数量 (mB) |
| `item_outputs` | `GenericStack` 数组 | ❌ | 物品输出 |
| `fluid_outputs` | `GenericStack` 数组 | ❌ | 流体输出 |
| `energy` | 整数 | ✅ | 消耗的总 AE 能量 |
| `time` | 整数 | ✅ | 加工时间 (tick，20 tick = 1 秒) |

> 至少需要指定一个输出（物品或流体）。

---

## 创建配方

### 简单物品配方

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

### 使用标签作为输入

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

## 删除配方

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'ufo:dma/quantum_anomaly' })
  event.remove({ type: 'ufo:dimensional_assembly', output: 'ufo:quantum_anomaly' })
})
```

---

## 修改配方

KubeJS 不支持直接修改自定义类型配方。建议：先删除，再重新添加。

---

## 提示与最佳实践

1. **能量范围**：基础 50K–500K AE，中期 1M–10M，终局 50M–500M AE。
2. **最低时间**：1 秒 (20 tick)，无论 Chrono 催化剂如何叠加。
3. **无序输入**：物品放置顺序无关紧要。
4. **最多 9 种物品输入**。
5. **流体输入到槽位 3**：冷却液（槽位 2）与配方无关。

---

## 数据包替代方案 (JSON)

将配方 JSON 文件放在：`data/<你的命名空间>/recipe/<配方名>.json`

---

*另见: [DMA](dma.md) · [催化剂](catalysts.md) · [材料与流体](materials.md)*
