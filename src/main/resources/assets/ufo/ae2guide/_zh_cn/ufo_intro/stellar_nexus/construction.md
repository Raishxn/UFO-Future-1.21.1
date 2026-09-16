---
navigation:
  parent: ufo_intro/stellar_nexus.md
  title: 结构与场核心
  icon: ufo:entropy_singularity_casing
  position: 10
---

# 恒星联结结构与场核心

当前恒星联结结构占用 **35×34×35** 空间。下方场景直接由实际生产结构生成，并非概念模型。
大型场景初始化可能需要片刻。拖动可以旋转和缩放视角。

<GameScene zoom="1.25" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/stellar_nexus.snbt" />
  <DiamondAnnotation pos="18.5 17.5 1.5" color="#80c6ff">恒星联结控制器</DiamondAnnotation>
  <IsometricCamera yaw="215" pitch="25" />
</GameScene>

## 标准默认材料构成

| 组件 | 数量 |
|---|---:|
| <ItemLink id="ufo:entropy_singularity_casing" /> | 980 |
| <ItemLink id="ufo:entropy_assembler_core_casing" /> | 534 |
| <ItemLink id="ufo:entropy_computer_condensation_matrix" /> | 168 |
| <ItemLink id="ufo:stellar_field_generator_t1" /> 或统一的更高等级 | 138 |
| <ItemLink id="ufo:stellar_nexus_controller" /> | 1 |

部分外壳位置可以使用必需的舱口角色替换默认奇点外壳。恒星联结不属于新的通用 5×5 机器系列；
请使用它自己的 JEI 结构查看器和扫描结果，不要移植其他机器的拓扑。
