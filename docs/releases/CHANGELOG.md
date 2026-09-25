# Changelog

Este arquivo passa a ser a lista principal de bugs corrigidos e implementacoes do UFO Future.

Fluxo combinado daqui para frente:

- Toda correcao entra em `Bug Fixes`
- Toda feature nova entra em `Implementations`
- Tudo novo vai primeiro para `Unreleased`
- Quando fecharmos uma versao, movemos os itens de `Unreleased` para a secao da versao
- Os arquivos [UPDATE_LOG_PT-BR.md](UPDATE_LOG_PT-BR.md) e [UPDATE_LOG_EN.md](UPDATE_LOG_EN.md) podem continuar sendo usados como resumo publico mais elaborado

## [Unreleased]

### Bug Fixes

### Implementations

## [3.0.0-beta.3] - 2026-09-25

### Bug Fixes

- O Wireless Tool agora usa `config/ufo/wireless.toml` como alcance padrão de cada origem, inclusive para vínculos antigos salvos com o padrão de 32 blocos. Alcances escolhidos manualmente continuam persistidos; `wireless.range = 0` remove o limite de distância como documentado.
- As configs do UFO ficam reunidas em `config/ufo/`. Arquivos antigos `ufo-common.toml` e `ufo-server.toml` são migrados ao iniciar, inclusive em `defaultconfigs` e saves, preservando os valores existentes.
- Corrigido o resgate do Reality Anchor após queda no vazio.
- Corrigidos o alinhamento do coolant, da energia e dos tanques na interface do Stellar Nexus.

### Implementations

- Nova interface do controller e da receita Stellar Nexus em JEI/EMI, com espaço para até 81 saídas de itens e 18 de fluidos, nome da simulação no cabeçalho e MK/tempo/energia na borda superior.
- O painel interno do EMI acompanha a altura da receita e posiciona o ícone do controller ao lado, sem cobrir a textura.
- Revisada a wiki de progressão, receitas, equipamentos, multiblocos e wireless em quatro idiomas, incluindo a restauração do chinês simplificado.

## [3.0.0-beta.2] - 2026-09-21

Segundo beta público da linha 3.0, reunindo o polimento e as correções abaixo.

Resumo do ciclo 3.0 (marcos L-0001 a L-0066). O detalhe tecnico por marco esta
em [CHANGELOG_L0001_L0037.md](CHANGELOG_L0001_L0037.md) e no ledger interno do
projeto. IDs, saves, receitas e progressao existentes foram preservados.

### Bug Fixes

- Aceleradores de tempo externos agora avançam o DMA e os demais controladores UFO até o limite
  seguro configurável por tick (64x por padrão, configurável de 1x a 256x), em vez de serem
  descartados pela deduplicação de `gameTime`.
- O renderer do Stellar Nexus restaura a composição visual estável da 2.1-fix7: shell translúcido
  interno em duas passagens para esconder a emenda e estrela opaca para impedir artefatos de ordenação.
- Replace do Structure Scanner agora troca variantes de Field Generator mesmo em estruturas já
  formadas; “Preservar hatches” reconhece somente portas de serviço reais, não todo IMultiblockPart.
- Replace agora devolve diretamente ao jogador cada bloco substituído, sem criar um drop solto
  intermediário que podia ser perdido durante a troca de Field Generators.
- A órbita do Nether no Stellar Nexus foi afastada da estrela pulsante, permanecendo antes da
  órbita do Overworld e dentro da redoma espacial.
- Pacotes compactos criados ao quebrar um controller com buffers grandes agora podem ser
  reinseridos com botão direito em qualquer bloco de uma rede ME ativa; quantidades que não
  couberem permanecem no mesmo pacote, evitando milhares de entidades e perda de recursos.
- Infinity Genesis Cell agora anuncia `Long.MAX_VALUE` por recurso aprendido, removendo o teto de
  2,1 bilhões que impedia planos de crafting grandes.
