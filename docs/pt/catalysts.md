# Catalisadores

O **DMA, QMF, Quantum Slicer, Quantum Processor Assembler e Quantum Cryoforge** aceitam catalisadores em quatro slots de upgrade. Existem quatro familias com tres tiers cada, alem do Catalisador Dimensional.

| Familia | Efeito principal | T1 / T2 / T3 |
|---|---|---|
| Matterflow | Reduz custo de energia da receita | −10% / −25% / −50% por carta |
| Chrono | Acelera processamento | +25% / +62,5% / +125% de velocidade por carta |
| Quantum | Chance de resultado extra | +10% / +25% / +50% por carta |
| Overflux | Modificador termico | Calor estatico negativo; depende do controller |

Matterflow e Chrono tambem multiplicam o buffer de energia por **10 / 100 / 1.000** nos tiers T1 / T2 / T3. Combinar catalisadores fortes pode aumentar muito o calor. Quatro cartas identicas ativam uma sinergia propria da familia e uma penalidade termica adicional.

## Overflux e calor

A antiga wiki dizia que Overflux reduzia uma chance de falha e resfriava ativamente o DMA. O codigo atual de processamento nao tem a mecanica correspondente de chance de falha. O DMA limita cada contribuicao negativa de calor a zero; portanto Overflux **nao** resfria ativamente o DMA. Multiblocos paralelos incluem o valor negativo no perfil termico combinado; quatro cartas Overflux reduzem ainda mais esse perfil.

## Catalisador Dimensional

Este item e **fabricavel no QMF MK3**, apesar do antigo nome de “catalisador criativo” na tooltip. Ele acelera fortemente o processamento, elimina os custos de energia e calor do processo e garante bonus de saida. **Os ingredientes da receita continuam sendo consumidos.** Veja a receita atual e cara no JEI/EMI.

Use o painel de status do controller para avaliar os efeitos combinados de velocidade, energia e calor antes de automatizar continuamente.

*Veja tambem: [DMA](dma.md) · [Linha de multiblocos](multiblock-line.md)*
