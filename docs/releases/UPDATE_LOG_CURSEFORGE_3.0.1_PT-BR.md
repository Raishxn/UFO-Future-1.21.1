# UFO Future 3.0.1

Uma versão de desempenho para servidor e cliente, com o RaishxCore 0.2.2.

## Correções

- Requer RaishxCore **0.2.2**: o autocrafting não falha mais com `RaishxCore planning stopped ... status=TIMED_OUT` quando falta material na rede. A busca exata do planejador agora é limitada e devolve o plano com a falta em vez de estourar o prazo.
- O cache de modelos do Stellar Nexus é limpo depois de recarregar recursos (F3+T), em vez de manter os modelos anteriores.

## Desempenho do servidor (TPS)

- Os multiblocos paralelos (QMF, Cryoforge, Slicer, Processor Assembler) não percorrem mais a estrutura inteira a cada tick para achar o Pattern Hatch.
- Com o Quantum Computation Nexus ocupado, o RaishxCore não força mais o AE2 a recalcular a lista de itens em crafting a cada tick.
- Módulos do Nexus, CPUs de crafting em redes sem Grid Link, o auto-start do Stellar Nexus, a Quantum Interface, as células de energia e os módulos de armadura fazem menos trabalho por tick.
- O RaishxCore 0.2.2 planeja pedidos grandes de crafting muito mais rápido.

## Desempenho do cliente (FPS)

- O holograma de estrutura é desenhado de um buffer na GPU: no Stellar Nexus, de 7,15 para 0,83 ms por frame.
- A ferramenta Quantum Wireless não copia mais os dados do item a cada frame.

## Limitação conhecida

O timeout relatado ao pedir mais de três Enhancement Cores ou Infinity Templates ainda não foi reproduzido com a rede afetada. O RaishxCore 0.2.2 elimina o timeout em redes com falta de material, a causa mais provável; se continuar, envie o log novo e a saída de `/raishxcore planner` logo após a falha.

## Requisitos

Minecraft **1.21.1**, NeoForge **21.1.216+**, AE2 **19.2.17 ou 19.2.18** (`[19.2.17, 20)`), AE2 Addon Lib **1.0.3-1.21.1+** e RaishxCore **0.2.2** (`[0.2.2, 1.0)`). Substitua os JARs antigos do UFO e do RaishxCore.
