---
navigation:
  parent: ufo_intro/materials.md
  title: 催化剂
  icon: ufo:chrono_catalyst_t1
  position: 20
item_ids:
  - ufo:matterflow_catalyst_t1
  - ufo:matterflow_catalyst_t2
  - ufo:matterflow_catalyst_t3
  - ufo:chrono_catalyst_t1
  - ufo:chrono_catalyst_t2
  - ufo:chrono_catalyst_t3
  - ufo:overflux_catalyst_t1
  - ufo:overflux_catalyst_t2
  - ufo:overflux_catalyst_t3
  - ufo:quantum_catalyst_t1
  - ufo:quantum_catalyst_t2
  - ufo:quantum_catalyst_t3
  - ufo:dimensional_catalyst
---

# 催化剂

催化剂是 DMA 和通用量子控制器可接受的升级卡。直接 Shift + 右键兼容的控制器即可安装一张，也可以通过其升级槽安装。最多可安装四个催化剂，它们会依据各自属性以乘法或加法方式叠加。

## 系列

| 系列 | T1 | T2 | T3 | 热量贡献 |
|---|---:|---:|---:|---:|
| 物质流 | 0.90× AE | 0.75× AE | 0.50× AE | +50 / +100 / +200% |
| 时序 | 1.25× 速度 | 1.625× 速度 | 2.25× 速度 | +100 / +250 / +400% |
| 超流 | 热量控制 | 热量控制 | 热量控制 | −50 / −100 / −200% |
| 量子 | +10% 额外产出 | +25% 额外产出 | +50% 额外产出 | +75 / +150 / +300% |

热量贡献会加到基础热量倍率上。超流可以抵消其他系列，但最终倍率不会变为负数。

量子的额外产出与承诺给 AE2 的确定性数量相互独立。承诺的基础产出用于完成合成任务；额外材料作为副产物插入，如果存储拒收则会继续缓存。

## 四卡协同

四张相同的催化剂物品会激活系列协同，并额外增加 1.5× 热量惩罚：

- 时序使合并后的速度倍率翻倍。
- 物质流使合并后的 AE 倍率减半。
- 量子再增加 50% 额外产出几率。
- 超流在通用惩罚之后使最终热量倍率减半。

## 次元催化剂（创造）

<ItemImage id="ufo:dimensional_catalyst" scale="3" float="left" />

次元催化剂（创造）会覆盖常规属性：近乎瞬时的加工、零 AE 消耗、不产生热量，并保证 100% 额外产出判定。配方输入仍会被消耗。

> 催化剂组合可能超出原本稳定机器的冷却能力。请先测试一个任务，然后在监控温度的同时提高线程数。
