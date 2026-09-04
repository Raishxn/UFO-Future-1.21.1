# UFO Future 3.0 — Continuity Ledger

Este arquivo é a memória operacional da reestruturação 3.0. Ele deve ser atualizado em toda etapa concluída, antes do encerramento do trabalho.

## Regras do ledger

- Registrar fatos verificados, não intenções vagas.
- Para cada correção: problema, causa, solução, arquivos e validação.
- Não marcar algo como corrigido sem build/teste proporcional ao risco.
- Manter pendências e riscos conhecidos visíveis.
- Preservar compatibilidade de saves e IDs salvo quando uma migração estiver documentada.

## Estado atual

- Linha-base: UFO Future `2.1-fix7`, Minecraft 1.21.1, NeoForge, Java 21.
- Objetivo: UFO Future 3.0 com arquitetura modular, multiblocos eficientes, visualização sem LDLib e progressão overkill segura.
- Build da linha-base: `./gradlew compileJava` aprovado em 2026-08-31, com 16 warnings.
- Testes automatizados: infraestrutura JUnit 5 adicionada; 146 testes ativos após a organização de resources do L-0037.
- Documento de auditoria: `.project-control/audits/auditoria-ufo-3.0.md`.
- L-0016–L-0023 e L-0025–L-0038 concluídos e aprovados; L-0024 concluído com validação automatizada. O L-0034 organizou assets/repositório e reformulou o GuideME, cuja hierarquia e cenas 3D receberam aprovação visual; o L-0035 removeu separadamente o protótipo tutorial/Ponder legado sem tocar no GuideME; o L-0036 integrou CTM nativo aos dois casings sem dependência runtime externa; o L-0037 resolveu owners duplicados e removeu árvores de textura legadas sem mover paths ativos; o L-0038 validou o coolant externo do ME Massive Fluid Hatch. O AE2 voltou a controlar integralmente seu lifecycle, células/upgrades UFO permaneceram funcionais, bônus de armadura preservam ownership/cadência segura, o hazard quente do DMA ganhou orçamento explícito, o runtime dos controllers paralelos tornou o idle praticamente sem custo, passou a enviar snapshot visual compacto e agora agrega transferências de storage por chave, e a invalidação/varredura estrutural deixou de fazer polling, busca quadrática e relink indiscriminado. O UI-0001 teve visual, interação e persistência aprovados; o follow-up autorizado de widgets Safe/OC/lock também foi aprovado, mas a trilha geral de telas continua pausada. Viewer JEI, seleção/alternatives e auto-build foram confirmados funcionando perfeitamente.

## Decisões arquiteturais vigentes

1. A migração será incremental; não haverá rewrite total sem checkpoints executáveis.
2. Segurança e integridade de recursos precedem novas mecânicas.
3. Uma única definição deverá alimentar scan, formação, holograma, tutorial e JEI/EMI.
4. LDLib será removido; os renderizadores próprios existentes serão aproveitados.
5. UFO Core começará como módulos internos e só será publicado separadamente após existir um segundo consumidor real.
6. Números extremos serão transações agregadas com matemática segura, não loops proporcionais à quantidade.

## Marcos concluídos

### L-0001 — Auditoria técnica inicial

**Data:** 2026-08-31
**Status:** concluído

**O que foi feito**

- Inventário dos repositórios UFO, AE2 Lightning Tech e Thunderbolt Core.
- Revisão dos caminhos críticos de multiblocos, ticks, coolant, AE2 storage, rede, células BigInteger, DMA, armaduras, mixins e repositório.
- Comparação do modelo de template/scanner/preview do AE2LT e da disciplina de API/testes do Thunderbolt Core.
- Verificação do NeoECO como referência conceitual e de suas restrições LDLib/GPL.
- Compilação da UFO para estabelecer uma linha-base verificável.

**Achados principais**

- 7 bloqueadores P0, 15 problemas P1 e outras dívidas categorizadas.
- Risco de perda de outputs e buffers internos.
- Pacotes de máquina sem autorização suficiente.
- Partition e contabilização das células BigInteger incorretos.
- DMA permite matching incorreto de múltiplos fluid requirements.
- Trabalho excessivo por tick e sincronização de NBT volumosa.
- Quatro modelos de multibloco coexistentes.

**Artefatos**

- `.project-control/audits/auditoria-ufo-3.0.md`

**Validação**

- `./gradlew compileJava`: sucesso.
- `git diff --check`: sucesso para o relatório.

## Marcos concluídos — continuação

### L-0002 — Segurança dos pacotes de máquina

**Data:** 2026-08-31
**Status:** concluído

**Problema**

Pacotes Stellar e universais aceitam um `BlockPos` arbitrário carregado e podem operar controllers remotos sem confirmar menu, distância ou limitar spam.

**Causa**

Cada payload fazia sua própria validação incompleta. Em geral bastava o cliente enviar um `BlockPos` de chunk carregado; não existia vínculo obrigatório entre payload, menu aberto e BE alvo, nem limitação de frequência.

**Como foi corrigido**

- Criado `MachinePacketGuard` como ponto único de autorização server-side.
- Uma ação agora exige `ServerPlayer`, menu correto aberto, host do menu na mesma posição, mesmo BE ainda presente no mundo, `stillValid`, distância máxima de 8 blocos e chunk já carregado.
- Criado `MachinePacketRateLimiter`, baseado em game ticks, isolado por jogador e ação, com poda periódica de entradas antigas.
- Scans têm intervalo mínimo de 20 ticks; toggles e start têm limites próprios sem bloquear botões diferentes entre si.
- `PacketChangeStellarRecipe` agora rejeita IDs ausentes e receitas de tipo diferente de `StellarSimulationRecipe`.
- Ordinais inválidos de side config são rejeitados antes de uso.
- Todos os payloads Stellar, universais e o stub de side config do DMA foram migrados para a guarda.

**Arquivos principais**

- `src/main/java/com/raishxn/ufo/network/MachinePacketGuard.java`
- `src/main/java/com/raishxn/ufo/network/MachinePacketRateLimiter.java`
- `src/main/java/com/raishxn/ufo/network/packet/Packet*Stellar*.java`
- `src/main/java/com/raishxn/ufo/network/packet/Packet*Universal*.java`
- `src/main/java/com/raishxn/ufo/network/packet/PacketChangeSideConfig.java`
- `src/test/java/com/raishxn/ufo/network/MachinePacketRateLimiterTest.java`
- `build.gradle`

**Validação**

- `./gradlew compileJava --stacktrace`: sucesso.
- `./gradlew test --stacktrace`: sucesso; 4 testes do rate limiter aprovados.
- `git diff --check`: sucesso.

**Limites conscientes**

- A guarda impede controle remoto/spam e exige posse do menu atual. Ownership/team/claim é uma política de gameplay ainda inexistente nos controllers e será adicionada à nova fundação, não improvisada no transporte.
- `PacketChangeSideConfig` permanece um no-op funcional; agora está protegido e valida entradas, mas a mecânica de lados ainda precisa ser implementada.

## Marcos concluídos — continuação

### L-0003 — Integridade transacional dos processos paralelos

**Data de início:** 2026-08-31
**Status:** concluído

**Problema**

Os controllers paralelos podem apagar buffers ao perder a rede/receita, descartar outputs que a ME não aceitou e perder recursos ao serem quebrados durante um processo.

**Plano verificável**

- Persistir a identidade exata das entradas consumidas e das saídas já sorteadas.
- Pausar processos em falhas transitórias, sem limpar estado.
- Inserir outputs parcialmente e conservar o restante como pendente.
- Reembolsar buffers com segurança quando o processo for cancelado ou o controller removido.
- Cobrir a lógica de contabilidade com testes e preparar cenários de validação dentro do jogo.

**O que foi implementado**

- `ParallelProcessState` agora persiste as chaves AE2 exatas e quantidades de cada entrada já retirada.
- Saídas são sorteadas/preparadas uma única vez e persistidas como pendentes; inserções parciais removem somente a quantidade aceita pela grid.
- Perda de nó/grid e redução temporária de tier agora pausam o processo em vez de apagá-lo.
- Receita removida tenta devolver entradas e energia; químicos retornam primeiro à grid e depois aos hatches compatíveis.
- Quebrar o controller tenta reembolsar a grid e ejeta qualquer saldo exato em pacotes genéricos de recuperação do AE2.
- Buffers redimensionados preservam valores existentes em vez de zerá-los.
- O matching de autocrafting passou a validar inputs mesmo quando somente uma receita possui o output solicitado, impedindo transmutação por padrão inválido.
- Inputs iguais vindos de vários counters são agregados com soma saturada; o teste de ingrediente não converte quantidade `long` para `int`.

**Compatibilidade consciente**

- Estados novos têm contabilidade exata e são reembolsáveis.
- Saves antigos possuem somente contagens por requisito. Eles podem continuar se a receita ainda existir e não são apagados em falhas transitórias; se a receita desaparecer, ficam pausados porque adivinhar uma variante de ingrediente causaria transmutação. Quebrar definitivamente esse controller legado ainda não permite reconstrução segura das chaves.

**Arquivos principais**

- `src/main/java/com/raishxn/ufo/block/entity/AbstractParallelMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/ParallelProcessState.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/TransactionalAmountLedger.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/TransactionalAmountLedgerTest.java`
- `.project-control/tests/testes-humanos-l-0003.md`

**Validação automatizada**

- `./gradlew compileJava --stacktrace`: sucesso; 9 warnings preexistentes/depreciações.
- `./gradlew test --stacktrace`: sucesso; 7 testes aprovados.
- Testes novos cobrem fusão por chave, consumo parcial, entradas inválidas e saturação sem overflow.
- O primeiro `./gradlew build` revelou ordem implícita entre `syncReleaseResources` e os testes. O build agora declara `mustRunAfter` para impedir acesso concorrente a `build/resources/main`.
- `./gradlew build --stacktrace`: sucesso após a correção; JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `dd7072851a1374b1d492edfc14c4845df962ff10d56a419ed875cee81bd1e37f`.

**Validação humana**

- H1 desconexão/reconexão: aprovado pelo usuário.
- H2 save/reload em andamento: aprovado pelo usuário.
- H3 output bloqueado e liberação posterior: aprovado pelo usuário.
- H4 quebra com inputs reservados: aprovado pelo usuário.
- H5 quebra com output pendente e pacote de recuperação: aprovado pelo usuário.
- Cliente de desenvolvimento encerrou normalmente após 25m52s; `runClient` terminou com `BUILD SUCCESSFUL`, sem crash.
- Resultado geral informado pelo usuário: todos os cenários passaram com êxito e sem perda/duplicação observada.

## Marcos concluídos — continuação

### L-0004 — Correção das células BigInteger

**Data de início:** 2026-08-31
**Status:** concluído

**Escopo**

- Corrigir partition/filter, inverter e fuzzy card.
- Definir e aplicar uma contabilidade de bytes coerente com o modelo do AE2.
- Endurecer limites, overflow, codecs e dados persistidos.
- Preservar IDs e saves existentes sempre que a informação armazenada permitir migração segura.
- Adicionar testes automatizados e validar Cell Workbench/ME Drive dentro do jogo se necessário.

**O que foi implementado**

- O filtro agora usa `IPartitionList` do AE2, incluindo whitelist, blacklist/Inverter Card e Fuzzy Card.
- A capacidade usa `ceil(totalAmount / amountPerByte) + tipos * 8`, sem multiplicação em `long` e com cache reconstruído ao abrir.
- Inserções finitas consideram antecipadamente overhead e limite de tipos; células Singularity continuam aceitando quantidades agregadas além de `long` ao longo de múltiplas operações.
- Estados `EMPTY`, `NOT_EMPTY`, `TYPES_FULL` e `FULL` passaram a refletir capacidade real.
- Codec, sincronização e NBT rejeitam negativos e representações acima de 4096 bits; a fila de entradas irrecuperáveis foi limitada a 256.
- O UUID passou a usar o registro ativo `ModDataComponents.CELL_UUID`; o ID persistente continua `ufo:cell_uuid`, preservando compatibilidade. O registro duplicado e nunca inicializado `OCDataComponents` foi removido.
- Logs diretos em `System.err` e `catch (Throwable)` foram substituídos por logger estruturado e exceções recuperáveis.
- As células já expunham dois slots de upgrade, mas não estavam registradas como alvos válidos na tabela global do AE2. Todas as 15 variantes BigInteger agora aceitam um Inverter Card e um Fuzzy Card no Cell Workbench.

**Causa da falha humana H3/H4**

- A lógica de partition consultava corretamente o inventário de upgrades, porém `UpgradeInventories.forItem` também valida as associações registradas em `Upgrades`. Como nenhuma célula UFO tinha associação com os cartões do AE2, o Workbench recusava a instalação antes do matching.

**Arquivos principais**

- `src/main/java/com/raishxn/ufo/item/custom/cell/AEBigIntegerCellInventory.java`
- `src/main/java/com/raishxn/ufo/item/custom/cell/AEBigIntegerCellData.java`
- `src/main/java/com/raishxn/ufo/item/custom/cell/BigCellCapacityMath.java`
- `src/main/java/com/raishxn/ufo/item/custom/cell/BigIntegerLimits.java`
- `src/main/java/com/raishxn/ufo/item/UFORegistryHandler.java`
- `src/test/java/com/raishxn/ufo/item/custom/cell/BigCellCapacityMathTest.java`
- `src/test/java/com/raishxn/ufo/item/custom/cell/BigIntegerLimitsTest.java`
- `.project-control/tests/testes-humanos-l-0004.md`

**Validação automatizada**

- `./gradlew compileJava test --stacktrace`: sucesso; 14 testes aprovados (7 novos).
- Novos testes cobrem arredondamento/overhead, limite de tipos, célula infinita, overflow de multiplicação e limites das representações decimal/binária.
- `./gradlew build --stacktrace`: sucesso; compilação, testes, datagen e empacotamento aprovados.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `79fd9ec95cec93d0f6905bdf710c023219cdf6f13f15965844fe00f2b46d20b6`.
- `git diff --check`: sucesso após código e documentação.
- Após a correção complementar de upgrades, `./gradlew compileJava test --stacktrace`: sucesso; 14 testes aprovados.
- `./gradlew build --stacktrace`: sucesso após a correção complementar; compilação, testes, datagen e empacotamento aprovados.
- JAR atualizado `build/libs/ufo-2.1-fix7.jar`, SHA-256 `0121502c72e386dc22958e90bc2dcdca64d7c4135de287ddeb18c4d6c519e064`.
- `git diff --check`: sucesso após a correção e a atualização dos documentos.

**Validação humana**

- H1 reconhecimento/tooltip: aprovado; 9 bytes para 1 item são corretos pela política adotada (1 byte de conteúdo + 8 de overhead do tipo).
- H2 partition whitelist: aprovado.
- H5 persistência/UUID: aprovado.
- H6 célula de fluidos: aprovado.
- H3 blacklist e H4 partition vazia: a primeira execução encontrou a ausência de associação do Inverter Card; ambos foram aprovados pelo usuário no reteste após a correção complementar.
- Logs `run/logs/latest.log` e `run/logs/debug.log`: nenhum erro de `cell_uuid`, BigInteger ou das células testadas. Permanecem mensagens preexistentes não relacionadas de receitas opcionais e modelos de fluidos.
- Cliente encerrou normalmente após o reteste; o mundo integrado salvou todas as dimensões e não houve crash.

**Pendências e riscos residuais**

- Fuzzy Card foi habilitado nas variantes de item, fluido e químico para manter um contrato uniforme; a comparação fuzzy só produz diferença prática para chaves que implementam variantes fuzzy.

## Marcos concluídos — continuação

### L-0005 — Reserva transacional de fluidos do DMA

**Data de início:** 2026-08-31
**Status:** concluído

**Problema**

- Receitas do Dimensional Matter Assembler com múltiplos requisitos de fluido podem iniciar usando o mesmo volume do slot 3 para satisfazer cada requisito isoladamente e depois produzir o output sem consumir a soma completa.

**Causa**

- `findRecipe` testa cada requisito contra a quantidade original do único tanque, sem reservar o volume já comprometido. Na conclusão, as subtrações são sequenciais e zeram silenciosamente o tanque quando falta fluido.

**Correção**

- Agregar com overflow seguro todos os requisitos compatíveis com o fluido presente no tanque único.
- Rejeitar receitas cujos requisitos não possam ser satisfeitos pela mesma chave armazenada.
- Revalidar a reserva imediatamente antes de gerar outputs e consumir o total uma única vez.
- Cobrir soma, insuficiência, incompatibilidade e overflow com testes unitários.

**Arquivos alterados**

- `build.gradle`
- `gradle.properties`
- `src/main/java/com/raishxn/ufo/block/entity/DimensionalMatterAssemblerBlockEntity.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/SingleTankFluidReservation.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/SingleTankFluidReservationTest.java`
- `.project-control/tests/testes-humanos-l-0005.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`
- `run/kubejs/data/ufo/recipe/dma/l0005_reservation_test.json` (fixture local ignorada pelo Git; não entra no mod distribuído)

**Validação automatizada**

- `./gradlew compileJava test --stacktrace`: sucesso; 19 testes aprovados (5 novos).
- Os novos testes cobrem soma exata, volume agregado insuficiente, requisito incompatível, quantidade inválida, overflow e receita sem fluido.
- `./gradlew build --stacktrace`: sucesso; compilação, testes, datagen e empacotamento aprovados.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `95a1a25ed934ac337614738ffe8af49e9f19b3a99be973557b0a8cd3a860197a`.
- `git diff --check`: sucesso após código, testes, documentação e fixture local.

**Validação humana**

- H1 soma insuficiente: aprovado; 1000 mB não iniciaram a receita de duas parcelas de 700 mB e nenhum output foi criado.
- H2 commit agregado: aprovado; com 2000 mB a receita produziu exatamente uma vez, consumiu 1400 mB e deixou 600 mB.
- H3 persistência: aprovado; os 600 mB sobreviveram ao reload sem output adicional.
- Cliente e servidor integrado encerraram normalmente, salvando todas as dimensões; nenhum erro da fixture ou do DMA apareceu no log.

**Pendências e riscos residuais**

- O formato de datapack continuará aceitando a lista existente para preservar compatibilidade, mas o DMA possui somente um tanque-base; portanto todos os requisitos da lista precisam aceitar a mesma chave de fluido.

## Marcos concluídos — instrumentação

### L-0006 — Instrumentação de performance

**Data de início:** 2026-08-31
**Status:** concluído

**Problema**

- Ticks, scans, operações AE2 e sincronização das máquinas não possuem métricas, impedindo comparar custo médio/p95/p99 ou verificar os budgets definidos na auditoria.

**Causa**

- Cada família de controller mede e sincroniza estado de forma independente; não existe coletor comum nem comandos de diagnóstico.

**Correção**

