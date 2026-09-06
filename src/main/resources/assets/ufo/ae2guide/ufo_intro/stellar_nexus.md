---
navigation:
  parent: ufo_intro/machines.md
  title: Stellar Nexus
  position: 50
item_ids:
  - ufo:stellar_nexus_controller
---

# Stellar Nexus

<BlockImage id="ufo:stellar_nexus_controller" scale="4"></BlockImage>

The **Stellar Nexus** is the endgame stellar simulation multiblock.

<SubPages />

## Core Mechanics

- Reads inputs directly from the ME network
- Charges a **200B AE** internal buffer while assembled
- Consumes fuel on start
- Consumes coolant while running
- Generates heat continuously during operation
- Requires one item input hatch, one item output hatch, one fluid output hatch and one AE energy input hatch

The **AE Energy Input Hatch** is the only port that charges the internal buffer. Every massive hatch connects to AE2 only through the face indicated by its orientation; the other five faces are electrically isolated, so neighboring hatches cannot create an invisible grid connection through the structure shell.

## Field Tiers

The four field generator positions must all match the same tier:

- **MK1**
- **MK2**
- **MK3**

Mixed tiers invalidate the structure.

## Safety And Overclock

- **Safe Mode** costs **2.5x** AE, fuel and coolant, but shuts down instead of exploding
- **Overclock** gives **5x** speed, **10x** AE cost and **5x** fuel, heat and coolant use

With Safe Mode disabled, overheat causes a local containment blast by default. Destructive block grief is disabled unless a server administrator explicitly enables it; the opted-in wave remains bounded by radius, per-tick CPU/block budgets, a total block limit and a dimension allowlist.

## Heat Profile

- Base heat generation: **recipe cooling level + 1 HU/tick**.
- Overclock heat generation: **5x** the recipe heat per tick.
- Stronger recipes naturally run hotter, so cooling level is part of the machine's thermal burden.

## Coolant Ladder

- The controller tries to consume **100 mB/tick** of coolant while running.
- **Safe Mode** raises that to **250 mB/tick**.
- **Overclock** multiplies coolant draw by **5x** on top of that.
- **Gelid Cryotheum**: **1 cooling per mB**.
- **Stable Coolant**: **4 cooling per mB**.
- **Temporal Fluid**: **8 cooling per mB**.
- Final cooling is multiplied by your field tier bonus, so better field generators make the same coolant stronger.

The values **1/4/8** are server-configurable under `stellar.coolant`; zero disables that coolant for the Stellar Nexus. The listed values are the defaults used by UFO progression.

## Safe Startup Procedure

1. Form the structure with four identical field generators.
2. Connect every massive hatch on its indicated face.
3. Wait for the internal buffer to charge and verify fuel/coolant availability.
4. Keep Safe Mode enabled for the first cycle.
5. Start one known recipe and watch temperature, stored AE and pending output.
6. Enable Overclock only after cooling remains stable for a complete normal
   cycle.

If output storage fills, the promised result remains buffered. If the AE grid is
disconnected, the controller pauses instead of discarding the active operation.

## Dedicated supply hatches

Install at least one **ME Massive Fluid Hatch** and one **AE Energy Input Hatch** in any Singularity Casing position. Item input and item output hatches are also required. The preview shows an example layout; hatch positions are interchangeable. Core casings, matrices and field generators retain their own requirements.

Supply coolant to the fluid hatch using pipes or a player-configured ME Export Bus. The machine consumes only coolant already in that tank; it never searches ME storage for coolant. The tank holds 16,000,000 mB of one coolant. Recipe fluids and chemicals remain separate.

The energy hatch accepts external **FE** cables and **AE2 grid energy**. FE is converted using the AE2 server conversion setting and stored locally (up to 1,000,000,000 AE equivalent); the local buffer is consumed first, then the hatch requests any remainder from its AE2 grid. Adding hatches does not multiply the controller's charging limit. The recipe network still needs power for item/fluid automation.
