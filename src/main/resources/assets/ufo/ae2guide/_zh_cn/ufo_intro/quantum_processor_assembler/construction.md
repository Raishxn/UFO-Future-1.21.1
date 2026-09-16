---
navigation:
  parent: ufo_intro/quantum_processor_assembler.md
  title: 结构与自动建造
  icon: ufo:quantum_hyper_mechanical_casing
  position: 10
---

# 处理器组装机结构

组装机占用 **5×7×12** 空间。其内部场结构不同于 QMF 和切片机；不要套用其他机器的分层方案。

<GameScene zoom="4" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/quantum_processor_assembler.snbt" />
  <DiamondAnnotation pos="2.5 3.5 0.5" color="#80c6ff">处理器组装机控制器</DiamondAnnotation>
  <IsometricCamera yaw="215" pitch="25" />
</GameScene>

## 材料清单

| 组件 | 数量 |
|---|---:|
| <ItemLink id="ufo:quantum_hyper_mechanical_casing" /> | 80（其中一个替换为样板缓存器或代理） |
| <ItemLink id="ae2:quartz_vibrant_glass" /> | 73（固定位置） |
| <ItemLink id="ae2:quartz_block" /> | 16（固定位置） |
| <ItemLink id="ae2:fluix_block" /> | 12（固定位置） |
| <ItemLink id="ufo:stellar_field_generator_t1" /> 或统一的更高等级 | 72 |
| <ItemLink id="ufo:quantum_pattern_hatch" /> | 1（替换一个外壳） |
| <ItemLink id="ufo:quantum_processor_assembler_controller" /> | 1 |

首先放置控制器，携带其余材料并使用自动建造。已有的正确方块会从需求数量中扣除。
任何被错误方块占用的位置都会中止预检，并保持原样；请使用扫描和高亮定位问题。
