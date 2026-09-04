---
navigation:
  parent: ufo_intro/stellar_nexus.md
  title: Safety, Overclock, and Recovery
  icon: ufo:stellar_field_generator_t3
  position: 40
---

# Nexus Safety, Overclock, and Recovery

Safe Mode is enabled by default. It multiplies energy, fuel and coolant cost by
**2.5**, but performs a controlled shutdown at maximum heat. Overclock provides
**5× speed**, with **10× AE cost** and **5× fuel, heat and coolant load**.

With Safe Mode disabled, reaching maximum heat triggers the configured local
containment failure. Destructive block damage is off by default; when explicitly
enabled by an administrator it remains bounded by radius, per-tick work, total
block budget and dimension allowlist.

## Recovery Table

| Symptom | Likely cause | Recovery |
|---|---|---|
| Structure invalid | Missing/mixed field or wrong hatch count | Scan, repair exact highlighted position |
| Energy does not rise | Energy hatch face/grid offline | Correct orientation, power and cabling |
| Start rejected | Recipe gate, fuel, AE or output unavailable | Read the controller's validation lines |
| Heat rises too fast | Weak/no coolant or Overclock | Disable Overclock, improve coolant, keep Safe Mode |
| Output remains pending | ME capacity/partition rejects key | Restore matching capacity; do not restart recipe |
| Offline after reload | Part of footprint/chunks unavailable | Load the whole structure and allow reconciliation |

Never disable Safe Mode for the first cycle of a new recipe or immediately after
changing field tier, coolant routing or server thermal configuration.
