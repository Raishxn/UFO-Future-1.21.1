# Catalisadores

Catalisadores são **cartões de upgrade AE2** usados nos 4 slots de upgrade do **Montador de Matéria Dimensional**. Eles modificam o desempenho da máquina — velocidade, custo de energia, chance de falha e chance de drop bônus — mas também afetam a geração de calor.

---

## Famílias de Catalisadores

Existem **4 famílias** com 3 tiers cada (T1 → T3), mais 1 catalisador criativo:

| Família | Efeito Principal | Efeito no Calor |
|---------|-----------------|-----------------|
| **Matterflow** | Reduz custo de energia | Aumenta produção de calor |
| **Chrono** | Aumenta velocidade | Aumenta produção de calor (maior) |
| **Overflux** | Reduz chance de falha | **Diminui** produção de calor (resfriamento) |
| **Quantum** | Aumenta chance de drop bônus | Aumenta produção de calor |
| **Dimensional** | Todos os bônus, crafting instantâneo | Reseta temperatura (Apenas Criativo) |

---

## Estatísticas Detalhadas

### Matterflow — Eficiência Energética

Reduz o custo de energia das receitas do DMA.

| Tier | Efeito na Energia | Multiplicador de Calor | Multiplicador de Buffer |
|------|------------------|----------------------|------------------------|
| T1 | −10,0% Custo de Energia | ×1,5 calor | ×10 buffer |
| T2 | −25,0% Custo de Energia | ×2,0 calor | ×100 buffer |
| T3 | −50,0% Custo de Energia | ×3,0 calor | ×1000 buffer |

### Chrono — Velocidade

Aumenta a velocidade de processamento.

| Tier | Efeito na Velocidade | Multiplicador de Calor | Multiplicador de Buffer |
|------|---------------------|----------------------|------------------------|
| T1 | +25,0% Velocidade | ×2,0 calor | ×10 buffer |
| T2 | +62,5% Velocidade | ×3,5 calor | ×100 buffer |
| T3 | +125,0% Velocidade | ×5,0 calor | ×1000 buffer |

> **Aviso**: Catalisadores Chrono produzem mais calor. Empilhar 4× Chrono T3 resultará em geração extrema de calor. Sempre use refrigerante adequado!

### Overflux — Estabilidade

Reduz a chance de falha e **resfria** a máquina.

| Tier | Efeito na Falha | Multiplicador de Calor | Multiplicador de Buffer |
|------|----------------|----------------------|------------------------|
| T1 | −10,0% Chance de Falha | ×0,5 calor (resfriamento!) | ×1 buffer |
| T2 | −25,0% Chance de Falha | ×0,0 calor (sem calor!) | ×1 buffer |
| T3 | −50,0% Chance de Falha | ×-1,0 calor (resfriamento ativo!) | ×1 buffer |

> **Dica**: Catalisadores Overflux são os únicos que _reduzem_ a geração de calor. Eles podem ser combinados com catalisadores Chrono para compensar penalidades térmicas.

### Quantum — Drops Bônus

Aumenta a chance de drops extras de itens.

| Tier | Efeito de Bônus | Multiplicador de Calor | Multiplicador de Buffer |
|------|----------------|----------------------|------------------------|
| T1 | +10,0% Drop Bônus | ×1,75 calor | ×1 buffer |
| T2 | +25,0% Drop Bônus | ×2,5 calor | ×1 buffer |
| T3 | +50,0% Drop Bônus | ×4,0 calor | ×1 buffer |

---

## Regras de Empilhamento

### Limite Suave
Ao empilhar múltiplos catalisadores do mesmo tipo:

| Quantidade | Efetividade |
|------------|------------|
| 1× | 100% |
| 2× | 175% |
| 3× | 225% |
| 4× | 250% |

### Bônus de Sinergia 4-Stack
Se você preencher todos os 4 slots de upgrade com o **mesmo catalisador**, um bônus especial de sinergia é ativado:

| Família | Bônus de Sinergia | Penalidade de Calor |
|---------|------------------|-------------------|
| **Chrono 4×** | ×2,0 multiplicador adicional de velocidade | ×1,5 calor adicional |
| **Matterflow 4×** | ×0,5 desconto adicional de energia | ×1,5 calor adicional |
| **Quantum 4×** | (Reservado para uso futuro) | ×1,5 calor adicional |
| **Overflux 4×** | Empilhamento padrão | ×1,5 calor adicional |

> **Proteção contra Exploit**: O DMA impõe um tempo mínimo de processamento de **1 segundo** (20 ticks). Empilhar catalisadores Chrono além deste limite apenas aumentará o multiplicador de calor sem reduzir mais o tempo de processamento.

---

## Catalisador Dimensional (Criativo)

O **Catalisador Dimensional** é um item exclusivo do modo criativo que combina todos os efeitos:

- ⚡ Crafting Instantâneo
- 💰 Sem Custo de Energia/Recursos
- 🎁 100% Drop Bônus
- ✅ Nunca Falha
- ❄️ Reseta Temperatura da Máquina

### Fabricação (Receita DMA)
Requer os quatro catalisadores T3 + Matriz de Componente Cosmic String + 10.000 mB de Matéria Transcendente.
- **Energia**: 500.000.000 AE
- **Tempo**: 10.000 ticks (8,3 minutos)

---

## Cálculo do Multiplicador de Calor

O multiplicador total de calor é calculado como:

```
totalHeatMult = 1.0
para cada catalisador nos slots de upgrade:
    totalHeatMult += max(0, catalisador.staticHeat / 100.0)

se sinergia 4-stack ativa:
    totalHeatMult *= 1.5
```

Este multiplicador é aplicado à taxa base de geração de calor (+1 HU a cada 2 ticks).

**Exemplo**: 4× catalisadores Chrono T3:
- Base: `1.0 + (4.0 × 4) = 17.0` → `× 1.5 sinergia = 25.5× geração de calor`
- Isso é **+12,75 HU/s** ao invés do base **+0,5 HU/s**!

---

## IDs dos Catalisadores

| Catalisador | ID de Registro |
|-------------|---------------|
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
| Dimensional | `ufo:dimensional_catalyst` |

---

*Veja também: [DMA](dma.md) · [Progressão](progression.md)*
