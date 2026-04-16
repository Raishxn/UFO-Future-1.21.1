# PRD: Stellar Apiary and Stellar Centrifuge

## 1. Summary

This proposal adds a new endgame bee-processing line to UFO Future built around two multiblocks:

- **Stellar Apiary**
- **Stellar Centrifuge**

The design goal is simple:

- integrate **UFO Future + AE2 + Productive Bees**
- preserve the fantasy of running many bees inside a single industrial machine
- produce **extreme-scale outputs**
- remain **TPS-safe** even inside large modpacks

The core idea is to avoid simulating large numbers of live bee entities. Instead, the multiblocks treat bees as high-throughput production profiles managed by a controller, then push bulk outputs directly into AE2 through massive hatches.

## 2. Product Goals

Add a bee-focused endgame system that:

- feels dramatically stronger than standard Productive Bees setups
- uses UFO Future field tiers as the main scaling model
- supports mass AE2 automation without inventory spam
- can generate millions of combs or comb blocks per minute in late game
- avoids catastrophic TPS loss from entity-heavy automation
- creates a clean two-step industrial loop:
  - **Apiary** for comb, comb block, and honey generation
  - **Centrifuge** for massive downstream processing

## 3. Player Fantasy

The player should feel that they have:

- built a forbidden stellar-scale bee reactor
- compressed entire bee populations into a controlled field chamber
- industrialized Productive Bees far beyond normal progression
- connected the entire system directly to AE2 for absurd throughput

This should feel more like a post-endgame industrial complex than an upgraded vanilla apiary.

## 4. Position In Progression

This system should sit firmly in late game or post-endgame, after the player already has:

- stable AE2 infrastructure
- reliable coolant production
- DMA automation
- QMF / Quantum multiblock progression
- comfortable access to high-tier UFO materials
- strong processor production

Recommended progression role:

- normal Productive Bees = species collection and early to mid automation
- Stellar Apiary = extreme comb and honey generation
- Stellar Centrifuge = extreme refinement and AE2-scale mass handling

## 5. Machine Identity

## 5.1 Stellar Apiary

The **Stellar Apiary** is the high-output producer.

Its responsibilities:

- hold many bees inside dedicated hatches
- convert those bees into bulk comb and honey output
- optionally compress output into comb blocks at higher tiers or with upgrades
- use field tiers, heat, and recover mode as the main balancing systems

## 5.2 Stellar Centrifuge

The **Stellar Centrifuge** is the bulk processor.

Its responsibilities:

- pull selected combs or comb blocks directly from the AE network
- process them with high parallel throughput
- send item and fluid outputs directly back into AE2 through massive output hatches

Together, these two machines form a clean industrial chain.

## 6. Structural Concept

## 6.1 Stellar Apiary Structure

Recommended footprint:

- **3x3x3** for a compact version
- **5x5x5** for a more premium endgame version

Recommended structure rules:

- outer shell made from dedicated casings
- internal core reserved for UFO field components
- up to **4 Bee Matrix Hatches**
- each hatch stores up to **32 bees**
- each hatch is limited to **one species**
- includes AE and output infrastructure on the shell

Recommended shell components:

- controller
- casing blocks
- AE energy input
- ME massive output hatch
- optional ME massive fluid output hatch
- honey reservoir or internal tank hatch
- up to 4 bee hatches

## 6.2 Stellar Centrifuge Structure

Recommended footprint:

- **5x5x5** or a vertical **3x3x5**

Recommended shell components:

- controller
- casing blocks
- AE energy input
- **ME Massive Comb Input Bus**
- ME massive output hatch
- ME massive fluid output hatch
- internal field structure

## 7. Field Tier Model

The machine line uses UFO field tiers as its primary scaling model.

Suggested tiers:

- **Tier 0** = no field blocks
- **Tier 1**
- **Tier 2**
- **Tier 3**

Design intent:

- higher field tiers increase production
- higher field tiers increase speed
- higher field tiers reduce heat pressure

This keeps the system aligned with UFO Future instead of adding a separate parallel upgrade language like food-only or floral-only scaling.

