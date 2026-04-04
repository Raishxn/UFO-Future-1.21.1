# Materiales y Fluidos

## Materiales de Fragmentos Estelares

### Fragmentos de Enana Blanca
Material estelar base. Formas: Lingote, Barra, Pepita, Polvo, Bloque.
- **Prefijo ID**: `ufo:white_dwarf_fragment_*`
- **Fabricación** (DMA): 2x Netherite + 4x Hielo Azul + 4x Piedra del Cielo + 250mB Criotheum = 100K AE

### Fragmentos de Estrella de Neutrones
Nivel intermedio. Formas: Lingote, Barra, Pepita, Polvo, Bloque.
- **Prefijo ID**: `ufo:neutron_star_fragment_*`
- **Fabricación** (DMA): 4x Lingote EB + Estrella del Nether + Matriz Obsidiana + 500mB Criotheum = 500K AE

### Fragmentos de Púlsar
Nivel alto. Formas: Lingote, Pepita, Polvo, Bloque.
- **Prefijo ID**: `ufo:pulsar_fragment_*`
- **Fabricación** (DMA): 2x Lingote EN + 2x Magnetita + 4x Pararrayos + 1000mB Criotheum = 1M AE

---

## Progresión de Materia

| Materia | Entradas Clave | Energía | ID |
|---------|---------------|---------|-----|
| Esfera de Neutronio | 9x Lingote EN | 1M | `ufo:neutronium_sphere` |
| Esfera Neutronio Enriquecida | Esfera + 2x Anomalía Cuántica | 8M | `ufo:enriched_neutronium_sphere` |
| Proto Materia | Esfera Enr. + Materia UU | 2M | `ufo:proto_matter` |
| Materia Corpórea | Proto + 64x Bloque Hierro | 5M | `ufo:corporeal_matter` |
| Cristal UU | Amatista + 10K mB UU | 20M | `ufo:uu_matter_crystal` |
| Materia EB | Corpórea + Cristal UU + Bloque/Fluido EB | 7M | `ufo:white_dwarf_matter` |
| Materia EN | Corpórea + Cristal UU + Bloque/Fluido EN | 9.5M | `ufo:neutron_star_matter` |
| Materia Púlsar | Corpórea + Cristal UU + Bloque/Fluido Púlsar | 12M | `ufo:pulsar_matter` |
| Materia Oscura | 16x cada EB/EN/Púlsar + Trascendente | 200M | `ufo:dark_matter` |

---

## Fluidos Personalizados

| Fluido | Uso | ID |
|--------|-----|-----|
| Luz Estelar Líquida | Fluido base para mayoría de recetas | `ufo:source_liquid_starlight_fluid` |
| Criotheum Gélido | Refrigerante + fabricación | `ufo:source_gelid_cryotheum` |
| Fluido Temporal | Mejor refrigerante (100 HU/mB) | `ufo:source_temporal_fluid` |
| Fluido Espacial | Catalizadores cuánticos | `ufo:source_spatial_fluid` |
| Materia Primordial | Fabricación de componentes | `ufo:source_primordial_matter_fluid` |
| Plasma Estelar Bruto | Procesamiento estelar | `ufo:raw_star_matter_plasma` |
| Materia Trascendente | Recetas endgame | `ufo:transcending_matter` |
| Materia UU | Replicación | `ufo:uu_matter` |
| Amplificador UU | Precursor de UU | `ufo:source_uu_amplifier_fluid` |

### Cadena Bootstrap
1. Agua + Bola de Nieve + Hielo Compacto → Polvo Blizz
2. Polvo Blizz + Bola de Nieve → Polvo Criotheum
3. Polvo Blizz + Agua → Criotheum Gélido
4. Matriz Obsidiana + Estrella Nether + Amplificador UU → Luz Estelar Líquida

---

*Ver también: [Progresión](progression.md) · [DMA](dma.md)*
