# Células de Armazenamento

UFO Future estende o sistema de armazenamento do AE2 com duas novas séries de células — **Anã Branca** (itens) e **Estrela de Nêutrons** (fluidos) — usando capacidades BigInteger muito além dos limites do AE2 vanilla. Também adiciona **Células Infinitas** para armazenamento ilimitado de um único recurso.

---

## Células de Itens Anã Branca

Células de armazenamento de itens de alta capacidade com contagem interna BigInteger. Todos os tiers usam o **Invólucro de Célula de Item Anã Branca**.

| Nome do Tier | Capacidade | Componente Necessário | ID de Registro |
|-------------|-----------|----------------------|---------------|
| **Echo** | 40M bytes | Matriz Phase Shift | `ufo:white_dwarf_cell_echo` |
| **Beacon** | 100M bytes | Matriz Hyper Dense | `ufo:white_dwarf_cell_beaco` |
| **Nexus** | 250M bytes | Matriz Tesseract | `ufo:white_dwarf_cell_nexus` |
| **Core** | 750M bytes | Matriz Event Horizon | `ufo:white_dwarf_cell_core` |
| **Singularity** | 2.147.483.647 bytes | Matriz Cosmic String | `ufo:white_dwarf_cell_singularity` |

### Fabricação
Cada célula: 1× Invólucro de Célula Anã Branca + 1× Matriz de Componente (receita sem forma).

**ID do Invólucro**: `ufo:white_dwarf_item_cell_housing`

---

## Células de Fluido Estrela de Nêutrons

| Nome do Tier | Capacidade | Componente Necessário | ID de Registro |
|-------------|-----------|----------------------|---------------|
| **Echo** | 40M bytes | Matriz Phase Shift | `ufo:neutron_star_reservoir_echo` |
| **Beacon** | 100M bytes | Matriz Hyper Dense | `ufo:neutron_star_reservoir_beaco` |
| **Nexus** | 250M bytes | Matriz Tesseract | `ufo:neutron_star_reservoir_nexus` |
| **Core** | 750M bytes | Matriz Event Horizon | `ufo:neutron_star_reservoir_core` |
| **Singularity** | 2.147.483.647 bytes | Matriz Cosmic String | `ufo:neutron_star_reservoir_singularity` |

**ID do Invólucro**: `ufo:neutron_fluid_cell_housing`

---

## Camaras quimicas Pulsar (opcionais)

A terceira serie BigInteger armazena quimicos do Mekanism nos tiers **Echo (40M), Beacon (100M), Nexus (250M), Core (750M) e Singularity (2.147.483.647 bytes)**. O invólucro e `ufo:pulsar_cell_housing`; as celulas usam `ufo:pulsar_chamber_echo`, `..._beaco`, `..._nexus`, `..._core` e `..._singularity`.

A serie depende da integracao com Mekanism. Sem ele, as camaras e seu invólucro ficam ocultos no Criativo e JEI/EMI, mas os IDs registrados permanecem para mundos existentes.

## Celula Infinity Genesis

A **Infinity Genesis Cell** (`ufo:infinity_genesis_cell`) aprende tipos de recurso quando eles sao inseridos. Depois oferece armazenamento AE2 ilimitado para os tipos aprendidos e aceita configuracoes de particao, inverter e fuzzy na Cell Workbench. As Infinity Cells de recurso fixo continuam sendo itens separados.

## Células Infinitas

Células de recurso único que armazenam quantidades **ilimitadas**. Perfeitas para automação em massa.

#### Recursos Vanilla

| Célula | ID de Registro |
|--------|---------------|
| Água | `ufo:infinity_water_cell` |
| Lava | `ufo:infinity_lava_cell` |
| Pedregulho | `ufo:infinity_cobblestone_cell` |
| Pedregulho de Ardósia | `ufo:infinity_cobbled_deepslate_cell` |
| Pedra do End | `ufo:infinity_end_stone_cell` |
| Netherrack | `ufo:infinity_netherrack_cell` |
| Areia | `ufo:infinity_sand_cell` |
| Obsidiana | `ufo:infinity_obsidian_cell` |
| Cascalho | `ufo:infinity_gravel_cell` |
| Tronco de Carvalho | `ufo:infinity_oak_log_cell` |
| Vidro | `ufo:infinity_glass_cell` |
| Fragmento de Ametista | `ufo:infinity_amethyst_shard_cell` |

#### AE2 / Mekanism

| Célula | ID de Registro |
|--------|---------------|
| Pedra do Céu | `ufo:infinity_sky_stone_cell` |
| Pelota de Antimatéria | `ufo:infinity_antimatter_pellet_cell` |
| Pelota de Plutônio | `ufo:infinity_plutonium_pellet_cell` |
| Pelota de Polônio | `ufo:infinity_polonium_pellet_cell` |
| Pelota HDPE | `ufo:infinity_hdpe_pellet_cell` |

#### Células de Corante (16 Cores)
Todas seguem o padrão: `ufo:infinity_<cor>_dye_cell`

As receitas das Infinity Cells agora usam o **QMF** e variam por recurso e tier. Consulte JEI/EMI para cada receita atual.

---

*Veja também: [DMA](dma.md) · [Progressão](progression.md) · [Materiais & Fluidos](materials.md)*
