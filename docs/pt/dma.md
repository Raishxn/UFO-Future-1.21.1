# Montador de Matéria Dimensional (DMA)

O **Montador de Matéria Dimensional** é a máquina principal do UFO Future. Todos os itens avançados, fluidos, materiais e componentes do mod são fabricados através do DMA. É uma máquina alimentada por AE2 com entradas de itens sem forma, entradas de fluidos, sistema de refrigeração e suporte para upgrades de catalisador.

---

## Fabricando o DMA

```
O P O
C M C
O E O
```

| Símbolo | Item |
|---------|------|
| O | Matriz de Obsidiana |
| P | Processador de Engenharia AE2 |
| C | Invólucro Banhado a Gráviton |
| M | Bloco Controlador AE2 |
| E | Olho de Ender |

> **Pré-requisito**: Você precisa de acesso ao AE2 e a um Invólucro Banhado a Gráviton primeiro.

---

## Visão Geral da GUI

A GUI do DMA tem o seguinte layout:

| Seção | Descrição |
|-------|-----------|
| **Grade de 9 Slots** | Entradas de itens sem forma (ordem não importa) |
| **2 Slots de Saída** | Saídas de itens/fluidos aparecem aqui |
| **Tanque de Refrigerante** (Slot 2) | Fluido refrigerante gerenciado pelo jogador — NÃO consumido pelas receitas |
| **Tanque de Fluido Base** (Slot 3) | Entrada de fluido exigido pela receita — consumido pelas receitas |
| **4 Slots de Upgrade** | Para cartões de upgrade de Catalisador |
| **Barra de Progresso** | Mostra o progresso do processamento |
| **Display de Energia** | Mostra a energia AE armazenada e a taxa de consumo |
| **Medidor de Temperatura** | Mostra o nível de calor atual vs capacidade máxima |

---

## Como Funciona

1. **Coloque itens** na grade de 9 slots. **A ordem não importa** — todas as receitas são sem forma.
2. **Adicione fluido base** ao tanque de fluido direito (Slot 3) se a receita exigir.
3. **Adicione refrigerante** ao tanque de fluido esquerdo (Slot 2) para prevenir superaquecimento.
4. **Insira catalisadores** nos 4 slots de upgrade para modificar o desempenho.
5. **Conecte energia AE2** — o DMA é uma entidade de bloco conectada à rede AE2. Ele puxa energia da rede ME ou do buffer interno.
6. A máquina começa a processar quando uma receita válida é encontrada e energia suficiente está disponível.

---

## Sistema Térmico

O DMA gera calor durante o processamento e deve ser resfriado para prevenir falha catastrófica.

### Zonas de Calor

| Zona | Limite | Efeitos |
|------|--------|---------|
| **Segura** | < 50% capacidade | Operação normal |
| **Perigo** | ≥ 50% capacidade | Anéis de partículas de chama giratórias, queima jogadores próximos sem armadura térmica |
| **Colapso** | 100% capacidade | Contagem regressiva de 5 segundos, depois **explosão** destruindo a máquina e blocos ao redor |

### Geração de Calor
- **Trabalhando**: +1 HU a cada 2 ticks (≈ +10 HU/s base), multiplicado pelos modificadores de calor dos catalisadores
- **Ociosa**: Resfriamento passivo −1 HU a cada 40 ticks (≈ −0.5 HU/s)

### Eficiência do Refrigerante

| Refrigerante | Eficiência | Notas |
|--------------|-----------|-------|
| **Fluido Temporal** | 100 HU por 1 mB | Mais eficiente — refrigerante endgame |
| **Luz Estelar Líquida** | 30 HU por 1 mB | Boa opção mid-game |
| **Fluidos padrão** | 15 HU por 1 mB | Qualquer fluido não reconhecido |
| **Criotheum Gélido** | 1 HU por 200 mB | Baseado em volume — precisa de bombeamento em massa |

### Explosão de Colapso
Quando a temperatura atinge 100% da capacidade máxima:
1. Uma **contagem regressiva de 5 segundos** começa (100 ticks)
2. **Som de alarme** toca a cada segundo
3. **Aviso vermelho na barra de ação** aparece para jogadores próximos: `"SOBRECARGA CRÍTICA EM X SEGUNDOS!"`
4. No zero, a máquina **explode** (poder 10.0, quebra blocos)
5. Um **alerta global no chat** transmite as coordenadas da explosão para todos os jogadores no servidor

> **Exotraje Resistor Térmico** fornece imunidade total ao dano térmico do DMA. Veja [Armadura](armor.md).

---

## Formato JSON de Receita

Todas as receitas do DMA usam o tipo `ufo:dimensional_assembly`. O formato JSON é:

```json
{
  "type": "ufo:dimensional_assembly",
  "item_inputs": [
    {
      "ingredient": { "item": "minecraft:netherite_ingot" },
      "count": 2
    },
    {
      "ingredient": { "item": "minecraft:blue_ice" },
      "count": 4
    }
  ],
  "fluid_inputs": [
    {
      "ingredient": { "fluid": "ufo:source_liquid_starlight_fluid" },
      "amount": 250
    }
  ],
  "item_outputs": [
    {
      "id": "ufo:white_dwarf_fragment_ingot",
      "amount": 1
    }
  ],
  "fluid_outputs": [],
  "energy": 100000,
  "time": 200
}
```

Veja a página [Receitas KubeJS](kubejs-recipes.md) para criação de receitas via script.

---

## Consumo de Energia

- Buffer interno base: **500.000 AE**
- **Catalisadores Matterflow** podem aumentar o buffer em 10x/100x/1000x por catalisador
- O consumo de energia é calculado por tick baseado em `energia / (tempo_receita / fator_velocidade)`
- **Multiplicador de Energia** dos catalisadores reduz o custo efetivo

---

## Auto-Exportação

O DMA suporta **auto-exportação** via configuração do gerenciador de config. Quando habilitada, as saídas finalizadas são automaticamente enviadas para inventários adjacentes/armazenamento ME.

---

*Veja também: [Catalisadores](catalysts.md) · [Receitas KubeJS](kubejs-recipes.md) · [Progressão](progression.md)*
