---
navigation:
  parent: ufo_intro/infrastructure.md
  title: 多方块组件
  position: 15
  icon: ufo:quantum_hyper_mechanical_casing
item_ids:
  - ufo:quantum_hyper_mechanical_casing
  - ufo:entropy_singularity_casing
  - ufo:entropy_assembler_core_casing
  - ufo:entropy_computer_condensation_matrix
---

# 多方块组件

这些方块构成了 UFO 多方块的外壳、内部框架和计算层。它们的位置具有结构性：
用外观相似的方块替换其中任何一个都无法通过结构校验。

## 量子超机械外壳

<BlockImage id="ufo:quantum_hyper_mechanical_casing" scale="3"></BlockImage>

量子物质制造机、量子切片机、量子处理器组装机和量子冷冻锻造机共用的结构外壳。
只有在各控制器明确允许的位置，才接受通用舱口替换。

<Recipe id="ufo:quantum_hyper_mechanical_casing" />

## 熵奇点外壳

<BlockImage id="ufo:entropy_singularity_casing" scale="3"></BlockImage>

恒星联结的外部承重外壳。它不能与量子超机械外壳或任意舱口互换。

<Recipe id="ufo:entropy_singularity_casing" />

## 熵装配核心外壳

<BlockImage id="ufo:entropy_assembler_core_casing" scale="3"></BlockImage>

这种外壳构成恒星联结致密的内部计算主体。请严格按照交互式建造场景所示
保留其内部位置。

<Recipe id="ufo:entropy_assembler_core_casing" />

## 熵计算冷凝矩阵

<BlockImage id="ufo:entropy_computer_condensation_matrix" scale="3"></BlockImage>

冷凝矩阵占据固定的内部位置，不得用外壳方块替换。它们是联结计算几何结构的
一部分。

<Recipe id="ufo:entropy_computer_condensation_matrix" />

## 建造规则

- 从最底层向上逐层建造，并将每一层与场景对照。
- 将控制器、恒星场和冷凝矩阵的位置视为固定。
- 只能通过该机器文档中记载的端口添加可选自动化。
- 如果结构成型失败，请先核对朝向以及控制器报告的第一个不匹配方块，
  再重建周围各层。
