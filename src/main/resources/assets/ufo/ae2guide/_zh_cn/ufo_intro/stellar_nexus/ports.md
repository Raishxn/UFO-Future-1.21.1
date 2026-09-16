---
navigation:
  parent: ufo_intro/stellar_nexus.md
  title: 端口、场与网络
  icon: ufo:ae_energy_input_hatch
  position: 20
---

# 恒星联结端口、场与网络

结构成型要求以下每种工作角色恰好一个：

<ItemGrid>
  <ItemIcon id="ufo:me_massive_input_hatch" />
  <ItemIcon id="ufo:me_massive_output_hatch" />
  <ItemIcon id="ufo:me_massive_fluid_hatch" />
  <ItemIcon id="ufo:ae_energy_input_hatch" />
</ItemGrid>

- ME 大型输入舱口提供配方物品。
- ME 大型输出舱口送回物品产物。
- ME 大型流体舱口负责输入冷却液。其 16,000,000 mB 本地储罐可从兼容 NeoForge 的管道接收冷却液，
  包括 Mekanism 管道。冷却流程只会消耗该本地储罐中的流体。
- FE 能量输入舱口只使用本地存储的 FE，为内部 **200B AE** 缓存充能。FE 能量线缆可以连接到任意面；
  能量传输不需要 ME 线缆。它绝不会抽取网络电力或 Applied Flux 存储。
  ME 网络仅负责独立的物品和流体访问。

每个 ME 舱口仅通过其朝向标示的那一面连接网络。其余五面与网络隔离；
结构外壳内部相邻并不会产生隐藏的 ME 连接。

流体管道不受上述 ME 连接面规则限制，可以从任意面填充冷却液。
只有凝滞冷却剂、稳定冷却剂和时间流体能够通过过滤器；外部抽取功能已禁用。

全部 **138** 个场位置都必须使用同一等级：MK1、MK2 或 MK3。
混用等级或缺少场生成器会直接使结构无法成型，而不是取性能平均值。
请保持与 **35×34×35** 结构范围相交的所有区块已加载。
