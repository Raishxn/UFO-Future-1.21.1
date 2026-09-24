# Tiers de multibloco

Os multiblocos universais compartilham os tiers **MK1 / MK2 / MK3**, que controlam o acesso as receitas e os bonus de eficiencia.

## Acesso as receitas

- Maquina **MK1**: executa apenas receitas MK1.
- Maquina **MK2**: executa receitas MK1 e MK2.
- Maquina **MK3**: executa receitas MK1, MK2 e MK3.

O controller nao inicia uma receita que exige um tier maior que o da maquina.

## Bonus de tier

Ao executar uma receita antiga numa maquina de tier superior, para cada tier de diferenca:

- O **tempo** cai pela metade.
- O **custo de AE** cai para **75%** do valor anterior.

Exemplos:

- MK2 executando receita MK1: **2x mais rapido** e **25% menos AE**.
- MK3 executando receita MK1: **4x mais rapido** e **43,75% menos AE**.

## Por que atualizar a maquina?

O upgrade acelera receitas existentes mesmo antes de liberar receitas novas. Isso ajuda quando a AE2 distribui muitos jobs ao mesmo controller por meio de um Quantum Pattern Buffer ou Proxy vinculado.
