# UFO Future License

UFO Future uses separate licenses for source code and assets.

## Source Code

All source code in this repository is licensed under the GNU Lesser General Public License v3.0 or later (LGPLv3+), unless a file states otherwise.

Full license text: https://www.gnu.org/licenses/lgpl-3.0.html

## Art and Texture Assets

Art, textures, models, sounds, guide images, and other non-code creative assets are licensed under Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0), unless a file states otherwise.

Full license text: https://creativecommons.org/licenses/by-nc-sa/3.0/legalcode

## Third-Party Notices

UFO Future is an addon for Applied Energistics 2. Applied Energistics 2 code is licensed under LGPLv3, while its textures and models use CC BY-NC-SA 3.0. This project follows that split so derived or referenced AE2-compatible code and visual assets remain license-compatible.

Some UFO Future textures are based on, adapted from, or inspired by work from the following projects. Thank you to their maintainers and contributors:

- AE2 Crystal Science: https://github.com/Frostbite-time/AE2-Crystal-Science
- GT New Horizons Modpack: https://github.com/GTNewHorizons/GT-New-Horizons-Modpack
- GTO Project / GTOCore: https://github.com/GregTech-Odyssey — the compact
  connected-texture casing sheets (`entropy_singularity_casing_ctm.png`,
  `quantum_hyper_mechanical_casing_ctm.png`), the Stellar Nexus renderer based on
  GTO Core's `EyeOfHarmonyRenderer` (as credited in its source), and a set of
  textures and models that are byte-for-byte identical to GTOCore. GTOCore's
  original textures are licensed CC BY-NC-SA 4.0. The item-by-item provenance, the
  current status and the required action for every matched file are tracked in
  [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md) and
  [docs/credits/texture-provenance.md](docs/credits/texture-provenance.md).
- AE2 Lightning Tech: its interactive multiblock preview, auto-build architecture, and compact connected-texture geometry were ported and adapted for UFO Future under LGPL-3.0. The `quick_build.png` toolbar icon is redistributed under CC BY-NC-SA 3.0 with attribution to the AE2 Lightning Tech contributors: https://github.com/ae2lt/AE2-Lightning-Tech

Assets that the provenance audit traced to all-rights-reserved projects — Thermal
Foundation's `gelid_cryotheum_flow.png` and `gelid_cryotheum_still.png`, and
Astral Sorcery's `liquid_starlight.png` — were removed from the repository on
2026-09-17. Their resource locations are still consumed by
`src/main/java/com/raishxn/ufo/fluid/ModFluidTypes.java` and must be re-created with
original art. Never reintroduce a third-party texture without a license that permits
redistribution; [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md) is the
authoritative list and `tools/check-texture-provenance.py` checks it.

The Quantum Wireless two-state toolbar widget adapts AE2 Lightning Tech's TextureToggleButton. Eight associated mode/import/export/speed icons are redistributed unchanged under CC BY-NC-SA 3.0. See [the provenance inventory](docs/credits/ae2-lightning-wireless.md) and the bundled `assets/ufo/AE2LT-WIRELESS-NOTICE.md` for the source revision, file list and licenses. Quantum Interface functional parity is still in progress.

The UFO Armor configuration uses AE2's standard checkbox, cog, back and slider visuals and redistributes AdvancedAE's `states.png` for its clear/uninstall icon. See `assets/ufo/ADVANCEDAE-UI-NOTICE.md` in the packaged resources.

The NeoForge MDK template files remain covered by their own MIT license notice in `TEMPLATE_LICENSE.txt`.
