---
navigation:
  parent: ufo_intro/equipment.md
  title: Armor Sets
  icon: ufo:ufo_chestplate
  position: 20
item_ids:
  - ufo:ufo_helmet
  - ufo:ufo_chestplate
  - ufo:ufo_leggings
  - ufo:ufo_boots
  - ufo:thermal_resistor_mask
  - ufo:thermal_resistor_chest
  - ufo:thermal_resistor_pants
  - ufo:thermal_resistor_boots
  - ufo:astral_nexus_helmet
  - ufo:astral_nexus_chestplate
  - ufo:astral_nexus_leggings
  - ufo:astral_nexus_boots
---

# Armor Sets

## Thermal Resistor Set

<ItemImage id="ufo:thermal_resistor_chest" scale="3" float="left" />

Wear the Mask, Chest, Pants and Boots together to satisfy UFO's thermal
protection check. A complete Thermal Resistor set, or a complete powered UFO set,
protects against tagged hazardous materials and the DMA industrial heat zone.
Individual pieces alone do not satisfy the full protection contract.

## Powered UFO Armor

<ItemImage id="ufo:ufo_chestplate" scale="3" float="left" />

All four pieces must be equipped and each piece must contain at least **400 RF**
for the continuous set bonuses to remain active.

- Resistance X and Night Vision are maintained while powered.
- Maximum health increases by 40 points (20 hearts).
- UFO-owned creative flight is granted without stealing flight supplied by
  Creative, Spectator or another system.
- Every piece consumes **400 RF per second** while the set is active.

Emergency protocols draw energy from the combined armor buffers:

| Protocol | Trigger | Energy |
|---|---|---:|
| Void recovery | Ordinary void damage | 50,000 RF |
| Anti-death | `/kill`-class or extreme damage | 100,000 RF |
| Emergency evacuation | Incoming damage would leave 2 hearts or less | 10,000 RF |
| Lazarus | Death event not already prevented | 200,000 RF |

When energy is unavailable, energy-dependent protection and flight do not
activate. Hold Shift over a piece to inspect its stored RF.

## Astral Nexus Armor

<ItemImage id="ufo:astral_nexus_chestplate" scale="3" float="left" />

The complete Astral Nexus set is the final armor tier. Its server-side contract
includes damage/death cancellation, one-million-times damage reflection,
creative flight, Night Vision, Water Breathing, Step Assist and full air supply.
When Mekanism is installed it also clears accumulated player radiation.

Astral protection requires the exact four Astral Nexus pieces. Mixing armor
families does not activate a complete-set bonus.
