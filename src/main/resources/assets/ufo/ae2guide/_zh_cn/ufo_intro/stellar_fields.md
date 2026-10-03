---
navigation:
  parent: ufo_intro/infrastructure.md
  title: 恒星场
  position: 47
item_ids:
  - ufo:stellar_field_generator_t1
  - ufo:stellar_field_generator_t2
  - ufo:stellar_field_generator_t3
---

# 恒星场生成器

<BlockImage id="ufo:stellar_field_generator_t1" scale="3"></BlockImage>

恒星场生成器是整个 UFO 高级多方块生产线中使用的稳定方块。

- 通用多方块会以场位置上**最低的恒星场生成器等级**作为机器等级。
- 所需等级更高的配方在机器等级达标前不会运行。
- 低于已安装等级的配方运行更快、消耗更少 AE：每高出一级，处理时间减半，并应用 **0.75 倍** AE 消耗系数。

## 通用冷却液优先级

- **MK1** 优先使用凝滞冷却剂：每 **120 mB** 提供 **1 HU**，最高 **1000 mB/tick**。
- **MK2** 优先使用稳定冷却剂：每 **1 mB** 提供 **50 HU**，最高 **10 mB/tick**。
- **MK3** 优先使用玻色-爱因斯坦凝聚态：每 **1 mB** 提供 **200 HU**，最高 **10 mB/tick**。

## 恒星联结的场生成器

恒星联结比通用多方块更严格：全部四个恒星场生成器位置必须使用同一等级。

- **MK1** 以 **500K AE/t** 为联结缓存充能。
- **MK2** 以 **1M AE/t** 为联结缓存充能。
- **MK3** 以 **2M AE/t** 为联结缓存充能。
- 更高的场等级会倍增联结的冷却强度：**MK1 x2**、**MK2 x3**、**MK3 x4**。
- 若允许不安全过热，失效半径随等级变化：**30 / 50 / 100 格**。
