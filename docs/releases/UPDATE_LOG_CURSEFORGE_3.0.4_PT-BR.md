# UFO Future 3.0.4

Corrige travamentos do servidor em jobs grandes de crafting com mega co-processadores.

## Correções

- **Travamento do servidor em jobs grandes.** CPUs de crafting com mega co-processadores podiam gastar um tick inteiro empurrando padrões para alvos lentos, como máquinas Mekanism, até o watchdog derrubar o servidor. Agora as CPUs de crafting do AE2 dividem um orçamento de tempo por tick; o restante continua no tick seguinte, e cada CPU ainda faz alguns pushes por tick. Jobs com alvos lentos podem demorar mais para terminar.
- **Nova opção de servidor** `performance.craftingCpuTickBudgetMillis` (padrão **10**, de 1 a 1000). Valores menores favorecem o TPS; maiores, a velocidade de crafting.
- **JEI:** páginas de estrutura de multibloco agora podem ser codificadas no AE2 como padrões de processamento, com os blocos da estrutura como entradas e o controlador como saída.

## Requisitos

Minecraft **1.21.1**, NeoForge **21.1.216+**, AE2 **19.2.17 ou 19.2.18** (`[19.2.17, 20)`), AE2 Addon Lib **1.0.3-1.21.1+** e RaishxCore **0.2.3** (`[0.2.3, 1.0)`). Substitua o JAR antigo do UFO e atualize o RaishxCore para 0.2.3, que também traz a correção do planner para AE2 Lightning Tech.
