# Auditoria de desempenho — UFO Future

Data: 2026-10-18 · base: `36fed36` (3.0.0, AE2 19.2.18) · rodada 1 em `7ccf92d`; rodada 2 nos commits seguintes.
Relatório irmão (biblioteca): `../RaishxCore/docs/auditoria-desempenho.md`.

## Resumo

O UFO já tem boa disciplina de tick: re-scan de estrutura só quando sujo, cache de receitas
(100 ticks), sync de cliente com hash e cadência de 5 ticks, `IGridTickable` dormindo quando
ocioso (Grid Link, DMA, hatches), limitador de aceleração externa e métricas por máquina.
Os achados abaixo são o que ainda custava trabalho proporcional ao tamanho da estrutura, ao
número de jogadores ou a cada frame. Rodada 1: 7 mudanças seguras. Rodada 2 (aprovada): as
propostas P1–P5 implementadas, P6 descartada após análise, P7 (medição) feita para o holograma.

Verificação depois das mudanças:

| Verificação | Antes | Depois |
|---|---:|---:|
| `./gradlew test` (unitários UFO) | 256/256 | 256/256 |
| `./gradlew runGameTestServer` (UFO, inclui RaishxCore do working tree) | — | 57/57 |
| `compileSoakTestJava compileLoadTestJava compileGameTestJava` (`-Werror`) | — | ok |
| Rodada 2: `test` / `runGameTestServer` | — | 256/256 · 57/57 |
| Rodada 2: `runSoakGameTestServer` | — | 2/2 |
| Rodada 2: `runLoadGameTestServer` (102 Singularities ativas, 10 705 ticks) | — | 2/2 · 0,667 ms/tick |
| Rodada 2: `runHologramClient` (frame time do holograma) | 7,15 ms/frame | 0,83 ms/frame |

Fora o holograma (medido com `runHologramClient`), os ganhos são contagens de operações tiradas
do código, não tempos medidos: não houve perfil com spark. O load test não tem número de antes
para comparar; o 0,667 ms/tick fica como referência para as próximas rodadas.

## Achados priorizados

Impacto: A = alto, M = médio, B = baixo. Risco da mudança proposta.

| # | Lado | Onde | Custo e frequência | Impacto | Risco | Estado |
|---|---|---|---|---|---|---|
| 1 | TPS | `AbstractParallelMultiblockControllerBE#getConnectedNetworkNode` (`block/entity/AbstractParallelMultiblockControllerBE.java:1154`) | `parts` é o footprint inteiro (QMF ≈ 337 células). Para achar o Pattern Hatch o laço fazia 1 lookup de chunk+BE por posição, **todo tick, por máquina** formada, até achar o hatch; sem hatch, varria tudo duas vezes. | A | B | **feito** |
| 2 | TPS | `NexusSharedCraftingCpuPool#containsCpu` (padrão da interface) | O AE2 chama `hasCpu` para cada crafting link em todo tick (`CraftingLinkNexus.isDead`); o padrão montava `getActiveCpus()` com stream + lista a cada chamada. | M | B | **feito** |
| 3 | TPS | `UfoArmorModule.byId` + `UfoArmorItem.isModuleEnabled` | `byId` fazia `Arrays.stream(values()).filter` por id; `isModuleEnabled` montava um `EnumSet` de todos os módulos instalados. Chamado ~15× por tick por jogador com armadura completa (`UfoArmorModuleEvents.onPlayerTick`) → ~100 streams/tick/jogador. | M | B | **feito** |
| 4 | TPS | `AdjacentEnergyExporter` (células de energia UFO/Quantum) | 6 `level.getCapability` sem cache por tick por célula (estado de bloco + BE + provedores a cada lookup). | M | B | **feito** |
| 5 | TPS | `HazardHandler` | `LOGGER.info` 3× por segundo por jogador segurando item perigoso (I/O de log na thread do servidor). | B | nulo | **feito** |
| 6 | FPS | `QuantumWirelessRenderer#render` | `CustomData.copyTag()` a cada frame enquanto a ferramenta está na mão: cópia profunda do NBT, com até 1024 links; mais 2 clones de `Direction.values()` por link. | M | B | **feito** |
| 7 | FPS | `StellarModelRegistry.clearCache()` nunca chamado | Após F3+T o renderer do Nexus mantinha os `BakedModel` do bake anterior (memória retida, modelo velho). | B | B | **feito** |
| 8 | FPS | `GhostHologramRenderer#onRenderLevelStage` | Com o holograma ligado, re-tessela **cada bloco fantasma a cada frame** via `renderBatched` (1820 blocos no Stellar Nexus) + 1 `getBlockState` por bloco. Medido: **6,6 ms de CPU por frame**. | A (quando ligado) | M | **feito (P1)** |
| 9 | TPS | `AggregateCraftingExecutor.dispatch` | Roda em **todo tick de toda CPU de crafting** (mixin em `CraftingCpuLogic`), mesmo em grids sem provedor agregado: até 256 sondagens com `getProviders` e `System.nanoTime()`. | M | B | **feito (P2)** |
| 10 | TPS | `StellarNexusControllerBE#serverTick` auto-start | Com auto-start ligado e algo bloqueando, `startOperation()` → `getStartErrors()` roda todo tick: todas as mensagens + N extrações SIMULATE no ME. | M | B | **feito (P3)**, sem mudar o tick do reinício |
| 11a | TPS | `ModBlockEntities#tickMegaCraftingUnit` — posse do Nexus | Achado na rodada 2: os módulos do Nexus ficam não formados de propósito, e `deferVanillaFormation` (lookup do controlador + `canOwnModuleAt`, 2 lookups de chunk) rodava **todo tick para cada módulo**, antes do escalonamento de 20 ticks. | M | B | **feito (P4)** |
| 11b | TPS | `ModBlockEntities#tickMegaCraftingUnit` — clusters inválidos | Cada bloco de um cluster UFO **não formado** chama `updateMultiBlock` a cada 20 ticks; cada chamada varre o cluster → O(N²)/s em clusters inválidos permanentes. O retry existe para recuperar CPUs no reload (`7ed0327`). | B | M | pendente (raro: só clusters inválidos) |
| 12 | TPS | `QuantumInterfaceBlockEntity#serverTick` | No modo rápido, recriava `createExternalStorageStrategies` (mapa + caches de capability) para até 4 alvos **todo tick**. | M | B | **feito (P5)** |
| 13 | TPS | `MachinePerformanceRegistry#recordTick` | `synchronized` + lookup em `LinkedHashMap` por máquina por tick. Sempre não-contendido (tudo na thread do servidor): dezenas de ns por máquina. | B | — | descartado (P6) |
| 14 | FPS | `StellarNexusRenderer` | `shouldRenderOffScreen=true`, distância 256 e `AABB` nova por frame. A malha é pequena (OBJ ≈ 10 KB); custo aceitável. | B | — | sem ação |

