---
navigation:
  parent: ufo_intro/qmf.md
  title: 结构与自动建造
  icon: ufo:quantum_hyper_mechanical_casing
  position: 10
---

# QMF 结构与自动建造

量子物质制造机占用固定的 **15×7×7** 空间。下方场景由扫描、JEI 和自动建造共同使用的同一份结构定义生成。
拖动即可旋转视角。

<GameScene zoom="4" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/quantum_matter_fabricator.snbt" />
  <DiamondAnnotation pos="7.5 1.5 1.5" color="#80c6ff">QMF 控制器</DiamondAnnotation>
  <IsometricCamera yaw="215" pitch="25" />
</GameScene>

## 材料清单

| 组件 | 数量 | 规则 |
|---|---:|---|
| <ItemLink id="ufo:quantum_hyper_mechanical_casing" /> | 245 | 任意一个外壳可替换为样板缓存器或代理 |
| <ItemLink id="ae2:quartz_vibrant_glass" /> | 26 | 固定观察外壳 |
| <ItemLink id="ae2:quartz_block" /> | 10 | 固定位置 |
| <ItemLink id="ae2:fluix_block" /> | 3 | 固定位置 |
| <ItemLink id="ufo:stellar_field_generator_t1" /> 或更高等级 | 52 | 所有生成器必须使用同一等级 |
| <ItemLink id="ufo:quantum_pattern_hatch" /> | 1 | 必须恰好替换一个外壳 |
| <ItemLink id="ufo:quantum_matter_fabricator_controller" /> | 1 | 首先放置；自动建造不会消耗控制器 |

空气通道是结构设计的一部分。石英块、福鲁伊克斯块和石英振动玻璃的位置固定；
只有量子超机械外壳可以被样板缓存器、样板代理或 ME 大型流体舱口替换。

## 使用自动建造

1. 放置控制器，让其正面背向为机器预留的空间。
2. 将所有需要放置的方块放入玩家物品栏。
3. 打开控制器并点击**自动建造**。
4. 等待服务器以每 tick 一个方块的速度完成放置，然后执行**扫描**。

自动建造会保留所有已经正确的方块，绝不会覆盖被错误方块占用的位置。
如果预检报告位置受阻，请清理对应坐标后重新开始。它只读取玩家物品栏，不会使用 ME 存储或其他容器。

## 等级变化

每个 `F` 位置都可使用 MK1、MK2 或 MK3 恒星场生成器。通过 JEI 的**替代材料**预览有效替换项。
混用等级不会取平均值：结构要求所有场生成器保持同一等级。
