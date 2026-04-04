# 材料与流体

## 星体碎片材料

### 白矮星碎片
基础星体材料。形式：锭、棒、粒、粉、块。
- **ID 前缀**: `ufo:white_dwarf_fragment_*`
- **合成** (DMA): 2x 下界合金锭 + 4x 蓝冰 + 4x AE2 天空石 + 250mB 极寒冰晶 = 100K AE

### 中子星碎片
中级材料。形式：锭、棒、粒、粉、块。
- **ID 前缀**: `ufo:neutron_star_fragment_*`
- **合成** (DMA): 4x 白矮星锭 + 地狱之星 + 黑曜石矩阵 + 500mB 极寒冰晶 = 500K AE

### 脉冲星碎片
高级材料。形式：锭、粒、粉、块。
- **ID 前缀**: `ufo:pulsar_fragment_*`
- **合成** (DMA): 2x 中子星锭 + 2x 磁石 + 4x 避雷针 + 1000mB 极寒冰晶 = 1M AE

---

## 物质进阶

| 物质 | 关键输入 | 能量 | ID |
|------|---------|------|-----|
| 中子球 | 9x 中子星锭 | 1M | `ufo:neutronium_sphere` |
| 富集中子球 | 中子球 + 2x 量子异常 | 8M | `ufo:enriched_neutronium_sphere` |
| 原始物质 | 富集中子球 + UU 物质 | 2M | `ufo:proto_matter` |
| 实体物质 | 原始物质 + 64x 铁块 | 5M | `ufo:corporeal_matter` |
| UU 物质水晶 | 紫水晶碎片 + 10K mB UU | 20M | `ufo:uu_matter_crystal` |
| 白矮星物质 | 实体 + UU 水晶 + 白矮星块/流体 | 7M | `ufo:white_dwarf_matter` |
| 中子星物质 | 实体 + UU 水晶 + 中子星块/流体 | 9.5M | `ufo:neutron_star_matter` |
| 脉冲星物质 | 实体 + UU 水晶 + 脉冲星块/流体 | 12M | `ufo:pulsar_matter` |
| 暗物质 | 16x 每种高级物质 + 超越物质 | 200M | `ufo:dark_matter` |

---

## 自定义流体

| 流体 | 用途 | ID |
|------|------|-----|
| 液态星光 | 大多数配方的基础流体 | `ufo:source_liquid_starlight_fluid` |
| 极寒冰晶 | 冷却液 + 合成 | `ufo:source_gelid_cryotheum` |
| 时间流体 | 最佳冷却液 (100 HU/mB) | `ufo:source_temporal_fluid` |
| 空间流体 | 量子催化剂 | `ufo:source_spatial_fluid` |
| 原始物质流体 | 组件合成 | `ufo:source_primordial_matter_fluid` |
| 原始星体等离子 | 星体加工 | `ufo:raw_star_matter_plasma` |
| 超越物质 | 终局配方 | `ufo:transcending_matter` |
| UU 物质 | 复制 | `ufo:uu_matter` |
| UU 放大器 | UU 前驱体 | `ufo:source_uu_amplifier_fluid` |

### 引导链（无循环依赖）
1. 水 + 雪球 + 浮冰 → 暴雪粉
2. 暴雪粉 + 雪球 → 冰晶粉
3. 暴雪粉 + 水 → 极寒冰晶
4. 黑曜石矩阵 + 地狱之星 + UU 放大器 → 液态星光

---

*另见: [进阶](progression.md) · [DMA](dma.md)*
