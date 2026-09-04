---
navigation:
  parent: ufo_intro/materials.md
  title: Catalysts
  icon: ufo:chrono_catalyst_t1
  position: 20
item_ids:
  - ufo:matterflow_catalyst_t1
  - ufo:matterflow_catalyst_t2
  - ufo:matterflow_catalyst_t3
  - ufo:chrono_catalyst_t1
  - ufo:chrono_catalyst_t2
  - ufo:chrono_catalyst_t3
  - ufo:overflux_catalyst_t1
  - ufo:overflux_catalyst_t2
  - ufo:overflux_catalyst_t3
  - ufo:quantum_catalyst_t1
  - ufo:quantum_catalyst_t2
  - ufo:quantum_catalyst_t3
  - ufo:dimensional_catalyst
---

# Catalysts

Catalysts are upgrade cards accepted by the DMA and universal quantum
controllers. Shift + Right Click a compatible controller to install one directly,
or use its upgrade slots. Up to four installed catalysts combine
multiplicatively/additively according to their stat.

## Families

| Family | T1 | T2 | T3 | Thermal contribution |
|---|---:|---:|---:|---:|
| Matterflow | 0.90× AE | 0.75× AE | 0.50× AE | +50 / +100 / +200% |
| Chrono | 1.25× speed | 1.625× speed | 2.25× speed | +100 / +250 / +400% |
| Overflux | thermal control | thermal control | thermal control | −50 / −100 / −200% |
| Quantum | +10% bonus | +25% bonus | +50% bonus | +75 / +150 / +300% |

Heat contributions are added to the base heat multiplier. Overflux can offset
other families, but the final multiplier cannot become negative.

Quantum bonus output is separated from the deterministic amount promised to
AE2. The promised base output completes the crafting job; bonus material is
inserted as a byproduct and remains buffered if storage rejects it.

## Four-Card Synergy

Four identical catalyst items activate a family synergy and add a 1.5× thermal
penalty:

- Chrono doubles the combined speed multiplier.
- Matterflow halves the combined AE multiplier.
- Quantum adds another 50% bonus chance.
- Overflux halves the resulting heat multiplier after the common penalty.

## Dimensional Catalyst

<ItemImage id="ufo:dimensional_catalyst" scale="3" float="left" />

The creative Dimensional Catalyst overrides normal profiles: near-instant
processing, zero AE cost, no generated heat and a guaranteed 100% bonus roll.
Recipe inputs are still consumed.

> Catalyst combinations can exceed the cooling capacity of an otherwise stable
> machine. Test one job, then increase thread count while watching temperature.
