# Receitas Customizadas KubeJS — DMA

Esta página documenta como criar, modificar e remover receitas do **Montador de Matéria Dimensional (DMA)** usando [KubeJS](https://kubejs.com/) para Minecraft 1.21.1 (NeoForge).

---

## Tipo de Receita

```
ufo:dimensional_assembly
```

---

## Estrutura Básica da Receita

Toda receita DMA consiste em:

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `item_inputs` | Array de `IngredientStack.Item` | ✅ | Ingredientes de itens com quantidades |
| `fluid_inputs` | Array de `IngredientStack.Fluid` | ❌ | Ingredientes de fluidos com quantidades (mB) |
| `item_outputs` | Array de `GenericStack` | ❌ | Saídas de itens com quantidades |
| `fluid_outputs` | Array de `GenericStack` | ❌ | Saídas de fluidos com quantidades |
| `energy` | Inteiro | ✅ | Total de energia AE consumida para esta receita |
| `time` | Inteiro | ✅ | Tempo de processamento em ticks (20 ticks = 1 segundo) |

> Pelo menos uma saída (item ou fluido) deve ser especificada.

---

## Criando Receitas com KubeJS

### Adicionando uma Receita Simples de Item

```js
// kubejs/server_scripts/dma_recipes.js

ServerEvents.recipes(event => {
  // Criar uma receita DMA customizada
  event.custom({
    type: 'ufo:dimensional_assembly',
    item_inputs: [
      {
        ingredient: { item: 'minecraft:diamond' },
        count: 4
      },
      {
        ingredient: { item: 'minecraft:netherite_ingot' },
        count: 1
      }
    ],
    fluid_inputs: [
      {
        ingredient: { fluid: 'ufo:source_liquid_starlight_fluid' },
        amount: 500
      }
    ],
    item_outputs: [
      {
        id: 'ufo:quantum_anomaly',
        amount: 1
      }
    ],
    fluid_outputs: [],
    energy: 1000000,
    time: 400
  }).id('kubejs:custom_quantum_anomaly')
})
```

### Adicionando uma Receita com Saída de Fluido

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:dimensional_assembly',
    item_inputs: [
      {
        ingredient: { item: 'ufo:white_dwarf_fragment_ingot' },
        count: 8
      }
    ],
    fluid_inputs: [
      {
        ingredient: { fluid: 'ufo:source_liquid_starlight_fluid' },
        amount: 1000
      }
    ],
    item_outputs: [],
    fluid_outputs: [
      {
        id: 'ufo:source_white_dwarf_fragment_fluid',
        amount: 2000
      }
    ],
    energy: 80000,
    time: 200
  }).id('kubejs:custom_white_dwarf_fluid')
})
```

### Saídas Mistas de Item + Fluido

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'ufo:dimensional_assembly',
    item_inputs: [
      {
        ingredient: { item: 'ufo:obsidian_matrix' },
        count: 4
      },
      {
        ingredient: { item: 'minecraft:nether_star' },
        count: 16
      }
    ],
    fluid_inputs: [
      {
        ingredient: { fluid: 'ufo:source_uu_amplifier_fluid' },
        amount: 100
      }
    ],
    item_outputs: [
      {
        id: 'ufo:scrap',
        amount: 8
      }
    ],
    fluid_outputs: [
      {
        id: 'ufo:source_liquid_starlight_fluid',
        amount: 10000
      }
    ],
    energy: 4000000,
    time: 1000
  }).id('kubejs:starlight_and_scrap')
})
```

---

## Usando Tags como Entradas

Você pode usar tags de itens ao invés de itens específicos:

```js
event.custom({
  type: 'ufo:dimensional_assembly',
  item_inputs: [
    {
      ingredient: { tag: 'c:ingots/iron' },
      count: 16
    },
    {
      ingredient: { item: 'minecraft:ender_pearl' },
      count: 4
    }
  ],
  fluid_inputs: [],
  item_outputs: [
    {
      id: 'ufo:obsidian_matrix',
      amount: 2
    }
  ],
  fluid_outputs: [],
  energy: 50000,
  time: 100
}).id('kubejs:tagged_obsidian_matrix')
```

