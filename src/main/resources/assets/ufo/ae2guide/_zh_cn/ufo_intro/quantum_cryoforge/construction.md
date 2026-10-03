---
navigation:
  parent: ufo_intro/quantum_cryoforge.md
  title: 结构与自动建造
  icon: ufo:quantum_hyper_mechanical_casing
  position: 10
---

# 量子低温锻造炉结构

重新设计后的低温锻造炉占用 **9×14×9** 的空间。旋转生成的场景，
即可查看其蓝冰腔室和场生成器晶格。

<GameScene zoom="3.2" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/quantum_cryoforge.snbt" />
  <DiamondAnnotation pos="4.5 1.5 0.5" color="#80c6ff">低温锻造炉控制器</DiamondAnnotation>
  <DiamondAnnotation pos="4.5 1.5 4.5" color="#7ae7f2">蓝冰腔室</DiamondAnnotation>
  <IsometricCamera yaw="215" pitch="25" />
</GameScene>

## 默认自动建造材料

| 组件 | 数量 | 放置规则 |
|---|---:|---|
| <ItemLink id="ufo:quantum_hyper_mechanical_casing" /> | 226 | 外壳位置；有效的通用舱口可手动替换允许的槽位 |
| <ItemLink id="ae2:quartz_vibrant_glass" /> | 16 | 固定玻璃位置 |
| <ItemLink id="ae2:quartz_block" /> | 86 | 固定石英框架 |
| <ItemLink id="ae2:fluix_block" /> | 72 | 固定福鲁伊克斯导管 |
| <ItemLink id="ufo:stellar_field_generator_t1" /> 或统一使用一种更高等级 | 45 | 决定机器等级 |
| <ItemLink id="minecraft:blue_ice" /> | 40 | 固定冷腔 |
| <ItemLink id="ufo:quantum_cryoforge_controller" /> | 1 | 首先放置 |

自动建造会在灵活的 `B` 位置使用默认外壳。请在最终扫描前，将必需的
通用舱口安装到有效的外壳槽位中。使用 JEI 的替代材料功能查看可接受的替换项。
