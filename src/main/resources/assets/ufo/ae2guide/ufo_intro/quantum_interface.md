---
navigation:
  parent: ufo_intro/infrastructure.md
  title: Quantum Interface
  position: 47
item_ids:
  - ufo:quantum_interface
---

# Quantum Interface

<BlockImage id="ufo:quantum_interface" scale="3"></BlockImage>

Stores 36 configured resources across two pages of eighteen slots. Connect it to a powered ME grid, configure the coolant or other supplies, and select the quantity using the gear above each slot. Stock capacity is 1024 storage bytes per slot, converted to each resource type's units.

Enable **Auto Export** to send configured stock to accepting faces. Enable **Auto Import** to return other resources to ME storage. Configured resources are excluded from import to avoid draining supplies straight back into the network. Both modes can operate together.

Shift-click a quantity gear to enable continuous supply from ME storage; this is not infinite or free material. The speed button increases transfer frequency, not machine recipe speed.

## Wireless destinations

Use the [Quantum Wireless Tool](quantum_wireless_tool.md) to select this interface, then click the coolant input face on each destination. Enable wireless mode using the sidebar button or by sneak-clicking the interface with the tool. Local mode targets adjacent faces; wireless mode targets the saved links.

Destinations must remain loaded, in range and in the same dimension. Each source starts at 32 blocks; its range gear adjusts this within the server maximum. Devices connected to another ME grid cannot be used as a wireless bridge. Isolated devices are supported.

The Quantum Interface provides logistics only and never grants wireless machine buffs. The Quantum Pattern Hatch owns the wireless bonus panel.

Wireless and transfer widget artwork is credited to **AE2 Lightning Tech**. See the bundled asset notice for authorship and license details. This implementation does not yet include every Overloaded Interface feature, such as EJECT mode and its filters.

<RecipeFor id="ufo:quantum_interface" />

See [Wireless Network and Bonuses](quantum_wireless_buffs.md) for setup, range and recipe bonus configuration.