- Criado coletor em memória por dimensão, posição e tipo, com janela circular de 256 amostras de tick, contadores saturados e poda após 20 minutos sem atividade.
- Instrumentados tempo de tick, scans/blocos avaliados, chamadas de storage e eventos/bytes de sync nos controllers Simple/Parallel, máquinas entrópicas, Stellar Nexus e DMA.
- Expostos snapshots administrativos por `/ufo debug perf`, `/ufo debug machine <x> <y> <z>` e limpeza por `/ufo debug perf reset`.
- O registro é limpo ao encerrar o servidor e não acrescenta dados aos saves das máquinas.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/UfoMod.java`
- `src/main/java/com/raishxn/ufo/diagnostic/MachineMetricKey.java`
- `src/main/java/com/raishxn/ufo/diagnostic/MachinePerformanceRegistry.java`
- `src/main/java/com/raishxn/ufo/diagnostic/UfoDebugCommands.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockPattern.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/FieldTieredCubeValidator.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractSimpleMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractParallelMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractEntropicMachineBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/StellarNexusControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/DimensionalMatterAssemblerBlockEntity.java`
- `src/test/java/com/raishxn/ufo/diagnostic/MachinePerformanceRegistryTest.java`
- `.project-control/tests/testes-humanos-l-0006.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- `./gradlew compileJava test --stacktrace`: sucesso durante a implementação; 23 testes aprovados antes da cobertura final de saturação.
- `./gradlew build --stacktrace`: sucesso; 24 testes aprovados, além de compilação, datagen e empacotamento.
- Os testes cobrem agregação e percentis, janela limitada, reset, poda de máquinas inativas e saturação do tempo acumulado.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `1afa22280e20bfdc28025a49f55ff557c8402df0250cfad1cc0a2244d192fa66`.
- `git diff --check`: sucesso após código, testes e consolidação documental.

**Validação humana**

- H1 resumo por tipo: aprovado; o usuário confirmou todos os campos e o log registrou métricas do Quantum Slicer.
- H2 consulta por posição: aprovado; o log identificou corretamente o DMA em `-9 -60 -7`, e o usuário confirmou a posição negativa.
- H3 scan/storage/sync: aprovado pelo usuário; o snapshot pós-reset registrou `storageOps=123` e `sync=1/34945B`. O incremento de scans/blocos foi observado antes do reset e confirmado pelo usuário, embora o valor anterior não permaneça no log de chat.
- H4 reset: aprovado; o log confirmou a limpeza e a coleta reiniciou com somente uma máquina e novos contadores.
- `latest.log` e `debug.log` não contêm erro de `MachinePerformanceRegistry` ou `UfoDebugCommands`; servidor e cliente encerraram normalmente e todas as dimensões foram salvas.

**Pendências e riscos residuais**

- A baseline ainda não mede processos ativos/bloqueados nem tamanhos das filas de output/refund e não integra spark/JFR.
- `syncBytes` mede o payload produzido pelos caminhos instrumentados, não o overhead do protocolo de rede.
- O custo do próprio coletor é limitado, mas o budget recomendado de máquina ociosa abaixo de 5 µs precisa ser medido em cenário representativo dentro do jogo.

## Pendências prioritárias

1. Avançar para o Coolant Input Hatch, `FluidPortGroup` e o primeiro recorte do `ThermalSystem` compartilhado, preservando os defaults configuráveis já aprovados.
2. Tratar separadamente o balanceamento de Safe Mode + overclock: a rede saturou `2.000.000 AE/t`, então a demora extrema vem dos multiplicadores/custos, não da fonte AE2 no cenário medido.
3. Reduzir incrementalmente o Stellar ocioso de `14,90 µs/tick` em direção ao alvo exploratório de `5 µs/tick` e estender instrumentação a processos/filas após o runtime unificado.

## Marcos concluídos — fundação multibloco

### L-0007 — Fundação multibloco 3.0: vertical slice do Quantum Slicer

**Data de início:** 2026-08-31
**Status:** concluído

**Problema**

- O template legado aceita forma irregular, controller duplicado e símbolos sem predicado; o scanner sempre coleta todos os erros e o Quantum Slicer repete scan completo a cada 200 ticks mesmo sem mudanças.
- Definição, scan, vínculo das partes e apresentação não possuem um contrato versionado único para a migração 3.0.

**Causa**

- `MultiblockPattern.Builder` convertia strings diretamente sem etapa de compilação/validação.
- Não havia distinção entre scan rápido do servidor e scan diagnóstico solicitado pelo jogador.
- Não existia índice reverso `posição da estrutura -> controller`, portanto mudanças distantes eram compensadas por polling periódico.

**Correção**

- Criada `MultiblockDefinition` imutável com ID, schema, nome, pattern, estados criativos, facings e roles explícitos.
- Extraídos compilador e transformações horizontais puros; o modo estrito falha cedo para template vazio/irregular, controller ausente/duplicado e símbolo sem predicado.
- Adicionado scan `FAST`, que para no primeiro erro, mantendo o scan diagnóstico completo para UI e scanner.
- Migrado o Quantum Slicer para uma única definição compilada compartilhada pelo controller e apresentação.
- Criado índice reverso por dimensão/posição, atualizado pelo footprint do Slicer e invalidado por eventos de quebra, colocação e notificação de vizinhos.
- Desabilitado somente para controllers migrados o polling de 200 ticks; os legados mantêm o comportamento anterior.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockCellRole.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockDefinition.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockScanMode.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockTemplateCompiler.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/StructureMembershipIndex.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockPattern.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockControllerDefinition.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockControllerDefinitions.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractSimpleMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/QuantumSlicerControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/pattern/QuantumSlicerPatternFactory.java`
- `src/main/java/com/raishxn/ufo/event/MultiblockStructureEvents.java`
- `src/main/java/com/raishxn/ufo/UfoMod.java`
- `src/test/java/com/raishxn/ufo/api/multiblock/MultiblockTemplateCompilerTest.java`
- `src/test/java/com/raishxn/ufo/api/multiblock/StructureMembershipIndexTest.java`
- `.project-control/tests/testes-humanos-l-0007.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- `./gradlew compileJava test --stacktrace`: sucesso; 32 testes aprovados.
- Cinco testes novos cobrem forma/dimensões/controller/símbolos e transformações nas quatro direções.
- Três testes novos cobrem sobreposição, isolamento por dimensão, atualização, unregister e reset do membership index.
- `./gradlew build --stacktrace`: sucesso; compilação, 32 testes, datagen e empacotamento aprovados.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `657e56e8d59f07cf3413c4a65543aab0093da27160595b84681db51e5f292da2`.
- `git diff --check`: sucesso após código, testes e documentação.

**Validação humana**

- H1 compatibilidade/receita: aprovado pelo usuário sem perda, duplicação ou recriação do controller.
- H2 invalidação: aprovado; casing distante desmontou a estrutura e o log registrou 2 scans/248 posições após as alterações.
- H3 reforma por colocação: aprovado; o Slicer voltou a formar sem intervenção no controller.
- H4 ausência de polling: aprovado; scans permaneceram estáveis durante a espera sem mudanças.
- H5 diagnóstico/apresentação: aprovado; o log apontou exatamente `[-12, -59, -12] Expected: Quantum Hyper Mechanical Casing`, e geometria/materiais permaneceram corretos.
- Nenhum erro de `MultiblockDefinition`, `MultiblockTemplateCompiler`, `StructureMembershipIndex`, `MultiblockStructureEvents` ou `QuantumSlicerControllerBE` apareceu durante os testes.
- O servidor integrado salvou todas as dimensões e o cliente encerrou normalmente, sem crash.

**Pendências e riscos residuais**

- A vertical slice ainda reutiliza processamento/booleans, parts e sync do controller paralelo legado; state machine, ports agregados e DTO compacto são etapas seguintes.
- Quantum Slicer e QMF estão no modo estrito/event-driven; QPA e Cryoforge ainda estavam legados neste ponto histórico. Stellar e entrópicas usam runtimes próprios.
- Eventos cobrem alterações normais de bloco; cenários especiais de movimento por pistão, edição em massa, unload de chunk e sobreposição precisam de GameTests/validação adicional.
- O índice é somente runtime e é reconstruído no primeiro scan após load; não altera o formato do save 2.x.

## Marcos concluídos

### L-0008 — State machine explícita do runtime multibloco

**Data de início:** 2026-08-31
**Status:** concluído

**Problema**

- O runtime infere estado por combinações livres de `assembled`, `running`, progresso, conexão, buffers e temperatura, permitindo estados ambíguos e dificultando diagnóstico e testes.

**Causa**

- Não existe enum autoritativo nem resolução central das prioridades entre estrutura inválida, grid ausente, reserva, execução, output bloqueado e superaquecimento.

**Correção**

- Introduzidos os estados explícitos do roadmap e um resolver puro com prioridades determinísticas.
- Integrada a fachada autoritativa somente em controllers com `MultiblockDefinition`; hoje, o Quantum Slicer.
- `FORMING` é emitido durante scan; estrutura, grid, outputs, temperatura e processos resolvem `UNFORMED`, `IDLE`, `RESERVING`, `RUNNING`, `OUTPUT_BLOCKED`, `PAUSED_NO_GRID`, `OVERHEATED` e `INVALID_RECIPE`.
- O estado é persistido em `runtimeState`; saves 2.x sem a chave continuam carregando pelos campos legados e recalculam no tick seguinte.
- `/ufo debug machine <pos>` exibe `state=<estado>` para controllers migrados.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockRuntimeState.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockRuntimeStateResolver.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/Ae2NodeAvailability.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractSimpleMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractParallelMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/diagnostic/UfoDebugCommands.java`
- `src/test/java/com/raishxn/ufo/api/multiblock/MultiblockRuntimeStateResolverTest.java`
- `src/test/java/com/raishxn/ufo/api/multiblock/Ae2NodeAvailabilityTest.java`
- `.project-control/tests/testes-humanos-l-0008.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- `./gradlew compileJava test --stacktrace`: sucesso.
- `./gradlew build --stacktrace`: sucesso após a correção complementar; compilação, 36 testes, datagen e empacotamento aprovados.
- Três testes do resolver cobrem lifecycle nominal, prioridades de pausa/falha e rejeição de contadores inválidos; o teste complementar de disponibilidade AE2 cobre ausência de nó/grid e nós inativos ou sem energia.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `3c9d2e9426eb0ca5c8d388c7cb7d3c4127523c5974b5666be5ca97057498935c`.
- `git diff --check`: sucesso.

**Validação humana**

- H1 `IDLE/UNFORMED/IDLE`: aprovado.
- H2 `RUNNING` durante receita: aprovado; a receita entrou diretamente em execução, portanto `RESERVING` não precisou ser observado.
- H4 `OUTPUT_BLOCKED -> IDLE`: aprovado, sem duplicação reportada.
- H5 persistência: aprovado; `OUTPUT_BLOCKED` sobreviveu ao reload e retornou a `IDLE` após liberar a saída.
- H3: aprovado no reteste final; ao desconectar, o Slicer exibiu `PAUSED_NO_GRID`, parou o processamento e retomou após a reconexão sem perda ou duplicação reportada.
- Causa complementar: `getConnectedNetworkNode` considerava `getGrid() != null` suficiente, embora um nó AE2 isolado ainda possa possuir uma grid local inativa/sem energia.
- Correção complementar implementada e validada: exigir `IGridNode.isActive()` e `isPowered()` além de nó/grid não nulos.
- A execução final registrou explicitamente `state=PAUSED_NO_GRID` no Slicer em `-14, -58, -12`; cliente e servidor integrado encerraram normalmente com `BUILD SUCCESSFUL`, salvando todas as dimensões e sem erro da state machine.

**Pendências e riscos residuais**

- Esta etapa é uma fachada autoritativa sobre o processamento legado; os booleans continuam presentes para compatibilidade de GUI/save e serão removidos incrementalmente.
- `PAUSED_NO_ENERGY`, `PAUSED_NO_COOLANT` e `ERROR_RECOVERABLE` ainda não são emitidos porque energia/coolant/erros não possuem ports/resultados tipados.
- `INVALID_RECIPE` pode ser transitório porque o fluxo atual tenta reembolsar e limpar a receita removida no mesmo tick.
- QMF já passou a usar a fachada de state machine no L-0018; QPA e Cryoforge ainda estavam pendentes neste ponto histórico. Stellar e máquinas entrópicas usam runtimes próprios.

## Marcos concluídos — continuação

### L-0009 — Outputs transacionais do Stellar Nexus

**Data de início:** 2026-08-31
**Status:** concluído

**Problema**

- Ao concluir uma simulação, o Stellar Nexus ignorava a quantidade aceita por `MEStorage.insert`, zerava o progresso e encerrava a operação mesmo quando a rede estava cheia, apagando o saldo de itens/fluidos.

**Causa**

- `injectOutputs` tratava a inserção como operação infalível e não havia buffer persistente, repetível ou recuperável para o resultado já produzido.

**Correção**

- Criado `PendingOutputBuffer`, uma contabilidade exata que prepara cada conjunto de outputs uma única vez e conserva o restante após inserções parciais.
- O Stellar Nexus persiste `pendingOutputs` em NBT, tenta drená-los a cada tick e bloqueia start manual/automático até a fila esvaziar.
- `/ufo debug machine <pos>` expõe `state=OUTPUT_BLOCKED pendingOutput=<quantidade>` no Stellar quando há saldo.
- Na quebra, o controller tenta inserir o saldo na grid e ejeta o restante como pacotes genéricos de recuperação AE2 antes de limpar a fila.
- Saves 2.x sem as novas chaves carregam com fila vazia, mantendo compatibilidade.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/block/entity/StellarNexusControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/PendingOutputBuffer.java`
- `src/main/java/com/raishxn/ufo/diagnostic/UfoDebugCommands.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/PendingOutputBufferTest.java`
- `.project-control/tests/testes-humanos-l-0009.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- `./gradlew compileJava test --stacktrace`: sucesso.
- `./gradlew build --stacktrace`: sucesso; compilação, 39 testes, datagen e empacotamento aprovados.
- Três testes novos cobrem inserção parcial, proteção contra preparação duplicada, restauração e resultados inválidos do inserter.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `3b9a0a4c44d19fa7b29930e718a5656b5b367cd9f1492d3b7daa11c3b3fca919`.
- `git diff --check`: sucesso.

**Validação humana**

- H1 rede cheia/liberação: aprovado; o saldo ficou bloqueado, impediu novo start e foi entregue uma única vez ao liberar espaço.
- H2 persistência: aprovado; o output pendente sobreviveu ao reload sem perda ou duplicação.
- H3 quebra desconectada: aprovado; o saldo foi recuperado corretamente em pacote(s) genérico(s) AE2.
- Cliente e servidor integrado encerraram normalmente com `BUILD SUCCESSFUL`, salvando todas as dimensões; nenhum erro da fila transacional apareceu nos logs.

**Pendências e riscos residuais**

- O Stellar ainda não usa a state machine autoritativa completa; `OUTPUT_BLOCKED` é exposto pelo diagnóstico como ponte até sua migração.
- Os inputs da simulação Stellar ainda são consumidos integralmente no start sem uma reserva persistida; isso pertence à migração posterior do runtime Stellar, enquanto esta etapa fecha especificamente a perda dos outputs concluídos.
- A recuperação na quebra usa o formato de pacote genérico do AE2, inclusive para fluidos, igual ao caminho já validado nos controllers paralelos.

## Marcos concluídos — continuação

### L-0010 — Política segura para falha térmica do Stellar Nexus

**Data de início:** 2026-08-31
**Status:** concluído e validado

**Problema**

- Com Safe Mode desligado, o Stellar Nexus podia iniciar automaticamente uma onda de até raio 100, fazer milhares de updates completos por tick, consultar entidades repetidamente e criar explosões secundárias, permitindo grief e degradação severa de TPS.

**Causa**

- O comportamento destrutivo era implícito e não possuía config server-side, allowlist de dimensões, budget temporal, limite total nem separação entre efeito local e grief administrativo.

**Correção**

- Criado `StellarExplosionPolicy`, que resolve explicitamente `LOCAL_ONLY` ou `BOUNDED_GRIEF` e aplica hard caps mesmo a configurações extremas.
- Registrado `UFOConfig.SERVER_SPEC`; `stellar.explosion.enableBlockGrief=false` por padrão. O modo destrutivo também exige que a dimensão esteja em `allowedDimensions`.
- O modo padrão usa explosão local sem interação com blocos e remove apenas o controller após recuperar/desvincular seu estado.
- O modo opt-in limita raio, mudanças por tick, mudanças totais e nanos por tick; não carrega chunks, usa `Block.UPDATE_CLIENTS` e mantém lava/explosões secundárias desligadas por padrão.
- Pulsos secundários, quando habilitados, usam `ExplosionInteraction.NONE` e não contornam o budget de blocos.
- O controller não é mais substituído no primeiro passo da própria onda; permanece como âncora do scheduler até conclusão/limite.
- Log estruturado registra modo, dimensão e todos os budgets resolvidos a cada falha térmica.
- Tooltip e GuideME explicam que grief exige opt-in do servidor.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/UFOConfig.java`
- `src/main/java/com/raishxn/ufo/UfoMod.java`
- `src/main/java/com/raishxn/ufo/block/entity/StellarNexusControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/StellarExplosionPolicy.java`
- `src/main/java/com/raishxn/ufo/screen/StellarNexusControllerScreen.java`
- `src/main/resources/assets/ufo/ae2guide/ufo_intro/stellar_nexus.md`
- `src/test/java/com/raishxn/ufo/block/entity/processing/StellarExplosionPolicyTest.java`
- `.project-control/tests/testes-humanos-l-0010.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- `./gradlew compileJava test --stacktrace`: sucesso.
- `./gradlew build --stacktrace`: sucesso; compilação, 42 testes, datagen e empacotamento aprovados.
- Três testes novos cobrem default local, bloqueio por dimensão e hard caps de todos os budgets destrutivos.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `21672f92b42261f94e932312acb9a9a998c01edd3cf424f9581edf92304f4e07`.
- `git diff --check`: sucesso.

**Validação humana**

- H1 aprovado: o config server-side permaneceu seguro por padrão, com grief, lava e explosões secundárias desabilitados e budgets presentes.
- H2 aprovado: o superaquecimento com Safe Mode ativou cooldown/auto-shutdown sem remover o controller nem alterar blocos; `latest.log` registrou `Safe Mode activated`.
- H3 aprovado: com Safe Mode desligado, a falha permaneceu local, removeu o controller e preservou blocos-testemunha/terreno, sem lava, onda ampla ou travamento perceptível.
- O cliente encerrou normalmente e o log confirmou o salvamento de todas as dimensões, sem erro atribuído aos caminhos novos da política térmica.

**Pendências e riscos residuais**

- Não existe integração genérica com ownership/claims; por isso o modo destrutivo continua opt-in e não deve ser habilitado em dimensões protegidas até existir adapter específico.
- A onda destrutiva em andamento é cancelada por unload/restart em vez de persistida. Isso é deliberadamente fail-safe, mas poderá receber fila persistente por chunk se esse modo virar recurso suportado.
- A validação humana cobre o default seguro; o modo destrutivo limitado fica coberto pela política pura e deve ser testado futuramente apenas em cópia descartável do mundo/GameTest.
- A linha estruturada `resolved to LOCAL_ONLY` esperada não foi localizada em `latest.log`, embora H3 tenha passado funcionalmente; revisar a visibilidade desse log quando a instrumentação voltar a ser priorizada.
- O jogador observou que o AE Energy Input hatch demora para acumular alguns bilhões de energia; avaliar isso na etapa de ports/energia e balanceamento, sem misturar com a correção de segurança L-0010.

## Marcos concluídos — Stellar Nexus

### L-0011 — Matemática e consumo proporcional de coolant do Stellar Nexus

**Data de início:** 2026-08-31
**Status:** concluído

**Problema**

- Extrações parciais podiam ser consumidas e produzir zero cooling porque a divisão inteira ocorria antes do multiplicador do tier.
- O Nexus extraía até o alvo cheio de coolant mesmo quando havia pouco calor a remover.
- Chaves e arrays da prioridade de coolant eram recriados em cada tick, e `consumeCoolant` recebia uma receita que não usava.

**Causa**

