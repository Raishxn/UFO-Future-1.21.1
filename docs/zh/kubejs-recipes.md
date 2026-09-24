# KubeJS 配方

本页介绍如何通过 [KubeJS](https://kubejs.com/) 在 Minecraft 1.21.1（NeoForge）中为 UFO Future 的主要多方块机器创建、修改和移除自定义配方。

## 支持的配方类型

- `ufo:dimensional_assembly`
- `ufo:stellar_simulation`
- `ufo:universal_multiblock`
- `ufo:qmf_recipe` （旧格式，QMF 仍然支持）

---

## DMA

### 类型

```txt
ufo:dimensional_assembly
```

### 基本结构

| 字段 | 类型 | 必填 | 说明 |
|--------|----------|-------|----------|
| `item_inputs` | Array | 是 | 物品输入及数量 |
| `fluid_inputs` | Array | 否 | 流体输入及数量，单位为 mB |
| `item_outputs` | Array | 否 | 物品输出 |
| `fluid_outputs` | Array | 否 | 流体输出 |
| `energy` | Integer | 是 | 总 AE 能耗 |
| `time` | Integer | 是 | 处理时间，单位为 tick |

> 至少需要一种输出：`item_outputs` 或 `fluid_outputs`。

### 示例

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:dimensional_assembly',
    item_inputs: [
      {
        ingredient: { item: 'minecraft:diamond' },
        count: 4
      },
      {
        ingredient: { item: 'minecraft:netherite_ingot' },
        count: 1
      }
    ],
    fluid_inputs: [
      {
        ingredient: { fluid: 'ufo:source_liquid_starlight_fluid' },
        amount: 500
      }
    ],
    item_outputs: [
      {
        id: 'ufo:quantum_anomaly',
        amount: 1
      }
    ],
    fluid_outputs: [],
    energy: 1000000,
    time: 400
  }).id('kubejs:custom_quantum_anomaly')
})
```

### 使用标签作为输入

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:dimensional_assembly',
    item_inputs: [
      {
        ingredient: { tag: 'c:ingots/iron' },
        count: 16
      }
    ],
    fluid_inputs: [],
    item_outputs: [
      {
        id: 'ufo:obsidian_matrix',
        amount: 2
      }
    ],
    fluid_outputs: [],
    energy: 50000,
    time: 100
  }).id('kubejs:tagged_obsidian_matrix')
})
```

### 移除配方

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'ufo:dma/quantum_anomaly' })
  event.remove({ type: 'ufo:dimensional_assembly', output: 'ufo:quantum_anomaly' })
})
```

### 备注

- DMA 配方中的物品输入不区分排列顺序。
- DMA 最多支持 9 种物品输入。
- 冷却液不属于配方定义的一部分。

---

## 恒星枢纽

### 类型

```txt
ufo:stellar_simulation
```

### 基本结构

| 字段 | 类型 | 必填 | 说明 |
|--------|----------|-------|----------|
| `simulation_name` | String | 是 | 控制器中显示的名称 |
| `item_inputs` | Array | 是 | 从 ME 网络消耗的物品 |
| `fluid_inputs` | Array | 是 | 从 ME 网络消耗的流体 |
| `item_outputs` | Array | 是 | AE2 GenericStack 格式的物品输出 |
| `fluid_outputs` | Array | 是 | AE2 GenericStack 格式的流体输出 |
| `energy` | Integer | 是 | 总 AE 能量 |
| `time` | Integer | 是 | 持续时间，单位为 tick |
| `cooling_level` | Integer | 是 | 热负荷等级，0 至 3 |
| `field_tier` | Integer | 是 | 所需的最低力场发生器等级 |
| `fuel_fluid` | String | 否 | 燃料流体 ID |
| `fuel_amount` | Integer | 否 | 燃料用量，mB |
| `coolant_amount` | Integer | 否 | 冷却液用量，mB |

冷却液类型由外部注入的 ME Massive Fluid Hatch 决定；`coolant_amount` 是配方需求量。序列化器没有 `coolant_fluid` 字段。

### AE2 GenericStack

```json
{ "#": 15000000, "#t": "ae2:i", "id": "minecraft:iron_ingot" }
```

- `#` = 数量
- `#t` = 类型：`ae2:i` 表示物品，`ae2:f` 表示流体
- `id` = 完整的注册 ID

