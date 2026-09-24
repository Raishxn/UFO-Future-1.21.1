# 量子无线系统

**量子无线系统（Quantum Wireless）**是 UFO 机器与 AE2 网络之间第一阶段可使用的无线传输功能，包含 Quantum Interface、Quantum Wireless Tool，以及 Pattern Hatch 和 Pattern Buffer/Proxy 的连接。它**不会**为机器无线传输 FE 电力。

## Quantum Interface

接口有 **36 个配置槽，分为两页，每页 18 个**。它可从 ME 网络补充资源；配置好的槽位可持续供应已连接的目标面。可在界面中选择本地或无线模式、导入／导出行为及 I/O 速度。ME 网络自身仍需要资源和电力。

## 连接目标

1. 将 Quantum Interface 接入有电的 ME 网络。
2. 在来源方启用无线模式。用 **Quantum Wireless Tool** 点击来源方以选中；潜行点击来源方可切换模式。
3. 用工具点击目标方块的一面，例如 DMA 输入面或多方块机器的物理冷却液接口，即可添加或移除连接。
4. 配置接口槽位及导出行为。ME 网络中必须有冷却液，且冷却液仍从机器的物理接口进入。

连接距离可配置，不会强制加载区块。工具要求来源与目标在同一维度；目标区块未加载时会等待其恢复。

## 样板连接与限制

**Quantum Pattern Hatch** 可连接 DMA；**Quantum Pattern Buffer** 可连接通用多方块机器的 **Quantum Pattern Proxy**。这种连接用于分发样板，与物理结构连接分开。

本功能仍处于初期阶段。远程 EJECT、导入过滤、FE 传输和完整的目标编辑尚未纳入当前流程。服务器配置位于 `config/ufo/wireless.toml`。
