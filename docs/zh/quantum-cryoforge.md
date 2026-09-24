# 量子冷锻炉

**量子冷锻炉（Quantum Cryoforge）**是通用多方块配方生产线中的低温加工阶段。它可以生产供后续机器使用的稳定冷却液等材料。

## 运行方式

- 最多执行 **27 个并行任务**，安全模式下为 **9 个**。
- 使用 MK1–MK3 配方门槛与[等级加成](multiblock-tiers.md)。
- 支持 `machine: 'quantum_cryoforge'` 的 `ufo:universal_multiblock` 自定义配方；参见 [KubeJS 配方](kubejs-recipes.md)。
- AE2 样板自动化需要一个量子样板缓冲器或已连接的代理。
- 从 ME 网络提取配方原料并送回产物。冷却液须从外部注入 **ME Massive Fluid Hatch**，FE 须接入 **FE Energy Input Hatch**。

使用游戏内结构预览或结构扫描仪查找接口位置。
