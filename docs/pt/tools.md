# Ferramentas & Armas

UFO Future introduz um **sistema de multi-ferramenta energética transformável**. Todas as ferramentas compartilham um único item alimentado por RF que pode se transformar entre diferentes tipos de ferramenta usando o ciclo da roda do mouse.

---

## O Sistema Multi-Ferramenta

Todas as ferramentas UFO implementam a interface `IEnergyTool` e compartilham estas características:

- **Alimentadas por RF**: Cada ferramenta consome energia RF por uso ao invés de durabilidade
- **Nome Arco-Íris**: Nomes das ferramentas exibem com efeito de cor arco-íris animado
- **Transformável**: Segure a ferramenta e role (Shift+Scroll) para ciclar entre todos os tipos
- **Energia Persistente**: A energia é preservada ao transformar entre tipos (usa `transmuteCopy`)
- **Barra de Energia**: Uma barra de energia dinâmica é exibida no item

---

## Ordem do Ciclo de Ferramentas

O ciclo de transformação passa por estas ferramentas em ordem:

| # | Ferramenta | Tipo | Recurso Especial |
|---|-----------|------|-----------------|
| 1 | Cajado UFO | Corpo a Corpo | Ferramenta base — ponto de entrada |
| 2 | Espada UFO | Espada | Combate padrão |
| 3 | Picareta UFO | Picareta | Toggle Auto-Fundição (Shift+Clique Direito) |
| 4 | Machado UFO | Machado | Derruba árvores |
| 5 | Pá UFO | Pá | Criação de caminhos |
| 6 | Enxada UFO | Enxada | Auto-replantio de colheitas (área 3x3) |
| 7 | Martelo UFO | Picareta+ | Mineração em área (1×1 / 3×3 / 5×5 / 7×7) |
| 8 | Grande Espada UFO | Espada | Dano pesado, mais lenta |
| 9 | Vara de Pesca UFO | Vara de Pesca | Pesca alimentada por energia |
| 10 | Arco UFO | Arco | Toggle modo tiro rápido |

---

## Detalhes Individuais das Ferramentas

### Cajado UFO
- **Tipo**: Arma corpo a corpo
- **Ataque**: Dano padrão do tier UFO
- **ID**: `ufo:ufo_staff`

### Espada UFO
- **Tipo**: Espada
- **Ataque**: +5 dano de ataque, −2,4 velocidade de ataque
- **Energia por uso**: Padrão
- **ID**: `ufo:ufo_sword`

### Picareta UFO
- **Tipo**: Picareta
- **Ataque**: +1 dano de ataque, −2,8 velocidade de ataque
- **Especial**: Modo **Auto-Fundição** — ative com `Shift+Clique Direito`. Quando ativo, minérios minerados são automaticamente fundidos em lingotes.
- **Energia por uso**: 50 RF/bloco
- **ID**: `ufo:ufo_pickaxe`

### Machado UFO
- **Tipo**: Machado
- **Ataque**: +6 dano de ataque, −3,2 velocidade de ataque
- **Energia por uso**: Padrão
- **ID**: `ufo:ufo_axe`

### Pá UFO
- **Tipo**: Pá
- **Ataque**: +1,5 dano de ataque, −3,0 velocidade de ataque
- **Energia por uso**: Padrão
- **ID**: `ufo:ufo_shovel`

### Enxada UFO
- **Tipo**: Enxada
- **Ataque**: +0 dano de ataque, −3,0 velocidade de ataque
- **Especial**: Lavra uma **área 3×3** quando usada. Auto-replanta colheitas se aplicável.
- **ID**: `ufo:ufo_hoe`

### Martelo UFO ⭐
- **Tipo**: Ferramenta de mineração em área (baseada em Picareta)
- **Ataque**: +7 dano de ataque, −3,4 velocidade de ataque
- **Energia por uso**: 50 RF **por bloco** (incluindo blocos da área)
- **Modos de Área**: Cicle com tecla de modo
  - 1×1 (padrão)
  - 3×3
  - 5×5
  - 7×7
- **Auto-Fundição**: Suporta toggle de auto-fundição — todos os blocos da área são fundidos se ativo
- **Energia Inteligente**: Para de quebrar blocos da área quando a energia acaba
- **ID**: `ufo:ufo_hammer`

### Grande Espada UFO
- **Tipo**: Espada pesada
- **Ataque**: +8 dano de ataque, −3,0 velocidade de ataque
- **ID**: `ufo:ufo_greatsword`

### Vara de Pesca UFO
- **Tipo**: Vara de pesca
- **Durabilidade**: 500 (ainda usa durabilidade)
- **ID**: `ufo:ufo_fishing_rod`

### Arco UFO
- **Tipo**: Arco
- **Durabilidade**: 5000
- **Especial**: Toggle de modo **Tiro Rápido** — permite puxar o arco instantaneamente
- **ID**: `ufo:ufo_bow`

---

## Fabricação

O Cajado UFO é o ponto de entrada do sistema de ferramentas. Todas as outras formas são acessadas via transformação.

### Cajado UFO (Receita Primária)
**Receita DMA**:
- **Entradas**: Esfera de Neutrônio Enriquecida Carregada ×1, Anomalia Quântica ×2, Protomatéria ×2, Lingote de Netherite ×4, Bola de Matéria AE2 ×16
- **Fluido**: 2.000 mB de Luz Estelar Líquida
- **Energia**: 12.000.000 AE
- **Tempo**: 2.400 ticks (2 minutos)

### Cajado UFO (Receita Alternativa)
**Receita DMA**:
- **Entradas**: Espada de Netherite, Picareta de Netherite, Machado de Netherite, Enxada de Netherite, Lingote de Fragmento de Estrela de Nêutrons
- **Energia**: 500.000 AE
- **Tempo**: 600 ticks (30 segundos)

---

## IDs das Ferramentas

| Ferramenta | ID de Registro |
|-----------|---------------|
| Cajado UFO | `ufo:ufo_staff` |
| Espada UFO | `ufo:ufo_sword` |
| Picareta UFO | `ufo:ufo_pickaxe` |
| Machado UFO | `ufo:ufo_axe` |
| Pá UFO | `ufo:ufo_shovel` |
| Enxada UFO | `ufo:ufo_hoe` |
| Martelo UFO | `ufo:ufo_hammer` |
| Grande Espada UFO | `ufo:ufo_greatsword` |
| Vara de Pesca UFO | `ufo:ufo_fishing_rod` |
| Arco UFO | `ufo:ufo_bow` |

---

*Veja também: [Armadura](armor.md) · [Progressão](progression.md)*
