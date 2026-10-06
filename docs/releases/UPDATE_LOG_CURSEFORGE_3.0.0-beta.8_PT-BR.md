# UFO Future 3.0.0-beta.8

## Melhorias

- Aumentado o throughput da Quantum Pattern Fabrication Matrix. A Matrix agora suporta até **16 lotes de receitas simultâneos com Fields MK1, 32 com MK2 e 64 com MK3**, em vez de esperar um lote terminar antes de aceitar outro.
- A Matrix pode devolver até **16 / 32 / 64 lotes ao armazenamento ME por tick**, conforme o tier dos Fields. Crafts iguais continuam sendo executados em lote.
- Tarefas de uma única cópia também usam o agendador agregado, permitindo avanço junto às tarefas em lote.

## Correções

- Corrigido o Quantum Grid Link perdendo a referência ao controlador ao carregar dados salvos. A entrega de lotes da Matrix mantém o throughput do tier após recarregar o mundo.
- Resultados pendentes continuam salvos e são reenviados quando o armazenamento ME está cheio ou aceita apenas parte de um lote.

## Validação

- Adicionados testes de regressão para os três tiers, lotes de um milhão de cópias, armazenamento cheio, entrega parcial, recuperação de lotes salvos e contabilidade exata de ingredientes e resultados.

## Requisitos

- Minecraft **1.21.1**, NeoForge **21.1.x**, Applied Energistics 2 **19.2.17 ou 19.x compatível** e RaishxCore **0.2 ou 0.x compatível** (`[0.2, 1.0)`).
- Esta continua sendo uma versão **beta**. Sem mudanças de receitas ou progressão em relação à beta.7.
- A velocidade efetiva depende das operações da CPU de crafting, ingredientes disponíveis, energia da rede e espaço para resultados.