### 示例

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:stellar_simulation',
    simulation_name: 'Custom Void Harvest',
    item_inputs: [
      {
        amount: 8,
        ingredient: { item: 'ufo:enriched_neutronium_sphere' }
      },
      {
        amount: 4,
        ingredient: { item: 'minecraft:nether_star' }
      }
    ],
    fluid_inputs: [
      {
        amount: 200000,
        ingredient: { fluid: 'ufo:raw_star_matter_plasma' }
      }
    ],
    item_outputs: [
      { '#': 5000000, '#t': 'ae2:i', id: 'minecraft:ender_pearl' },
      { '#': 2000000, '#t': 'ae2:i', id: 'minecraft:blaze_rod' }
    ],
    fluid_outputs: [
      { '#': 100000, '#t': 'ae2:f', id: 'ufo:liquid_starlight' }
    ],
    energy: 300000000,
    time: 30000,
    cooling_level: 2,
    field_tier: 2,
    fuel_fluid: 'mekanism:hydrogen',
    fuel_amount: 20000,
    coolant_amount: 25000
  }).id('kubejs:custom_void_harvest')
})
```

### 移除配方

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'ufo:stellar_simulation/iron_core_fusion' })
  event.remove({ type: 'ufo:stellar_simulation' })
})
```

---

## 通用多方块配方

这是其他大型自动化多方块机器推荐使用的配方格式。

### 类型

```txt
ufo:universal_multiblock
```

### 支持的机器

- `machine: 'qmf'`
- `machine: 'quantum_slicer'`
- `machine: 'quantum_processor_assembler'`
- `machine: 'quantum_cryoforge'`

### 基本结构

| 字段 | 类型 | 必填 | 说明 |
|--------|----------|-------|----------|
| `machine` | String | 是 | 目标多方块机器 |
| `recipe_name` | String | 否 | 内部名称或显示名称 |
| `item_inputs` | Array | 是 | 物品输入及数量 |
| `fluid_inputs` | Array | 否 | 流体输入及数量，mB |
| `chemical_inputs` | Array | 否 | 可选 Mekanism 化学品：`{ "chemical": "mekanism:oxygen", "amount": 1000 }` |
| `item_output` | Object | 否 | 单项物品输出 |
| `fluid_output` | Object | 否 | 单项流体输出 |
| `fluid_output_amount` | Integer | 否 | 流体输出总量，mB |
| `energy` | Integer/Long | 是 | 总 AE 能量 |
| `time` | Integer | 是 | 时间，单位为 tick |
| `required_tier` | Integer | 否 | 机器的最低等级 |

> 至少需要一种输出：`item_output` 或 `fluid_output`。

### 数据格式

物品输入：

```json
{
  "ingredient": { "item": "minecraft:diamond" },
  "amount": 64
}
```

标签输入：

```json
{
  "ingredient": { "tag": "c:ingots/iron" },
  "amount": 256
}
```

流体输入：

```json
{
  "fluid": {
    "id": "ufo:uu_matter",
    "amount": 1
  },
  "amount": 128000
}
```

物品输出：

```json
{
  "id": "ufo:dimensional_processor",
  "count": 64
}
```

流体输出：

```json
{
  "id": "ufo:source_stable_coolant",
  "amount": 1
}
```

还需要配合设置：

```json
"fluid_output_amount": 128000
```

### 示例 - QMF

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:universal_multiblock',
    machine: 'qmf',
    recipe_name: 'kubejs/qmf/corporeal_matter_batch',
    item_inputs: [
      {
        ingredient: { item: 'ufo:proto_matter' },
        amount: 128
      },
      {
        ingredient: { item: 'minecraft:iron_block' },
        amount: 4096
      },
      {
        ingredient: { item: 'ufo:obsidian_matrix' },
        amount: 1024
      }
    ],
    fluid_inputs: [
      {
        fluid: {
          id: 'ufo:uu_matter',
          amount: 1
        },
        amount: 256000
      }
    ],
    item_output: {
      id: 'ufo:corporeal_matter',
      count: 64
    },
    energy: 1280000000,
    time: 3600,
    required_tier: 1
  }).id('kubejs:qmf_corporeal_matter_batch')
})
```

### 示例 - 量子处理器装配机

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:universal_multiblock',
    machine: 'quantum_processor_assembler',
    recipe_name: 'kubejs/quantum_processor_assembler/dimensional_processor',
    item_inputs: [
      {
        ingredient: { item: 'ufo:printed_dimensional_processor' },
        amount: 64
      },
      {
        ingredient: { item: 'ae2:printed_silicon' },
        amount: 64
      },
      {
        ingredient: { item: 'ae2:fluix_dust' },
        amount: 128
      }
    ],
    item_output: {
      id: 'ufo:dimensional_processor',
      count: 64
    },
    energy: 12000000,
    time: 1200
  }).id('kubejs:quantum_processor_assembler_dimensional_processor')
})
```