## 8. Recommended Scaling Model

The scaling should feel exponential, but still remain readable.

Example starting point:

| Tier | Production Multiplier | Speed Multiplier | Heat Multiplier |
|---|---:|---:|---:|
| Tier 0 | `1x` | `1x` | `1.00x` |
| Tier 1 | `4x` | `3x` | `0.85x` |
| Tier 2 | `20x` | `8x` | `0.70x` |
| Tier 3 | `100x` | `20x` | `0.50x` |

Important note:

If production and speed are both scaled aggressively, total throughput rises much faster than it looks. For example, `100x` production and `20x` speed effectively create a `2000x` throughput jump.

That is acceptable for endgame, but should be a deliberate payoff.

## 9. Bee Hatch Model

Each **Bee Matrix Hatch** should:

- accept up to **32 bees**
- accept only **one species at a time**
- show installed bee count
- show species identity
- show estimated contribution to output

One species per hatch is recommended because it:

- simplifies balancing
- simplifies the GUI
- reduces edge cases
- makes output prediction easier

With 4 hatches, the controller can support up to:

- `128` bees total
- `4` active species at once

## 10. Production Model

The Stellar Apiary should not simulate one active process per bee.

Instead, it should:

- read the installed bees from each hatch
- aggregate total effective production by hatch and species
- run the machine in cycles
- generate bulk outputs at the end of each cycle

Recommended logic:

1. Read all valid hatches
2. Resolve each hatch into a production profile
3. Aggregate outputs
4. Apply field tier multipliers
5. Apply machine upgrades
6. Generate item and fluid results in bulk
7. Inject all outputs into AE2 in as few operations as possible

This preserves the fantasy of “many bees working at once” without creating the TPS cost of many live entities.

## 11. Main Outputs

The Stellar Apiary should focus on:

- combs
- comb blocks
- honey
- optional secondary byproducts depending on species

Recommended rule:

- baseline output is **comb**
- higher tier or specialized upgrades can convert part of the output to **comb block**
- honey is always generated as a fluid side-product and stored in an internal tank or exported directly

This keeps the output model easy to read.

## 12. Special Upgrades

The machine should have its own dedicated upgrade family.

These upgrades should be stronger than current Omega-level upgrades in their specialty, but only for this machine line.

Recommended philosophy:

- not “more Omega than Omega”
- instead “Omega-tier but apiculture-specialized”

Possible upgrade themes:

- increase effective bee count per hatch
- improve comb-to-comb-block conversion
- improve byproduct chance
- reduce recover duration
- improve honey efficiency

Recommended crafting direction:

- crafted from **Omega upgrade + catalysts**
- assembled through **DMA** or **QMF**

## 13. Heat System

The heat system should be simpler than the DMA or Stellar Nexus failure model.

Recommended behavior:

- heat rises while the machine is active
- field tiers reduce heat pressure
- high parallels and aggressive throughput raise heat faster
- reaching max heat does **not** explode the machine
- reaching max heat forces the machine into **Recover Mode**

This supports an “OP but stable” fantasy better than catastrophic destruction.

## 14. Recover Mode

When maximum heat is reached:

- the machine stops immediately
- it enters **Recover Mode**
- it remains offline for **10 minutes**
- this recover timer is saved in NBT
- breaking and replacing the controller must **not** reset the timer

Recommended reasons for this design:

- prevents abuse
- avoids frustration from explosions
- still creates a meaningful cost for overdriving the machine
- preserves the identity of heat as a real balancing system

Optional extension:

- future versions could scale recover duration by severity
- but the first implementation should prefer a simple fixed timer

## 15. Parallelism

The machine should support very high throughput, but parallelism should remain aggregated.

Recommended interpretation:

- parallels are not “one thread per bee”
- parallels are “how many bulk production batches the machine resolves per cycle”

This matters because per-bee threading would become a hot path very quickly.

Recommended throughput target:

- late game should comfortably reach **millions of combs or comb blocks per minute**

That is acceptable if the implementation remains:

- cycle-based
- aggregated
- AE2-native
- low-allocation

## 16. GUI Concept For Stellar Apiary