- Memory Card agora também restaura os cartões de upgrade usando as regras nativas do AE2.
- Jade agora recebe do servidor o fluido real armazenado no ME Massive Fluid Hatch e substitui a
  linha universal desatualizada que exibia `Empty`.
- Corrigida a origem do render central do Stellar Nexus nas quatro orientações e no eixo vertical.
- Quantum Interface agora anuncia suporte aos cartões Crafting, Fuzzy e Induction nos respectivos
  registros de upgrades do AE2/Applied Flux.
- Memory Card do AE2 copia Safe Mode, Overclock e upgrades entre controladores UFO ociosos sem
  copiar estado de processo, inventários, temperatura ou progresso.
- Tickable UFO block entities now bound repeated external ticker calls to the configurable
  `performance.maxExternalAccelerationTicksPerGameTick` budget (default 64, maximum 256),
  preventing a time accelerator from turning one controller into an unbounded
  amount of work in a single Minecraft tick. Unit and GameTests reproduce
  repeated calls with unchanged `gameTime`.
- Cabos AE2 agora conectam em todas as faces do Quantum Pattern Buffer e do ME Massive Fluid Hatch, inclusive na face frontal visível.
- Pacotes de recuperação de recursos agora mostram o recurso e a quantidade, em vez do nome interno `Wrapped Generic Stack`.
- Corrigida a codificação UTF-8 de nomes e tooltips em português brasileiro.
- Encerramento do mundo deixa de reprogramar invalidacoes multibloco ou estados visuais durante o drain de chunks; callbacks de rede AE2 nao promovem mais controllers em despromocao, eliminando a permanencia na tela `Saving World` ao sair de saves com estruturas endgame carregadas.
- Baldes de Liquid Starlight, Primordial Matter, Raw Star Matter Plasma, Transcending Matter, UU Matter, UU Amplifier, Temporal Fluid e Spatial Fluid agora exibem nomes traduzidos em vez das chaves `item.ufo.*_bucket`.
- Itens legados Astral Nexus e Bismuth removidos por completo, incluindo registros, receitas, modelos, traducoes e integracoes obsoletas; o modulo Astral Wings da armadura UFO permanece funcional.
- Quantum Computation Nexus nao captura mais CPUs AE2/de addons colocados no espaco interno; mundos afetados limpam a ownership legada e remontam o cluster normal, inclusive ao desmontar o Nexus.
- Configuracao do UFO agora recebe os eventos de load/reload no mod bus: o custo da Infinity Cell e
  os limites `armor.moduleCaps.*` passam a usar os valores realmente carregados do arquivo.
- Velocidade e inercia de voo agora reaplicam o cap do servidor durante o uso, como os outros 13
  ajustes da armadura; um GameTest reduz `flight_speed` em runtime e prova o clamp efetivo.
- Processos paralelos transacionais: inputs, energia e outputs persistem e sao recuperados apos rede cheia, reload ou quebra, sem perda nem duplicacao.
- Celulas BigInteger: partition/inverter/fuzzy corrigidos, contagem de bytes coerente com o AE2, limites/NBT endurecidos e suporte completo ao Cell Workbench.
- DMA: multiplos requisitos de fluido agora reservam e consomem a soma correta.
- Stellar Nexus: outputs aguardam espaco na rede sem perda, falha termica segura por padrao (grief opt-in), coolant e energia restritos aos ports dedicados.
- State machine explicita nos controllers e invalidacao estrutural por eventos: sem polling periodico em idle, com scans orientados a invalidacao.
- Quantum Grid Link: outputs pendentes sobrevivem a quebra e ao reload fisico de chunk; quebra recupera o saldo exato em pacotes AE2, sem duplicar.
- Ciclo de chunks do trio: despromocao preserva a estrutura com controller sem ticking; unload/reload completo reconstroi o indice e recupera a formacao automaticamente.
- Lookups entre chunks sem tickets na thread do servidor, incluindo chunks materializados despromovidos para desvincular membros retidos durante teardown.
- Mekanism tornou-se opcional real; Pulsar Chambers/caracacas ocultas no criativo, JEI e EMI sem Mekanism, com IDs e receitas preservadas.
- GeckoLib removido do contrato runtime do UFO; mob Apocalypse Type-A removido.
- Warnings javac bloqueiam o build UFO/Core com `-Xlint:all -Werror` (excecao documentada: this-escape de registro NeoForge).

