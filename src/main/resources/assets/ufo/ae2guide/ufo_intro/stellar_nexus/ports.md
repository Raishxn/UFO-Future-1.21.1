---
navigation:
  parent: ufo_intro/stellar_nexus.md
  title: Ports, Fields, and Network
  icon: ufo:ae_energy_input_hatch
  position: 20
---

# Nexus Ports, Fields, and Network

Formation requires exactly one of each operational role:

<ItemGrid>
  <ItemIcon id="ufo:me_massive_input_hatch" />
  <ItemIcon id="ufo:me_massive_output_hatch" />
  <ItemIcon id="ufo:me_massive_fluid_hatch" />
  <ItemIcon id="ufo:ae_energy_input_hatch" />
</ItemGrid>

- Massive Item Input supplies recipe items.
- Massive Item Output returns item products.
- Massive Fluid Hatch is the coolant-input role. Its 16,000,000 mB local tank
  accepts coolant from NeoForge-compatible pipes, including Mekanism pipes, and
  retains its existing ME fluid access.
- AE Energy Input Hatch charges the internal **200B AE** buffer and supplies the
  controller's AE power connection.

Connect each hatch through the face indicated by its orientation. The remaining
five faces are isolated; adjacency inside the shell does not create a hidden ME
connection.

Fluid pipes are independent of those ME-facing rules and may fill coolant from
any face. The Nexus drains the local tank first and ME storage second. Only
Gelid Cryotheum, Stable Coolant and Temporal Fluid pass the filter; external
drain is disabled.

All **138** field positions must be filled with a single tier: MK1, MK2 or MK3.
Mixed or missing fields invalidate formation rather than averaging performance.
Keep every chunk intersecting the 35×34×35 footprint loaded.
