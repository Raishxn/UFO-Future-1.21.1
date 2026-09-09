---
navigation:
  parent: ufo_intro/infrastructure.md
  title: Quantum Hatches
  position: 46
item_ids:
  - ufo:quantum_pattern_hatch
  - ufo:quantum_pattern_buffer
  - ufo:quantum_pattern_proxy
  - ufo:quantum_pattern_provider_part
  - ufo:me_massive_output_hatch
  - ufo:me_massive_fluid_hatch
  - ufo:me_massive_input_hatch
  - ufo:ae_energy_input_hatch
---

# Quantum Hatches

<BlockImage id="ufo:quantum_pattern_buffer" scale="3"></BlockImage>

Every universal processing multiblock requires exactly one **Quantum Pattern
Buffer** or **Quantum Pattern Proxy** in a compatible casing position. The
Buffer stores up to **72 patterns**, connects to the ME grid, and serves the
controller in its own structure. A Proxy contains no patterns and needs no ME
cable; it lets another multiblock use a linked Buffer's patterns.

The legacy **Quantum Pattern Hatch** is now exclusive to standalone DMA
automation and is not a valid multiblock part.

<ItemImage id="ufo:quantum_pattern_provider_part" scale="3" />

The **Quantum Pattern Provider** is the cable-bus part version of the expanded provider.

- **ME Massive Output Hatch** returns finished items to the ME network.
- **ME Massive Fluid Hatch** is also the shared coolant input: it combines a
  **16,000,000 mB local tank** for external coolant supply with ME recipe output support.
- **ME Massive Input Hatch** feeds bulk item throughput into multiblocks.
- **AE Energy Input Hatch** accepts AE2 grid power and external FE.

## AE2 Connection Rules

Massive hatches remain **ME cable ports**. The Massive Fluid Hatch additionally
exposes a native NeoForge fluid input capability for coolant automation.

- Connect the ME cable to the face indicated by the hatch orientation. The other
  five faces are intentionally isolated from the ME grid.
- Fluid pipes may fill the Massive Fluid Hatch from any face, including the face
  used by an ME cable. Pipe access is input-only and accepts Gelid Cryotheum,
  Stable Coolant or Temporal Fluid; it cannot drain the tank or mix coolants.
- Put recipe item and fluid inputs in ME storage; coolant must be delivered to the local tank.
- Finished item and fluid outputs return to ME storage through the matching hatch.
- The AE Energy Input Hatch accepts FE from any face and AE from its connected grid.
  External energy is buffered, saved and consumed first; AE2 supplies any remainder.
  Conversion and consumption follow the AE2 power configuration.
- Use the hatch role the structure asks for: item input, item output, fluid output or AE energy.

Stellar Nexus, QMF, QPA, Slicer and Cryoforge require one coolant hatch and one
energy hatch in any compatible casing positions. The assembly previews show examples. Coolant
comes exclusively from the local tank: there is no automatic ME storage fallback.
Use pipes or an explicitly configured export device to control coolant delivery.
Existing structures can keep their current compatible casing positions, but an
old Pattern Hatch must be replaced by a Buffer or Proxy.
Additional supported hatches may replace casing positions; Vibrant Glass remains
glass-only. Exactly one Pattern Buffer or Proxy is required for QMF, QPA,
Slicer and Cryoforge.

## Sharing Patterns Between Multiblocks

Install a Pattern Buffer in the primary multiblock and put all encoded patterns
there. Install a Pattern Proxy in every additional multiblock. With the Quantum
Wireless Tool, right-click the Buffer and then each Proxy. The Buffer first
tries its own controller; a pattern that does not belong there, or cannot start
there, is offered to linked Proxies. Links, pattern inventory and controller
membership persist across world reloads.

Right-click a massive hatch with an empty hand to see whether it is online and
linked to a controller. A neighboring hatch does not create an invisible cable
connection through the multiblock shell.

## Pattern Ownership

When AE2 pushes a processing pattern, the items delivered with that pattern are
owned by the process immediately. The controller does not charge those item
requirements a second time. Missing fluids or supported chemicals may still be
pulled through their configured storage paths.

The Pattern Buffer owns multiblock wireless bonuses. The legacy Pattern Hatch
owns DMA bonuses only. See [Wireless Network and Bonuses](quantum_wireless_buffs.md)
for values configured in config/ufo/wireless.toml and the adjustable source range.