- A expressão era avaliada como `(extracted * efficiency / target) * tierMultiplier`, truncando o termo intermediário.
- Não existia uma etapa para converter o calor atual no menor volume útil antes da extração.
- Definição, prioridade e execução estavam misturadas dentro do método por tick.

**Correção**

- Criada matemática inteira pura para `extracted * efficiency * tierMultiplier / target`, com multiplicação saturada.
- O volume necessário agora é calculado com divisão arredondada para cima e limitado ao alvo de fluxo do tick.
- O storage é consultado com `SIMULATE` e só recebe `MODULATE` quando o saldo disponível remove ao menos uma unidade de calor.
- A quantidade realmente extraída é usada no cálculo final, protegendo diferenças entre simulação e commit.
- Definições/chaves de coolant são inicializadas uma vez por controller e a prioridade por tier usa tabelas estáticas sem arrays por tick.
- O parâmetro `StellarSimulationRecipe` sem uso foi removido de `consumeCoolant`.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/block/entity/StellarNexusControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/StellarCoolantMath.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/StellarCoolantMathTest.java`
- `.project-control/tests/testes-humanos-l-0011.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`
- `run/kubejs/data/ufo/recipe/stellar_simulation/l0011_coolant_test.json` (fixture local ignorada pelo Git; não entra no mod distribuído)

**Validação automatizada**

- `./gradlew compileJava test --tests com.raishxn.ufo.block.entity.processing.StellarCoolantMathTest --stacktrace`: sucesso; 4 testes novos aprovados.
- Os testes cobrem a ordem correta das operações, volume mínimo para o calor atual, limite por tick, entradas inválidas e saturação sem overflow negativo.
- `./gradlew test --stacktrace`: sucesso; 46 testes aprovados.
- `./gradlew build --stacktrace`: sucesso; compilação, 46 testes, datagen e empacotamento aprovados.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `477921be6bf7f3f4378418d38763ea7de36c7917f0cc4f09b1c73c7decc5c092`.
- `git diff --check`: sucesso após código, testes e documentação.

**Validação humana**

- H1 aprovado pelo jogador: o saldo parcial inútil não foi consumido.
- H2 aprovado: 5.000 mB passaram a 1.200 mB, consumo exato de 3.800 mB. Esse valor demonstra 19 mB/tick por 200 ticks e identifica Field Generator Mk.III efetivo (`ceil(3 × 100 / (4 × 4))`), em vez do Mk.II descrito no roteiro.
- H3 aprovado: 12.200 mB passaram a 2.800 mB, consumo exato de 9.400 mB. Esse valor demonstra 47 mB/tick por 200 ticks em Safe Mode e Field Generator Mk.III (`ceil(3 × 250 / (4 × 4))`).
- A divergência dos totais previstos veio do tier efetivo da estrutura, não de execução abreviada: ambos os saldos fecham exatamente em 200 ticks com o multiplicador Mk.III.
- O cliente encerrou normalmente; `overworld`, `the_end`, `the_nether` e `ae2:spatial_storage` foram salvos, sem erro em `StellarCoolantMath`, `consumeCoolant` ou `StellarNexusControllerBE`.

**Pendências e riscos**

- Esta correção fecha a matemática imediata do Stellar, mas ainda consome coolant diretamente da ME network; hatch de coolant, port aggregation e `ThermalSystem` compartilhado pertencem à próxima etapa alpha.3.
- Coolant do controller paralelo e do DMA continua duplicado/inconsistente e não foi alterado neste marco.
- A vazão do AE Energy Input hatch precisa ser medida na etapa de ports/energia ou balanceamento; o relato é demora para acumular alguns bilhões de energia.

## Marcos concluídos — ports e energia

### L-0012 — Port explícito e diagnóstico de energia do Stellar Nexus

**Data de início:** 2026-08-31
**Status:** concluído e validado

**Problema**

- O bloco `AE Energy Input Hatch` era obrigatório na estrutura, mas a carga usava um nó AE2 genérico e podia entrar por qualquer outro hatch conectado.
- Não havia medição separada de AE solicitado e realmente aceito, então a demora para acumular bilhões não distinguia limite da UFO de falta de throughput/energia na rede AE2.
- Custos com Safe Mode/overclock ainda usavam `double`, e as eficiências `1/4/8` dos coolants Stellar estavam fixas no código.

**Causa**

- O controller reutilizava `getConnectedNetworkNode()` para energia, inputs, coolant e outputs; a role visual do hatch não tinha contrato de port.
- A instrumentação contava operações de storage, mas não transferências de energia.
- Taxas, custos e coolant estavam acoplados ao `StellarNexusControllerBE`.

**Correção**

- Criados `EnergyInputPort` e `EnergyPortGroup`: o snapshot imutável é reconstruído ao formar a estrutura e inclui somente o bloco com role `AE_ENERGY_INPUT_HATCH`.
- `MassiveOutputHatchBE` implementa a porta de energia somente quando seu block state é o hatch correto; a extração respeita `SIMULATE/MODULATE` da AE2.
- Após H2 reproduzir alimentação residual, todos os massive hatches passaram a expor o nó AE2 somente na face indicada pelo bloco direcional. A implementação anterior expunha as seis faces e permitia ligação invisível entre hatches/blocos adjacentes.
- O controller carrega exclusivamente pelo grupo de ports e falha fechado quando a estrutura/port é removida ou aguarda chunks.
- O nó usado para itens/fluidos/coolant/output agora é escolhido de um snapshot ordenado com no máximo quatro posições de hatch, reconstruído no scan, em vez de atravessar até 41.649 posições de `parts` por tick.
- `StellarEnergyMath` calcula request, custo efetivo e ETA somente com inteiros, saturação e divisão arredondada para cima.
- `/ufo debug machine <pos>` agora mostra buffer, port count, taxa configurada, último aceito/solicitado, custo da recipe e ETAs; `/ufo debug perf` agrega tentativas e AE aceito/solicitado.
- As eficiências Stellar de Gelid Cryotheum, Stable Coolant e Temporal Fluid passaram ao config de servidor em `stellar.coolant`, mantendo defaults `1/4/8`. Valor zero desabilita o coolant no Nexus; o cache guarda chaves, não eficiências, portanto o config atual é consultado sem recriar AE keys por tick.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/api/multiblock/port/EnergyInputPort.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/port/EnergyPortGroup.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/StellarEnergyMath.java`
- `src/main/java/com/raishxn/ufo/block/entity/MassiveOutputHatchBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/StellarNexusControllerBE.java`
- `src/main/java/com/raishxn/ufo/diagnostic/MachinePerformanceRegistry.java`
- `src/main/java/com/raishxn/ufo/diagnostic/UfoDebugCommands.java`
- `src/main/java/com/raishxn/ufo/UFOConfig.java`
- `src/main/resources/assets/ufo/ae2guide/ufo_intro/stellar_nexus.md`
- `src/test/java/com/raishxn/ufo/api/multiblock/port/EnergyPortGroupTest.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/StellarEnergyMathTest.java`
- `src/test/java/com/raishxn/ufo/diagnostic/MachinePerformanceRegistryTest.java`
- `.project-control/tests/testes-humanos-l-0012.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Testes focados de ports, energia e métricas: sucesso.
- Após a correção de orientação e cache, `./gradlew test --stacktrace`: sucesso com 52 testes. O source set JUnit puro não inclui classes Minecraft, portanto a orientação física permanece coberta pela confirmação integrada H2/H4.
- `./gradlew build --stacktrace`: sucesso final após a correção de isolamento/cache, com 52 testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `32f829968205c91f5e1600360aad5ad1a624160dac6f4778701bca1c7489f12a`.
- `git diff --check`: sucesso após código, config, guia e documentação.
- Testes cobrem agregação parcial de múltiplas portas, propagação de simulação, requests inválidos, limite por capacidade, custos exatos normal/Safe/OC, saturação, ETA com ceil e contadores de transferência.

**Avaliação de balanceamento**

- Com as taxas atuais, recipes normais T2 levam 45–60 s para carregar e recipes T3 52,5–162,5 s, frente a 58–100 min de processamento; não há evidência para buff geral imediato.
- Em Safe Mode, o intervalo T3 vira 2m11–6m46. Em overclock vira 8m45–27m05; Safe + overclock chega a 21m52–67m42, enquanto a operação acelerada dura cerca de 11m40–20m.
- A espera desproporcional está concentrada em overclock, especialmente com Safe Mode. A taxa não foi alterada ainda: H1–H4 vão medir se a porta entrega os 2.000.000 AE/t de Mk.III ou se a rede AE2 já chega abaixo disso.
- As eficiências de coolant ficaram configuráveis sem mudar os defaults; novos valores devem preservar a progressão Gelid < Stable < Temporal e ser validados pela fórmula antes de distribuição.

**Validação humana**

- H1 aprovado: o Stellar Mk.III mostrou `ports=1`, `rate=2000000AE/t` e `last=2000000/2000000AE`, comprovando que a fonte consegue saturar a taxa configurada.
- H3 aprovado: o buffer cresceu `1.074.000.000 AE` em 537 ticks, exatamente `2.000.000 AE/t`.
- H5 aprovado: `run/config/ufo-server.toml` contém os defaults `1/4/8` sob `stellar.coolant`.
- H2/H4 reprovaram antes da correção e revelaram a exposição indevida das seis faces. Na validação final, duas leituras mantiveram o buffer exatamente em `28.181.999.997 AE`, com `ports=1` e `last=0/2000000AE`; as métricas registraram `0/764.000.000 AE` durante o isolamento e `186.000.000/1.202.000.000 AE` após a reconexão.
- O cache de até quatro hatches reduziu o Stellar ocioso de aproximadamente `1.080 µs/tick` para `14,90 µs/tick` no intervalo limpo, cerca de 98,6%. Um scan estrutural posterior elevou a média agregada para `27,75 µs`, mas p95/p99 permaneceram `18,84/42,96 µs`, confirmando que a travessia per-tick foi removida.
- O cliente encerrou normalmente com `BUILD SUCCESSFUL`, todas as dimensões foram salvas e não houve erro nos caminhos novos da L-0012. Erros de cursor Wayland/fonte e avisos preexistentes de assets/recipes não foram atribuídos à UFO.

**Pendências e riscos**

- A estrutura ainda exige exatamente uma porta de cada tipo; o agregador aceita múltiplas portas para a fundação futura, mas gameplay/topologia não foi alterado silenciosamente.
- A implementação física continua compartilhando a classe legada `MassiveOutputHatchBE`; separar BEs/capabilities por role continua pendente.
- Saves existentes preservam a orientação dos hatches, mas cabos ligados por uma face diferente da indicada deixam de conectar. Isso corrige o contrato visual; pode ser necessário girar/recolocar o hatch para apontar a face ao cabo externo.
- As eficiências configuráveis abrangem o Stellar Nexus. O controller paralelo e o DMA usam escalas incompatíveis e só devem compartilhar valores quando existir o `ThermalSystem`/`CoolantRegistry` único.
- Decidir qualquer buff de energia somente após comparar `lastAccepted` com `configuredRate` no jogo.
- A medição demonstrou que a rede entrega os `2.000.000 AE/t` completos; a demora de bilhões vem da taxa/custo configurados, não de gargalo da fonte no cenário testado. O balanceamento deve concentrar-se nos multiplicadores de overclock e Safe Mode, sem buff geral antes do reteste H2.
- Após remover a travessia de `parts`, o Stellar ocioso mediu `14,90 µs/tick`, ainda acima do alvo exploratório de `5 µs`; continuar a otimização incremental sem bloquear o avanço de ports/térmica.

## Marcos concluídos — térmica compartilhada

### L-0013 — Núcleo matemático do ThermalSystem

**Data de início:** 2026-09-01
**Status:** concluído e validado

**Problema**

- Stellar e controllers paralelos calculam coolant com algoritmos e representações diferentes, duplicando arredondamento, saturação e planejamento `SIMULATE -> MODULATE`.
- Introduzir imediatamente um hatch obrigatório quebraria estruturas salvas; falta primeiro um contrato térmico comum que preserve o balanceamento aprovado.

**Causa**

- Cada família incorporou sua própria relação calor/mB diretamente no controller, inclusive o caso inverso de Gelid (`120 mB` por unidade de calor) nos paralelos.

**Correção**

- Extrair um `ThermalSystem` puro com perfil racional (`heatNumerator/millibucketDenominator`), vazão máxima, planejamento do menor volume útil e matemática saturada.
- Migrar Stellar e controllers paralelos para o mesmo núcleo sem alterar seus perfis numéricos atuais.
- Chaves e ordens de prioridade dos três coolants nos controllers paralelos passam a ser cacheadas por instância, removendo arrays/AE keys do caminho por tick.
- O diagnóstico de controllers compilados passa a mostrar `temp=<atual>/<máximo>` para tornar a regressão integrada observável.
- Manter o futuro Coolant Input Hatch opcional durante a migração de save/topologia; torná-lo obrigatório exige versão de schema e roteiro de migração próprios.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/block/entity/processing/ThermalSystem.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/StellarCoolantMath.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractParallelMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/diagnostic/UfoDebugCommands.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/ThermalSystemTest.java`
- `.project-control/tests/testes-humanos-l-0013.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Testes focados de `ThermalSystem` e regressão `StellarCoolantMath`: sucesso.
- `./gradlew test --stacktrace`: sucesso com 56 testes.
- `./gradlew build --stacktrace`: sucesso com 56 testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `3ef7976087bfc6a84d28c5dd972db94ee058aa39cd4991c5d230d598d01340b1`.
- `git diff --check`: sucesso.

**Validação humana**

- H1 aprovado: a regressão Stellar Mk.III repetiu o saldo exato da L-0011, de `5.000 mB` para `1.200 mB` após 200 ticks.
- H2 aprovado: duas recipes que durariam 10 s em Mk.I concluíram em 2,5 s no Quantum Slicer Mk.III e consumiram `50 mB` de Temporal Fluid (`5.000 -> 4.950 mB`); o total observado permaneceu dentro do limite de `10 mB/tick` e o processo continuou normalmente.
- H3 aprovado: o jogador confirmou que o saldo abaixo do volume mínimo útil não foi desperdiçado e que o consumo ocorreu ao completar os `120 mB` exigidos pelo perfil de Gelid Cryotheum.
- O cliente e o servidor integrado encerraram normalmente; `overworld`, `the_end`, `the_nether` e `ae2:spatial_storage` foram salvos e o log confirmou que todas as dimensões terminaram salvas.
- Avisos preexistentes de assets e receitas de addons não foram atribuídos ao `ThermalSystem`.

**Pendências e riscos**

- O núcleo unifica a matemática, mas temperaturas persistidas continuam nas unidades legadas de cada família. A migração autoritativa para `uH long` exige versionamento de save/UI e não foi misturada neste recorte.
- Coolant ainda vem diretamente da ME network. `FluidInputPort`, tanque local e migração topológica continuam no próximo recorte.

## Marcos concluídos — ports de fluido

### L-0014 — Port agregado de coolant do Stellar Nexus

**Data de início:** 2026-09-01
**Status:** concluído e validado

**Problema**

- Embora a estrutura exigisse um `ME Massive Fluid Hatch`, o coolant do Stellar era extraído pelo primeiro nó AE2 genérico disponível entre quatro hatches.
- Um hatch de item, energia ou output podia substituir silenciosamente a role fluida, impedindo evoluir para tanque local, vazão por hatch e coolant return sem novo acoplamento.

**Causa**

- `consumeCoolant` recebia a grid obtida por `getConnectedNetworkNode()` e acessava diretamente o `MEStorage`; a role estrutural do hatch fluido não tinha contrato runtime.

**Correção**

- Criados `FluidInputPort<K>` e `FluidPortGroup<K>` como contrato genérico e snapshot imutável, com propagação de simulação, extração parcial entre portas e limite autoritativo do solicitado.
- `MassiveOutputHatchBE` implementa a porta somente quando seu block state é o `ME Massive Fluid Hatch`; a extração usa `SIMULATE/MODULATE` e identifica o próprio hatch como action source.
- O Stellar reconstrói o grupo ao formar a estrutura e o invalida em chunk incompleto, deformação, remoção, quebra e load.
- O consumo térmico usa exclusivamente esse grupo. A estrutura e o NBT não mudaram; o hatch fluido já era obrigatório.
- `/ufo debug machine <pos>` passou a expor `heat`, `coolantPorts` e `coolantLast=<aceito>/<solicitado>mB` para validar isolamento e retomada.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/api/multiblock/port/FluidInputPort.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/port/FluidPortGroup.java`
- `src/main/java/com/raishxn/ufo/block/entity/MassiveOutputHatchBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/StellarNexusControllerBE.java`
- `src/main/java/com/raishxn/ufo/diagnostic/UfoDebugCommands.java`
- `src/test/java/com/raishxn/ufo/api/multiblock/port/FluidPortGroupTest.java`
- `.project-control/tests/testes-humanos-l-0014.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Testes focados de `FluidPortGroup`, `ThermalSystem` e `StellarCoolantMath`: sucesso.
- `./gradlew test --stacktrace`: sucesso com 59 testes.
- `./gradlew build --stacktrace`: sucesso com 59 testes, datagen e empacotamento; JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `76f2819eda5fdf6c59251d7356e677be3a6e65e3267dacf3b7d1afa0922be20f`.
- Os três testes novos cobrem agregação parcial, propagação de key/simulação/volume restante, clamp de uma porta que reporta além do pedido, grupo vazio e entradas inválidas.
- `git diff --check`: sucesso.

**Validação humana**

- H1 aprovado: com o hatch fluido conectado, o jogador confirmou a regressão de consumo e diagnóstico pela porta explícita.
- H2 aprovado: com somente o `ME Massive Fluid Hatch` desconectado e outro hatch ainda ligado à rede, a operação concluiu sem consumir coolant pelo caminho errado.
- H3 aprovado: reconectar a face indicada restaurou o consumo de coolant.
- O cliente encerrou normalmente, `./gradlew runClient` terminou com `BUILD SUCCESSFUL` após 10m18s e todas as dimensões foram salvas.
- O snapshot final de performance registrou o Stellar ativo sem erro novo; mensagens preexistentes de assets/receitas de addons permaneceram fora do escopo.

**Pendências e riscos**

- Saves e estrutura permanecem compatíveis, mas uma instalação que alimentava o coolant por outro hatch precisa conectar a face indicada do `ME Massive Fluid Hatch`; esse isolamento é a correção pretendida.
- A porta ainda é um adapter da ME network, não um tanque local. Capacidade, vazão física e um futuro `Coolant Input Hatch` dedicado exigem schema/migração próprios.
- O grupo suporta múltiplas portas, mas a estrutura atual continua exigindo exatamente um hatch fluido.
- H3 revelou que `heatLevel` fica congelado e o port de coolant não é consultado quando o Stellar está ocioso. Dissipação passiva e resfriamento idle pertencem ao próximo recorte térmico; não invalidam o isolamento aprovado da L-0014.

## Marcos concluídos — comportamento térmico idle

### L-0015 — Dissipação passiva e coolant idle do Stellar Nexus

**Data de início:** 2026-09-01
**Status:** concluído e validado

**Problema**

- H3 da L-0014 deixou o Stellar com 60% de calor e mostrou que o valor permanecia congelado depois do fim da operação.
- Reconectar o hatch fluido não iniciava resfriamento porque `consumeCoolant` só era chamado no caminho ativo da recipe.

**Causa**

- `processMachineTick` retornava antes de qualquer etapa térmica quando não existia recipe ativa ou `running` era falso.
- Não havia política/config de dissipação passiva nem intervalo para coolant idle.

**Correção**

- `ThermalSystem.dissipate` aplica dissipação inteira sem underflow.
- Stellar ocioso dissipa por default uma unidade de calor a cada segundo, inclusive deformado; `stellar.thermal.passiveDissipationPerSecond=0` desabilita o comportamento.
- Quando formado e ocioso, o controller consulta o port fluido por default a cada 20 ticks; `stellar.thermal.idleCoolantIntervalTicks=0` desabilita o consumo idle.
- Coolant idle usa a vazão-base de `100 mB` e não herda penalidades de Safe Mode/overclock. O caminho ativo preserva exatamente os alvos `100/250/500/1250 mB/tick`.
- Nenhum campo persistente ou requisito estrutural foi adicionado; os dois valores são config server-side.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/UFOConfig.java`
- `src/main/java/com/raishxn/ufo/block/entity/StellarNexusControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/ThermalSystem.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/StellarCoolantMath.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/ThermalSystemTest.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/StellarCoolantMathTest.java`
- `.project-control/tests/testes-humanos-l-0015.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Testes focados de `ThermalSystem`, `StellarCoolantMath` e `FluidPortGroup`: sucesso.
- `./gradlew test --stacktrace`: sucesso com 61 testes.
- `./gradlew build --stacktrace`: sucesso com 61 testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `5622f57b5fb36bdfd6cc7b7ef6eb7bdd265565d76f862c0a8ddc1eb4e02e1c89`.
- Os testes novos cobrem dissipação normal, clamp em zero, taxas inválidas e preservação exata das quatro vazões ativas frente à vazão idle.
- `git diff --check`: sucesso.

**Validação humana**

- H1 aprovado: sem acesso ao coolant, o calor ocioso passou a cair lentamente conforme a dissipação passiva.
- H2 aprovado: ao reconectar o hatch com aproximadamente 51% de calor e `5.000 mB` de Stable Coolant, o Stellar começou a extrair coolant e resfriar mesmo sem recipe ativa.
- H3 aprovado: o consumo ativo continuou reproduzindo a regressão exata de `5.000 -> 1.200 mB`.
- H4 aprovado: os defaults `passiveDissipationPerSecond=1` e `idleCoolantIntervalTicks=20` foram confirmados no config de servidor.
- O cliente encerrou normalmente, `./gradlew runClient` terminou com `BUILD SUCCESSFUL` após 7m02s e todas as dimensões foram salvas.
- A primeira tentativa encontrou a falha intermitente preexistente do handoff `EARLYDISPLAY`; a segunda abriu normalmente e o problema não foi atribuído à L-0015.

**Pendências e riscos**

- O default passivo leva dez minutos para remover as 600 unidades observadas em H3 sem coolant; é deliberadamente lento e configurável.
- Coolant idle reduz tempo ao custo de recurso e opera somente com estrutura formada/porta conectada. O cooldown punitivo de superaquecimento em Safe Mode continua separado e não pode ser contornado por esse caminho.

## Marcos concluídos — ports químicos

### L-0016 — Port agregado e transacional para química

**Data de início:** 2026-09-01
**Data de conclusão:** 2026-09-01
**Status:** concluído e aprovado

**Problema**

- Controllers paralelos percorriam `parts` em cada reserva/refund químico e alteravam `MekanismChemicalStorage.setStoredChemical` diretamente.
- Esse caminho não respeitava simulação, duplicava a semântica da capability Mekanism e mantinha o runtime acoplado aos campos concretos do hatch.

**Causa**

- Não existia um contrato de port químico nem snapshot formado da estrutura; a primeira integração Mekanism expôs apenas getters/setter do tanque local.

**Correção**

- Criados `ChemicalPort<K>` e `ChemicalPortGroup<K>` como contrato bidirecional e snapshot imutável, com agregação parcial, clamp autoritativo e operações transacionais `simulate -> commit` para extração e inserção.
- `MekanismChemicalStorage` estende o contrato genérico; a capability Mekanism e o controller agora delegam ao mesmo caminho de transferência.
- `MassiveOutputHatchBE` aceita química somente no block state `ME Massive Fluid Hatch`, rejeita chaves incompatíveis e não muta o tanque durante simulação.
- Controllers paralelos constroem o snapshot ao formar/reformar a estrutura, invalidam-no imediatamente em `markStructureDirty` e na quebra, e deixaram de percorrer partes ou chamar o setter durante processamento/refund.
- Após os dois ciclos reprovados de H2, o ownership foi tornado explícito e híbrido: item/fluid são entregues pelo processing pattern AE2; química entregue pelo pattern preenche somente a quantidade correspondente e o job reserva no port formado apenas a parcela faltante. Química errada ou acima do requisito continua fazendo o pattern falhar fechado.
- A causa do segundo ciclo era interoperabilidade: Applied Mekanistics 1.6.3 codifica chemicals como `appmek:chemical`, mas a Pulsar e o controller aceitavam somente a chave paralela `ufo:chemical`. Com AppMek presente, células Pulsar, patterns, reservas e refunds agora usam `MekanismKey`; sem ele, a integração nativa da UFO permanece como fallback isolado.
- O JEI universal passou a renderizar `ChemicalStack` como tanque vertical com quantidade e tooltip química. A fixture ganhou `1 dirt` como input-gatilho para permitir um processing pattern real `1 dirt -> 1 cobblestone`.
- O clique direito dos quatro hatches usa agora o nome traduzido do block state, removendo a string fixa incorreta de output hatch sem alterar as telas planejadas.
- `/ufo debug machine <pos>` expõe `chemicalPorts` também nos controllers paralelos ainda não migrados para a definition compilada.
- Uma fixture local e isolada da Quantum Cryoforge cobre química sem adicionar receita de produção ou alterar balanceamento.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/api/multiblock/port/ChemicalPort.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/port/ChemicalPortGroup.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractParallelMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/MassiveOutputHatchBlock.java`
- `src/main/java/com/raishxn/ufo/block/entity/MassiveOutputHatchBE.java`
- `src/main/java/com/raishxn/ufo/compat/jei/UniversalMultiblockRecipeCategory.java`
- `src/main/java/com/raishxn/ufo/compat/mekanism/MekanismChemicalStorage.java`
- `src/main/java/com/raishxn/ufo/compat/mekanism/MekanismChemicalCompat.java`
- `src/main/java/com/raishxn/ufo/compat/mekanism/AppliedMekanisticsCompat.java`
- `src/main/java/com/raishxn/ufo/compat/mekanism/UfoMekanismStorageCompat.java`
- `src/main/java/com/raishxn/ufo/diagnostic/UfoDebugCommands.java`
- `src/test/java/com/raishxn/ufo/api/multiblock/port/ChemicalPortGroupTest.java`
- `run/kubejs/data/ufo/recipe/universal/quantum_cryoforge/l0016_chemical_port_test.json` (fixture local ignorada)
- `.project-control/tests/testes-humanos-l-0016.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Teste focado de `ChemicalPortGroup`: sucesso.
- `./gradlew test --stacktrace`: sucesso com 66 testes.
- `./gradlew build --stacktrace`: sucesso com 66 testes, datagen e empacotamento.
- Build/reteste automatizado do follow-up de H2: sucesso com os mesmos 66 testes, datagen e empacotamento.
- Build do segundo follow-up de H2: `compileJava`, 66 testes, datagen e empacotamento concluídos com sucesso; o datagen carregou conjuntamente AE2 19.2.17, Applied Mekanistics 1.6.3 e Mekanism 10.7.18.
- JAR do segundo follow-up `build/libs/ufo-2.1-fix7.jar`, SHA-256 `41e64ed9f37d68f4c345b58e9e457da5125792ccb1577b672746b819ad1e00af`.
- JAR do primeiro follow-up `build/libs/ufo-2.1-fix7.jar`, SHA-256 `15b78305ad56ecb59736411386519310202b02b4816dc8a6facdc29c4d7d30a3` (substituído pelo segundo follow-up acima).
- Os cinco testes novos cobrem agregação parcial, propagação de chave/simulação/volume restante, sequência simulate→commit sem mutação dupla, rejeição de chave incompatível, grupo vazio e entradas inválidas.
- `git diff --check`: sucesso após a atualização documental final.

**Validação humana**

- Primeiro ciclo parcialmente reprovado: H1 confirmou `chemicalPorts=1`, mas H2 não iniciou e o JEI não mostrou o requisito de oxigênio; H3/H4 não foram executados. O log registrou encerramento limpo, `BUILD SUCCESSFUL` após 17m19s e todas as dimensões salvas.
- A falha revelou que `pushPattern` pré-preenchia buffers químicos, tornando a extração pelo port inalcançável, e que `UniversalMultiblockRecipeCategory` ignorava `chemical_inputs`.
- Durante o mesmo ciclo, o jogador identificou que todos os quatro hatches exibiam a string fixa `ME Massive Output Hatch` no clique direito.
- Segundo ciclo reprovado: o oxigênio apareceu como item no JEI, a Cryoforge recusou o job e o cancelamento informou que a Pulsar não podia armazenar o chemical. A inspeção do addon exato confirmou duas AEKey químicas incompatíveis; o cliente foi encerrado normalmente e todas as dimensões foram salvas.
- Terceiro ciclo aprovado pelo usuário: H1–H5 passaram, cobrindo renderer de tanque, patterns com e sem chemical, consumo sem cobrança dupla, rejeição de hidrogênio, invalidação/reconstrução do snapshot e identificação correta dos hatches.
- O log final confirmou `/ufo debug perf`, `chemicalPorts=0` durante a invalidação intencional, encerramento normal, salvamento de overworld, End, Nether e Spatial Storage, e `runClient` com `BUILD SUCCESSFUL` em 3m33s.

**Pendências e riscos**

- Não há recipes de produção com `chemical_inputs`; a integração exige fixture local para ser exercitada e o balanceamento químico continua deliberadamente inalterado.
- A interoperabilidade AppMek é opcional e versionada pelo file ID `7096962` (1.6.3); mudanças futuras no tipo público `MekanismKey` exigirão recompilação/validação, enquanto instalações sem AppMek continuam no fallback `UfoMekanismKey`.
- A estrutura da Quantum Cryoforge aceita universal hatches em posições `B`; QMF, Quantum Slicer e Quantum Processor Assembler ainda não aceitam esse hatch em suas casings atuais. Expandir roles/topologias pertence à migração versionada de cada definition, não a este recorte.
- A fonte oficial do AE2 usada como referência futura foi clonada fora da UFO em `/home/raishxn/MineProjects/references/Applied-Energistics-2-19.2.17`, tag `neoforge/v19.2.17`, commit `79ee2c7`; ela corresponde à dependência AE2 `19.2.17` resolvida pela UFO.
- A fonte oficial do Mekanism foi clonada fora da UFO em `/home/raishxn/MineProjects/references/Mekanism-1.21.1-10.7.18.84`, tag `v1.21.1-10.7.18.84`, commit `a08892c`; ela corresponde exatamente aos artefatos Mekanism resolvidos pelo build.
- A pausa estrutural estabelecida após este marco foi levantada pelo autor depois do UI-0001. A próxima sessão volta ao core; não continuar widgets ou JEI enquanto essa nova orientação estiver vigente.

## Marcos concluídos — ports de item

### L-0017 — Port agregado e transacional de itens

**Data de início:** 2026-09-01
**Status:** concluído e aprovado

**Problema**

- O Stellar exigia `ME Massive Input Hatch` e `ME Massive Output Hatch`, mas itens eram reservados e inseridos pelo primeiro nó AE2 utilizável entre todas as quatro roles.
- O commit da reserva ignorava a quantidade realmente retirada depois do `SIMULATE`, permitindo start após extração parcial se a disponibilidade mudasse.
- Nos controllers paralelos, os itens entregues pelo processing pattern já pertencem ao job; aplicar a mesma extração de port cobraria os inputs duas vezes.

**Causa**

- Não existia contrato runtime para ports de item nem snapshots separados de input/output.
- `MassiveOutputHatchBE` compartilhava a mesma implementação entre quatro block states, e o método legado de injeção não restringia a role.
- `pushPattern` validava os counters, mas preenchia buffers de item pelo total teórico da recipe em vez de materializar explicitamente o ownership entregue.

**Correção implementada**

- Criados `ItemPort<K>` e `ItemPortGroup<K>` bidirecionais, com snapshot imutável, agregação parcial, clamp autoritativo e planejamento `simulate -> commit`.
- `MassiveOutputHatchBE` extrai itens somente no block state `ME Massive Input Hatch`; inserção de output pelo caminho legado exige `ME Massive Output Hatch`. O input hatch também aceita reinserção para rollback.
- O Stellar constrói grupos separados de input/output ao formar e invalida ambos em dirty, deformação, remoção, quebra e load.
- Checks e reservas de item usam exclusivamente o input group. Se o commit aceitar menos que o planejado, a operação não inicia e todos os itens já retirados são devolvidos ao port/storage; qualquer saldo que ambas recusarem é ejetado como pacote de recuperação exato.
- Outputs `AEItemKey` permanecem no `PendingOutputBuffer` até o output group aceitá-los; fluid outputs continuam no caminho existente, sem ampliar o recorte.
- `/ufo debug machine <pos>` exibe `itemPorts=<inputs>/<outputs>` no Stellar.
- Controllers paralelos continuam exigindo os itens completos no processing pattern. Seus buffers agora são preenchidos a partir dos counters realmente entregues, sem segunda extração e sem mudança de recipes/topologia.
- Adicionada fixture local `2 dirt -> 1 cobblestone` para teste integrado, sem entrar no artefato distribuído.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/api/multiblock/port/ItemPort.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/port/ItemPortGroup.java`
- `src/main/java/com/raishxn/ufo/block/entity/MassiveOutputHatchBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/StellarNexusControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractParallelMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/diagnostic/UfoDebugCommands.java`
- `src/test/java/com/raishxn/ufo/api/multiblock/port/ItemPortGroupTest.java`
- `run/kubejs/data/ufo/recipe/stellar_simulation/l0017_item_port_test.json` (fixture local ignorada)
- `.project-control/tests/testes-humanos-l-0017.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- `./gradlew compileJava compileTestJava --stacktrace`: sucesso.
- `./gradlew test --stacktrace`: sucesso com 74 testes.
- Seis testes novos cobrem agregação parcial, propagação de chave/simulação/saldo, clamp, `simulate -> commit`, capacidade parcial, diferença entre simulação e commit, grupo vazio e entradas inválidas.
- `./gradlew build --stacktrace`: sucesso com compilação, 74 testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `9eae172867622307e3d358a7ca7fc5ad03cc85b163bf669b9fe5a9461a8c6898`.
- `git diff --check`: sucesso.

**Validação humana**

- H1–H6 executados com êxito pelo autor em 2026-09-01: snapshots/roles, consumo isolado pelo input hatch, recusa de outra role, output bloqueado/retomada, persistência/quebra e ausência de dupla cobrança no processing pattern.
- O log confirmou `itemPorts=1/1`, invalidação para `0/0`, reforma para `1/1`, três tentativas registradas e encerramento limpo com todas as dimensões salvas.
- Não houve erro relacionado ao recorte do L-0017.

**Pendências e riscos**

- Fuel e fluid inputs do Stellar ainda usam o storage AE2 genérico; migrá-los exige um marco próprio e não foi misturado ao item port.
- `EntropicAssemblerMatrixBE` saiu da fila de correções incrementais por decisão do autor: o multibloco legado será excluído e integralmente reformulado em um marco próprio, com política explícita para IDs e compatibilidade/migração de saves.

## Marcos em validação — topology/definition compilada

### L-0018 — Migração versionada da topology/definition do QMF

**Data de início:** 2026-09-01
**Status:** concluído e aprovado

**Problema**

- O Quantum Matter Fabricator ainda construía um `MultiblockPattern` legado e não expunha `MultiblockDefinition`.
- Por isso o QMF permanecia no scan periódico, fora do scanner rápido, do índice reverso de membership e do runtime state observável já validados no Quantum Slicer.

**Causa**

- `QmfControllerBE` cacheava apenas o pattern, enquanto `QmfPatternFactory` misturava o template textual e os predicates sem ID, schema ou roles compiladas.
- Preview e runtime consumiam adapters diferentes, embora descrevessem a mesma estrutura.

**Correção implementada**

- Criado `QmfTopologySchema`, Java puro, com schema 1 e cópias defensivas das cinco camadas legadas.
- `QmfPatternFactory` compila uma única `MultiblockDefinition` imutável, fail-fast e identificada como `ufo:quantum_matter_fabricator`.
- Roles são explícitas: controller `H`, port `P`, estrutura `C/F/G` e ar obrigatório `A`; os quatro facings horizontais e todos os predicates/default states foram preservados.
- `QmfControllerBE` passa a optar pelo scanner FAST e pelo `StructureMembershipIndex`; o polling periódico legado deixa de ser usado nessa máquina.
- Preview/tutorial obtêm o adapter a partir da mesma definition compilada. Nenhum ID, NBT, recipe, tier, dimensão, anchor ou requisito de bloco foi alterado.
- Fixture local `1 dirt -> 1 cobblestone` foi adicionada apenas para regressão integrada do processing pattern.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/api/multiblock/topology/QmfTopologySchema.java`
- `src/main/java/com/raishxn/ufo/block/entity/pattern/QmfPatternFactory.java`
- `src/main/java/com/raishxn/ufo/block/entity/QmfControllerBE.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockControllerDefinitions.java`
- `src/test/java/com/raishxn/ufo/api/multiblock/QmfTopologySchemaTest.java`
- `run/kubejs/data/ufo/recipe/universal/qmf/l0018_topology_test.json` (fixture local ignorada)
- `.project-control/tests/testes-humanos-l-0018.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- `./gradlew compileJava compileTestJava --stacktrace`: sucesso.
- `QmfTopologySchemaTest`: dois testes aprovados; cobrem schema, dimensões 5×5×5, anchor `(2,2,0)`, símbolos, contagem exata das 125 células e cópia defensiva.
- `./gradlew test --stacktrace`: sucesso com 76 testes.
- `./gradlew build --stacktrace`: sucesso com compilação, 76 testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `5a15ab4e39f9f4705755ef3db53612fcec4e7a92efa645cdf7f6dfba71557bf7`.
- `git diff --check`: sucesso.

**Validação humana**

- H1–H4 executados com êxito pelo autor em 2026-09-01.
- O log confirmou `IDLE -> UNFORMED -> IDLE -> RUNNING`, processamento real, reconstrução após reload e encerramento limpo com todas as dimensões salvas.
- Não houve erro relacionado ao recorte do L-0018.

**Pendências e riscos**

- O índice é runtime e intencionalmente reconstruído após load; nenhuma lista de membership foi adicionada ao save.
- QPA e Quantum Cryoforge continuavam em definitions legadas ao encerrar este marco; a QPA foi encaminhada ao L-0019.

### L-0019 — Migração versionada da topology/definition da QPA

**Data de início:** 2026-09-01
**Status:** concluído e aprovado

**Problema**

- A Quantum Processor Assembler ainda cacheava apenas um `MultiblockPattern` legado, mantendo scan periódico e fontes separadas para runtime e preview.

**Causa**

- `QpaPatternFactory` não expunha ID, schema ou roles; `QuantumProcessorAssemblerControllerBE` não optava pelo runtime compilado.

**Correção implementada**

- Criado `QpaTopologySchema`, Java puro, schema 1, com cópias defensivas das cinco camadas legadas.
- `QpaPatternFactory` compila uma única definition `ufo:quantum_processor_assembler`, strict/fail-fast, com roles explícitas e os mesmos predicates, candidates, defaults e facings.
- A topology própria da QPA foi preservada: 5×5×5, anchor `(2,0,0)` e contagens exatas `A=20`, `C=51`, `F=7`, `G=45`, `H=1`, `P=1`.
- Controller, scanner rápido, membership e preview passam a consumir a mesma definition; nenhum ID, NBT, recipe, tier ou bloco exigido mudou.
- Fixture local `1 dirt -> 1 cobblestone` cobre a regressão integrada sem alterar recipes distribuídas.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/api/multiblock/topology/QpaTopologySchema.java`
- `src/main/java/com/raishxn/ufo/block/entity/pattern/QpaPatternFactory.java`
- `src/main/java/com/raishxn/ufo/block/entity/QuantumProcessorAssemblerControllerBE.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockControllerDefinitions.java`
- `src/test/java/com/raishxn/ufo/api/multiblock/QpaTopologySchemaTest.java`
- `run/kubejs/data/ufo/recipe/universal/quantum_processor_assembler/l0019_topology_test.json` (fixture local ignorada)
- `.project-control/tests/testes-humanos-l-0019.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- `./gradlew compileJava compileTestJava --stacktrace`: sucesso.
- `QpaTopologySchemaTest`: dois testes aprovados; cobrem schema, dimensões, anchor, símbolos, contagens das 125 células e cópia defensiva.
- `./gradlew test --stacktrace`: sucesso com 78 testes.
- `./gradlew build --stacktrace`: sucesso com compilação, 78 testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `d1be93a2dfbbf0662951a0a531e19b295339d6357f46e192b1671accfb6421e6`.
- `git diff --check`: sucesso.

**Validação humana**

- H1 compatibilidade estrutural, H2 invalidação/reforma event-driven, H3 recipe/processing pattern e H4 reload/reconstrução do índice foram executados com êxito pelo autor em 2026-09-01.
- O client foi fechado normalmente após o teste.
- Roteiro: `.project-control/tests/testes-humanos-l-0019.md`.

**Pendências e riscos**

- O índice permanece runtime e é reconstruído após load, sem alteração do formato de save.
- Quantum Cryoforge continua na definition legada. A pedido do autor, preview JEI e auto-build foram priorizados antes dessa migração para facilitar os próximos testes.

### L-0020 — Preview JEI nativo de multiblocos

**Data de início:** 2026-09-01
**Status:** concluído; validações automatizada e humana aprovadas

**Problema**

- A página de multiblocos no JEI dependia integralmente de LDLib, Yoga e Taffy, contrariando a arquitetura 3.0 e tornando uma dependência externa dona da apresentação das definitions UFO.
- O mesmo acoplamento impedia evoluir uma experiência própria de inspeção, camadas, shell e variantes antes do auto-build seguro.

**Causa**

- `MultiblockInfoCategory` herdava de `ModularUIRecipeCategory` e construía um mundo virtual LDLib; não existia um modelo client-side UFO derivado diretamente de `MultiblockDefinition`.

**Correção implementada**

- Criado `StructurePreviewModel`, snapshot imutável derivado da mesma `PreviewEntry`/definition usada pelo runtime, com células, roles, materiais, alternativas e ingredientes de foco JEI.
- A categoria foi substituída por `IRecipeCategory` e widget JEI nativos. Após a reprovação do primeiro visual provisório, o viewer foi portado para o contrato AE2LT: toolbar de ícones, label `Y n/total`, abas `Blocks/Details`, rotação, pan, zoom, seleção 3D por projeção, inspeção de role/posição/alternativas e lista rolável de blocos.
- LDLib, Yoga e Taffy foram removidos das dependências diretas. Glodium permanece somente em `localRuntime`, pois Extended AE e Applied Flux do pack de desenvolvimento declaram essa dependência; nenhum código ou artefato UFO depende dele.
- O viewer é um port adaptado de `InteractiveMultiblockPreview` do AE2 Lightning Tech sob LGPL-3.0, compatível com a licença LGPLv3+ do UFO. Cabeçalho e `LICENSE.md` registram o crédito; nenhum asset AE2LT foi copiado.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/client/preview/StructurePreviewModel.java`
- `src/main/java/com/raishxn/ufo/client/preview/StructurePreviewLayout.java`
- `src/main/java/com/raishxn/ufo/compat/jei/MultiblockInfoCategory.java`
- `src/main/java/com/raishxn/ufo/compat/jei/StructurePreviewWidget.java`
- `src/main/java/com/raishxn/ufo/registry/RegistryHandler.java`
- `src/test/java/com/raishxn/ufo/client/preview/StructurePreviewLayoutTest.java`
- `build.gradle`
- `gradle.properties`
- `.project-control/tests/testes-humanos-l-0020.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- `./gradlew compileJava compileTestJava --stacktrace`: sucesso.
- `StructurePreviewLayoutTest`: dois testes aprovados, cobrindo classificação de shell e os três contratos de visibilidade por camada.
- `./gradlew test --stacktrace`: sucesso com 80 testes.
- `./gradlew build --stacktrace`: sucesso com compilação, 80 testes, datagen e empacotamento após manter Glodium restrito ao runtime local.
- A primeira inspeção humana mostrou frame e tooltips, mas estrutura e linhas de materiais invisíveis. O widget passava coordenadas locais da receita para `GuiGraphics.enableScissor`, cuja API exige coordenadas absolutas de tela; o offset aplicado pelo JEI excluía todo o conteúdo recortado. `enableLocalScissor` agora incorpora a translação corrente da pose nas duas regiões.
- A segunda inspeção reprovou mistura de idiomas, aparência provisória, ausência de seleção e shell inefetivo no Stellar. A causa do shell era geométrica: o template Stellar é esparso dentro de 35×34×35, portanto quase nenhum bloco ocupado tocava a borda do volume. O modelo agora omite air ignorado e classifica casing/glass/frame estrutural semanticamente.
- Rebuild do port AE2LT aprovado com 83 testes, datagen e empacotamento em conjunto com L-0021.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `65619b677f65337bda53697b95475234a07c8731b7c99761b03d92503c6170ce` após centralização, ícone `quick_build`, glow de seleção e alternatives Mk.I–III.
- `git diff --check`: sucesso.

**Validação humana**

- H1 abertura e enquadramento das cinco structures; H2 interação de câmera/shell/layers/variantes; H3 materiais, tooltips e focos JEI; H4 compatibilidade sem erros LDLib.
- Roteiro: `.project-control/tests/testes-humanos-l-0020.md`.
- Follow-up visual de 2026-09-02: a captura confirmou auto-build funcional, mas mostrou ícones/rótulos fora do centro óptico e ausência da moldura clara em torno do recipe body. O port havia truncado os bitmaps AE2LT de nove para sete linhas, usava offsets fixos e retornava `needsRecipeBorder() == false`. Os bitmaps completos, centralização pela ink bounds/`font.lineHeight`, bevel do label e borda JEI padrão foram restaurados; recompilação e 83 testes passaram.
- Segunda inspeção do follow-up considerou o layout quase perfeito e identificou somente falta de feedback persistente da seleção e alternatives incompletas. O pass de glow do AE2LT foi portado com overlay azul-claro nas seis faces (seleção/hover), e Details passou a separar `Fixed block` de slots reais com tooltips. A causa dos tiers ausentes era metadata divergente: as predicates de QMF/QPA/Slicer/Cryoforge aceitavam Mk.I–III, mas `displayCandidates` declarava apenas Mk.I; todos agora usam `allFieldCandidates()` sem alterar validação, defaults ou auto-build.
- Em 2026-09-02, o autor confirmou o follow-up completo: `tudo funcionando perfeitamente`. L-0020 aprovado.

**Pendências e riscos**

- O renderer nativo precisa de inspeção visual no client para confirmar iluminação, clipping, escala das structures grandes e captura de input pelo JEI.
- A seleção mostra o estado representativo, role, posição e até oito alternativas da definition. Destaque luminoso do bloco selecionado pode ser refinado depois se a seleção funcional for aprovada.
- A integração completa de slots JEI individuais no painel Details ainda pode ser refinada; todos os blocos continuam indexados como focus global da structure.

### L-0021 — Auto-build seguro no contrato AE2 Lightning Tech

**Data de início:** 2026-09-02
**Status:** concluído; validações automatizada e humana aprovadas

**Problema**

- O único auto-build existente ficava oculto no `StructureScannerItem`, exigia Creative + sneak e substituía blocos divergentes instantaneamente sem inventário ou preflight.
- Não havia widget contextual no controller nem um planner compartilhado pelas cinco definitions suportadas.

**Causa**

- `MultiblockPattern.assembleAsCreative` era uma conveniência antiga, não um fluxo server-owned; controller GUIs só expunham scan.

**Correção implementada**

- Portado o fluxo AE2LT para `MultiblockAutoBuildPlan<T>` Java puro: calcula somente posições faltantes com target placeable, relata ocupações divergentes e ordena bottom-up/outward sem tocar o mundo.
- `MultiblockAutoBuildService` faz preflight de chunks, bounds, obstáculos e inventário; cria sessão server-side por dimensão/controller e coloca um bloco por tick com som. Survival consome um item após cada colocação bem-sucedida; Creative não consome.
- Mudança concorrente, item removido, logout/troca de dimensão, controller removido ou falha de placement interrompem a sessão com segurança. Bloco divergente nunca é substituído.
- O packet usa menu, posição, distância, chunk e rate limit existentes. Universal e Stellar ganharam o widget lateral `quick_build` original do AE2LT, visível e ativo apenas enquanto `isAssembled == false`; o asset CC BY-NC-SA possui atribuição explícita em `LICENSE.md`.
- Defaults montam a parte determinística. Hatches/variantes configuráveis que a definition aceita mas não escolhe automaticamente permanecem para instalação explícita do jogador.
- Entropic Assembler Matrix não participa: não existe `PreviewEntry/getDefinition` para ela por decisão do autor.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockAutoBuildPlan.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockAutoBuildService.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockPattern.java`
- `src/main/java/com/raishxn/ufo/network/packet/PacketAutoBuildMultiblock.java`
- `src/main/java/com/raishxn/ufo/network/MachinePacketGuard.java`
- `src/main/java/com/raishxn/ufo/network/ModPackets.java`
- `src/main/java/com/raishxn/ufo/screen/AbstractUniversalMultiblockControllerScreen.java`
- `src/main/java/com/raishxn/ufo/screen/StellarNexusControllerScreen.java`
- `src/main/java/com/raishxn/ufo/client/gui/widget/UfoQuickBuildButton.java`
- `src/main/resources/assets/ufo/textures/gui/buttons/quick_build.png`
- `src/test/java/com/raishxn/ufo/api/multiblock/MultiblockAutoBuildPlanTest.java`
- `LICENSE.md`
- `.project-control/tests/testes-humanos-l-0021.md`

