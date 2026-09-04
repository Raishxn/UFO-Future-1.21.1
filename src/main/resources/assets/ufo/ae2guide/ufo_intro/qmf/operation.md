---
navigation:
  parent: ufo_intro/qmf.md
  title: Operation and Recipes
  icon: ufo:quantum_matter_fabricator_controller
  position: 20
---

# QMF Operation and Recipes

The QMF executes both native QMF recipes and compatible DMA recipes. Recipe
requirements are checked against the machine tier before any resource is
committed.

## Startup Checklist

1. Confirm **FORMED** and **GRID ONLINE**.
2. Confirm the displayed MK tier meets the recipe requirement.
3. Supply item/fluid ingredients and sufficient AE energy.
4. Keep an output path available.
5. Begin with one thread before dispatching a large batch.

## Thread Lifecycle

Each accepted job owns one of 27 persistent process slots. A slot reserves exact
input keys, charges its energy ledger, progresses, prepares deterministic output
and retries insertion until storage accepts it. Reloading or losing AE2 pauses
the slot without discarding its accounting.

| State | Meaning | Action |
|---|---|---|
| `RUNNING` | Energy and requirements are available | None |
| `PAUSED` | Thread paused from its cell control | Resume when ready |
| `PAUSED_NO_GRID` | ME node/grid unavailable | Restore cable, power and loaded chunks |
| `OUTPUT_BLOCKED` | Promised output is waiting | Free matching ME capacity |
| `UNFORMED` | Structure no longer matches | Scan and repair highlighted positions |

## Scaling Safely

Normal mode admits up to **27** simultaneous jobs; Safe Mode admits **9**. New
requests wait when all allowed slots are occupied. Existing reservations are not
silently deleted when the limit or tier changes.
