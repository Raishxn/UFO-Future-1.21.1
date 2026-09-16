---
navigation:
  parent: ufo_intro/quantum_slicer.md
  title: 结构与自动建造
  icon: ufo:quantum_hyper_mechanical_casing
  position: 10
---

# 量子切片机结构

切片机采用固定的 **13×5×5** 非对称外壳。场景中可见的开放空气通道是结构设计的一部分。
拖动场景可检查每一层。

<GameScene zoom="4" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/quantum_slicer.snbt" />
  <DiamondAnnotation pos="6.5 2.5 0.5" color="#80c6ff">切片机控制器</DiamondAnnotation>
  <IsometricCamera yaw="215" pitch="25" />
</GameScene>

## 材料清单

| 组件 | 数量 |
|---|---:|
| <ItemLink id="ufo:quantum_hyper_mechanical_casing" /> | 143（其中一个替换为样板缓存器或代理） |
| <ItemLink id="ae2:quartz_vibrant_glass" /> | 36（固定位置） |
| <ItemLink id="ae2:quartz_block" /> | 24（固定位置） |
| <ItemLink id="ae2:fluix_block" /> | 12（固定位置） |
| <ItemLink id="ufo:stellar_field_generator_t1" /> 或统一的更高等级 | 61 |
| <ItemLink id="ufo:quantum_pattern_hatch" /> | 1（替换一个外壳） |
| <ItemLink id="ufo:quantum_slicer_controller" /> | 1 |

自动建造会先执行完整预检，保留匹配方块，并拒绝覆盖被错误方块占用的位置。
材料必须位于玩家物品栏中。完成后使用扫描功能，找出任何被手动替换成无效等级的场生成器。