### Implementations

- Structure Scanner agora pode ser vinculado a um Wireless Access Point do AE2, consumir materiais
  da rede durante a construção e selecionar Field Generator MK1, MK2 ou MK3.
- Mega Crafting Storages foram rebalanceados para 4 GiB, 16 GiB, 64 GiB,
  256 GiB e 1 TiB; Mega Co-Processors agora fornecem 4.096, 8.192, 16.384,
  32.768 e 65.536 lanes. Os registry IDs foram preservados para compatibilidade
  de mundos, todos os tiers UFO permanecem acima dos addons suportados e o
  Quantum Computation Nexus continua sendo o caminho para computacao infinita.
- Stellar Nexus simulations now complete in at most four minutes at normal speed (three minutes for Mk2 programs), so an endgame factory delivers its payoff in active play instead of hour-long waits.
- Atualizados os overlays de `general1`, Infinity Fabrication Singularity, multiblocos, QMF e Quantum Computation Nexus com o novo pacote visual e suas animacoes.
- Atualizadas as texturas de `scrap`, `scrap_box`, `quantum_wireless_tool` e das 17 cartas de upgrade UFO; as cartas agora usam tiras animadas autorais de 11 frames.
- Substituidas 26 texturas ativas por arte autoral nova; `dust_blizz` e `dust_cryotheum` agora são tiras animadas autorais de 16x176. Os quatro overlays antigos de nêutrons foram removidos e os sprites-base autorais passaram a ser usados diretamente.
- Removido por completo o conjunto 3D Climber sem consumidor; os modelos `space` e `star` foram substituídos por geometria UV procedural original do UFO.
- Recursos locais `*_backup_original` foram excluídos dos JARs de runtime e sources sem apagar os backups de trabalho.
- Organizadas as texturas de bloco em subpastas por função (`casings`, `crafting` e `machines`), com models, providers e contratos atualizados e sem PNGs soltos na raiz de `textures/block`.
- Runtime multibloco 3.0: definitions compiladas compartilhadas por scan/holograma/JEI, indice estrutural por chunk e fast scan server-side.
- Ports explicitos de energia, fluido, itens e quimicos com planejamento simulate -> commit e ThermalSystem compartilhado entre familias.
- Trio endgame: Quantum Computation Nexus (CPU de crafting compartilhada), Quantum Pattern Fabrication Matrix e Infinity Fabrication Singularity com Quantum Grid Link e lotes que reutilizam catalisadores intactos.
- Diagnostico integrado: `/ufo debug machine` e `/ufo debug perf` com metricas de tick/scan/storage/sync.
- Suite de GameTests de ciclo de vida (26 cenarios) e soak dedicado de 102 estruturas reais por 10.000 ticks, com workflow manual no CI.
- Baseline do soak aguarda chunks ativos, inicializacao AE2 concluida e 40 ticks sem scans; regressao de inicializacao tardia e smoke curto executados no CI.
- RaishxCore 0.1.0-alpha.2 como dependencia versionada (planner de crafting iterativo), com config COMMON testada no grid real e falhas assincronas deterministicas.
- `planner.enabled` e config COMMON de instancia, lida a cada novo pedido: desligada, delega ao AE2; nao cancela calculos ja submetidos. Grafo nao suportado e fila cheia delegam antes da submissao; timeout, cancelamento cooperativo e erros posteriores propagam pelo Future sem retry automatico. Cancelar um Future na fila impede execucao sem diagnostico do worker.
- Fixtures de NBT 2.1 com origem registrada: buffers dos controllers, aliases de energia Stellar e SavedData de celulas UUID/beaco testados em servidor real; validacao de save completo ainda humana.
- Profiling do serverTick do trio e carga ativa dedicada: receitas reais, supply FE finito, outputs bloqueados/drenados e reconexao fisica, com JFR/ledgers exportados e workflow manual. A medicao headless nao substitui TPS/trafego no modpack real.
- GuideME reformulado e CTM nativo nos casings, sem dependencia runtime externa.