Recommended controller GUI sections:

- installed bee hatches
- species per hatch
- bee count per hatch
- machine upgrades
- projected outputs
- honey tank
- heat bar
- recover timer when locked
- current field tier

Possible UX nicety:

- show estimated output per minute for the current configuration

## 17. Stellar Centrifuge Core Concept

The **Stellar Centrifuge** should be built specifically for AE2 mass handling.

Unlike simpler centrifuge designs with a few input and output slots, this machine should:

- pull large amounts of combs or comb blocks from AE2
- process them with high parallel throughput
- output items and fluids directly back into AE2

This machine exists because once the Apiary is generating at absurd scale, normal slot-based centrifuge designs become the true bottleneck.

## 18. ME Massive Comb Input Bus

The **ME Massive Comb Input Bus** is a special hatch for the Stellar Centrifuge.

Its GUI should:

- display the combs and comb blocks currently available in the ME network
- let the player choose which types are valid inputs for the multiblock
- act as a selection and filtering layer rather than a normal inventory

Recommended implementation rule:

- do **not** rescan the entire network every frame

Instead, cache the visible list and refresh it:

- when the GUI opens
- when the player presses refresh
- or on a throttled interval

Without this guardrail, the UI itself could become a performance hotspot on large AE networks.

## 19. Stellar Centrifuge Processing Model

Recommended logic:

1. Read selected comb filters from the input bus
2. Pull large batches from the AE network
3. Resolve centrifuge outputs in bulk
4. Aggregate identical outputs
5. Insert all item outputs through massive output hatches
6. Insert all fluid outputs through massive fluid hatches

This should support very high parallel counts without depending on many physical inventory slots.

## 20. Performance Principles

This proposal is only successful if it remains TPS-safe.

Hard rules:

- do not simulate live bee entities inside the multiblock
- do not evaluate every bee every tick as an independent process
- do not insert outputs one item at a time
- do not poll the full AE network every frame for the GUI
- do not rebuild expensive lists when nothing changed

Recommended guardrails:

- cycle-based processing every `20` or `40` ticks
- cached production profiles
- cached GUI item lists
- aggregated output maps
- one or few AE2 insert calls per output type

## 21. Open Questions

These should be resolved before implementation:

- Should the machines use a **3x3x3** or **5x5x5** final footprint?
- Should the Apiary produce only **comb**, or also support **direct comb block** mode?
- Should honey be just a byproduct buffer, or also a machine upkeep resource?
- Should field tiers require all four internal positions to match, like other UFO multiblocks?
- Should the Apiary use only dedicated UFO upgrades, or also expose Productive Bees-style upgrade widgets?
- Should recover always be a fixed **10 minutes**, or scale later with overheat severity?
- Should the Centrifuge process both **comb** and **comb block** from day one, or launch with one format first?

## 22. Possible Contradictions

These are the main places where the concept could become internally inconsistent:

- Wanting a “simple” system while also deeply reproducing Productive Bees native upgrade behavior may add unnecessary complexity.
- Saying the new upgrades are “stronger than Omega” can blur the role of Omega unless these upgrades are clearly specialized.
- A Tier 0 version that is too strong may invalidate normal Productive Bees progression too early.
- Very high throughput with too many species-specific side-products can move the real bottleneck from the machine to GUI sync and AE2 insertion paths.

## 23. Main Risks

Main implementation risks:

- coupling too tightly to Productive Bees internals instead of building a stable UFO-owned production layer
- making the GUI query AE2 too aggressively
- letting heat become cosmetic instead of meaningful
- allowing controller break-and-place to bypass recover mode
- balancing Tier 3 so high that Tier 1 and Tier 2 feel irrelevant

## 24. Recommendation

Recommended implementation direction:

- build **Stellar Apiary** first
- use **one species per hatch**
- use **aggregated cycle-based production**
- use **field tiers 0 to 3**
- use **heat + recover mode**
- then build the **Stellar Centrifuge** as the dedicated post-processing solution

This path preserves the intended fantasy while keeping the architecture friendly to large modpacks and AE2-scale factories.