---

## Removendo Receitas Existentes

```js
ServerEvents.recipes(event => {
  // Remover por ID da receita
  event.remove({ id: 'ufo:dma/quantum_anomaly' })
  
  // Remover todas as receitas DMA que produzem um item específico
  event.remove({ type: 'ufo:dimensional_assembly', output: 'ufo:quantum_anomaly' })
  
  // Remover todas as receitas DMA (use com cuidado!)
  event.remove({ type: 'ufo:dimensional_assembly' })
})
```

---

## Modificando Receitas Existentes

KubeJS não suporta modificação direta de receitas para tipos customizados. A abordagem recomendada é:

1. **Remover** a receita original
2. **Re-adicionar** com valores modificados

```js
ServerEvents.recipes(event => {
  // Remover receita original
  event.remove({ id: 'ufo:dma/quantum_anomaly' })
  
  // Re-adicionar com energia e tempo modificados
  event.custom({
    type: 'ufo:dimensional_assembly',
    item_inputs: [
      {
        ingredient: { item: 'ufo:pulsar_fragment_dust' },
        count: 6  // reduzido de 12
      },
      {
        ingredient: { item: 'ufo:obsidian_matrix' },
        count: 1
      }
    ],
    fluid_inputs: [
      {
        ingredient: { fluid: 'ufo:source_pulsar_fragment_fluid' },
        amount: 4000  // reduzido de 8000
      }
    ],
    item_outputs: [
      {
        id: 'ufo:quantum_anomaly',
        amount: 1
      }
    ],
    fluid_outputs: [],
    energy: 3000000,  // reduzido de 6000000
    time: 600  // reduzido de 1200
  }).id('ufo:dma/quantum_anomaly')
})
```

---

## Referência de IDs de Itens & Fluidos

### IDs Comuns de Itens

| Item | ID |
|------|-----|
| Matriz de Obsidiana | `ufo:obsidian_matrix` |
| Processador Dimensional | `ufo:dimensional_processor` |
| Processador Dimensional Impresso | `ufo:printed_dimensional_processor` |
| Prensa do Processador Dimensional | `ufo:dimensional_processor_press` |
| Lingote de Fragmento de Anã Branca | `ufo:white_dwarf_fragment_ingot` |
| Lingote de Fragmento de Estrela de Nêutrons | `ufo:neutron_star_fragment_ingot` |
| Lingote de Fragmento de Pulsar | `ufo:pulsar_fragment_ingot` |
| Protomatéria | `ufo:proto_matter` |
| Matéria Corpórea | `ufo:corporeal_matter` |
| Matéria de Anã Branca | `ufo:white_dwarf_matter` |
| Matéria de Estrela de Nêutrons | `ufo:neutron_star_matter` |
| Matéria de Pulsar | `ufo:pulsar_matter` |
| Matéria Escura | `ufo:dark_matter` |
| Anomalia Quântica | `ufo:quantum_anomaly` |
| Estrela Nuclear | `ufo:nuclear_star` |
| Esfera de Neutrônio | `ufo:neutronium_sphere` |
| Esfera de Neutrônio Enriquecida | `ufo:enriched_neutronium_sphere` |
| Esfera de Neutrônio Enriquecida Carregada | `ufo:charged_enriched_neutronium_sphere` |
| Cristal de Matéria UU | `ufo:uu_matter_crystal` |
| Sucata | `ufo:scrap` |
| Caixa de Sucata | `ufo:scrap_box` |
| Matéria Instável de Buraco Branco | `ufo:unstable_white_hole_matter` |
| Matéria de Contenção Segura | `ufo:safe_containment_matter` |
| Cápsula de Contenção Aether | `ufo:aether_containment_capsule` |

### Matrizes de Componentes

