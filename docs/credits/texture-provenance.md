# UFO Future - proveniencia das texturas (auditoria GTOCore)

Fonte: RESOURCE_MATCHES.md (snapshot UFO db7a3690, GTOCore 63f96666).
O laudo encontrou 53 recursos byte a byte identicos. Este checklist acompanha o que falta
corrigir. Ele e gerado por tools/check-texture-provenance.py a partir de
docs/credits/texture-provenance.json; nao edite este arquivo a mao.

Status: IDENTICAL = ainda igual ao GTOCore (falta mexer); CHANGED = ja substituido;
REMOVED = removido do repositorio.

## Prioridade 1 - remover/substituir (ARR: sem licenca de uso)

| # | arquivo | origem | status |
|---:|---|---|---|
| 8 | src/main/resources/assets/ufo/textures/block/fluid/gelid_cryotheum_flow.png | Thermal Foundation / CoFH | CHANGED |
| 9 | src/main/resources/assets/ufo/textures/block/fluid/gelid_cryotheum_still.png | Thermal Foundation / CoFH | CHANGED |
| 10 | src/main/resources/assets/ufo/textures/block/fluid/liquid_starlight.png | Astral Sorcery | CHANGED |

## Prioridade 2 - terceiros GTNH (GT5-Unofficial / TecTech): atribuir ou substituir

| # | arquivo | origem | status |
|---:|---|---|---|
| 1 | src/main/resources/assets/ufo/models/obj/climber.mtl | GT5-Unofficial (climber) | REMOVED |
| 2 | src/main/resources/assets/ufo/models/obj/climber.obj | GT5-Unofficial (climber) | REMOVED |
| 3 | src/main/resources/assets/ufo/models/obj/space.mtl | TecTech (space/star) | CHANGED |
| 4 | src/main/resources/assets/ufo/models/obj/space.obj | TecTech (space/star) | CHANGED |
| 5 | src/main/resources/assets/ufo/models/obj/star.mtl | TecTech (space/star) | CHANGED |
| 6 | src/main/resources/assets/ufo/models/obj/star.obj | TecTech (space/star) | CHANGED |
| 7 | src/main/resources/assets/ufo/textures/block/fluid/fluid.stable_coolant.png | GT5-Unofficial (stable baryonic matter) | CHANGED |
| 11 | src/main/resources/assets/ufo/textures/block/fluid/raw_star_matter_plasma.png | GT5-Unofficial (raw star matter / spatial / temporal) | CHANGED |
| 12 | src/main/resources/assets/ufo/textures/block/fluid/spatial_fluid.png | GT5-Unofficial (raw star matter / spatial / temporal) | CHANGED |
| 13 | src/main/resources/assets/ufo/textures/block/fluid/temporal_fluid.png | GT5-Unofficial (raw star matter / spatial / temporal) | CHANGED |
| 38 | src/main/resources/assets/ufo/textures/block/multiblock/overlay_front.png | GTNH multiblock overlay | CHANGED |
| 45 | src/main/resources/assets/ufo/textures/block/obj/climber_overlay.png | GT5-Unofficial (climber overlay) | REMOVED |
| 46 | src/main/resources/assets/ufo/textures/block/qmf/overlay_front.png | GT5-Unofficial (DTPF overlay) | CHANGED |
| 47 | src/main/resources/assets/ufo/textures/block/qmf/overlay_front_active.png | GT5-Unofficial (DTPF overlay) | CHANGED |
| 48 | src/main/resources/assets/ufo/textures/block/qmf/overlay_front_active_emissive.png | GT5-Unofficial (fusion glow overlay) | CHANGED |
| 49 | src/main/resources/assets/ufo/textures/block/qmf/overlay_front_emissive.png | GT5-Unofficial (fusion glow overlay) | CHANGED |

## Prioridade 3 - GTO original: manter com atribuicao CC BY-NC-SA 4.0 ou substituir

| # | arquivo | origem | status |
|---:|---|---|---|
| 14 | src/main/resources/assets/ufo/textures/item/dust_blizz.png | GTOCore | CHANGED |
| 15 | src/main/resources/assets/ufo/textures/item/dust_cryotheum.png | GTOCore | CHANGED |

## Prioridade 4 - a confirmar com os autores do GTO: confirmar, atribuir ou substituir