## [2.1-fix4] - 2026-05-09

### Bug Fixes

- Nenhuma mudanca ainda.

### Implementations

- Atualizada a licenca do projeto para separar codigo sob LGPLv3+ e assets visuais sob CC BY-NC-SA 3.0, mantendo compatibilidade com o modelo de licenciamento do AE2.
- Adicionados creditos para AE2 Crystal Science e GT New Horizons Modpack por texturas usadas, adaptadas ou usadas como inspiracao.

## [2.1-fix3] - 2026-05-08

### Bug Fixes

- Corrigido o reload das CPUs de crafting UFO do AE2: mega crafting storages e mega co-processors agora tentam reformar/reconectar o multibloco apos carregar no servidor, evitando a necessidade de quebrar e recolocar blocos.
- Corrigida a formatacao dos mega co-processors e mega crafting storages na lista de CPUs do terminal AE2, exibindo valores compactos como `50M`/`1T` em vez de numeros longos ilegiveis.
- Corrigida novamente a textura JEI do `Stellar Nexus`, usando explicitamente o asset `ufo:textures/guis/stellar_nexus_jei.png` em minusculas e removendo a copia duplicada em `assets/ae2` com nome maiusculo.
- Corrigida a renderizacao visual do fluido no `Dimensional Matter Assembler`, usando o fluido presente nos tanques da maquina e mantendo os dois reservatorios visiveis mesmo quando apenas um fluido esta carregado.
- Corrigidas falhas visuais nas juncoes do modelo custom do `Dimensional Matter Assembler`, trocando o modelo para renderizacao `cutout` adequada a textura sem semi-transparencia.

### Implementations

- Reformulado o visual do `Dimensional Matter Assembler` com modelo e textura customizados em Blockbench.
- Adicionado renderer client-side dedicado para exibir o volume de fluido dinamico dentro do `Dimensional Matter Assembler`.

## [2.1-fix2] - 2026-05-05

### Bug Fixes

- Corrigida novamente a tela JEI do `Stellar Nexus`: a categoria deixou de depender de PNG externo para o fundo e agora desenha o layout diretamente em codigo, evitando o fundo preto/rosa mesmo quando o resource pack/cache falha ao resolver a textura.

### Implementations

- Nenhuma mudanca ainda.

## [2.1-fix1] - 2026-05-05

### Bug Fixes

- Corrigida a textura preto/rosa do JEI do `Stellar Nexus`, usando o caminho correto da textura da GUI e desenhando o background explicitamente na categoria JEI.
- Atualizados os nomes dos mega crafting storages e mega co-processors no guia aberto pela tecla `G`, removendo referencias antigas como `Quantum Drive Matrix`, `Tesseract Unit` e `Singularity Accelerator`.
- Ajustado o `Entropic Assembler Matrix` para aceitar apenas patterns compativeis com molecular assembler, evitando que patterns de outros tipos entrem no fluxo errado.

### Implementations

- Adicionado `zh_cn.json` como idioma chines do mod, com as mesmas chaves do `en_us.json`.
- Atualizados os nomes chineses dos mega crafting storages e mega co-processors para acompanhar a nomenclatura atual.
- Adicionadas recipes condicionais do Applied Flux para `Printed Energy Processor` e `Energy Processor`, junto com dependencias runtime opcionais para Applied Flux e MEGA Cells.

