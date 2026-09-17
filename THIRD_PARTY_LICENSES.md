# Third-Party Licenses and Asset Provenance

UFO Future source code is LGPL-3.0-or-later and its own creative assets are
CC BY-NC-SA 3.0 (see LICENSE.md). This file lists every third-party work that is
redistributed, adapted or referenced, with its license and the action taken.

The mechanical audit of assets that are byte-for-byte identical to GTOCore lives in
docs/credits/texture-provenance.md, generated from
docs/credits/texture-provenance.json and checked by
tools/check-texture-provenance.py.

## Applied Energistics 2

- Code: LGPL-3.0. Textures and models: CC BY-NC-SA 3.0.
- Usage: required runtime dependency; the AE2-compatible visuals follow that split.

## AdvancedAE (PedroKS / Pedroksl)

- License: LGPL-3.0.
- Usage: states.png is redistributed for the armor clear/uninstall icon, and the
  Infinity Fabrication Singularity interface is an attributed adaptation of the
  Quantum Crafter interface. See CREDITS.md and assets/ufo/ADVANCEDAE-UI-NOTICE.md.

## AE2 Lightning Tech

- Code: LGPL-3.0. Textures and assets: CC BY-NC-SA 3.0.
- Usage: the interactive multiblock preview, the auto-build architecture and the
  compact connected-texture geometry were ported and adapted; eight
  wireless/import/export icons and quick_build.png are redistributed unchanged.
- See CREDITS.md and docs/credits/ae2-lightning-wireless.md.

## GTOCore / GTO Project (GregTech-Odyssey)

- Original textures: CC BY-NC-SA 4.0, as declared by the GregTech-Odyssey
  repository for its original art.
- Usage: a number of UFO textures and models were found byte-for-byte identical to
  GTOCore. The full list, the current status and the required action for each entry
  are in docs/credits/texture-provenance.md.
- GTOCore itself contains third-party resources that remain under their original
  licenses; the byte-identical items that trace to those projects are handled in the
  next section and are not covered by the GTOCore CC BY-NC-SA 4.0 claim.

## Assets that must not be redistributed here

The audit found UFO assets that trace to third-party, all-rights-reserved or
license-unknown projects. They are not covered by the GTOCore CC BY-NC-SA 4.0
claim.

| Source | License | Affected UFO assets | Action |
| --- | --- | --- | --- |
| Thermal Foundation / CoFH | All rights reserved | textures/block/fluid/gelid_cryotheum_flow.png and ..._still.png | Removed on 2026-09-17 |
| Astral Sorcery | All rights reserved | textures/block/fluid/liquid_starlight.png | Removed on 2026-09-17 |
| GT New Horizons (GT5-Unofficial, TecTech) | Asset license to verify | the entries with origin gtnh in texture-provenance.md | Verify license, attribute or replace |
| GTOCore items still unconfirmed | Unknown until the GTO authors confirm | the entries with origin unconfirmed in texture-provenance.md | Confirm, attribute or replace |

The three removed fluid textures are still referenced by
src/main/java/com/raishxn/ufo/fluid/ModFluidTypes.java. New, original art must be
dropped at the same resource locations (plus the matching .mcmeta when animated)
before the next release.

Separately, seven entropy component textures had no consumer anywhere in the mod
(no block, model, blockstate or renderer, only stale language keys) and were removed
as dead weight on 2026-09-17:
entropy_catalyst_bank_components.png and its _active/_active_emissive variants,
entropy_containment_chamber_components.png, and entropy_coolant_matrix_components.png
and its _active/_active_emissive variants, each with its .mcmeta. The
entropy_computer_condensation_matrix and entropy_assembler_core_casing textures are
still in use and are not affected.

## Checking the provenance

    python3 tools/check-texture-provenance.py

The script fails while any all-rights-reserved asset is still present and reports
how many of the matched resources are still byte-identical to the audit.
