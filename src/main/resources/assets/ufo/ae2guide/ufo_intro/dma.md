---
navigation:
  parent: ufo_intro/index.md
  title: Dimensional Matter Assembler
  position: 30
---

# Dimensional Matter Assembler (DMA) — Player Guide
...
**Overview**
The Dimensional Matter Assembler (DMA) is the primary advanced crafting machine in UFO Future. It processes item and fluid inputs, uses catalysts to modify outcomes, and relies on a coolant/thermal system. This guide explains catalysts, the DMA thermal model (thermo), and practical usage.

---

## Quickstart
1. Provide **power** (DMA consumes very large amounts of energy).
2. Place item inputs into the item input slots and fluids into the fluid input tank.
3. Add **coolant** into the coolant tank.
4. Insert **catalysts** into the catalyst/upgrade slots according to the recipe.
5. Configure I/O (auto-input / auto-eject) as needed and start the recipe.

**Tip:** Start with a mid-tier coolant (e.g. Spatial Fluid) when testing recipes that use UU-Matter or Transcending Matter.

---

## Catalysts — What they do

Catalysts are inserted into the DMA to change performance (efficiency, speed, stability, and extra outputs). Each catalyst belongs to a family and has 3 tiers (T1, T2, T3).

### Families
- **Matterflow** — energy efficiency modifier (reduces energy cost).
- **Chrono** — speed modifier (reduces processing time).
- **Overflux** — stability modifier (reduces failure chance) and improves coolant effectiveness.
- **Quantum** — increases chance for extra outputs / bonus drops.
- **Dimensional Catalyst** — special single-use/creative-tier catalyst that *ignores thermal penalties and failure mechanics*, granting maximum buffs.

### Base effects (game constants)
- Matterflow base: **-10% energy cost** (per base multiplier applied by tier stack).
- Chrono base: **+25% processing speed**.
- Overflux base: **-10% failure chance**.
- Quantum base: **+10% extra output chance**.

*(These are the underlying base values used before tier multipliers and stacking.)*

### Tiers & multipliers
- **T1** multiplier = **×1.0**
- **T2** multiplier = **×2.5**
- **T3** multiplier = **×5.0**

You can stack catalysts in the DMA slots **up to 4 copies per slot**. Stacking uses a soft-cap formula to apply diminishing returns when multiple copies are present. Mixed-tier stacks are averaged with weighting by tier multiplier.

**Player rule of thumb:**
- Use T3 for high-value runs or when you need big effect jumps.
- Use Matterflow T2/T3 for long runs to save energy.
- Use Overflux when processing unstable recipes (UU-Matter, high-end alloys).
- Use Chrono only if you can supply extra coolant or Overflux to compensate for added heat.

---

## Catalyst heat & tradeoffs
- Each catalyst has a **static heat penalty** (different per family & tier). More/larger catalysts → more heat.
- **Chrono** reduces coolant efficiency (requires more coolant or Overflux to stabilize).
- **Overflux** increases coolant efficiency (~**+15%** multiplier to coolant effect).
- **Chrono** decreases coolant efficiency (~**-10%**).
- **Quantum** tends to add heat as a tradeoff for extra outputs.

**Implementation notes**
- The DMA calculates `recipeHeat + sum(catalystHeatPerTier * counts)` to yield the heat impact of an operation.
- Overflux and Chrono alter how coolant counteracts that heat.

---

## Thermal system (Thermo)

The DMA uses a temperature model with zones. Keep the temperature under control—exceeding certain thresholds reduces performance or causes failures.

### Key constants (from code)
- Ambient temperature baseline: **273 K** (used internally).
- Coolant consumption rate: **5 mB per tick** (per active cooldown consumption).
- Meltdown fuse: **200 ticks** (10 seconds) once MELTDOWN threshold reached.

### Temperature zones (Celsius)
- **SAFE**: below **4000°C** — normal operation.
- **OVERHEAT**: **4000–5999°C** — warnings, slight penalties.
- **INEFFICIENCY**: **6000–8499°C** — coolant is half as effective (≈ **-50%** coolant efficiency).
- **DESTABILIZATION**: **8500–9999°C** — high failure chance; alarms; salvage risk.
- **MELTDOWN**: **≥10000°C** — triggers meltdown fuse (200 ticks). If not corrected, DMA may explode and destroy/consume outputs.

### Coolants (player-focused)
- **Spatial Fluid** — stable, reliable coolant for intermediate recipes.
- **Temporal Fluid** — mid-tier coolant; interacts thematically with Chrono.
- **Gelid Cryotheum** — strong cryogenic coolant (approx **-60°C per tick** equivalent).
- **Transcending Matter Fluid** — **best coolant** (approx **-100°C per tick**), expensive and used for high-end DMA recipes.

**Effects & modifiers**
- **Overflux** increases coolant efficiency (~**+15%**).
- **Chrono** reduces coolant efficiency (~**-10%**).
- Coolant is consumed at a base rate (5 mB/tick) while active; stronger coolants reduce temperature faster per mB.

### Failure & meltdown behavior
- In **DESTABILIZATION** zone, there is an increased chance for partial failures: outputs may be consumed, or salvage outputs may be produced instead.
- On **MELTDOWN**, a **200-tick fuse** begins. If the temperature isn't reduced (by coolant or removing catalysts) before the fuse ends, the DMA can explode, destroying items and producing salvage/debris.

---

## DMA usage best practices
- **Always keep coolant** in the DMA, especially when using Chrono or multiple T3 catalysts.
- Prioritize **Matterflow** (T2/T3) for long continuous runs to reduce energy costs.
- Use **Overflux**T2/T3 if the recipe carries a high failure risk (e.g., UU-Matter conversions).
- **Dimensional Catalyst** (if available) disables thermal penalties — only use on extremely valuable runs.
- **I+F rule**: watch recipe complexity — some DMA recipes enforce `ItemInputs + FluidInputs <= 4`. If your recipe uses too many inputs, it may be invalid.

---

## Example workflows
**Example: Infinity Water Cell (simple)**
1. Item inputs: filters/containers as recipe specifies.
2. Fluid input: water / fluid-specific ingredient.
3. Coolant: Spatial Fluid (fill at least a few buckets).
4. Catalysts: Matterflow T2 + Overflux T1 (reduces energy and stabilizes).
5. Start the process and monitor temperature and coolant consumption.

---

## Troubleshooting
- DMA runs too hot quickly: remove Chrono catalysts or switch to a stronger coolant (Gelid or Transcending).
- Frequent failures on high-end recipes: add Overflux, reduce Chrono, or buffer coolant.
- Meltdown fuse active: immediately remove catalysts, add high-efficiency coolant, or stop the machine and drain heat by pausing operations.

---

## Advanced / Server admin notes
- Consider gating Transcending Matter Fluid and T3 catalysts behind research/quests or requiring multi-step crafting to prevent early-game abuse.
- Recommend backups before enabling recipes using Transcending Matter Fluid or UU-Matter.

---

*End of DMA guide.*
