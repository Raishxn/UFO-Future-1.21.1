---
navigation:
  title: Troubleshooting
  icon: ufo:structure_scanner
  parent: ufo_intro/index.md
  position: 90
---

# Troubleshooting

## Structure Will Not Form

1. Run Scan on the controller.
2. Check the controller facing and every highlighted position.
3. Use JEI Alternatives for cells that accept more than one hatch or field tier.
4. Ensure chunks containing the full structure are loaded.
5. Replace a suspicious hatch only after disconnecting active jobs safely.

The scanner and viewer use the same compiled structure definition as formation,
so a disagreement is a bug worth reporting with the controller position and the
highlighted expected block.

## `PAUSED_NO_GRID`

- Connect cables to the indicated hatch face.
- Check AE2 channels and grid power.
- Verify the structure is still formed after reconnecting.
- Existing process buffers are persistent; do not break the controller merely
  to restart it.

## Pattern Is Visible but Does Not Start

- The recipe tier may be above the machine tier.
- All 27 process slots may already be reserved.
- A required item alternative, fluid or chemical may be unavailable.
- The process may be manually paused.
- Safe Mode may be holding the machine at its thermal limit.

## `OUTPUT_BLOCKED`

Free capacity for the exact output key. Accepted amounts are removed from the
pending ledger; rejected amounts remain saved and retry later. Do not repeatedly
break and replace the controller while output is pending.

## Machine Overheats

- Verify coolant is in the coolant path, not the recipe-fluid path.
- Upgrade from Gelid Cryotheum to Stable Coolant or Temporal Fluid.
- Reduce active threads or disable Overclock.
- Enable Safe Mode while tuning the installation.
- Review catalyst heat multipliers.

## After a Datapack Reload

A process whose recipe ID disappeared enters invalid-recipe recovery. Tracked
inputs and buffered AE are refunded when their destinations can accept them.
If storage is full, make room and allow the controller to retry.