**Validação automatizada**

- `./gradlew compileJava compileTestJava --stacktrace`: sucesso.
- Três testes novos cobrem slots matching/ignored/controller, ocupação divergente sem replacement e ordenação bottom-up/outward.
- `./gradlew test --stacktrace`: sucesso com 83 testes.
- `./gradlew build --stacktrace`: sucesso com compilação, 83 testes, datagen e empacotamento.
- O JAR contém `UfoQuickBuildButton.class` e o `quick_build.png` original, com hash do asset idêntico ao repositório AE2LT (`52573f45271c173b8fc8461fce054c490d520adef68fda140d5eb7f92d3c45f8`).
- JAR/hash e `git diff --check` são os mesmos registrados no follow-up do L-0020.

**Validação humana**

- Em 2026-09-02, o autor confirmou: `autobuild funcionando perfeitamente`.
- Roteiro preservado em `.project-control/tests/testes-humanos-l-0021.md`.

**Pendências e riscos**

- Sessões são runtime e canceladas em logout/troca de dimensão/restart; nenhum estado temporário foi adicionado ao save.
- O scanner Creative antigo ainda existe por compatibilidade, mas não é usado pelo novo widget. Sua remoção/depreciação deve ocorrer separadamente após aprovação do replacement.

### L-0022 — Migração versionada da topology/definition da Quantum Cryoforge