| # | arquivo | origem | status |
|---:|---|---|---|
| 16 | src/main/resources/assets/ufo/textures/item/neutron_star_fragment_dust_overlay.png | GTOCore (unconfirmed) | REMOVED |
| 17 | src/main/resources/assets/ufo/textures/item/neutron_star_fragment_ingot_overlay.png | GTOCore (unconfirmed) | REMOVED |
| 18 | src/main/resources/assets/ufo/textures/item/neutron_star_fragment_nugget_overlay.png | GTOCore (unconfirmed) | REMOVED |
| 19 | src/main/resources/assets/ufo/textures/item/neutron_star_fragment_rod_overlay.png | GTOCore (unconfirmed) | REMOVED |
| 20 | src/main/resources/assets/ufo/textures/item/neutron_star_matter.png | GTOCore (unconfirmed) | CHANGED |
| 21 | src/main/resources/assets/ufo/textures/item/obsidian_matrix.png | GTOCore (unconfirmed) | CHANGED |
| 22 | src/main/resources/assets/ufo/textures/item/proto_matter.png | GTOCore (unconfirmed) | CHANGED |
| 23 | src/main/resources/assets/ufo/textures/item/pulsar_matter.png | GTOCore (unconfirmed) | CHANGED |
| 24 | src/main/resources/assets/ufo/textures/item/thermal_resistor_plating.png | GTOCore (unconfirmed) | CHANGED |
| 25 | src/main/resources/assets/ufo/textures/item/unstable_star.png | GTOCore (unconfirmed) | CHANGED |
| 26 | src/main/resources/assets/ufo/textures/item/unstable_white_hole_matter.png | GTOCore (unconfirmed) | CHANGED |
| 27 | src/main/resources/assets/ufo/textures/block/multiblock/entropy_catalyst_bank_components.png | GTOCore (unconfirmed) | REMOVED |
| 28 | src/main/resources/assets/ufo/textures/block/multiblock/entropy_catalyst_bank_components_active.png | GTOCore (unconfirmed) | REMOVED |
| 29 | src/main/resources/assets/ufo/textures/block/multiblock/entropy_catalyst_bank_components_active_emissive.png | GTOCore (unconfirmed) | REMOVED |
| 30 | src/main/resources/assets/ufo/textures/block/multiblock/entropy_computer_condensation_matrix.png | GTOCore (unconfirmed) | CHANGED |
| 31 | src/main/resources/assets/ufo/textures/block/multiblock/entropy_computer_condensation_matrix_ctm.png | GTOCore (unconfirmed) | REMOVED |
| 32 | src/main/resources/assets/ufo/textures/block/multiblock/entropy_containment_chamber_components.png | GTOCore (unconfirmed) | REMOVED |
| 33 | src/main/resources/assets/ufo/textures/block/multiblock/entropy_coolant_matrix_components.png | GTOCore (unconfirmed) | REMOVED |
| 34 | src/main/resources/assets/ufo/textures/block/multiblock/entropy_coolant_matrix_components_active.png | GTOCore (unconfirmed) | REMOVED |
| 35 | src/main/resources/assets/ufo/textures/block/multiblock/entropy_coolant_matrix_components_active_emissive.png | GTOCore (unconfirmed) | REMOVED |
| 36 | src/main/resources/assets/ufo/textures/block/multiblock/entropy_singularity_casing.png | GTOCore (unconfirmed) | CHANGED |
| 37 | src/main/resources/assets/ufo/textures/block/multiblock/entropy_singularity_casing_ctm.png | GTOCore (unconfirmed) | CHANGED |
| 39 | src/main/resources/assets/ufo/textures/block/multiblock/quantum_hyper_mechanical_casing.png | GTOCore (unconfirmed) | CHANGED |
| 40 | src/main/resources/assets/ufo/textures/block/general1/overlay_front.png | GTOCore (unconfirmed) | CHANGED |
| 41 | src/main/resources/assets/ufo/textures/block/general1/overlay_front_active.png | GTOCore (unconfirmed) | CHANGED |
| 42 | src/main/resources/assets/ufo/textures/block/general1/overlay_front_active_emissive.png | GTOCore (unconfirmed) | CHANGED |
| 43 | src/main/resources/assets/ufo/textures/block/general1/overlay_front_emissive.png | GTOCore (unconfirmed) | CHANGED |
| 44 | src/main/resources/assets/ufo/textures/block/obj/climber_cable.png | GTOCore (unconfirmed) | REMOVED |
| 50 | src/main/resources/assets/ufo/textures/block/quantum_processor_assembler/overlay_front.png | GTOCore (unconfirmed) | REMOVED |
| 51 | src/main/resources/assets/ufo/textures/block/quantum_processor_assembler/overlay_front_active.png | GTOCore (unconfirmed) | REMOVED |
| 52 | src/main/resources/assets/ufo/textures/block/quantum_slicer/overlay_front.png | GTOCore (unconfirmed) | REMOVED |
| 53 | src/main/resources/assets/ufo/textures/block/quantum_slicer/overlay_front_active.png | GTOCore (unconfirmed) | REMOVED |

## Verificacao

    python3 tools/check-texture-provenance.py
