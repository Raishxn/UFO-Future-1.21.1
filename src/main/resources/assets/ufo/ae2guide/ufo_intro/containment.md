---
navigation:
  parent: ufo_intro/materials.md
  title: Containment Concepts (ACC & SCM)
  position: 80
item_ids:
  - ufo:aether_containment_capsule
  - ufo:safe_containment_matter
---

# Safe Containment & Aether Capsules

These are functional one-slot containers for resources tagged as hazardous.

<div style="display: flex; gap: 10px; margin-bottom: 20px;">
  <ItemImage id="ufo:safe_containment_matter" scale="2" />
  <ItemImage id="ufo:aether_containment_capsule" scale="2" />
</div>

## SCM — Hazardous Item Container

1. Hold the SCM in one hand and a hazardous item stack in the other.
2. Use the SCM to store that stack in its single internal slot.
3. To retrieve it, leave the other hand empty and use the SCM again.

The tooltip shows the contained item. Non-hazardous items are rejected.

## ACC — Hazardous Fluid Container

The ACC stores up to **4,000 mB** of a fluid in UFO's hazardous-fluid tag.

1. Hold the ACC and another fluid container in opposite hands.
2. Use normally to transfer hazardous fluid from the other container into the
   ACC.
3. Sneak-use to transfer up to 1,000 mB from the ACC into the other container.

The ACC rejects untagged fluids and never mixes a different fluid into an
occupied capsule. Recipe ingredients and tags remain pack-configurable; use JEI
to find the current crafting path.
