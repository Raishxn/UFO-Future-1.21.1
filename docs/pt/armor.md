# Conjuntos de Armadura

UFO Future adiciona dois conjuntos de armadura com mecânicas únicas: o **Exotraje Resistor Térmico** (mid-game, proteção térmica) e a **Armadura UFO** (endgame, modo deus completo com voo).

---

## Exotraje Resistor Térmico

Um conjunto de armadura especializado projetado para proteger contra o dano térmico da zona de perigo do DMA. Este é um pré-requisito para operar configurações de DMA de alta temperatura com segurança.

### Estatísticas & Habilidades (por peça)

| Recurso | Valor |
|---------|-------|
| Armadura Base | Tier padrão de Armadura UFO |
| Imunidade ao Fogo | ✅ Extingue fogo instantaneamente |
| +15% Eficiência de Mineração | Por peça (modificador de atributo) |
| Item Resistente ao Fogo | Não pode ser destruído por fogo/lava |

### Bônus de Conjunto Completo

Quando **todas as 4 peças** (Capacete, Peitoral, Calças, Botas) estão equipadas:

| Bônus | Descrição |
|-------|-----------|
| Resistência II | Redução de dano permanente (amplificador 1) |
| Imunidade a Fogo/Lava | Imunidade completa a dano de fogo e lava |
| Imunidade ao Calor do DMA | Seguro contra dano de queimadura da zona de perigo do DMA |
| Estabilidade Térmica | Proteção térmica total de todas as fontes |

> O Exotraje Resistor Térmico é a **única** armadura que protege do dano térmico do DMA na zona de perigo (≥50% calor).

### Fabricação

#### Blindagem de Resistor Térmico (Material)
**Receita DMA**:
- Lingote de Netherite ×1 + Matriz de Obsidiana ×1
- 500 mB Criotheum Gélido
- 50.000 AE, 100 ticks

#### Peças de Armadura (Receitas DMA)

| Peça | Qtd. Blindagem | Energia | Tempo |
|------|---------------|---------|-------|
| Máscara (Capacete) | 5 | 1.000.000 AE | 500 ticks |
| Peitoral | 8 | 1.500.000 AE | 500 ticks |
| Calças | 7 | 1.200.000 AE | 500 ticks |
| Botas | 4 | 800.000 AE | 500 ticks |

Todas as peças requerem 1.000 mB de Criotheum Gélido como entrada de fluido.

### IDs dos Itens

| Peça | ID de Registro |
|------|---------------|
| Blindagem de Resistor Térmico | `ufo:thermal_resistor_plating` |
| Máscara | `ufo:thermal_resistor_mask` |
| Peitoral | `ufo:thermal_resistor_chest` |
| Calças | `ufo:thermal_resistor_pants` |
| Botas | `ufo:thermal_resistor_boots` |

---

## Armadura UFO (Endgame)

O conjunto de armadura definitivo do UFO Future. Requer o Exotraje Resistor Térmico como base e concede poderes divinos quando vestido como conjunto completo.

### Estatísticas & Habilidades

| Recurso | Valor |
|---------|-------|
| Armadura Base | Tier mais alto do mod |
| **Alimentada por RF** | Cada peça armazena e consome energia RF |
| Barra de Energia | HUD de energia dinâmica em cada peça |
| Nome Arco-Íris | Efeito animado de nome arco-íris |

### Bônus de Conjunto Completo (Com RF Disponível)

Quando **todas as 4 peças** estão equipadas e têm energia RF suficiente:

| Bônus | Descrição |
|-------|-----------|
| **Resistência X** | Quase-invulnerabilidade (amplificador 9) |
| **Visão Noturna** | Visão noturna permanente |
| **Voo Criativo** | Habilidade de voo completo do modo criativo |
| **+40 Vida Máxima** | Adiciona 20 corações extras (+40 HP) |

### Consumo de Energia
- **400 RF por segundo** (20 RF/tick) por peça
- Drenado a cada 20 ticks para evitar spam de som de equipar
- Todos os efeitos são removidos imediatamente quando qualquer peça fica sem energia
- Voo é desabilitado (cai ao chão) quando a energia acaba

### Fabricação (Receitas DMA)

Cada peça requer a peça correspondente do Resistor Térmico como entrada:

| Peça | Entradas | Energia | Tempo |
|------|---------|---------|-------|
| Capacete | Máscara do Resistor Térmico + 2× Esfera de Neutrônio Enriquecida + Anomalia Quântica | 50.000.000 AE | 2.000 ticks |
| Peitoral | Peitoral do Resistor Térmico + 2× Esfera de Neutrônio Enriquecida + Anomalia Quântica | 50.000.000 AE | 2.000 ticks |
| Calças | Calças do Resistor Térmico + 2× Esfera de Neutrônio Enriquecida + Anomalia Quântica | 50.000.000 AE | 2.000 ticks |
| Botas | Botas do Resistor Térmico + 2× Esfera de Neutrônio Enriquecida + Anomalia Quântica | 50.000.000 AE | 2.000 ticks |

Todas as peças requerem 2.000 mB de Matéria Transcendente como entrada de fluido.

### IDs dos Itens

| Peça | ID de Registro |
|------|---------------|
| Capacete | `ufo:ufo_helmet` |
| Peitoral | `ufo:ufo_chestplate` |
| Calças | `ufo:ufo_leggings` |
| Botas | `ufo:ufo_boots` |

---

*Veja também: [Ferramentas](tools.md) · [Progressão](progression.md)*
