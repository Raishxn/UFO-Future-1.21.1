# UFO Future 2.0.0-fix4

Resumo publico das mudancas desde `2.0.0-fix3`.

## Destaques

- Adicionada a `Infinity Genesis Cell`, uma celula endgame para AE2 que aprende qualquer recurso inserido e passa a disponibilizar esse recurso como infinito na rede, cobrindo items, fluidos e chemicals compatíveis com o storage do AE.
- `UFO Energy Cell` e `Quantum Energy Cell` agora exportam FE automaticamente para blocos adjacentes e expõem capability de energia para cabos e maquinas externas.
- Adicionada a linha endgame `Astral Nexus`, com set de armadura, render de asas, imunidade absoluta a dano/morte, voo criativo, visao noturna, respiracao aquatica, step assist e limpeza de radiacao do Mekanism quando disponivel.
- Adicionada a `Reality Ripper`, espada endgame com dano extremo e remocao forcada de alvos resistentes.

## Balanceamento e recipes

- Corrigido o matcher do `Dimensional Matter Assembler` para priorizar receitas mais especificas e trocar a recipe em cache quando os inputs mudam.
- Rebalanceado o bootstrap do DMA, melhorando o papel do `Gelid Cryotheum` como coolant inicial e ajustando tempo/output de recipes de progressao.
- Reduzida a resistencia do `Dimensional Matter Assembler` para facilitar reposicionamento no ponto em que a maquina aparece.
- Ajustado o espelhamento de recipes do DMA para o bulk do QMF, permitindo desligar mirrors quando a progressao exige uma rota separada.
- Ajustadas recipes do QMF/DMA para poeiras, rods e materias de `White Dwarf` e `Neutron Star`.
- Removidos mirrors bulk indevidos para a `Thermal Resistor Exosuit` e para o `UFO Staff` quando esses crafts nao devem seguir a rota automatica do DMA.
- `Quantum Cryoforge` agora gera metade do calor base, ficando menos agressivo termicamente.

## Fixes e estabilidade

- Corrigido o comportamento de falha termica do DMA, dos multiblocos universais paralelos e do `Stellar Nexus`: a explosao remove o controller e evita repeticao infinita de efeitos.
- Corrigidas pendencias de recipes/JEI: recipe visivel para `Quantum Cryoforge Controller`, conversao visivel de `Quantum Pattern Provider` via hatch, e recipe atualizada da `Quantum Energy Cell`.
- Ajustada a progressao dos `component matrix batch recipes` do QMF para output de `24`.
- Corrigida a sincronizacao de progresso dos multiblocos paralelos para evitar UI travada enquanto o servidor continuava processando.
- Corrigida a conectividade AE2 dos multiblocos entropicos usando casings AE reais, node multiblock e canal compartilhado por estrutura.
- Reduzido o custo de validacao dos multiblocos entropicos durante load/unload e mudancas de estrutura.
- Corrigidos crashes e revalidacoes recursivas no `Entropic Convergence`, incluindo separacao de `BlockEntityType` e remocao de fluxo que podia recursar durante `onReady`.

## Guide, JEI e dados

- AE2 Guide recebeu notas de perfil termico para `Quantum Processor Assembler` e `Quantum Slicer`.
- Paginas e notas do guide foram ampliadas para DMA, QMF, Quantum Cryoforge e Stellar Nexus, com valores de HU, coolant ladder e comportamento termico.
- JEI do `Stellar Nexus` mostra coolant como `MK1`, `MK2` ou `MK3` e fuel em formato abreviado, mantendo o nome completo no tooltip.
- `runData` passou a ser validado no CI para evitar generated resources desatualizados.
- Workflow de release por tag foi adicionado para publicar artefatos gerados pelo GitHub Actions.