Verificados e sem problema relevante: `QuantumGridLinkBE`/`MassiveOutputHatchBE`/DMA (`IGridTickable`
dormindo quando ocioso), `AbstractParallelMultiblockControllerBE#machineTick` (cache de receitas,
perfil de catalisador e sync com hash já existiam), `getUpdateTag` dos controladores (só o
`viewerState`, não o footprint), telas (formatação só em tooltip ou poucas strings por frame),
`DimensionalMatterAssemblerRenderer`, `StructureHighlightRenderer`, destaque do martelo
(1 raycast por frame), `MultiblockAutoBuildService` (só com sessão ativa).

## O que foi mudado

1. **Índice dos endpoints de padrão** — `AbstractParallelMultiblockControllerBE`.
   No scan (`rebuildNetworkNodeCandidates`, que já percorria os `parts`), guarda as posições dos
   `QuantumPatternHatchBE` e `QuantumPatternProxyBE` na ordem dos `parts`. O lookup por tick
   percorre só essas posições (normalmente 1). A ordem e as regras são as mesmas (primeiro hatch
   decide; proxy só sem hatch; fallback para candidatos). Enquanto não há scan do layout atual
   (antes do primeiro tick após carregar, após `markStructureDirty`, após `loadAdditional`), usa
   a varredura completa original. Colocar/remover hatch ou proxy já marca a estrutura suja.
   Custo: de até ~337 lookups/tick/máquina (QMF) para 1.
2. **`containsCpu` sem alocação** — `NexusSharedCraftingCpuPool` percorre `active.values()` direto.
3. **Módulos de armadura** — `UfoArmorModule.byId` usa um `Map` imutável; `isModuleEnabled`
   consulta a lista de ids instalada (`contains(module.id())`), equivalente porque ids e módulos
   são um-para-um. `byId(null)` continua retornando `null`.
4. **Exportação de energia** — `AdjacentEnergyExporter.Targets` mantém um `BlockCapabilityCache`
   por lado (invalidado pelo próprio NeoForge); as duas células usam. A sobrecarga antiga sem cache
   continua existindo. Diferença: um vizinho em chunk não carregado agora dá `null` em vez de
   carregar o chunk.
5. **`HazardHandler`** — os 3 `LOGGER.info` por segundo viraram `debug`.
6. **`QuantumWirelessRenderer`** — lê o NBT por `CustomData.getUnsafe()` (somente leitura) em vez
   de copiar; `Direction.values()` em array estático.
7. **`StellarModelRegistry`** — `onBakingCompleted` limpa o cache, registrado em `UfoModClient`.

Mudança relacionada no RaishxCore (afeta o UFO em jogo): o pool compartilhado do Nexus não força
mais o AE2 a recalcular `currentlyCrafting` em todo tick; ver o relatório do Core.

## Rodada 2 — o que foi mudado

