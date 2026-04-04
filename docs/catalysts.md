# Catalysts

Catalysts are **AE2 upgrade cards** used in the **Dimensional Matter Assembler**'s 4 upgrade slots. They modify the machine's performance — speed, energy cost, failure chance, and bonus drop chance — but also affect heat generation.

---

## Catalyst Families

There are **4 families** with 3 tiers each (T1 → T3), plus 1 creative catalyst:

| Family | Primary Effect | Heat Effect |
|--------|---------------|-------------|
| **Matterflow** | Reduces energy cost | Increases heat production |
| **Chrono** | Increases speed | Increases heat production (highest) |
| **Overflux** | Reduces failure chance | **Decreases** heat production (cooling) |
| **Quantum** | Increases bonus drop chance | Increases heat production |
| **Dimensional** | All bonuses, instant crafting | Resets temperature (Creative only) |

---

## Detailed Stats

### Matterflow — Energy Efficiency

Reduces the energy cost of DMA recipes.

| Tier | Energy Effect | Heat Multiplier | Buffer Multiplier |
|------|--------------|-----------------|-------------------|
| T1 | −10.0% Energy Cost | ×1.5 heat | ×10 buffer |
| T2 | −25.0% Energy Cost | ×2.0 heat | ×100 buffer |
| T3 | −50.0% Energy Cost | ×3.0 heat | ×1000 buffer |

### Chrono — Speed

Increases processing speed.

| Tier | Speed Effect | Heat Multiplier | Buffer Multiplier |
|------|-------------|-----------------|-------------------|
| T1 | +25.0% Speed | ×2.0 heat | ×10 buffer |
| T2 | +62.5% Speed | ×3.5 heat | ×100 buffer |
| T3 | +125.0% Speed | ×5.0 heat | ×1000 buffer |

> **Warning**: Chrono catalysts produce the most heat. Stacking 4× Chrono T3 will result in extreme heat generation. Always use adequate coolant!

### Overflux — Stability

Reduces failure chance and **cools** the machine.

| Tier | Failure Effect | Heat Multiplier | Buffer Multiplier |
|------|---------------|-----------------|-------------------|
| T1 | −10.0% Failure Chance | ×0.5 heat (cooling!) | ×1 buffer |
| T2 | −25.0% Failure Chance | ×0.0 heat (no heat!) | ×1 buffer |
| T3 | −50.0% Failure Chance | ×-1.0 heat (active cooling!) | ×1 buffer |

> **Tip**: Overflux catalysts are the only ones that _reduce_ heat generation. They can be combined with Chrono catalysts to offset thermal penalties.

### Quantum — Bonus Drops

Increases chance of bonus item drops.

| Tier | Bonus Effect | Heat Multiplier | Buffer Multiplier |
|------|-------------|-----------------|-------------------|
| T1 | +10.0% Bonus Drop | ×1.75 heat | ×1 buffer |
| T2 | +25.0% Bonus Drop | ×2.5 heat | ×1 buffer |
| T3 | +50.0% Bonus Drop | ×4.0 heat | ×1 buffer |

---

## Stacking Rules

### Soft Cap
When stacking multiple catalysts of the same type:

| Stacked Count | Effectiveness |
|---------------|--------------|
| 1× | 100% |
| 2× | 175% |
| 3× | 225% |
| 4× | 250% |

### 4-Stack Synergy Bonus
If you fill all 4 upgrade slots with the **same catalyst**, a special synergy bonus activates:

| Family | Synergy Bonus | Heat Penalty |
|--------|--------------|--------------|
| **Chrono 4×** | ×2.0 additional speed multiplier | ×1.5 additional heat |
| **Matterflow 4×** | ×0.5 additional power discount | ×1.5 additional heat |
| **Quantum 4×** | (Reserved for future use) | ×1.5 additional heat |
| **Overflux 4×** | Standard stacking | ×1.5 additional heat |

> **Exploit Protection**: The DMA enforces a minimum processing time of **1 second** (20 ticks). Stacking Chrono catalysts beyond this cap will only increase the heat multiplier without further reducing processing time.

---

## Dimensional Catalyst (Creative)

The **Dimensional Catalyst** is a creative-mode only item that combines all effects:

- ⚡ Instant Crafting
- 💰 No Energy/Resource Cost
- 🎁 100% Bonus Drop
- ✅ Never Fails
- ❄️ Resets Machine Temperature

### Crafting (DMA Recipe)
Requires all four T3 catalysts + Cosmic String Component Matrix + 10,000 mB Transcending Matter.
- **Energy**: 500,000,000 AE
- **Time**: 10,000 ticks (8.3 minutes)

---

## Heat Multiplier Calculation

The total heat multiplier is calculated as:

```
totalHeatMult = 1.0
for each catalyst in upgrade slots:
    totalHeatMult += max(0, catalyst.staticHeat / 100.0)

if 4-stack synergy active:
    totalHeatMult *= 1.5
```

This multiplier is applied to the base heat generation rate (+1 HU every 2 ticks).

**Example**: 4× Chrono T3 catalysts:
- Base: `1.0 + (4.0 × 4) = 17.0` → `× 1.5 synergy = 25.5× heat generation`
- That's **+12.75 HU/s** instead of the base **+0.5 HU/s**!

---

## Catalyst Item IDs

| Catalyst | Registry ID |
|----------|-------------|
| Matterflow T1 | `ufo:matterflow_catalyst_t1` |
| Matterflow T2 | `ufo:matterflow_catalyst_t2` |
| Matterflow T3 | `ufo:matterflow_catalyst_t3` |
| Chrono T1 | `ufo:chrono_catalyst_t1` |
| Chrono T2 | `ufo:chrono_catalyst_t2` |
| Chrono T3 | `ufo:chrono_catalyst_t3` |
| Overflux T1 | `ufo:overflux_catalyst_t1` |
| Overflux T2 | `ufo:overflux_catalyst_t2` |
| Overflux T3 | `ufo:overflux_catalyst_t3` |
| Quantum T1 | `ufo:quantum_catalyst_t1` |
| Quantum T2 | `ufo:quantum_catalyst_t2` |
| Quantum T3 | `ufo:quantum_catalyst_t3` |
| Dimensional | `ufo:dimensional_catalyst` |

---

*See also: [DMA](dma.md) · [Progression](progression.md)*
