# Changelog

Este arquivo passa a ser a lista principal de bugs corrigidos e implementacoes do UFO Future.

Fluxo combinado daqui para frente:

- Toda correcao entra em `Bug Fixes`
- Toda feature nova entra em `Implementations`
- Tudo novo vai primeiro para `Unreleased`
- Quando fecharmos uma versao, movemos os itens de `Unreleased` para a secao da versao
- Os arquivos [UPDATE_LOG_PT-BR.md](/C:/Users/erick/OneDrive/Documents/UFO-Future-1.21.1-main/UPDATE_LOG_PT-BR.md) e [UPDATE_LOG_EN.md](/C:/Users/erick/OneDrive/Documents/UFO-Future-1.21.1-main/UPDATE_LOG_EN.md) podem continuar sendo usados como resumo publico mais elaborado

## [Unreleased]

### Bug Fixes

- Corrigido o comportamento de falha termica do `Dimensional Matter Assembler`, dos multiblocos universais paralelos e do `Stellar Nexus`: a explosao agora remove o controller em vez de repetir efeitos infinitos enquanto o bloco resistente continua no mundo.
- Corrigidos os pendentes do PDF de tasks ligados a recipes e JEI: `Quantum Cryoforge Controller` agora tem recipe visivel, `Quantum Pattern Provider` ganhou conversao recipe visivel com o hatch, e o `Quantum Energy Cell` foi atualizado para usar `Cosmic String Component Matrix` e `Dark Matter`.
- Ajustada a progressao dos `component matrix batch recipes` do QMF para output de `24`, evitando que `Tesseract`, `Event Horizon` e `Cosmic String` continuem baratos demais no bulk craft.

- Corrigido o matcher do `Dimensional Matter Assembler` para priorizar receitas mais especificas e trocar a recipe em cache quando os inputs mudam, evitando que a craft de `neutron star fragment ingot` execute a recipe de `white dwarf fragment rod`.
- Rebalanceado o coolant inicial do DMA: `gelid cryotheum` agora resfria com eficiencia muito maior, e as recipes de bootstrap/producao inicial foram aceleradas e tiveram output ajustado para sustentar melhor a progressao.
- Reduzida a resistencia do `Dimensional Matter Assembler` para facilitar reposicionamento sem wrench e evitar que a maquina pareca dura demais para o stage em que aparece.
- CI agora valida `runData` e falha quando `src/generated/resources` estiver desatualizado, reduzindo risco de recipes corrigidas ficarem de fora do artefato publicado.
- Adicionado workflow de release por tag para publicar artefatos gerados pelo GitHub Actions, melhorando a reprodutibilidade dos proximos releases.
- Corrigida a sincronizacao de progresso dos multiblocos paralelos no controller, evitando casos em que crafts de fluidos continuavam processando no servidor enquanto a tela ficava travada em valores como `0.0/15.0s`.
- Corrigida a conectividade AE2 dos multiblocos entropicos para seguir o padrao do `ExtendedAE`, com casings AE reais, node multiblock e canal compartilhado por estrutura.
- Revertida a troca indevida das texturas antigas de `entropy_assembler_core_casing`, `entropy_assembler_core_casing_base`, `entropy_computer_condensation_matrix` e `entropy_singularity_casing`, preservando os outros multiblocos que dependiam delas.
- Corrigido o clique direito dos blocos entropicos para so interceptar uso quando o multibloco estiver completo e conectado, permitindo colocar varios blocos seguidos com a mao vazia sem travar a montagem.
- Removido o rescan quente por tick dos `Entropic Casing`, trocando a montagem para revalidacao por evento e definicao unica de anchor da estrutura, reduzindo custo de TPS e sincronizando melhor a conexao AE2.
- Hotfix de estabilidade: removida a revalidacao automatica em `onReady()` dos `Entropic Casing`, evitando tempestade de scans e updates durante load/unload de chunks e entrada/saida do mundo.
- Migrado o `Entropic Convergence` para uma base real de `CraftingBlockEntity`, aproximando o comportamento do `AdvancedAE/ExtendedAE`, removendo o ticker custom dessa estrutura e permitindo que a casing entre no fluxo real de CPU do AE2.
- Hotfix de crash no load: corrigido o `BlockEntityType` usado pelo `Entropic Convergence`, separando corretamente o caminho da `Entropic Convergence Casing` e o caminho legado para evitar `Invalid block entity state` ao entrar no mundo.
- Reduzido o custo de load do `Entropic Convergence`: removida a marcacao automática de `structureDirty` em `onReady()` e eliminada uma consulta duplicada de `getStorageBytes()` no mixin da CPU do AE2.
- Refeito o fluxo do `Entropic Convergence` para seguir mais de perto o padrao do `AdvancedAE/AE2`: calculadora estrutural propria para o cubo `7x7x7`, cluster real de CPU do AE2 com `GridCraftingCpuChange`, e atualizacao por `onReady` e `neighborChanged` em vez da validacao preguiçosa por getter.
- Hotfix de crash na inicializacao do `Entropic Convergence`: removido o invoker que tentava atravessar `AEBaseBlockEntity.onReady()` e acabava entrando em recursao infinita durante o load do mundo.
- Corrigido o gatilho de formacao do `Entropic Convergence`: mudancas nos `Stellar Field Generator` agora tambem notificam a CPU entropica, permitindo que a estrutura entre no grid quando o ultimo bloco colocado estiver no interior.

### Implementations

- Adicionadas paginas e notas mais completas no guide do AE2 para `DMA`, `QMF`, `Quantum Cryoforge` e `Stellar Nexus`, incluindo valores de `HU`, ladder de coolant e perfil termico.
- Movido o `UFO Staff` para a linha de recipe universal do `QMF`, alinhando a progressao dele com a UFO suit e removendo o craft direto no `DMA`.

- Ajustada a exibicao do JEI do `Stellar Nexus` para mostrar o coolant apenas como `MK1`, `MK2` ou `MK3`, com tooltip exibindo o nome completo do fluido.
- Ajustada a exibicao do JEI do `Stellar Nexus` para mostrar o fuel em formato abreviado dentro do box, com tooltip exibindo o nome completo do combustivel e a quantidade.
- Iniciada a base dos multiblocos `Entropic Convergence Engine` e `Entropic Assembler Matrix`, com PRD finalizado, notas tecnicas de estabilidade/performance e validador frio para cubo `7x7x7` com interior `5x5x5` preenchido por fields uniformes.
- Criados os novos blocos exclusivos `Entropic Assembler Casing` e `Entropic Convergence Casing`, que agora sao as unicas cascas validas desses dois multiblocos, com receitas, modelos e texturas proprias.
- Alinhada a interacao para abrir a GUI por qualquer bloco da estrutura formada e conectada, sem controller visivel no shell.
- Reduzido o custo de revalidacao estrutural, mantendo rescan periodico mais lento quando montado e resposta imediata via dirty flag quando a estrutura muda.

## [2.0.0]

### Notes

- Resumo detalhado disponivel em [UPDATE_LOG_PT-BR.md](/C:/Users/erick/OneDrive/Documents/UFO-Future-1.21.1-main/UPDATE_LOG_PT-BR.md) e [UPDATE_LOG_EN.md](/C:/Users/erick/OneDrive/Documents/UFO-Future-1.21.1-main/UPDATE_LOG_EN.md).

### Bug Fixes

- Consolidado nos arquivos de update log desta versao.

### Implementations

- Consolidado nos arquivos de update log desta versao.