**Data de início:** 2026-09-02
**Status:** concluído e aprovado

**Problema**

- A Quantum Cryoforge era a última máquina universal cujo controller ainda cacheava somente um `MultiblockPattern` legado.
- Runtime e apresentação não consumiam uma única definition compilada, mantendo a máquina no polling estrutural de 200 ticks e fora da state machine/membership event-driven já aprovadas no Slicer, QMF e QPA.

**Causa**

- As sete camadas estavam embutidas diretamente em `QuantumCryoforgePatternFactory`, sem schema puro/versionado.
- `QuantumCryoforgeControllerBE` não sobrescrevia `getMultiblockDefinition()`; a entrada de preview reconstruía um adapter separado a partir do pattern.

**Correção implementada**

- Criado `QuantumCryoforgeTopologySchema`, schema 1 e Java puro, com cópias defensivas das sete camadas legadas.
- `QuantumCryoforgePatternFactory` compila uma única definition estrita `ufo:quantum_cryoforge`; controller, scan FAST, membership, viewer e auto-build agora partem do mesmo objeto.
- Preservados exatamente: dimensões 6×7×7, anchor `(5,1,3)`, facing especial invertido, 294 células e contagens `A=64`, `B=158`, `C=1`, `D=9`, `E=38`, `F=24`.
- Roles permanecem `C=CONTROLLER`, `A=AIR` e `B/D/E/F=STRUCTURE`. O predicate de `B` continua aceitando casing ou qualquer universal hatch, sem exigir, mover ou reclassificar hatches existentes; `F` continua aceitando Mk.I–III.
- Nenhum ID de bloco/item/BE/menu, recipe, machine kind, campo NBT, multiplicador térmico, tier, processamento ou topologia foi alterado. Saves sem `runtimeState` continuam carregando pelos campos legados e recompõem o índice no primeiro scan.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/api/multiblock/topology/QuantumCryoforgeTopologySchema.java`
- `src/main/java/com/raishxn/ufo/block/entity/pattern/QuantumCryoforgePatternFactory.java`
- `src/main/java/com/raishxn/ufo/block/entity/QuantumCryoforgeControllerBE.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockControllerDefinitions.java`
- `src/test/java/com/raishxn/ufo/api/multiblock/QuantumCryoforgeTopologySchemaTest.java`
- `.project-control/tests/testes-humanos-l-0022.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Testes focados de `QuantumCryoforgeTopologySchemaTest` e `MultiblockTemplateCompilerTest`: sucesso.
- Os dois testes novos cobrem schema, dimensões, anchor, símbolos, contagem exata das 294 células e cópia defensiva.
- `./gradlew test --stacktrace`: sucesso com 85 testes.
- `./gradlew build --stacktrace`: sucesso com compilação, 85 testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `62306f20c6b5fe785c4b504e200c262476a85558a6461a2e51b6bd77629e1456`.
- `git diff --check`: sucesso também após a consolidação documental final.

**Validação humana**

- H1–H4 foram executados com êxito pelo autor em 2026-09-02: formação do save existente, invalidação/reforma orientadas a eventos, recipe/processamento e reconstrução após reload.
- O log confirmou a Cryoforge existente em `state=IDLE`, reconhecimento de `chemicalPorts=1`, transição imediata `IDLE -> UNFORMED -> IDLE`, seis scans/1.758 posições e atividade real com `storageOps=1267`.
- O usuário confirmou o resultado exato do processamento e do reload sem perda, duplicação ou migração manual.
- O cliente encerrou normalmente; `./gradlew runClient` terminou com `BUILD SUCCESSFUL` em 4m17s e overworld, Nether, End e Spatial Storage foram salvos. Não houve erro ligado aos caminhos novos do L-0022.

**Pendências e riscos**

- O índice de membership é somente runtime e é reconstruído após load; nenhum footprint foi adicionado ao NBT.
- A cobertura integrada passou, mas continua humana porque a suíte atual não possui GameTests de mundo para formação/reload.

### L-0023 — Contrato determinístico de autocrafting e byproducts

**Data de início:** 2026-09-02
**Status:** concluído e aprovado

**Problema**

- Controllers universais aceitavam processing patterns que anunciavam tanto a saída-base quanto o máximo possível dos Quantum Catalysts.
- Ao concluir, o total era sorteado. O CPU do AE2 registra exatamente a quantidade anunciada em `waitingFor`; portanto um pattern máximo podia esperar indefinidamente quando o sorteio entregava menos.

**Causa**

- `patternOutputAmountMatches` tratava base e máximo como promessas equivalentes, enquanto `finishRecipe` misturava a saída garantida e o bônus aleatório no mesmo saldo.
- O estado persistente não distinguia output prometido ao job de byproduct nem versionava o contrato usado no aceite.

**Correção implementada**

- Criada `AutocraftingOutputPolicy`, Java puro, que aceita somente a quantidade-base como promessa determinística e calcula os rolls garantidos/fracionários do bônus com saturação.
- Novos jobs gravam `outputPolicyVersion=1`. A saída-base entra em `pendingOutputs`; bônus continuam com a mesma chance, mas entram em `pendingByproducts`, são persistidos e drenados somente depois da saída prometida.
- Bloqueio, reload e quebra contabilizam e recuperam ambos os ledgers sem reroll.
- Saves antigos permanecem legíveis. Processos inacabados sem versão são concluídos uma vez com o máximo seguro de quatro vezes a base, cobrindo qualquer pattern base/máximo que o contrato antigo pudesse ter aceitado; saldos já preparados continuam exatamente como foram salvos.
- O diagnóstico de controllers paralelos expõe `pendingOutput` e `pendingByproduct` separadamente.
- Nenhum ID, recipe, custo, tempo, tier, topology ou capacidade dos quatro controllers foi alterado.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/block/entity/processing/AutocraftingOutputPolicy.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/ParallelProcessState.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractParallelMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/diagnostic/UfoDebugCommands.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/AutocraftingOutputPolicyTest.java`
- `.project-control/tests/testes-humanos-l-0023.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Teste focado de `AutocraftingOutputPolicyTest`: sucesso com seis casos novos.
- Os testes cobrem promessa exclusivamente base, hit/miss fracionário, rolls garantidos, máximo informativo, migração legada e saturação/entradas inválidas.
- `./gradlew test --stacktrace`: sucesso com 91 testes.
- `./gradlew build --stacktrace`: sucesso com compilação, 91 testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `2081fc6d0c90df92c10c9abdd5cbc80db479a60231c9b94462923508fbb96f8a`.
- `git diff --check`: sucesso antes da consolidação documental.

**Validação humana**

- H1–H4 aprovados pelo autor em 2026-09-02: job base, recusa do máximo probabilístico, bloqueio/reload e recuperação na quebra.
- O teste usou um Quantum Catalyst T3, com 50% de chance. H1/H3 produziram somente o output-base e H4 sorteou byproduct, comportamento probabilístico esperado.
- O primeiro H2 com `1 -> 4` foi desconsiderado após o autor esclarecer que havia somente um catalyst. O reteste conclusivo usou `1 -> 2`, máximo que o código antigo aceitava nessa configuração, e confirmou que o novo contrato recusa o job sem iniciar nem consumir dirt.
- O log registrou `pendingOutput=1 pendingByproduct=0` antes e depois do reload. Em um lote posterior registrou `pendingOutput=9 pendingByproduct=4`, preservado em `OUTPUT_BLOCKED -> UNFORMED -> OUTPUT_BLOCKED`, confirmando contabilidade independente durante deformação/reforma.
- O primeiro `runClient` encerrou com `BUILD SUCCESSFUL` em 13m22s e o reteste H2 em 2m18s. Ambos salvaram overworld, Nether, End e Spatial Storage sem erro dos caminhos do L-0023.
- Roteiro e resultados: `.project-control/tests/testes-humanos-l-0023.md`.