| Componente | ID |
|-----------|-----|
| Matriz de Componente Phase Shift | `ufo:phase_shift_component_matrix` |
| Matriz de Componente Hyper Dense | `ufo:hyper_dense_component_matrix` |
| Matriz de Componente Tesseract | `ufo:tesseract_component_matrix` |
| Matriz de Componente Event Horizon | `ufo:event_horizon_component_matrix` |
| Matriz de Componente Cosmic String | `ufo:cosmic_string_component_matrix` |

### IDs de Catalisadores

| Catalisador | ID |
|-------------|-----|
| Matterflow T1 | `ufo:matterflow_catalyst_t1` |
| Matterflow T2 | `ufo:matterflow_catalyst_t2` |
| Matterflow T3 | `ufo:matterflow_catalyst_t3` |
| Chrono T1 | `ufo:chrono_catalyst_t1` |
| Chrono T2 | `ufo:chrono_catalyst_t2` |
| Chrono T3 | `ufo:chrono_catalyst_t3` |
| Overflux T1 | `ufo:overflux_catalyst_t1` |
| Overflux T2 | `ufo:overflux_catalyst_t2` |
| Overflux T3 | `ufo:overflux_catalyst_t3` |
| Quantum T1 | `ufo:quantum_catalyst_t1` |
| Quantum T2 | `ufo:quantum_catalyst_t2` |
| Quantum T3 | `ufo:quantum_catalyst_t3` |
| Dimensional (Criativo) | `ufo:dimensional_catalyst` |

### IDs de Fluidos

| Fluido | ID |
|--------|-----|
| Luz Estelar Líquida | `ufo:source_liquid_starlight_fluid` |
| Criotheum Gélido | `ufo:source_gelid_cryotheum` |
| Fluido Temporal | `ufo:source_temporal_fluid` |
| Fluido Espacial | `ufo:source_spatial_fluid` |
| Matéria Primordial | `ufo:source_primordial_matter_fluid` |
| Plasma de Matéria Estelar Bruta | `ufo:raw_star_matter_plasma` |
| Matéria Transcendente | `ufo:transcending_matter` |
| Matéria UU | `ufo:uu_matter` |
| Amplificador UU | `ufo:source_uu_amplifier_fluid` |
| Fluido de Fragmento de Anã Branca | `ufo:source_white_dwarf_fragment_fluid` |
| Fluido de Fragmento de Estrela de Nêutrons | `ufo:source_neutron_star_fragment_fluid` |
| Fluido de Fragmento de Pulsar | `ufo:source_pulsar_fragment_fluid` |

---

## Dicas & Boas Práticas

1. **Escala de energia**: Receitas básicas usam 50K–500K AE. Receitas mid-game usam 1M–10M. Receitas endgame podem usar 50M–500M AE.
2. **Tempo vs Velocidade**: O tempo base é afetado por catalisadores Chrono. Uma receita `time: 200` leva 10 segundos base (200 ticks / 20).
3. **Limite mínimo de tempo**: O DMA impõe um tempo mínimo de processamento de **1 segundo** (20 ticks) independente do empilhamento de catalisadores Chrono.
4. **Entradas sem forma**: Todas as receitas DMA são sem forma — a ordem de colocação dos itens nos 9 slots não importa.
5. **Máximo 9 entradas de item**: O DMA tem 9 slots de entrada, então receitas não podem exceder 9 tipos distintos de itens.
6. **Fluidos de entrada vão para o Slot 3**: Fluidos requeridos pela receita são consumidos do tanque de fluido base (Slot 3), não do tanque de refrigerante (Slot 2).
7. **Refrigerante NÃO faz parte das receitas**: O refrigerante é gerenciado pelo jogador para controle térmico e é completamente independente das definições de receita.

---

## Alternativa via Datapack (JSON)

Se você preferir datapacks ao invés de KubeJS, coloque os arquivos JSON de receita em:

```
data/<seu_namespace>/recipe/<nome_receita>.json
```

Usando o mesmo formato JSON mostrado na [página do DMA](dma.md#formato-json-de-receita).

---

*Veja também: [DMA](dma.md) · [Catalisadores](catalysts.md) · [Materiais & Fluidos](materials.md)*
