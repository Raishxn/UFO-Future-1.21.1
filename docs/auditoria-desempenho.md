# Auditoria de desempenho — UFO Future

Data: 2026-10-18 · base: `36fed36` (3.0.0, AE2 19.2.18) · mudanças **sem commit**.
Relatório irmão (biblioteca): `../RaishxCore/docs/auditoria-desempenho.md`.

## Resumo

O UFO já tem boa disciplina de tick: re-scan de estrutura só quando sujo, cache de receitas
(100 ticks), sync de cliente com hash e cadência de 5 ticks, `IGridTickable` dormindo quando
ocioso (Grid Link, DMA, hatches), limitador de aceleração externa e métricas por máquina.
Os achados abaixo são o que ainda custava trabalho proporcional ao tamanho da estrutura, ao
número de jogadores ou a cada frame. Foram aplicadas 7 mudanças seguras; 7 propostas
ficam para decisão.

Verificação depois das mudanças:

| Verificação | Antes | Depois |
|---|---:|---:|
| `./gradlew test` (unitários UFO) | 256/256 | 256/256 |
| `./gradlew runGameTestServer` (UFO, inclui RaishxCore do working tree) | — | 57/57 |
| `compileSoakTestJava compileLoadTestJava compileGameTestJava` (`-Werror`) | — | ok |

Não houve medição em jogo (perfilador/spark): os ganhos abaixo são contagens de operações
tiradas do código, não tempos medidos. As suites `runSoakGameTestServer`/`runLoadGameTestServer`
não foram executadas.

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
| 8 | FPS | `GhostHologramRenderer#onRenderLevelStage` (`client/GhostHologramRenderer.java:148`) | Com o holograma ligado, re-tessela **cada bloco fantasma a cada frame** via `renderBatched` (milhares de blocos no Stellar Nexus 35×34×35) + 1 `getBlockState` por bloco. | A (quando ligado) | M | proposta P1 |
| 9 | TPS | `AggregateCraftingExecutor.dispatch` (`crafting/AggregateCraftingExecutor.java:50`) | Roda em **todo tick de toda CPU de crafting** (mixin em `CraftingCpuLogic`), mesmo em grids sem provedor agregado: até 256 sondagens com `getProviders` e `System.nanoTime()`, e pula `offset` entradas do mapa a cada chamada (O(n) só para retomar). | M | M | proposta P2 |
| 10 | TPS | `StellarNexusControllerBE#serverTick` auto-start (`block/entity/StellarNexusControllerBE.java:587`) | Com auto-start ligado e insumos faltando, `startOperation()` → `getStartErrors()` roda todo tick: N extrações SIMULATE no ME + vários `Component`/`String.format`. | M | M (muda o momento do reinício) | proposta P3 |
| 11 | TPS | `ModBlockEntities#tickMegaCraftingUnit` (`init/ModBlockEntities.java:61`) | Cada bloco de um cluster de crafting UFO **não formado** chama `updateMultiBlock` a cada 20 ticks; cada chamada varre o cluster → O(N²) a cada segundo em clusters inválidos permanentes. | M | M | proposta P4 |
| 12 | TPS | `QuantumInterfaceBlockEntity#serverTick` (`block/entity/QuantumInterfaceBlockEntity.java:108`) | No modo rápido, recria `createExternalStorageStrategies` (mapa + wrappers) para até 4 alvos **todo tick**. | M | M | proposta P5 |
| 13 | TPS | `MachinePerformanceRegistry#recordTick` (`diagnostic/MachinePerformanceRegistry.java:17`) | `synchronized` + lookup em `LinkedHashMap` por máquina por tick, só para diagnóstico. | B | B | proposta P6 |
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

## Propostas pendentes (precisam de confirmação)

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