### L-0024 — Contrato explícito de dependências no metadata

**Data:** 2026-09-02
**Status:** concluído e validado automaticamente

**Problema**

- O JAR declarava somente Minecraft e NeoForge, embora classes carregadas pela UFO dependam diretamente de AE2, AE2AddonLib, GeckoLib e Mekanism.
- Uma instalação incompleta podia avançar até o carregamento de classes e terminar em `NoClassDefFoundError`, sem a mensagem antecipada e específica do loader.

**Causa**

- O template `neoforge.mods.toml` não acompanhou o grafo runtime do código.
- A auditoria original também confundia dependências diretas, transitivas e do ambiente de desenvolvimento: GuideME já é obrigatório por meio do metadata do AE2; Glodium pertence aos addons do dev pack; LDLib, Yoga e Taffy já saíram do build da UFO.

**Correção implementada**

- AE2 `[19.2.17,20)`, AE2AddonLib `[1.0.3-1.21.1,2)`, GeckoLib `[4.8.2,5)` e Mekanism `[10.7.18,11)` passaram a ser dependências obrigatórias `BOTH`, ordenadas `AFTER` seus hosts.
- O range de Minecraft foi corrigido para a versão realmente suportada, `[1.21.1]`; NeoForge permanece `[21.1.216,)`.
- JEI foi declarado opcional e client-side; Applied Mekanistics e Applied Flux foram declarados opcionais em ambos os lados. Ausência dessas integrações não virou erro de carregamento.
- GuideME não foi duplicado: AE2 19.2.17 já declara `guideme` obrigatório `[21.1.1,)`, e a versão 21.1.14 continua presente no build. LDLib e Glodium não aparecem no metadata da UFO.
- Nenhum ID, save, recipe, registro ou comportamento de gameplay foi alterado.

**Arquivos alterados**

- `gradle.properties`
- `build.gradle`
- `src/main/templates/META-INF/neoforge.mods.toml`
- `src/test/java/com/raishxn/ufo/metadata/ModMetadataContractTest.java`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Dois testes novos leem o `neoforge.mods.toml` processado e verificam os seis requisitos totais — plataforma mais quatro mods —, ranges, lados, ordenação e as três integrações opcionais; a suíte passou com 93 testes.
- A primeira execução focada expôs apenas que o parser do teste não aceitava comentários inline do template legado; a cobertura foi corrigida e o reteste passou.
- `./gradlew build --stacktrace`: sucesso em 11 s com 93 testes, carregamento real do metadata pelo NeoForge, datagen e empacotamento.
- O JAR final contém exatamente o contrato testado. SHA-256: `ea5f8935d36708c2281de8f64dc7feca2558798602f6eb1c6aeeed9977bc607f`.
- `git diff --check`: sucesso.

**Validação humana**

- Não necessária: o marco altera somente metadata de carregamento, e o loader NeoForge, os testes do resource processado e o artefato final validaram o contrato sem mudança de gameplay.

### L-0025 — Remoção da interceptação global do lifecycle AE2

**Data:** 2026-09-02
**Status:** concluído e aprovado

**Problema**

- `MixinAppEngBase` cancelava toda chamada a `AppEngBase.postRegistrationInitialization` depois da primeira por meio de uma flag estática global.
- Uma chamada legítima posterior deixaria de executar inicialização de grid linkables, storage cells, P2P, cauldrons, dispensers e upgrades do AE2, afetando também addons e testes.

**Causa**

- O mixin entrou no mesmo commit que moveu `UFORegistryHandler.onInit()` para o `enqueueWork`, sem justificativa, reprodução ou teste associado.
- O source de AE2 19.2.17 possui uma única chamada no próprio `FMLCommonSetupEvent`; nenhum caminho UFO chama esse método. O registro UFO é separado e já possui seu próprio guard idempotente.

**Correção implementada**

- `MixinAppEngBase` foi removido do código e de `ufo.mixins.json`.
- O timing e o conteúdo de `UFORegistryHandler.onInit()` foram preservados. AE2 voltou a ser o único owner de seu lifecycle pós-registro.
- Nenhum ID, save, recipe, registro UFO ou gameplay foi alterado.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/mixin/MixinAppEngBase.java` (removido)
- `src/main/resources/ufo.mixins.json`
- `src/test/java/com/raishxn/ufo/metadata/MixinConfigurationContractTest.java`
- `.project-control/tests/testes-humanos-l-0025.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Um teste novo garante que o manifesto de mixins não volte a interceptar o lifecycle global; suíte completa aprovada com 94 testes.
- `./gradlew build --stacktrace`: sucesso em 9 s. O NeoForge e AE2 concluíram common setup, datagen e empacotamento sem registro duplicado.
- O JAR não contém `MixinAppEngBase` e seu `ufo.mixins.json` preserva os outros oito mixins. SHA-256: `15286ed5df65b3a8e0083a4cff16181a97d213d0a8a7e6747c37274fbb0be188`.
- `git diff --check`: sucesso.

**Validação humana**

- H1/H2 aprovados pelo autor: a célula BigInteger UFO continuou reconhecida pelo ME Drive/terminal e o Cell Workbench continuou aceitando Inverter Card e Fuzzy Card.
- `runClient` encerrou com `BUILD SUCCESSFUL` em 5m22s e salvou overworld, Nether, End e Spatial Storage.
- Nenhum erro mencionou `postRegistrationInitialization`, `InitStorageCells`, `InitUpgrades`, `UFORegistryHandler` ou registro duplicado. Avisos do JEI sobre itens repetidos em creative tabs aparecem para vários mods e não são falhas de registro.
- Roteiro e resultados: `.project-control/tests/testes-humanos-l-0025.md`.

### L-0026 — Ownership compartilhado do voo de armaduras

**Data:** 2026-09-02
**Status:** concluído e aprovado

**Problema**

- O Astral Nexus marcava ownership mesmo quando `mayfly` já estava ativo por outra origem.
- `UfoArmorItem` concedia voo sem ownership e o revogava sempre que o conjunto deixava de estar ativo, podendo desligar voo externo.

**Causa**

- Os dois conjuntos alteravam diretamente a ability global e não compartilhavam uma política de aquisição/liberação.
- A tag legada do Astral registrava apenas que o set havia sido observado, não que a UFO havia realizado a transição `false -> true`.

**Correção implementada**

- `AstralNexusEvents` passou a ser o único ponto que concede ou revoga `mayfly` para todas as fontes UFO.
- A UFO só assume ownership quando encontra `mayfly=false` e efetivamente concede voo; voo já ativo é preservado como externo.
- O voo só é revogado quando nenhuma fonte UFO continua ativa, a ownership é confiável e o jogador não está em Creative/Spectator.
- Alternar entre Astral Nexus e qualquer conjunto energético baseado em `UfoArmorItem` não cria uma janela de revogação.
- A tag legada `ufoAstralNexusFlight` é removida como ownership não confiável. Saves continuam legíveis e a migração conservadora prefere não revogar voo cuja origem não pode ser provada.
- IDs, itens, recipes, energia, formação e demais bônus foram preservados.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/event/AstralNexusEvents.java`
- `src/main/java/com/raishxn/ufo/event/FlightOwnershipPolicy.java`
- `src/main/java/com/raishxn/ufo/item/custom/UfoArmorItem.java`
- `src/test/java/com/raishxn/ufo/event/FlightOwnershipPolicyTest.java`
- `.project-control/tests/testes-humanos-l-0026.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Cinco testes de política cobrem aquisição real, preservação de voo externo, troca entre fontes UFO, revogação exclusiva, Creative/Spectator e descarte seguro da tag legada.
- Teste focado: sucesso.
- `./gradlew build --stacktrace`: sucesso com 99 testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `e5ae3a78e1003fb3d27e8bff8d4264cdfde55ed8b6b9c6af425b1dcde637b0e0`.
- `git diff --check`: sucesso antes da consolidação documental.

**Validação humana**

- O autor informou que todos os cenários foram executados com êxito.
- Roteiro e resultado: `.project-control/tests/testes-humanos-l-0026.md`.

**Limite consciente**

- A ability vanilla é um booleano sem contagem de providers. Se uma fonte externa conceder voo depois que a UFO já o possui, não existe API nativa para detectar essa sobreposição. A regra implementada resolve o caso auditado — nunca assumir ownership sobre voo preexistente — sem inventar integração específica com outros mods.

### L-0027 — Cadência segura dos efeitos de armadura

**Data:** 2026-09-02
**Status:** concluído e aprovado

**Problema**

- Astral Nexus reaplicava Night Vision e Water Breathing e escrevia radiação zero em todo tick.
- O conjunto energético baseado em `UfoArmorItem` reaplicava Resistance X e Night Vision em todo tick.
- A auditoria inicial atribuía quatro refreshes por tick ao Thermal Exosuit, porém a classe citada não está registrada; os IDs atuais usam `UfoArmorItem`, e somente o peitoral executa a lógica.

**Causa**

- A duração longa dos efeitos era renovada incondicionalmente, sem consultar efeito atual, força, duração restante ou origem mais forte.
- A limpeza de ar/radiação também escrevia mesmo quando o valor já estava no alvo.

**Correção implementada**

- `ArmorEffectRefreshPolicy` aplica imediatamente efeitos ausentes/fracos e renova efeitos equivalentes somente nos 20 ticks finais da janela configurada.
- Efeitos mais fortes e efeitos infinitos de outras fontes são preservados.
- Astral Nexus só restaura ar quando necessário e só zera a capability de radiação quando o valor não é zero.
- A classe Thermal Resistor não registrada também foi endurecida: apenas o peitoral pode renovar Resistance, evitando quatro chamadas caso volte a ser usada no futuro.
- Duração, amplificadores, consumo de energia, voo, vida, step assist, proteção térmica, IDs, recipes e saves foram preservados.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/event/ArmorEffectRefreshPolicy.java`
- `src/main/java/com/raishxn/ufo/event/AstralNexusEvents.java`
- `src/main/java/com/raishxn/ufo/item/custom/UfoArmorItem.java`
- `src/main/java/com/raishxn/ufo/item/custom/ThermalResistorExosuitItem.java`
- `src/test/java/com/raishxn/ufo/event/ArmorEffectRefreshPolicyTest.java`
- `.project-control/tests/testes-humanos-l-0027.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Quatro testes cobrem aplicação imediata, limiar exato, preservação de efeitos mais fortes/infinitos e argumentos inválidos.
- Teste focado e `./gradlew build --stacktrace`: sucesso com 103 testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `ee5ba47e11fdb10aa58e062273087307f5beb59684c3f2a61a5bd92dd8a95cdb`.
- `git diff --check`: sucesso.

**Validação humana**

- H1–H3 aprovados pelo autor: Astral Nexus e conjunto UFO mantiveram seus efeitos por mais de 30 segundos, sem piscada/expiração nem mudança percebida no consumo; remoção e expiração continuaram normais.
- `runClient`: sucesso em 5m58s, com salvamento limpo de todas as dimensões.
- Roteiro e resultado: `.project-control/tests/testes-humanos-l-0027.md`.

### L-0028 — Orçamento do hazard quente do DMA

**Data:** 2026-09-02
**Status:** concluído e aprovado

**Problema**

- Cada DMA acima de 50% emitia 36 partículas por tick, equivalentes a 720 chamadas de envio por segundo.
- O loop declarava seis pontos por anel, mas iterava doze vezes com divisor angular seis, duplicando cada posição.
- A busca AABB por jogadores rodava em todos os ticks, embora o teste de dano dentro do loop só permitisse dano a cada 20 ticks.

**Causa**

- Geometria, envio visual e hazard de gameplay estavam no mesmo bloco sem cadência explícita.
- O limitador de dano foi colocado depois da consulta de entidades, eliminando somente o dano excedente, não o trabalho caro.

**Correção implementada**

- `DmaHazardCadence` formaliza três anéis, seis pontos únicos por anel, emissão a cada 10 ticks e busca de dano a cada 20 ticks.
- Emissões visuais são escalonadas pela posição do DMA para distribuir máquinas quentes entre ticks.
- O DMA agora envia 18 partículas duas vezes por segundo: 36 envios/s, redução de 20 vezes frente aos 720/s anteriores.
- A consulta AABB ocorre somente uma vez por segundo, redução de 20 vezes, preservando o mesmo tick global de dano.
- Temperatura/limiar, raios, dano de 4 pontos, fogo de 60 ticks, proteção Thermal/UFO, alarmes e explosão foram preservados. Nenhum ID, recipe ou save mudou.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/block/entity/processing/DmaHazardCadence.java`
- `src/main/java/com/raishxn/ufo/block/entity/DimensionalMatterAssemblerBlockEntity.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/DmaHazardCadenceTest.java`
- `.project-control/tests/testes-humanos-l-0028.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Quatro testes cobrem contagem/geometria única, throttle/escalonamento, cadência de dano e índices inválidos.
- Teste focado e `./gradlew build --stacktrace`: sucesso com 107 testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `3a90b1888d155064b462d485a18033697169b7a8a0eaf894face92463f845757`.
- `git diff --check`: sucesso.

**Validação humana**

- H1–H3 aprovados pelo autor: três anéis permaneceram reconhecíveis, dano/fogo continuaram aproximadamente uma vez por segundo e o conjunto completo continuou protegendo.
- O autor confirmou resfriamento/encerramento seguro. O log do cliente não contém uma sessão de servidor integrado para corroborar os saves, portanto essa parte fica corretamente atribuída ao resultado humano.
- `runClient`: sucesso em 2m29s, sem erro do caminho do DMA.
- Roteiro e resultado: `.project-control/tests/testes-humanos-l-0028.md`.

### L-0029 — Primeiro recorte do runtime dos controllers paralelos

**Data:** 2026-09-02
**Status:** concluído e aprovado

**Problema**

- Mesmo formada e ociosa, cada máquina paralela percorria todas as partes para reencontrar um nó AE2, recalculava o perfil dos catalysts, reconstruía até 27 DTOs/labels traduzidos e marcava o chunk como alterado em todo tick.
- O throttle de sync era consultado somente depois da construção do estado cliente, portanto não eliminava o custo que deveria limitar.

**Causa**

- Nó, perfil de upgrades e estado de apresentação não possuíam caches com invalidação ligada ao ciclo estrutural/inventário.
- Persistência, apresentação e atividade transitória compartilhavam um caminho de tick incondicional.

**Correção implementada**

- Os candidatos a nó AE2 são cacheados ao formar/revalidar a estrutura e consultados diretamente, preservando a ordem original das partes; referências removidas, grids inativas e nós sem energia continuam rejeitados.
- O perfil dos catalysts é cacheado e invalidado por `saveChanges`, preservando slots, tiers, bônus e saves existentes.
- Reconstrução de receitas exibidas, hashing e sync passam por uma janela explícita de cinco ticks; os caminhos desconectados obedecem à mesma cadência.
- `setChanged()` deixou de ser chamado pela máquina ociosa e só acompanha atividade persistente real antes/depois do tick.
- Nenhum ID, recipe, formação, capacidade de threads, ordem de seleção de rede ou contrato de processamento foi alterado.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/block/entity/AbstractParallelMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/ParallelRuntimeCadence.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/ParallelRuntimeCadenceTest.java`
- `.project-control/tests/testes-humanos-l-0029.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Quatro testes cobrem sync forçado, janela de cinco ticks, relógio reiniciado e detecção de atividade persistente.
- Teste focado e `./gradlew build --stacktrace`: sucesso com 111 testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `d300dfe06c200e4945934055e8e9b2bf442cf5e9d174728b88d0be845ad49bc3`.
- `git diff --check`: sucesso antes da validação humana.

**Validação humana**

- H1–H5 aprovados pelo autor: idle/GUI, conclusão exata, desconexão/reconexão AE2, troca de catalyst e encerramento passaram com êxito.
- No intervalo ocioso limpo, a QMF registrou `avg=4,05 µs`, `p95=3,06 µs`, `p99=174,62 µs`, `ticks=734`, `scans=0`, `storageOps=0` e `sync=0/0B`.
- A sessão também percorreu `RUNNING` e `PAUSED_NO_GRID`; `runClient` terminou com `BUILD SUCCESSFUL` em 15m22s e salvou todas as dimensões.
- Roteiro e resultados: `.project-control/tests/testes-humanos-l-0029.md`.

**Pendências e riscos**

- O update tag ainda carrega partes, upgrades, DTOs e estados completos. As amostras ativas ficaram próximas de 45 KiB por envio; separar DTO compacto/menu sync continua pendente.
- Operações de storage ainda são executadas por requisito/thread, chegando a `storageOps=5668` no cenário instrumentado; batching por chave continua pendente.
- Portanto o P1 de runtime paralelo foi reduzido, mas não está integralmente encerrado.

### L-0030 — Invalidação estrutural completa sem polling

**Data:** 2026-09-02
**Status:** concluído e aprovado

**Problema**

- A auditoria ainda descrevia polling estrutural a cada 200 ticks na base paralela. Esse polling já estava desativado para definitions compiladas, e todas as quatro subclasses concretas — Slicer, QMF, QPA e Cryoforge — já haviam sido migradas.
- Restava, porém, um ramo legado morto e cobertura incompleta para unload/reload de chunks, explosões e pistões.

**Causa**

- O membership index resolvia somente uma posição exata, sem associação reversa por chunk.
- O subscriber global cobria apenas break, place e neighbor notify; mudanças especiais podiam escapar da invalidação event-driven.

**Correção implementada**

- Removidos o contador e o ramo de polling periódico de `AbstractSimpleMultiblockControllerBE`; scan agora ocorre no primeiro tick/load, por `structureDirty` ou por pedido explícito do jogador.
- O índice passou a associar cada controller a todos os chunks do footprint, incluindo o chunk do próprio controller, com replacement/unregister/reset simétricos.
- Unload invalida controllers que permanecem carregados e remove registrations de controllers descarregados; load agenda revalidação após a promoção do chunk para evitar acesso precoce/deadlock.
- Explosões invalidam todas as posições realmente afetadas. Pistões invalidam origens, destinos e blocos destruídos após o movimento.
- O booleano `structureDirty` mantém o debounce natural: muitos eventos antes do próximo tick produzem um único scan FAST.
- IDs, NBT, recipes, roles, geometria, formação, ordem de parts e comportamento de processamento foram preservados.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/api/multiblock/IMultiblockController.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/StructureMembershipIndex.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractSimpleMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/event/MultiblockStructureEvents.java`
- `src/test/java/com/raishxn/ufo/api/multiblock/StructureMembershipIndexTest.java`
- `.project-control/tests/testes-humanos-l-0030.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Dois testes novos cobrem footprint em múltiplos chunks, coordenadas negativas e limpeza simétrica por replacement/unregister/reset; os três testes anteriores do índice continuam aprovados.
- Teste focado: sucesso.
- `./gradlew build --stacktrace`: sucesso com 113 testes, compilação, datagen e empacotamento; permanecem oito warnings preexistentes/depreciações.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `35c216511efa9b40517575841200859eaa91cc8d77961c9cd3529e4c8f94af54`.
- `git diff --check`: sucesso antes da validação humana.

**Validação humana**

