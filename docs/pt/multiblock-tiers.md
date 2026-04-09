# Tiers de Multibloco

A linha de multiblocos universais usa um sistema compartilhado de **MK1 / MK2 / MK3** para gate e escalonamento.

## Acesso a Recipes

- maquina **MK1**: recipes MK1
- maquina **MK2**: recipes MK1 e MK2
- maquina **MK3**: recipes MK1, MK2 e MK3

Se o tier da maquina for menor que o tier da recipe, o controller nao inicia.

## Bonus de Tier

Rodar uma recipe antiga em uma maquina melhor da bonus automatico:

- **tempo** cai pela metade a cada tier acima da recipe
- **energia** cai para **75%** a cada tier acima da recipe

Exemplos:

- MK2 rodando recipe MK1 = **2x mais rapido** e **25% menos AE**
- MK3 rodando recipe MK1 = **4x mais rapido** e **43,75% menos AE**

## Por Que Isso Importa

Melhorar o tier nao serve apenas para liberar recipes novas. Tambem comprime linhas antigas em throughput de fundo, o que fica ainda mais importante quando o AE2 comeca a jogar muitos jobs no mesmo controller pelo Quantum Pattern Hatch.
