---
navigation:
  parent: ufo_intro/tools.md
  title: Quantum Wireless Tool
  position: 10
item_ids:
  - ufo:quantum_wireless_tool
---

# Quantum Wireless Tool

<ItemImage id="ufo:quantum_wireless_tool" scale="4" />

Enable wireless mode on the source first using its UI button or sneak-right-click with the tool. Selecting or editing links while wireless is disabled is refused with a reminder.

1. Right-click a Quantum Pattern Hatch block or [Quantum Interface](quantum_interface.md) to select the source.
2. Right-click a destination machine's accepting face to add a link. Click the same face again to remove it.
3. Sneak-right-click the source to toggle wired/wireless mode, or use its screen button.

While holding the tool, blue lines connect the selected source to blue destination faces. A yellow cube identifies the selected source, and yellow previews a new face under the crosshair. Links refresh automatically while the source is loaded. The HUD also identifies the source, coordinates and dimension. The cable-bus Quantum Pattern Provider does not currently act as a wireless source.

For processing patterns, link the machine's item/fluid input face. Crafting outputs still need a return path to ME storage, such as Auto Import on a Quantum Interface linked to an output face. Wireless does not load chunks or bridge dimensions/grids. Replacing a destination invalidates its old link.

The original 16×16 tool texture belongs to UFO's visual design; it does not reuse the AE2 Lightning tool texture. Sidebar widget credits remain with AE2 Lightning Tech.

<RecipeFor id="ufo:quantum_wireless_tool" />

The default range is 32 blocks per source, adjustable in the hatch/interface UI. See [Wireless Network and Bonuses](quantum_wireless_buffs.md).