- H1–H5 aprovados pelo autor: quebra/restauração comum, ausência de polling, unload/reload de chunk membro, pistão/explosão e encerramento passaram com êxito.
- Após reset da instrumentação, a QMF permaneceu `IDLE` com `scans=0` e `blocks=0` no intervalo observado.
- `runClient`: sucesso em 6m54s, com salvamento limpo de todas as dimensões e sem erro dos novos caminhos.
- Roteiro e resultado: `.project-control/tests/testes-humanos-l-0030.md`.

### L-0031 — Scan compilado e reconciliação seletiva

**Data:** 2026-09-02
**Status:** concluído e aprovado

**Problema**

- O matcher estrutural reinterpretava toda a matriz tridimensional em cada scan e métodos auxiliares repetiam os mesmos loops.
- A reconciliação de parts fazia `List.contains` dentro de outro loop, resultando em O(n²), e chamava `linkToController` para todas as parts mesmo quando nada havia mudado.
- O footprint era reempacotado e registrado novamente a cada scan; `setChanged()` podia sujar o chunk apesar de o estado persistente permanecer idêntico.
- Os controles Safe Mode, overclock e lock ainda eram botões textuais provisórios sem os sprites definitivos fornecidos pelo autor.

**Causa**

- `MultiblockPattern` conservava apenas a matriz fonte, sem uma representação linear compilada de células testáveis e offsets por símbolo.
- O controller não distinguia layout idêntico, troca real de part e part nova colocada na mesma coordenada.
- O índice tratava toda chamada de `register` como replacement, mesmo para footprints equivalentes.
- As telas usavam `Button` vanilla em vez de um widget capaz de combinar o atlas UFO com estados nativos do AE2.

**Correção implementada**

- `MultiblockPattern` compila uma lista imutável de células testadas na mesma ordem Y/Z/X e offsets imutáveis por símbolo; scan FAST/diagnóstico, tracked positions e posições esperadas reutilizam essa representação.
- O controller mantém o footprint packed por facing, compara layouts linearmente, cria `HashSet` somente quando há mudança e desvincula apenas membros removidos.
- Parts são religadas somente quando o BE naquela coordenada não aponta para o controller atual, preservando corretamente o caso de substituição de um hatch na mesma posição.
- `StructureMembershipIndex.register` tornou-se idempotente para footprints equivalentes, inclusive com ordem/duplicatas diferentes.
- `setChanged()` após scan ocorre somente quando layout ou algum estado persistente realmente muda.
- O widget `UfoStateIconButton` reutiliza fundo/hover/foco do AE2 e alterna entre sprites do atlas UFO e `Icon` nativo. Safe ON usa `(0,47)-(13,58)`, Safe OFF usa `Icon.INVALID`, overclock usa `(0,33)-(13,46)` e Stellar lock usa `Icon.LOCKED/UNLOCKED`.
- O tooltip duplicado observado na primeira rodada foi eliminado mantendo somente o `ITooltip` do AE2 na tela universal; o Stellar conserva o tooltip vanilla exigido por sua tela.
- IDs, NBT, recipes, roles, geometria, ordem de scan/parts, formação e processamento foram preservados.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/api/multiblock/MultiblockPattern.java`
- `src/main/java/com/raishxn/ufo/api/multiblock/StructureMembershipIndex.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractSimpleMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/client/gui/widget/UfoStateIconButton.java`
- `src/main/java/com/raishxn/ufo/screen/AbstractUniversalMultiblockControllerScreen.java`
- `src/main/java/com/raishxn/ufo/screen/StellarNexusControllerScreen.java`
- `src/main/resources/assets/ae2/textures/guis/universalgui2.png`
- `src/test/java/com/raishxn/ufo/api/multiblock/StructureMembershipIndexTest.java`
- `.project-control/tests/testes-humanos-l-0031.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Novo teste cobre idempotência do registration e aplicação de footprint realmente alterado.
- `./gradlew compileJava`: sucesso.
- `./gradlew build --stacktrace`: sucesso com 114 testes, compilação, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `febb92ccf02f99b7ddb3e904645ede69a8a7bb39e346578d6a8f7cecccdafed8`.
- `universalgui2.png` validado como PNG RGBA 256×256 e os recortes foram inspecionados em escala de pixel.
- `git diff --check`: sucesso.

**Validação humana**

- H1–H5 aprovados: formação/scan/invalidação, substituição e relink de part, widgets universais, estados do Stellar e reload/encerramento funcionaram corretamente.
- Após o único defeito visual encontrado e corrigido — tooltip universal duplicado — o autor confirmou o resultado como perfeito.
- Dois `runClient` terminaram com `BUILD SUCCESSFUL` em 1m56s e 40s, com todas as dimensões salvas.
- Roteiro e evidências: `.project-control/tests/testes-humanos-l-0031.md`.

**Pendências preservadas**

- O update tag ativo próximo de 45 KiB foi encaminhado ao L-0032 e acabou reduzido para snapshots entre `1.381 B` e `23.548 B` nas amostras aprovadas; somente o batching de storage por chave permanece como recorte P1 separado do runtime paralelo.
- A trilha geral de UI permanece pausada; este follow-up foi limitado aos widgets explicitamente fornecidos/autorizados pelo autor.
- O GuideME recebeu a primeira reformulação completa no L-0034, autorizada explicitamente pelo autor e mantida separada das correções de core.

### L-0032 — Snapshot compacto de visualização dos controllers paralelos

**Data de início:** 2026-09-02
**Status:** concluído e aprovado

**Problema**

- O update packet ativo reutilizava `saveAdditional`, enviando o NBT persistente completo — parts, upgrades, 27 processos e seus ledgers transacionais — somente para atualizar a GUI. A instrumentação do L-0029 mediu aproximadamente 45 KiB por envio.

**Causa**

- Persistência de disco e apresentação client-side compartilhavam `getUpdateTag`; `displayedRecipes` dependia do BE cliente, enquanto os escalares do menu já possuíam `GuiSync` próprio.

**Correção implementada**

- `getUpdateTag` agora contém apenas `viewerState`: escalares de apresentação e as linhas compactas de processo (ícone por ID, quantidade, progresso, índice e pause).
- O NBT de disco continua em `saveAdditional`, incluindo parts, upgrades, `processStates`, buffers, ledgers e timers; o cliente reconhece o marcador do snapshot sem tentar carregar esses dados persistentes.
- `displayedRecipes` deixou de ser gravado em novos saves por ser projeção transitória; saves existentes continuam legíveis e o runtime o reconstrói.
- O snapshot limita explicitamente as linhas ao máximo real de 27 threads. O envio ainda usa o update packet do bloco para manter compatibilidade de sincronização; direcionar snapshots exclusivamente a viewers permanece uma otimização posterior de transporte, sem reintroduzir NBT persistente.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/block/entity/AbstractSimpleMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/ParallelViewerSnapshotBudget.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/ParallelViewerSnapshotBudgetTest.java`
- `.project-control/tests/testes-humanos-l-0032.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Dois testes novos cobrem o limite de 27 linhas e rejeição de contagem negativa; a suíte passou com 116 testes.
- `./gradlew build --stacktrace`: sucesso com compilação, testes, datagen e empacotamento.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `177f81b7a34463a9a817d0ec66798b2fb2d307c550c3242eac831aec62989c4c`.
- `git diff --check`: sucesso.

**Validação humana**

- H1–H5 foram executados com êxito pelo autor: GUI, paginação de 27 threads, pause, `OUTPUT_BLOCKED`, desconexão/reconexão AE2 e reload preservaram comportamento e contabilidade.
- O snapshot pequeno mediu `1.381 B/update`; sob carga crescente, a maior amostra estável mediu `23.548 B/update`, aproximadamente 48% abaixo da baseline próxima de 45 KiB.
- O log confirmou estado `RUNNING`, 129 eventos instrumentados, encerramento normal e salvamento de overworld, End, Nether e Spatial Storage sem erro do snapshot/NBT/menu.
- Roteiro e evidências: `.project-control/tests/testes-humanos-l-0032.md`.

**Pendência preservada**

- O batching das operações de storage por chave continua como marco P1 separado. O transporte compacto ainda usa update packet de bloco para os jogadores que acompanham o chunk; direcionamento exclusivo ao viewer pode ser refinado em outro recorte sem voltar a enviar o NBT persistente.

### L-0033 — Batching transacional de storage por chave

**Data de início:** 2026-09-03
**Status:** concluído e aprovado

**Problema**

- Os controllers paralelos executavam `MEStorage.extract/insert` separadamente para cada requisito e cada uma das até 27 threads. Chaves iguais repetiam a mesma consulta e transferência no mesmo tick, ampliando o custo observado em `storageOps`.

**Causa**

- Cada `ParallelProcessState` simulava e efetivava sua transferência imediatamente. Não existia um plano compartilhado por tick que preservasse a ordem dos processos, agregasse quantidades por `AEKey` e redistribuísse resultados parciais.

**Correção implementada**

- Entradas de item e fluido agora compartilham disponibilidade simulada por chave: uma simulação reserva quantidades em ordem determinística e um único commit por `AEKey` distribui somente o valor realmente extraído.
- Outputs prometidos, byproducts e refunds de inputs rastreados são agregados por chave e distribuídos aos processos na ordem original. Aceitação parcial permanece no ledger do processo para nova tentativa; nenhum saldo rejeitado é descartado.
- Recipe inválida e remoção do controller reutilizam o refund agregado. Energia e chemical ports conservam suas políticas próprias; coolant e batching químico permanecem fora deste recorte.
- O helper puro `KeyedTransferBatch` usa ordem de inserção, soma saturada, clamp de retornos inválidos e alocação conservativa. IDs, NBT, recipes, buffers persistidos, limite de 27 threads e formato do snapshot L-0032 não foram alterados.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/block/entity/AbstractParallelMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/KeyedTransferBatch.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/KeyedTransferBatchTest.java`
- `.project-control/tests/testes-humanos-l-0033.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Cinco testes novos cobrem agregação em uma chamada por chave, ordem determinística, transferência parcial, clamp, saturação e lote vazio.
- `./gradlew compileJava --stacktrace`: sucesso.
- `./gradlew test --stacktrace`: sucesso com 121 testes.
- `./gradlew build --stacktrace`: sucesso com compilação, testes, datagen e empacotamento.

**Validação humana**

- O autor executou com êxito H1–H6: baseline de uma thread, 27 jobs iguais, escassez/Alternatives, output parcial ou bloqueado, desconexão AE2, reload/refund e encerramento.
- Foram confirmadas contabilidade exata, ausência de duplicação/perda e operação correta dos processos após o batching.
- Roteiro e resultado: `.project-control/tests/testes-humanos-l-0033.md`.

### L-0034 — Organização de assets, repositório e reformulação do GuideME

**Data de início:** 2026-09-03
**Status:** concluído e aprovado

**Problema**

- A raiz misturava snapshots Java, scripts e exportações de multiblocos com código de produção; o `.gitignore` terminava em bytes NUL e o site MkDocs gerado permanecia versionado.
- A árvore de texturas não distinguia claramente runtime e fontes de trabalho. Duas imagens `_ctm` pareciam ativas, embora não possuíssem um consumidor correto.
- O GuideME era plano, incompleto e continha afirmações especulativas, sem hubs, percurso inicial ou troubleshooting comparável à referência do AE2 Lightning Tech.

**Correção implementada**

- O `.gitignore` foi refeito em UTF-8/ASCII com regras ancoradas para Gradle, runs, IDEs, caches, site gerado e exports locais. `site/` saiu somente do índice do Git; os arquivos continuam presentes e regeneráveis pelo MkDocs.
- Ferramentas de multibloco, exports históricos, script legado e snapshots Java foram preservados sob `tools/`, com READMEs que explicitam ownership e finalidade. Nenhum save em `run/` foi apagado.
- A política de assets foi registrada em `docs/texturas-e-assets.md`; nenhuma textura runtime foi removida apenas por ser duplicada. A auditoria confirmou que `_ctm.png` não ativa CTM sozinho: uma metadata LDLib aponta para uma textura GTOCore e a outra variante está órfã.
- O GuideME recebeu índice orientado a progressão, hubs de máquinas/infraestrutura/materiais/equipamentos, getting started e troubleshooting. Páginas de máquinas, containment, catalysts, armaduras, storage e hatches foram reescritas com comportamento verificado no código, imagens de item/bloco, checklists operacionais e links hierárquicos.
- O follow-up inspirado na organização do AE2 Lightning Tech dividiu QMF, Slicer,
  QPA, Cryoforge e Stellar Nexus em páginas de construção, operação e controles.
  Cada construção usa `GameScene`/`ImportStructure` gerado diretamente da
  `MultiblockPattern` canônica, sem duplicar nem alterar a topologia runtime.
- A primeira inspeção visual expôs SNBT incompatível com o conversor textual do
  GuideME, quatro `ItemLink` sem owner e três IDs antigos de recipes. O gerador
  agora emite listas textuais, `entities` e uma `palette` completa como os exports
  do AE2 Lightning Tech. O próprio datagen executa a conversão usada pelo
  `ImportStructure`; os componentes estruturais possuem página própria e os
  recipes apontam para os JSONs `*_fragment` reais.
- A Entropic Assembler Matrix permanece deliberadamente excluída e identificada como futura reformulação, sem inventar topologia ou tutorial.
- Um teste de integridade valida páginas vazias, parents/links locais, IDs usados
  em componentes visuais, textos provisórios, colisões do índice, todo `ItemLink`,
  todo `<Recipe>` e a sintaxe dos SNBTs gerados.

**Arquivos principais**

- `.gitignore`
- `tools/README.md`
- `tools/multiblock/README.md`
- `tools/legacy/README.md`
- `docs/texturas-e-assets.md`
- `src/main/resources/assets/ufo/ae2guide/ufo_intro/*.md`
- `src/test/java/com/raishxn/ufo/docs/GuideContentIntegrityTest.java`
- `.project-control/tests/testes-humanos-l-0034.md`

**Validação automatizada**

- `GuideContentIntegrityTest`: 7 testes aprovados.
- `./gradlew build --stacktrace`: sucesso com 128 testes, datagen e empacotamento.
- No `runClient` do segundo follow-up, QMF, QPA e Stellar Nexus abriram suas
  páginas Construction e instanciaram `StructureTemplate` sem qualquer aviso do
  `PageCompiler`; o erro `missing from palette` não voltou a ocorrer.
- O segundo `runClient` carregou o GuideME sem conflitos de item index nem erros das páginas UFO; o primeiro revelou três owners duplicados do Pattern Hatch, corrigidos e cobertos pelo quarto teste.
- `git diff --check`: sucesso; `.gitignore` reconhecido como texto ASCII.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `271727ef226d047e9ded075ccffdab57ff01f7da98a6523334b087a7f9e57817`.

**Validação humana**

- O autor aprovou visualmente a reformulação do GuideME e classificou as cenas 3D dos multiblocos como perfeitas.
- Os SNBTs foram confirmados no formato do AE2 Lightning Tech, com `data`, `entities` e `palette`.
- H1–H5 do roteiro `.project-control/tests/testes-humanos-l-0034.md` estão aprovados.

### L-0035 — Remoção completa do tutorial/Ponder legado

**Data:** 2026-09-03
**Status:** concluído e validado automaticamente

**Problema**

- Um protótipo client-only inspirado em Ponder coexistia com o GuideME reformulado. Ele aparecia como botão `?` no Stellar Nexus, keybind universal `W` e hint nos itens de controller.
- O protótipo mantinha sua própria API, registry, geração de cenas, tela/renderizador 3D, descoberta de contexto e traduções, duplicando responsabilidades já cobertas pelo GuideME aprovado.

**Causa**

- O MVP havia sido ligado diretamente ao client setup, ao tick do cliente e às tooltips. A remoção de um único botão deixaria keybind, registros, tela e conteúdo morto acessíveis por outros caminhos.

**Correção implementada**

- Removidos os pacotes `com.raishxn.ufo.api.tutorial` e `com.raishxn.ufo.client.tutorial`, incluindo registry, entries/scenes/steps, descoberta por controller/item/slot/JEI e a tela/renderizador monolítico.
- Removidos o botão legado do Stellar Nexus, o keybind `OPEN_UFO_TUTORIAL`, seu registro, o hold de 12 ticks no cliente, o hint dos controllers e todas as traduções `ufo.tutorial.*`/`key.ufo.open_tutorial`.
- Removido `docs/ufo-ponder-plan.md` e atualizadas referências operacionais que ainda indicavam o protótipo como caminho de montagem.
- A auditoria comprovou que não existiam packet, mixin, PNG/model/blockstate ou dependência exclusiva do tutorial. Portanto nenhuma dependência foi alterada.
- Preservados integralmente `assets/ufo/ae2guide`, `GuideStructureProvider`, SNBTs, páginas, recipes, navegação, cenas GuideME, viewer JEI, holograma, auto-build e o botão normal do GuideME fornecido pelas telas AE2 compatíveis.
- Nenhum ID, save, recipe, buffer, gameplay ou topologia foi alterado. A Entropic Assembler Matrix continua excluída.

**Arquivos principais**

- `src/main/java/com/raishxn/ufo/api/tutorial/` (removido)
- `src/main/java/com/raishxn/ufo/client/tutorial/` (removido)
- `src/main/java/com/raishxn/ufo/UfoMod.java`
- `src/main/java/com/raishxn/ufo/UfoModClient.java`
- `src/main/java/com/raishxn/ufo/event/ModKeyBindings.java`
- `src/main/java/com/raishxn/ufo/event/ModTooltipEventHandler.java`
- `src/main/java/com/raishxn/ufo/screen/StellarNexusControllerScreen.java`
- `src/main/resources/assets/ufo/lang/en_us.json`
- `src/main/resources/assets/ufo/lang/pt_br.json`
- `src/test/java/com/raishxn/ufo/docs/LegacyTutorialRemovalContractTest.java`
- `docs/ufo-ponder-plan.md` (removido)

**Validação automatizada**

- Quatro testes novos impedem o retorno dos pacotes/classes, documento Ponder, textos/chaves, keybind/registro/handler e assets órfãos; o mesmo contrato exige que páginas, gerador e dependência GuideME permaneçam instalados.
- `./gradlew test --stacktrace`: sucesso com 132 testes.
- `./gradlew runData --stacktrace`: sucesso; todos os providers concluíram e o provider `UFO GuideME multiblock structures` permaneceu ativo.
- `./gradlew build --stacktrace`: sucesso com compilação, 132 testes, datagen e empacotamento.
- O JAR não contém caminho ou classe com `tutorial`/`ponder`; busca nos sources/resources de produção não encontrou os identificadores e textos removidos.
- JAR `build/libs/ufo-2.1-fix7.jar`, SHA-256 `f00ad75b0fcb3e0f024944cf3a6e743ba48b5218aab656facbec1856e395f8b1`.
- `git diff --check`: sucesso.

**Validação humana**

- Não exigida para fechar este marco: os pontos de entrada foram removidos estruturalmente e o build/datagen validou o carregamento. A próxima execução visual do Marco 2 também deve confirmar incidentalmente a ausência do botão/hint, sem reabrir o escopo.

### L-0036 — CTM nativo integrado ao UFO Future

**Data:** 2026-09-03
**Status:** concluído e aprovado visualmente

**Problema e causa**

- Os casings `quantum_hyper_mechanical_casing` e `entropy_singularity_casing` possuíam folhas CTM 32×32 sem consumidor; os models eram `cube_all` vanilla.
- O único metadata existente usava a chave `ldlib` e apontava para `gtocore:block/casings/sps_casing_ctm`. Sem esse backend externo, o Minecraft ignorava o metadata e exibia somente as bases 16×16.

**Correção implementada**

