# UFO Future 3.0.0-alpha.1

O primeiro alpha público da 3.0 traz um novo trio de fabricação endgame, abastecimento dedicado de energia e coolant, armadura modular e melhorias de automação, estabilidade e desempenho.

**Antes de atualizar um mundo 2.1 ou 2.1-fix1–fix7, leia a seção de migração:** algumas estruturas precisam de ajustes manuais, e o antigo Entropic Assembler Matrix foi removido.

## Dependências

- **Nova dependência obrigatória:** [RaishxCore](https://github.com/Raishxn/RaishxCore/releases/tag/v0.1.0-alpha.2) `0.1.0-alpha.2` ou versão compatível, baixado no próprio repositório do Core. Quem usava alphas anteriores com o antigo `ufocore` deve substituir esse JAR.
- **Applied Energistics 2:** 19.2.17 ou superior da linha 19.x.
- **AE2 Addon Lib:** 1.0.3 para Minecraft 1.21.1 ou versão compatível da linha 1.x.
- **Mekanism agora é opcional:** armazenamento e processamento químico ficam disponíveis com ele instalado.
- **GeckoLib deixou de ser obrigatório para o UFO.** Mantenha-o se outro mod do seu pack, como AdvancedAE, precisar dele.

## Novidades

- **Novo trio de fabricação endgame**, substituindo o Entropic Assembler Matrix:
  - **Quantum Computation Nexus:** CPUs de crafting com recursos compartilhados na rede ME.
  - **Quantum Pattern Fabrication Matrix:** biblioteca de patterns de crafting, smithing e stonecutting, com automação do buffer de patterns pelo multibloco.
  - **Infinity Fabrication Singularity:** fabricação agregada em lotes, conectada à rede pelo Quantum Grid Link, com reutilização de catalisadores intactos.
- **Modo Nexus Infinito** para o Stellar Nexus.
- **Armadura UFO modular**, com upgrades quânticos.
- **Integração opcional com Productive Bees:** abelhas de Matter Ball, Scrap e Scrap Box, com ovos de obtenção exclusivos por receitas do Quantum Matter Fabricator (QMF). Sem Productive Bees, o conteúdo de abelhas não carrega.
- **Montagem automática nas interfaces do Stellar Nexus e dos controllers universais:** usa os materiais do inventário e preserva blocos divergentes já colocados.
- **Preview 3D nativo de multiblocos no JEI**, com camadas, rotação, zoom e detalhes dos blocos; disponível no EMI pela ponte JEI/EMI, com JEI instalado.
- **Guia GuideME reorganizado por progressão**, com cenas das estruturas atualizadas. O protótipo de tutorial/Ponder foi removido.
- **Texturas conectadas nativas nos casings Quantum e Entropy**, sem dependência externa de CTM.
- **Diagnóstico no jogo:** `/ufo debug machine` e `/ufo debug perf`, incluindo métricas de processamento, scans, armazenamento e sincronização, com exportação para análise.

## Mudanças e balanceamento

- **Abastecimento dedicado de coolant e energia:** Stellar Nexus, QMF, Quantum Processor Assembler (QPA), Quantum Slicer e Quantum Cryoforge agora exigem um ME Massive Fluid Hatch e um FE Energy Input Hatch nas posições designadas da estrutura. Abasteça o tanque de coolant por fora e conecte uma fonte FE ao hatch de energia.
- **O FE Energy Input Hatch consome apenas seu reservatório local:** não há consumo automático de energia da rede ME ou do armazenamento do Applied Flux. A Singularity também exige seu próprio hatch FE; a rede ME e as CPUs de crafting continuam precisando de alimentação separada.
- **Safe Mode e Overclock do Stellar Nexus** rebalanceados para custos energéticos de **2x e 8x**, respectivamente.
- **Receitas DMA/QMF rebalanceadas:** custos, energia, duração e lotes de component matrices, field generators, catalisadores, fragments e infinity cells foram revisados. Confira as receitas e recodifique os patterns afetados.
- **Stellar Nexus:** consumo de coolant proporcional ao calor e resfriamento enquanto ocioso.
- **Quantum Computation Nexus:** o paralelismo de crafting se ajusta à energia armazenada na rede, reduzindo a demanda quando o buffer está baixo para evitar quedas repetidas de alimentação.
- **Planner de crafting migrado para o RaishxCore**, com configuração `planner.enabled` por instância. Desativá-la usa o planner nativo do AE2 em novos pedidos; cálculos já enviados continuam em andamento.
- Removidos o mob experimental **Apocalypse Type-A** e seu ovo de spawn.

## Correções

- **Processamento paralelo transacional:** recursos e progresso são preservados para recuperação após rede cheia, recarga do mundo ou quebra da máquina, corrigindo perdas e duplicações nesses caminhos.
- **Autocrafting com catalisadores:** patterns prometem somente a saída garantida. Bônus são entregues separadamente e persistem quando bloqueados ou após reload, sem novo sorteio e sem deixar a CPU esperando um resultado probabilístico.
- **Células BigInteger:** corrigidos partition/inverter/fuzzy, contagem de bytes, limites e leitura de NBT, com suporte completo ao Cell Workbench.
- **Dimensional Matter Assembler (DMA):** receitas com múltiplos requisitos de fluido reservam e consomem o total correto; a identificação do coolant também foi corrigida.
- **Stellar Nexus:** outputs aguardam espaço na rede. Falhas térmicas têm efeitos destrutivos desativados por padrão, com ativação opcional na configuração.
- **Quantum Grid Link:** outputs pendentes persistem durante unload/reload físico do chunk. Na quebra do link, a recuperação devolve à rede o que ela aceita e ejeta o saldo em pacotes AE2, preservando quantidades e componentes.
- **Trio endgame:** estruturas se reformam e reconectam automaticamente após unload/reload completo dos chunks; membros retidos durante a desativação de chunks são desvinculados corretamente.
- Corrigido o travamento das CPUs de crafting quando providers agregados estão ocupados.
- Patterns da Singularity deixam de aparecer indevidamente como craftáveis no terminal.
- O wireless crafting volta a ser retomado corretamente após salvar e recarregar o mundo.
- Restaurada a exibição das receitas e categorias UFO com **JEI + EMI**, incluindo receitas químicas do Mekanism pela ponte entre os viewers.
- Corrigida a compatibilidade com **BiggerAE2**, o induction card do **Applied Flux**, os upgrades do **Quantum Pattern Hatch** e as células **Stellar ANY**.
- Restaurados o recebimento de FE das **UFO/Quantum Energy Cells** e a devolução de itens dos pattern hatches.
- Removida uma interferência no ciclo de carregamento do AE2 que afetava drives e upgrades das células UFO.
- Corrigida a remoção indevida de voo concedido por Creative, Spectator ou outros mods ao trocar armaduras UFO.
- Corrigido o carregamento da geometria do crafting cube; o DMA agora exibe status de coolant e upgrades.
- Armazenamento químico Pulsar e sua carcaça ficam ocultos no criativo/JEI/EMI sem Mekanism, mantendo os IDs para saves existentes.
- Ações das interfaces de máquinas receberam validações adicionais no servidor, incluindo menu aberto, distância e frequência dos pacotes.

## Desempenho

- **Scans estruturais orientados a eventos**, sem verificações periódicas de estruturas ociosas. Quebra de blocos, mudanças de chunks, explosões e pistões invalidam a estrutura quando necessário.
- Consultas a blocos entre chunks usam dados já carregados, na thread do servidor, sem forçar carregamento ou adicionar tickets de chunk.
- Menos trabalho por tick nos controllers ociosos e nos efeitos das armaduras; partículas e buscas de entidades do DMA quente foram cadenciadas.
- Sincronização das interfaces mais compacta, evitando enviar o NBT persistente completo a cada atualização.
- Transferências iguais para o armazenamento AE2 no mesmo tick são agrupadas por recurso, preservando a ordem e a consistência das operações.

## Migração do 2.1

1. **Faça backup completo do mundo, incluindo a pasta `data`, e teste em uma cópia.** Esta versão é um alpha.
2. Instale as dependências obrigatórias e atualize o UFO.
3. **Entropic Assembler Matrix e seu casing foram removidos:** blocos e itens antigos desaparecem ao serem carregados, sem conversão automática ou drops. Seus patterns de crafting podem ser transferidos para a Singularity do novo trio.
4. Nos Stellar Nexus, QMF, QPA, Quantum Slicer e Quantum Cryoforge existentes, troque as duas posições de casing indicadas pelo preview pelos hatches de fluido e energia. **Essas estruturas só formam após o ajuste.** Abasteça coolant e FE externamente.
5. Mova hatches do Stellar Nexus que estavam em posições livres para os slots de hatch designados.
6. Confira no JEI as receitas rebalanceadas e recodifique os patterns cujos ingredientes ou resultados mudaram. Verifique também receitas antigas em andamento antes de retomá-las.

Quem vem de alphas 3.0 anteriores também deve conferir o hatch FE da Singularity e a conexão externa do Quantum Grid Link à rede ME.

Guia completo: [migração do 2.1 para o 3.0](https://github.com/Raishxn/UFO-Future-1.21.1/blob/main/docs/releases/MIGRATION_2.1-to-3.0.md).

## Validação e limites deste alpha

- **26 GameTests**, cobrindo ciclo de vida, recuperação e leitura de dados legados, aprovados com e sem Mekanism, além das suítes de regressão no CI.
- **Teste ocioso:** 102 estruturas reais por 10.000 ticks, para verificar a ausência de scans periódicos após a inicialização.
- **Teste de carga ativa:** 102 Singularities por 10.000 ticks ativos, com outputs inicialmente bloqueados, liberação de espaço e desconexão/reconexão física da alimentação ME. Conferência exata de materiais e energia, sem outputs pendentes ao final.
- Fixtures automatizadas verificam buffers dos controllers, campos de energia Stellar e células com UUID do formato 2.1-fix7.
- Builds UFO/RaishxCore aprovados com zero warnings de javac; warnings bloqueiam o build, exceto a categoria `this-escape` documentada para registro NeoForge.

**Ainda faltam a validação de um mundo 2.x completo e a medição de desempenho no modpack real com jogadores e 100+ máquinas ativas.** Fixtures de saves e testes de carga sem jogadores não comprovam esses cenários. Procedimento: [QA da versão 3.0](https://github.com/Raishxn/UFO-Future-1.21.1/blob/main/docs/releases/RELEASE_QA_3.0.md).
