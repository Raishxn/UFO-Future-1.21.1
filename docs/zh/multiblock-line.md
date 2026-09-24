# 当前多方块机器系列

UFO Future 共有八台多方块机器。前五台处理资源，并使用 MK1–MK3 力场等级系统；后三台提供大规模 AE2 自动合成基础设施。

| 机器 | 主要用途 |
|---|---|
| [量子物质制造机](quantum-matter-fabricator.md) | 批量执行 DMA 和 QMF 配方，最多 27 个并行线程（安全模式下为 9 个） |
| [量子切片机](quantum-slicer.md) | 批量制备印刷部件 |
| [量子处理器装配机](quantum-processor-assembler.md) | 组装最终处理器 |
| 量子冷锻炉（Quantum Cryoforge） | 低温加工，包括稳定冷却液生产线 |
| [恒星枢纽](stellar-nexus.md) | 终局恒星模拟，内部缓存为 2000 亿 AE |
| 量子计算枢纽（Quantum Computation Nexus） | 将 UFO 合成存储器和协处理器模块组合为虚拟 AE2 CPU |
| 量子样板制造矩阵（Quantum Pattern Fabrication Matrix） | 可搜索的样板库与虚拟装配器，支持合成、锻造和切石 |
| 无限制造奇点（Infinity Fabrication Singularity） | 最终合成机器，最多支持 128 条持久化样板路线 |

## 样板与网络连接

每台通用加工多方块机器都必须安装且仅安装一个**量子样板缓冲器（Quantum Pattern Buffer）**或**量子样板代理（Quantum Pattern Proxy）**。缓冲器可存储 **72 个编码样板**，并服务于本机控制器。已连接的代理可让其他控制器共享同一个缓冲器。旧版**量子样板仓（Quantum Pattern Hatch）**用于单方块 DMA 流程。

加工机器从 ME 网络提取配方原料，并通过 ME 接口送回产物。冷却液须从外部注入 **ME Massive Fluid Hatch**，FE 电力须从外部接入 **FE Energy Input Hatch**。ME 网络的电力不会自动为这些控制器供能。[多方块等级](multiblock-tiers.md)页面解释配方门槛和效率加成；[KubeJS 指南](kubejs-recipes.md)介绍自定义加工配方。

安装要求及更完整的功能列表请参阅[项目 README](https://github.com/Raishxn/UFO-Future-1.21.1#readme)。