### 示例 - 量子冷锻炉

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:universal_multiblock',
    machine: 'quantum_cryoforge',
    recipe_name: 'kubejs/quantum_cryoforge/stable_coolant_t3',
    item_inputs: [
      {
        ingredient: { item: 'minecraft:blue_ice' },
        amount: 256
      },
      {
        ingredient: { item: 'ufo:obsidian_matrix' },
        amount: 64
      },
      {
        ingredient: { item: 'ufo:quantum_anomaly' },
        amount: 16
      }
    ],
    fluid_inputs: [
      {
        fluid: {
          id: 'ufo:source_gelid_cryotheum',
          amount: 1
        },
        amount: 128000
      }
    ],
    fluid_output: {
      id: 'ufo:source_stable_coolant',
      amount: 1
    },
    fluid_output_amount: 128000,
    energy: 50000000,
    time: 9600,
    required_tier: 3
  }).id('kubejs:quantum_cryoforge_stable_coolant_t3')
})
```

### 示例 - 量子切片机

目前量子切片机没有现成的 datapack 示例，但其序列化器已支持使用相同格式接收自定义配方：

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:universal_multiblock',
    machine: 'quantum_slicer',
    recipe_name: 'kubejs/quantum_slicer/printed_singularity_core',
    item_inputs: [
      {
        ingredient: { item: 'ae2:singularity' },
        amount: 1
      },
      {
        ingredient: { item: 'ufo:obsidian_matrix' },
        amount: 8
      },
      {
        ingredient: { tag: 'c:dusts/fluix' },
        amount: 64
      }
    ],
    fluid_inputs: [
      {
        fluid: {
          id: 'ufo:source_temporal_fluid',
          amount: 1
        },
        amount: 16000
      }
    ],
    item_output: {
      id: 'ufo:printed_dimensional_processor',
      count: 8
    },
    energy: 64000000,
    time: 900,
    required_tier: 2
  }).id('kubejs:quantum_slicer_printed_singularity_core')
})
```

### 移除配方

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'ufo:universal/qmf/corporeal_matter_batch' })
  event.remove({ type: 'ufo:universal_multiblock' })
})
```

---

## QMF 旧格式兼容性

QMF 仍然支持：

```txt
ufo:qmf_recipe
```

该格式可用于兼容旧内容。新配方建议使用 `ufo:universal_multiblock`，并设置 `machine: 'qmf'`。

### 示例

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:qmf_recipe',
    recipe_name: 'kubejs/qmf/legacy_proto_matter',
    item_inputs: [
      {
        ingredient: { item: 'ufo:obsidian_matrix' },
        amount: 32
      }
    ],
    fluid_inputs: [
      {
        fluid: {
          id: 'ufo:uu_matter',
          amount: 1
        },
        amount: 32000
      }
    ],
    output: {
      id: 'ufo:proto_matter',
      count: 4
    },
    energy: 40000000,
    time: 600,
    required_tier: 1
  }).id('kubejs:qmf_legacy_proto_matter')
})
```

---

## 数据包

如果选择使用 datapack 而非 KubeJS，请将 JSON 放在：

```txt
data/<your_namespace>/recipe/<recipe_name>.json
```

使用与上述示例相同的结构即可。

---

*另见： [DMA](dma.md) · [量子物质制造机](quantum-matter-fabricator.md) · [量子处理器装配机](quantum-processor-assembler.md) · [恒星枢纽](stellar-nexus.md) · [催化剂](catalysts.md) · [材料与流体](materials.md)*