- Criado o loader client-side `ufo:connected_texture` sobre as APIs de geometria/model data do NeoForge 1.21.1, com predicado `ufo:same_block`.
- Cada face usa quatro quads e a folha compacta 4×4 de tiles 8×8; máscaras de quatro bordas e quatro diagonais resolvem linha, interior, canto interno e canto externo nas seis direções.
- O cálculo ocorre durante rebuild do chunk, sem tick ou packet; quads são cacheados por face/máscaras e faces internas do mesmo bloco são omitidas.
- Os dois models são gerados com paths exclusivamente `ufo:`. Itens usam models `cube_all` separados com a base 16×16; ausência de `ModelData` ou do sprite CTM degrada para a base.
- Removido o `.png.mcmeta` LDLib somente após o replacement funcional. Nenhuma dependência foi adicionada.
- Resource packs podem substituir models, bases e folhas nos paths documentados em `docs/texturas-e-assets.md`.
- Nenhum ID, save, recipe, buffer, regra ou topologia de multibloco foi alterado. A aparência conecta igualmente em estrutura formada ou desmontada.

**Defeito encontrado durante validação**

- O primeiro `runClient` mostrou todos os blocos CTM pretos, isolados ou conectados, enquanto o item vanilla permanecia correto.
- A causa era a seleção da sobrecarga inteira `setColor(1, 1, 1, 1)`: `QuadBakingVertexConsumer` interpreta os canais como 0–255. A correção usa `255, 255, 255, 255` e possui teste de regressão.

**Arquivos principais**

- `src/main/java/com/raishxn/ufo/client/ctm/`
- `src/main/java/com/raishxn/ufo/datagen/ConnectedTextureModelBuilder.java`
- `src/main/java/com/raishxn/ufo/datagen/ModBlockStateProvider.java`
- `src/generated/resources/assets/ufo/models/block/*_casing.json`
- `src/generated/resources/assets/ufo/models/block/*_casing_inventory.json`
- `src/generated/resources/assets/ufo/models/item/*_casing.json`
- `src/main/resources/assets/ufo/textures/block/multiblock/quantum_hyper_mechanical_casing.png.mcmeta` (removido)
- `src/test/java/com/raishxn/ufo/client/ctm/CtmTileSelectorTest.java`
- `src/test/java/com/raishxn/ufo/docs/CtmFaceGeometryContractTest.java`
- `src/test/java/com/raishxn/ufo/docs/ConnectedTextureContractTest.java`
- `docs/texturas-e-assets.md`
- `.project-control/tests/testes-humanos-l-0036.md`

**Validação**

- O datagen carregou o registro do loader, executou todos os providers e gerou models UFO autocontidos.
- O primeiro teste visual descobriu o defeito de cor; o segundo confirmou isolados, linhas, dois layouts em L, cantos internos/externos e casings em estrutura grande sem preto, costura ou orientação incorreta.
- Parede, piso e teto foram cobertos pelas seis tabelas de orientação e pela estrutura 3D; estruturas formada e desmontada preservaram o mesmo contrato visual. O autor aprovou o resultado como perfeito.
- `runClient` encerrou normalmente após 2m30s, salvando overworld, Nether, End e Spatial Storage.
- O log não contém erro de loader/model/CTM UFO; avisos de fluidos e recipes de addons já existentes são alheios ao marco.

### L-0037 — Limpeza segura do repositório e ownership de texturas

**Data:** 2026-09-03
**Status:** concluído e aprovado pelo autor

**Problema e causa**

- A raiz ainda continha treze changelogs históricos separados e a árvore de
  texturas possuía três layouts concorrentes para o mesmo conjunto UFO.
- Quinze resources existiam simultaneamente em `src/main/resources` e
  `src/generated/resources`; como o source set gerado tem precedência, os arquivos
  manuais eram cópias ocultas ou versões inefetivas de recipes/loot.
- Models customizados ativos e exports antigos tinham nomes parecidos, tornando
  perigosa uma limpeza baseada apenas em filename.

**Correção implementada**

- O histórico de releases foi preservado em `docs/releases/`, com links relativos
  válidos e README de ownership. Arquivos de build, licenses e ledger permaneceram
  na raiz.
- Os quinze conflitos manual/gerado foram resolvidos em favor do datagen já
  efetivo. Nenhuma recipe, loot table ou aparência empacotada mudou por essa
  remoção.
- Removidos 134 arquivos de textura versionados em árvores antigas:
  `block/crafting/`, `textures/ufoset/`, `item/ufoset/armors/`, overlays antigos
  de QPA/Slicer e exports soltos de ferramentas/animações. Também saíram quatro
  models `ufo_bow_pulling_*` sem consumidor e com referências legadas.
- Os models 3D ativos em `models/item/ufoset/` e o owner canônico
  `textures/item/ufoset/` foram preservados. Bow e fishing rod mantêm seus
  overrides; armor layers permanecem em `textures/models/armor/`.
- Nenhum path runtime ativo foi movido. CTM, GUI, fluidos, GuideME, saves, assets
  AE2 exigidos pelo loader e `coolant_fluid_hatch_overlay.png` foram preservados.

**Proteções e validação**

- `RepositoryAssetLayoutContractTest` possui seis testes para owners únicos,
  ausência das árvores legadas, pares PNG/mcmeta, assets estáveis, release logs e
  resolução do grafo local de models/OBJ/texturas.
- A primeira versão do teste tentou usar Gson, ausente no classpath puro; ela foi
  substituída por validação textual sem adicionar dependência. A segunda execução
  detectou corretamente models OBJ e o contrato foi ampliado para validá-los.
- `./gradlew test --stacktrace`: sucesso com 146 testes.
- `./gradlew runData --stacktrace`: sucesso; 685 resources foram catalogados e os
  quinze conflitos removidos não voltaram.
- `./gradlew build --stacktrace`: sucesso com testes, datagen e empacotamento.
- `runClient` carregou resources, entrou no mundo e iniciou o JEI sem citar nenhum
  path removido. A execução foi encerrada pelo terminal após o servidor integrado
  salvar overworld, Nether, End e Spatial Storage; os warnings de blockstates dos
  fluidos UFO já existentes não pertencem a esta limpeza.
- Roteiro visual: `.project-control/tests/testes-humanos-l-0037.md`; H1–H5 aprovados pelo autor.

## Marcos de interface — trilha pausada pelo autor

### UI-0001 — Base paginada da Universal GUI

**Data de início:** 2026-09-01
**Status:** visual, comportamento interativo e persistência H3 aprovados pelo autor

**Problema**

- A tela universal antiga apresentava até oito grupos como linhas de texto, escondia o inventário e não representava as até 27 threads paralelas individualmente.
- O layout provisório de 175×182 e os botões vanilla sobrepostos ao rodapé não correspondiam ao protótipo 176×256 aprovado pelo autor.

**Causa**

- `AbstractUniversalMultiblockControllerScreen` ainda renderizava a apresentação provisória construída antes das novas texturas.
- O style AE2 apontava para `universal_new_gui.png`, não incluía o inventário do jogador e não possuía paginação.

**Correção**

- `universalgui2.png` passou a fornecer o recorte principal exato `[40,0,176,256]`; `universalguipages.png` pertence ao namespace UFO e é desenhada em `(8,36)` como camada reutilizável de 160×107.
- Inventário e hotbar voltaram a ser interativos nas posições `(8,172)` e `(8,230)` da tela.
- As receitas ativas são exibidas individualmente em nove células por página. A quantidade de páginas deriva do maior valor entre capacidade sincronizada e processos recebidos; as máquinas universais de 27 threads resultam em três páginas, sem limitar as entrópicas a esse valor.
- Ícone, progresso, estado textual provisório e tooltip são renderizados por processo. Setas usam os sprites fornecidos no atlas e ficam desabilitadas nos limites.
- O scan ganhou um adapter UFO sobre o widget do AE2 e usa `Icon.SCHEDULING_DEFAULT`, célula `(0,240)` de `states.png`, conforme escolha do autor. O asset do AE2 é referenciado, não copiado.
- Pause/return por thread usa identidade estável, estado persistido/sincronizado e pacote server-side protegido; a ação não destrói nem reembolsa os buffers.

**Arquivos alterados**

- `src/main/java/com/raishxn/ufo/screen/AbstractUniversalMultiblockControllerScreen.java`
- `src/main/java/com/raishxn/ufo/client/gui/widget/UfoAe2IconButton.java`
- `src/main/java/com/raishxn/ufo/client/gui/widget/UfoAtlasButton.java`
- `src/main/java/com/raishxn/ufo/block/entity/IUniversalMultiblockController.java`
- `src/main/java/com/raishxn/ufo/block/entity/UniversalDisplayedRecipe.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractSimpleMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/AbstractParallelMultiblockControllerBE.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/ParallelProcessState.java`
- `src/main/java/com/raishxn/ufo/block/entity/processing/ProcessPauseState.java`
- `src/main/java/com/raishxn/ufo/network/MachinePacketGuard.java`
- `src/main/java/com/raishxn/ufo/network/ModPackets.java`
- `src/main/java/com/raishxn/ufo/network/packet/PacketToggleUniversalProcessPaused.java`
- `src/test/java/com/raishxn/ufo/block/entity/processing/ProcessPauseStateTest.java`
- `src/main/resources/assets/ae2/screens/universal_multiblock_controller.json`
- `src/main/resources/assets/ae2/textures/guis/universalgui2.png` (asset fornecido pelo autor, preservado)
- `src/main/resources/assets/ufo/textures/gui/universalguipages.png` (cópia byte a byte do export fornecido pelo autor)
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- PNGs confirmados como RGBA 256×256; a camada de páginas foi comparada byte a byte com o export do autor.
- Composição offline confirmou o encaixe 160×107 em `(8,36)`, os nove slots e os inventários sem sobreposição.
- `./gradlew compileJava --rerun-tasks --stacktrace`: sucesso; permanecem 16 warnings preexistentes/depreciações.
- `./gradlew test --stacktrace`: sucesso; 66 testes aprovados.
- `./gradlew build --stacktrace`: sucesso; compilação, 66 testes, datagen e empacotamento aprovados. A primeira tentativa confinada não iniciou por bloqueio ambiental de socket do lock do Gradle; a repetição autorizada fora do sandbox concluiu normalmente.
- Follow-up visual após o primeiro teste: `./gradlew compileJava --rerun-tasks --stacktrace`, `./gradlew test --stacktrace` e `git diff --check` aprovados; 66 testes permanecem ativos.
- Follow-up funcional de pause/resume: compilação integral e `git diff --check` aprovados; `./gradlew test --stacktrace` passou com 68 testes. Os dois testes novos cobrem rejeição de pause inativo, pause/resume ativo e reset do flag.
- `./gradlew build --stacktrace`: sucesso após o ajuste funcional e geométrico final; compilação, 68 testes, datagen e empacotamento aprovados.
- Refinamento visual após aprovação funcional: `./gradlew compileJava --rerun-tasks --stacktrace`, `./gradlew test --stacktrace` (68 testes) e `git diff --check` aprovados; permanecem somente os 16 warnings já conhecidos.
- Follow-up da centralização bidimensional de `RUNNING/PAUSED`: compilação integral, 68 testes e `git diff --check` aprovados; a validação humana do eixo Y permanece pendente.
- Compensação óptica de `+0,5 px`: compilação integral, 68 testes e `git diff --check` aprovados. Uma primeira execução concorrente de `test` colidiu com `compileJava --rerun-tasks` em outro daemon e observou classes parcialmente reconstruídas; a repetição sequencial após a compilação passou em 1 s, confirmando que não era falha do código.

**Validação humana**

- Primeiro ciclo parcialmente aprovado: a captura confirmou fundo, nove células, inventário/hotbar, paginação `1/3`, recipe e progresso sem sobreposição.
- Foram reprovados o posicionamento dos controles, o espaçamento da barra superior e a ausência do sprite pause/return. A causa visual foi confirmada: os controles UFO foram posicionados manualmente sobre o painel de upgrades da direita, a barra era uma string única e o sprite de ação nunca era desenhado.
- Follow-up implementado: scan/safe/overclock agora entram na `VerticalButtonBar` do AE2 abaixo do OpenGuide; os quatro campos superiores possuem áreas centralizadas próprias nos eixos X/Y.
- O segundo ciclo confirmou o sprite pause no slot, mas reprovou a ausência de clique e apontou deslocamento vertical de um pixel para baixo. O sprite deixou de ser desenho passivo e virou um widget 11×10 em `(40,22)` de cada célula ativa, exatamente 1 px acima da posição anterior.
- Pause/resume individual agora usa índice estável do `processStates`, estado persistido, sincronização explícita e payload protegido pela mesma validação de menu/posição/distância/chunk/rate limit das demais ações. Pausar não limpa recipe, progresso, energia, inputs nem outputs.
- O terceiro ciclo aprovou o clique e o comportamento funcional de pause/resume. Como refinamento visual final solicitado, o widget foi movido 2 px à esquerda, de `(40,22)` para `(38,22)`. A primeira correção de `RUNNING/PAUSED` centralizou somente o eixo X; o follow-up passou a calcular também o eixo Y pela altura real da fonte em escala 0,5 dentro da faixa 24×5 da célula.
- A captura `printUI/Imagem colada (4).png` mostrou que a centralização geométrica ainda parecia recortada no alto por causa da margem/baseline assimétrica da fonte pixelada. Foi adicionada compensação óptica de `+0,5 px` no eixo Y, sem alterar X nem o botão.
- O autor aprovou o alinhamento final após a compensação óptica (`ficou perfeito`). H1 e H2 estão encerrados; somente H3, persistência através de reload, não recebeu resultado humano explícito.

**Pendências e riscos**

- O estado `PAUSED` manual agora é sincronizado por thread e ligado ao índice estável do processo. Outros motivos transitórios de espera — por exemplo falta de grid, energia ou saída disponível — ainda dependem do estado global/contrato futuro e não recebem um motivo visual individual por célula.
- O controle implementa pause/resume, não cancelamento destrutivo. Um futuro botão de stop/cancel exige política explícita de refund para todos os buffers.
- A barra superior ainda não mostra `GRID ONLINE` de forma autoritativa porque essa informação não existe no contrato GUI atual.
- Safe Mode e overclock receberam os widgets definitivos fornecidos pelo autor no follow-up aprovado do L-0031; lock/unlock reutiliza os ícones nativos do AE2 no Stellar Nexus.
- O style continua carregado pelo namespace AE2 devido a `InitScreens`; migrar style/background para um loader UFO deve ser feito separadamente, sem misturar com a validação geométrica deste recorte.

### Decisão operacional e handoff para retorno ao core

- Em 2026-09-01, o autor pausou a trilha de UI para continuar o core do mod. Não abrir UI-0002 nem implementar o mockup JEI até nova autorização.
- O H3 de persistência do UI-0001 permanece documentado e pode ser retomado junto da próxima rodada de UI; ele não bloqueia o core porque o estado possui persistência automatizada e o comportamento interativo já foi aprovado.
- Widgets definitivos, categoria JEI própria e telas específicas ficam no backlog. O mockup conceitual v1 permanece em `/home/raishxn/MineProjects/printUI/mockup-jei-multiblocos-universal-v1.png`, apenas como referência.
- **L-0017 concluído e aprovado.** A auditoria limitou os item ports às roles físicas onde o contrato agrega valor, manteve processing patterns como owner dos itens entregues e não alterou receitas/balanceamento.
- Depois da validação humana da L-0017, não corrigir incrementalmente a `EntropicAssemblerMatrixBE`: o autor decidiu excluir e reformular totalmente esse multibloco. A remoção/substituição terá marco próprio e política explícita para IDs/saves.
- Os recortes do `ThermalSystem`, port de coolant e dissipação/resfriamento idle já foram concluídos e validados em L-0013–L-0015; não reabri-los sem achado novo.
- L-0018–L-0022 foram aprovados. A migração estrutural compatível da Cryoforge está encerrada; não repetir viewer nem auto-build.
- L-0023 foi aprovado no jogo e L-0024 fechou automaticamente o metadata obrigatório/opcional. Não declarar GuideME, LDLib ou Glodium como dependências diretas da UFO sem novo uso comprovado.
- L-0025 removeu a interceptação de `AppEngBase`; não voltar a cancelar lifecycle global do AE2 para contornar sintomas locais.
- Medir novamente throughput do AE Energy Input hatch e custos de bilhões somente no marco específico de energia/balanceamento; não aplicar buffs globais por percepção isolada.

## Análises paralelas concluídas

### A-0001 — Triagem de receitas e balanceamento do port Minecraft 26

**Data:** 2026-08-31
**Status:** concluída; nenhuma alteração de gameplay importada sem evidência

**Problema**

- O port `liwybloc/UFO_FUTURE_REMASTERED` possui mudanças recentes de recipes e uma auditoria própria; era necessário identificar conteúdo novo ou balanceamento aproveitável na linha 1.21.1/3.0.

**Causa do risco de importação direta**

- Minecraft 26 usa schemas de ingrediente diferentes da 1.21.1.
- O port contém três fontes que divergem: resources manuais, resources gerados e `ModRecipeProvider`; seu build prioriza a cópia manual e exclui o datagen da compilação.
- A documentação do port declara as receitas como WIP, e sua auditoria propõe custos T3 maiores enquanto o commit mais recente barateia fortemente a família Chrono.

**Resultado e decisão**

- Comparado o `main` do port no commit `9c3067f` contra o conjunto efetivo da base atual.
- A base 1.21.1 possui 283 recipes efetivas contra 270 do port; nenhuma existe somente no port.
- As 13 exclusivas da base atual (Pulsar, Stellar/Mekanism e Infinity/Mekanism) foram preservadas.
- Nove deltas numéricos reais foram isolados: Chrono T1/T2/T3, Scrap Box simples/bulk, UU Amplifier simples/bulk e Liquid Starlight simples/bulk.
- Nenhum foi importado: os buffs chegam a reduzir gates em 75%, acelerar etapas em 6,67× e dobrar outputs sem métricas que demonstrem necessidade.
- A observação sobre a baixa vazão do AE Energy Input hatch será usada na etapa apropriada para medir os custos em bilhões antes de rebalancear toda a cadeia.

**Arquivos alterados**

- `.project-control/audits/comparacao-receitas-port-26.md`
- `.project-control/audits/auditoria-ufo-3.0.md`
- `CONTINUITY_LEDGER.md`

**Validação automatizada**

- Inventário por caminho relativo: 283 recipes na base, 270 no port, 0 exclusivas do port e 13 exclusivas da base.
- Comparação semântica dos JSONs comuns, normalizando a representação de ingredientes entre as versões, isolou os nove deltas numéricos documentados.
- Nenhum arquivo de recipe/runtime foi alterado por esta triagem; o build validado da L-0011 permanece aplicável.

**Validação humana**

- Não aplicável à triagem. Qualquer rebalance futuro exigirá teste de progressão/throughput, não apenas carregamento do datapack.

**Pendências e riscos**

- Reavaliar Scrap/UU/Liquid Starlight somente após métricas de energia, ports e térmica.
- Acompanhar o port por ideias e correções, mas comparar sempre a intenção do código com o artefato efetivamente empacotado.
- Não remover receitas de integrações da 1.21.1 apenas porque as dependências ainda não existem no ecossistema Minecraft 26.

## Riscos conhecidos

- A suíte possui 128 testes puros e ainda não executa GameTests; cada correção estrutural precisa ampliar a cobertura proporcionalmente.
- IDs com typos não podem ser renomeados diretamente sem aliases/migração.
- Mixins em internals do AE2 podem quebrar em atualizações menores.
- Código derivado de projetos GPL não pode entrar acidentalmente no core LGPL.