8. **Holograma em buffer de GPU (P1)** — `GhostHologramRenderer` tessela os blocos visíveis uma vez
   num `VertexBuffer` (mesma `RenderType.translucent()`, mesmas matrizes que o `BufferUploader` usa no
   caminho imediato) e só redesenha o buffer. Os quads translúcidos são reordenados quando a câmera
   anda mais de 1 bloco. A malha é reconstruída quando o conjunto de fantasmas visíveis muda
   (checado por tick em vez de por frame; reconstrução no máximo a cada 10 ticks) e a cada 10 s para
   atualizar luz e culling de faces. O buffer é liberado ao desligar o holograma ou trocar de mundo.
   Diferenças visuais possíveis: um fantasma pode continuar visível até 0,5 s depois de um bloco ser
   colocado no lugar, e a luz/culling dele pode ficar até 10 s desatualizada.
9. **Executor agregado (P2)** — `AggregateCraftingExecutor.mayHaveAggregateProviders(grid)` pula o
   `dispatch` em grids sem `QuantumGridLinkBE` (o único `IAggregateCraftingProvider`). CPUs sem grid
   (jobs virtuais do Nexus) continuam sondando. A lista precisa acompanhar novas implementações.
10. **Auto-start do Stellar Nexus (P3)** — `getStartErrors()` virou `collectStartErrors(firstOnly)`;
    o auto-start chama `hasStartBlocker()`, que para no primeiro bloqueio (antes das simulações no ME
    se o bloqueio for energia, cooldown, tier etc.) e só então `startOperation()`, que revalida tudo.
    O tick em que a máquina reinicia não muda; a GUI continua recebendo a lista completa.
11. **Posse dos módulos do Nexus (P4)** — em `tickMegaCraftingUnit`, a checagem de posse só roda no
    tick escalonado da unidade. As tentativas de formação acontecem nos mesmos ticks; uma posse órfã
    (controlador sumiu) é limpa no tick escalonado, até 19 ticks depois. A liberação normal pelo
    controlador (`releaseOwnedModules`) já forma o cluster na hora.
12. **Quantum Interface (P5)** — as estratégias externas ficam em cache por (posição, face), como no
    storage bus do AE2; elas resolvem a capability por `BlockCapabilityCache`, então seguem o alvo. O
    cache é limpo quando passa de `max(16, 2 × alvos)` entradas.

Ferramenta nova (só desenvolvimento): `./gradlew runHologramClient` abre um cliente numa cópia do
mundo de smoke, coloca um Stellar Nexus, mede os frames com o holograma desligado, com o buffer novo
e com o caminho antigo reproduzido no próprio teste, e salva screenshots em `run/screenshots`.

### Medição do holograma (P1)

Stellar Nexus, 1820 blocos fantasmas visíveis, 10 s por fase após 4 s de aquecimento, sem limite de
FPS, 1920×1080 (`runHologramClient`, máquina local):

| Fase | FPS | ms/frame | CPU do laço do holograma |
|---|---:|---:|---:|
| holograma desligado | 1395 | 0,72 | — |
| buffer novo | 1205 | 0,83 | (incluso nos 0,83) |
| caminho antigo | 140 | 7,15 | 6,59 ms/frame |

As screenshots do buffer novo e do caminho antigo saíram iguais.

## Propostas da rodada 1 — status

P1, P2, P3 e P5 implementadas (acima); P4 implementada na forma 11a, 11b continua pendente;
P6 descartada; P7 feita (holograma medido; soak/load executados — ver Verificação).

Texto original das propostas, para referência:

- **P1 — Holograma fantasma em buffer.** Tesselar os blocos uma vez num `VertexBuffer`/`MeshData`
  e redesenhar o buffer; reconstruir quando um bloco do footprint mudar (checagem a cada N ticks ou
  via evento de bloco). Ganho grande de FPS com o holograma do Nexus ligado. Risco médio: código de
  render novo, ordenação translúcida.
- **P2 — Atalho no executor agregado.** (a) Pular `dispatch` quando o grid não tem nenhum
  `IAggregateCraftingProvider` (contador mantido por adição/remoção de provedores) e (b) guardar o
  iterador/posição em vez de pular `offset` entradas a cada tick. Risco médio: toca o caminho de
  execução de crafting de todas as CPUs.
- **P3 — Backoff do auto-start do Stellar Nexus.** Depois de uma tentativa que falhou, tentar de
  novo a cada 10–20 ticks (ou ao mudar o estoque de insumos). Muda o tick exato do reinício, por isso
  não foi aplicado.
- **P4 — Formação de clusters UFO não formados.** Só uma posição por cluster (a menor `BlockPos`, por
  exemplo) chamar `updateMultiBlock`, ou re-tentar por evento de vizinhança em vez de por tempo.
  Precisa revisar a interação com `NexusCraftingUnitOwnership`.
- **P5 — Cache de estratégias externas da Quantum Interface.** Guardar o `ExternalStorageStrategy`
  por alvo (como o AE2 faz com `BlockApiCache`) e só recriar o wrapper quando o alvo mudar.
- **P6 — Métricas sem lock.** Objeto de métricas por BE (o BE já guarda a chave), sem `synchronized`
  nem lookup em mapa por tick; o registro só coleta referências.
- **P7 — Medição em jogo.** Rodar `runLoadGameTestServer`/`runSoakGameTestServer` e um perfil com spark
  num mundo com Nexus + QMFs para ter números antes/depois das mudanças acima.
