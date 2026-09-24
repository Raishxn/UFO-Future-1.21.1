# 存储单元

UFO Future 扩展了 AE2 的存储系统，新增两个系列 — **白矮星**（物品）和 **中子星**（流体）— 使用 BigInteger 容量。还添加了**无限单元**用于无限存储单一资源。

---

## 白矮星物品单元

| 等级 | 容量 | 所需组件 | ID |
|------|------|---------|-----|
| **Echo** | 40M 字节 | Phase Shift 组件矩阵 | `ufo:white_dwarf_cell_echo` |
| **Beacon** | 100M 字节 | Hyper Dense 组件矩阵 | `ufo:white_dwarf_cell_beaco` |
| **Nexus** | 250M 字节 | Tesseract 组件矩阵 | `ufo:white_dwarf_cell_nexus` |
| **Core** | 750M 字节 | Event Horizon 组件矩阵 | `ufo:white_dwarf_cell_core` |
| **Singularity** | 2,147,483,647 字节 | Cosmic String 组件矩阵 | `ufo:white_dwarf_cell_singularity` |

**外壳**: `ufo:white_dwarf_item_cell_housing`

---

## 中子星流体单元

| 等级 | 容量 | 所需组件 | ID |
|------|------|---------|-----|
| **Echo** | 40M 字节 | Phase Shift 组件矩阵 | `ufo:neutron_star_reservoir_echo` |
| **Beacon** | 100M 字节 | Hyper Dense 组件矩阵 | `ufo:neutron_star_reservoir_beaco` |
| **Nexus** | 250M 字节 | Tesseract 组件矩阵 | `ufo:neutron_star_reservoir_nexus` |
| **Core** | 750M 字节 | Event Horizon 组件矩阵 | `ufo:neutron_star_reservoir_core` |
| **Singularity** | 2,147,483,647 字节 | Cosmic String 组件矩阵 | `ufo:neutron_star_reservoir_singularity` |

**外壳**: `ufo:neutron_fluid_cell_housing`

---

## 脉冲星化学存储仓（可选）

第三种 BigInteger 存储系列用于 Mekanism 化学品，拥有 **Echo（40M）、Beacon（100M）、Nexus（250M）、Core（750M）和 Singularity（2,147,483,647 字节）**等级。外壳 ID 为 `ufo:pulsar_cell_housing`；存储仓 ID 分别为 `ufo:pulsar_chamber_echo`、`..._beaco`、`..._nexus`、`..._core` 与 `..._singularity`。

本系列依赖 Mekanism 集成。没有 Mekanism 时，存储仓及其外壳不会显示在创造模式和 JEI/EMI 中，但注册 ID 会保留，以兼容已有存档。

## Infinity Genesis 存储单元

**Infinity Genesis Cell**（`ufo:infinity_genesis_cell`）会在资源插入时学习其类型，此后为已学习类型提供无限 AE2 存储，并支持 Cell Workbench 中的分区、反转和模糊设置。固定资源的 Infinity Cells 仍是独立物品。

## 无限单元

存储**无限**数量的单一资源。包括：水、岩浆、圆石、沙子、黑曜石、玻璃等。还有 16 色染料和 Mekanism 资源。

Infinity Cells 当前通过 **QMF** 配方制作，所需材料随资源和等级变化。请在 JEI/EMI 中查看各配方。

---

*另见: [DMA](dma.md) · [进阶](progression.md) · [材料](materials.md)*
