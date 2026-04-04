# Materiais & Fluidos

## Materiais de Fragmentos Estelares

### Fragmentos de Anã Branca
Material estelar base. Formas: Lingote, Barra, Pepita, Pó, Bloco.
- **Prefixo do ID**: `ufo:white_dwarf_fragment_*`
- **Fabricação** (DMA): 2x Lingote de Netherite + 4x Gelo Azul + 4x Pedra do Céu AE2 + 250mB Criotheum Gélido = 100K AE

### Fragmentos de Estrela de Nêutrons
Tier intermediário. Formas: Lingote, Barra, Pepita, Pó, Bloco.
- **Prefixo do ID**: `ufo:neutron_star_fragment_*`
- **Fabricação** (DMA): 4x Lingote AB + Estrela do Nether + Matriz de Obsidiana + 500mB Criotheum Gélido = 500K AE

### Fragmentos de Pulsar
Tier alto. Formas: Lingote, Pepita, Pó, Bloco.
- **Prefixo do ID**: `ufo:pulsar_fragment_*`
- **Fabricação** (DMA): 2x Lingote EN + 2x Magnetita + 4x Para-raio + 1000mB Criotheum Gélido = 1M AE

---

## Progressão de Matéria

| Matéria | Entradas Chave | Energia | ID |
|---------|---------------|---------|-----|
| Esfera de Neutrônio | 9x Lingote EN | 1M | `ufo:neutronium_sphere` |
| Esfera de Neutrônio Enriquecida | Esfera Neutrônio + 2x Anomalia Quântica | 8M | `ufo:enriched_neutronium_sphere` |
| Protomatéria | Esfera EN Enr. + Matéria UU | 2M | `ufo:proto_matter` |
| Matéria Corpórea | Protomatéria + 64x Bloco de Ferro | 5M | `ufo:corporeal_matter` |
| Cristal de Matéria UU | Fragmento de Ametista + 10K mB Matéria UU | 20M | `ufo:uu_matter_crystal` |
| Matéria de Anã Branca | Corpórea + Cristal UU + Bloco/Fluido AB | 7M | `ufo:white_dwarf_matter` |
| Matéria de Estrela de Nêutrons | Corpórea + Cristal UU + Bloco/Fluido EN | 9.5M | `ufo:neutron_star_matter` |
| Matéria de Pulsar | Corpórea + Cristal UU + Bloco/Fluido Pulsar | 12M | `ufo:pulsar_matter` |
| Matéria Escura | 16x cada AB/EN/Pulsar Matter + Transcendente | 200M | `ufo:dark_matter` |

---

## Fluidos Customizados

| Fluido | Uso | ID |
|--------|-----|-----|
| Luz Estelar Líquida | Fluido base para maioria das receitas | `ufo:source_liquid_starlight_fluid` |
| Criotheum Gélido | Refrigerante + fabricação | `ufo:source_gelid_cryotheum` |
| Fluido Temporal | Melhor refrigerante (100 HU/mB) | `ufo:source_temporal_fluid` |
| Fluido Espacial | Catalisadores quânticos | `ufo:source_spatial_fluid` |
| Matéria Primordial | Fabricação de componentes | `ufo:source_primordial_matter_fluid` |
| Plasma de Matéria Estelar Bruta | Processamento estelar | `ufo:raw_star_matter_plasma` |
| Matéria Transcendente | Receitas endgame | `ufo:transcending_matter` |
| Matéria UU | Replicação | `ufo:uu_matter` |
| Amplificador UU | Precursor de UU | `ufo:source_uu_amplifier_fluid` |
| Fluido de Anã Branca | Fabricação de Matéria AB | `ufo:source_white_dwarf_fragment_fluid` |
| Fluido de Estrela de Nêutrons | Fabricação de Matéria EN | `ufo:source_neutron_star_fragment_fluid` |
| Fluido de Pulsar | Fabricação de Matéria Pulsar | `ufo:source_pulsar_fragment_fluid` |

### Cadeia Bootstrap (Sem Dependências Circulares)
1. Água + Bola de Neve + Gelo Compacto → Pó Blizz
2. Pó Blizz + Bola de Neve → Pó Criotheum
3. Pó Blizz + Água → Criotheum Gélido
4. Matriz de Obsidiana + Estrela do Nether + Amplificador UU → Luz Estelar Líquida

---

*Veja também: [Progressão](progression.md) · [DMA](dma.md)*