## [2.1] - 2026-05-05

### Bug Fixes

- Corrigida a progressao dos `component matrix batch recipes` do QMF: `Tesseract`, `Event Horizon` e `Cosmic String` agora consomem `24` do tier anterior para produzir `1` item, seguindo a regra correta de tier anterior em vez de gerar lotes baratos.
- Corrigida a confusao da primeira montagem do `Quantum Matter Fabricator`: a preview do multibloco agora aponta para `Stellar Field Generator Mk.I or better`, mostrando Mk.I como candidato inicial sem sugerir indevidamente tiers que dependem de `Event Horizon Component Matrix`.
- Corrigida a colisao de recipes dos catalysts T1 no `Dimensional Matter Assembler`: cada familia agora tem um ingrediente assinatura proprio, evitando que AE/automacao transforme pedidos de `Chrono Catalyst T1` em outro catalyst.
- Removidos recipes bulk QMF obsoletos dos catalysts, evitando rotas antigas conflitantes com a progressao atual do DMA.
- Corrigida outra colisao de assinatura em recipes de processo: rods de `White Dwarf Fragment` e `Neutron Star Fragment` nao compartilham mais os mesmos itens das recipes de dust.
- Corrigidos crafts duplicados das ferramentas UFO que compartilhavam o mesmo pattern por pares, evitando resultados ambiguos na crafting table e em patterns AE.
- Auditados os recipes gerados e hand-authored para duplicatas de assinatura e loops de progressao bloqueantes; os ciclos restantes sao conversoes reversiveis ou loops tardios com rota de entrada existente.

### Implementations

- Nenhuma mudanca ainda.

## [2.0.0-fix7] - 2026-04-28

### Bug Fixes

- Corrigido o crash ao abrir a GUI dos controllers universais, incluindo o `Quantum Slicer`, adicionando o include do `toolbox` exigido pelo `UpgradeableScreen` do AE2.
- Corrigida a textura preto/rosa do JEI do `Stellar Nexus`, apontando a categoria para o caminho correto da textura.
- Corrigido o suporte de catalysts no `Quantum Cryoforge`, registrando o controller na lista de maquinas que aceitam os catalysts UFO.
- Adicionada instalacao direta de catalysts com `Shift + botao direito` no controller do multibloco, mantendo a GUI limpa sem inventario do jogador sobreposto.

### Implementations

- Nenhuma mudanca ainda.

## [2.0.0-fix6] - 2026-04-27

### Bug Fixes

- Corrigida a mineracao dos blocos de multiblocos UFO, incluindo o `Quantum Matter Fabricator Controller`, adicionando os controllers, hatches, casings e field generators nas tags de picareta/ferramenta correta.
- Adicionado suporte de wrench AE/tag `c:tools/wrench` para rotacionar controllers direcionais e desmontar controllers de multiblocos com shift + wrench.
- Corrigida a progressao do `Stellar Field Generator MK1`: a receita nao depende mais de hatches/pecas entropicas tardias e agora usa materiais pre-QMF.

### Implementations

- Nenhuma mudanca ainda.

## [2.0.0-fix4] - 2026-04-23

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

- Adicionada a `Infinity Genesis Cell`, objetivo final de storage AE2 que aprende qualquer recurso AE inserido (item, fluido ou chemical compativel) e passa a fornece-lo como infinito na rede.
- `UFO Energy Cell` e `Quantum Energy Cell` agora exportam FE automaticamente para blocos adjacentes em todas as faces e tambem expõem capability de energia para integracao com cabos/maquinas externas.
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

- Resumo detalhado disponivel em [UPDATE_LOG_PT-BR.md](UPDATE_LOG_PT-BR.md) e [UPDATE_LOG_EN.md](UPDATE_LOG_EN.md).

### Bug Fixes

- Consolidado nos arquivos de update log desta versao.

### Implementations

- Consolidado nos arquivos de update log desta versao.
