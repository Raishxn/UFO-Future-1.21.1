# Quantum Wireless — primeira etapa

Esta é a primeira implementação jogável da infraestrutura. Não representa paridade completa com a Overloaded ME Interface, nem conclusão de todos os itens da proposta.

## Implementado

- Quantum Interface registrada com a textura quantum_interface.png do usuário, receita, loot e aba criativa.
- 36 slots de configuração/armazenamento, em duas páginas de 18; quantidade configurada via menu AE2.
- Fornecimento contínuo por slot: Shift+clique no botão de quantidade. Retira do estoque real da rede, preservando material rejeitado e a capacidade do destino.
- Modo local/wireless, auto-import/export OFF/AUTO e velocidade de I/O normal/rápida.
- Reposição dos slots pelo InterfaceLogic do AE2 e transferência de itens/fluidos pelas estratégias de armazenamento.
- Retenção persistente de material rejeitado após uma simulação de transferência; novas transferências aguardam o retorno pendente.
- Ferramenta de vínculos por face, seleção da origem e alternância de modo com agachar.
- Vínculos persistentes com identidade do bloco, alcance de 128 configurável, até 1.024 faces; chunks não são carregados.
- Wireless na Quantum Pattern Hatch existente, encaminhando planos a ICraftingMachine e patterns de processamento a inventários/fluidos remotos (incluindo o caminho de entrada do DMA). O vínculo estrutural permanece separado.
- Entregas parciais usam a fila persistente do AE2, com destino fixado por posição/face/identidade; mudar o modo não redireciona insumos pendentes.
- Capacidade por slot de 1024 bytes do tipo de recurso, incluindo 8.192 B de fluido, equivalente ao limite do Overloaded Interface.
- Tooltips sem duplicação, toolbar externa e abertura do menu expandido corrigida na versão de cabo.
- HUD da ferramenta com origem, coordenadas, dimensão e última contagem registrada; páginas GuideME em inglês/português e ferramenta também na aba de itens.
- Preservação das travas AE2 na entrega remota e distribuição com cursor entre destinos.
- Widgets e ícones AE2LT com créditos e licenças incluídos no JAR.

## Como experimentar

1. Conectar a Quantum Interface à rede ME com recursos e energia.
2. Usar Quantum Wireless Tool na interface para selecionar a origem.
3. Usar a ferramenta na face do DMA ou na hatch física de coolant do multiblock para adicionar/remover o vínculo.
4. Ativar wireless pela tela ou agachando e usando a ferramenta na origem.
5. Configurar coolant nos slots da interface e habilitar auto-export. O fluido deve existir na rede.
6. Para padrões, selecionar a Quantum Pattern Hatch como origem, vincular controladores que aceitam planos e habilitar wireless.

Destinos AE já conectados a outra rede com mais de um nó são recusados. Dispositivos AE isolados podem ser vinculados; isso não fornece energia à máquina nem integra seu nó ao grid.

## Ainda pendente

- Wireless na variante de cabo do provider e adaptador dedicado de planos para o DMA (o caminho de inventário de processamento já está disponível).
- Vincular controlador para resolver automaticamente suas hatches de coolant: nesta etapa selecionar a hatch física.
- EJECT remoto, filtros de importação, FE/AppFlux e paridade completa de UI/agendamento.
- Lista editável de destinos, diagnóstico detalhado, vínculos em lote e ajustes de distribuição por ocupação.
- Retorno remoto de crafting coordenado com a hatch: usar os caminhos de saída existentes ou a interface, evitando dois caminhos concorrentes.
- Validação visual e funcional em mundo com 10+ máquinas, reinícios, chunks descarregados e rede cheia.

A ferramenta utiliza a textura original `quantum_wireless_tool.png`, exatamente 16×16 com transparência. Mockup e prompts estão em `docs/art/`. Nenhuma textura da ferramenta AE2LT foi copiada.

## Verificação automatizada

## Buffs implementados

Apenas a Pattern Hatch concede buffs. Os três efeitos são automáticos, definidos uma vez em config/ufo/wireless.toml: tetos padrão +20% velocidade, -10% energia e -15% calor, escalando de 1 a 10 DMAs ou de 1 a 4 multiblocos distintos (contagens separadas por família) que avançaram receitas nos últimos 20 ticks. Patterns diferentes e insumos wired/manuais também contam, desde que a máquina esteja vinculada a uma hatch wireless ativa. Não existem perfis nem pontos. Tetos independentes por família DMA/multiblocos.

Multiblocos paralelos salvam fatores em cada ParallelProcessState; o DMA salva seu trabalho atual. Não existem autorizações por inputs/outputs nem contexto de dispatch. Os fatores são fixados antes de processar/reservar energia e mantidos até concluir. Energia é descontada por execução, calor produtivo é compensado pela velocidade e preserva frações. O painel da hatch mostra conexões e atividade/fatores DMA / Multi; os controllers incluem os fatores médios dos trabalhos ativos. Os detalhes estão no GuideME em quantum_wireless_buffs.md.

Esta etapa cobre DMA, QMF, Slicer, Processor Assembler e Cryoforge; Stellar Nexus não recebe bônus. A janela de atividade é transitória e reinicia após reload; fatores dos trabalhos existentes são persistentes. Cada avanço renova atividade, inclusive em receitas longas. Destinos descarregados, fora do alcance, criativos ou apenas aguardando deixam de contar. Faces/threads não multiplicam contagem; hatches não acumulam efeitos. A primeira rodada após inatividade pode ser neutra; receitas seguintes usam a atividade construída.

WirelessBonusTest cobre efeitos automáticos simultâneos, saturação, energia/calor por receita e receitas mínimas. WirelessActivityWindowTest cobre inatividade, expiração, renovação, reload e dez DMAs produtivos sem tickets. Testes automáticos não substituem a verificação de crafting em um mundo real.

QuantumTransferTest verifica destino cheio, aceitação menor após simulação, extração menor que o solicitado e quantidades bulk acima de Integer.MAX_VALUE. Compilação, testes e geração de dados também verificam o registro de conteúdo e carregamento do mixin durante datagen. Isso não substitui a validação dentro do jogo.

Alcance local salvo por origem: 32 blocos por padrão, ajustável na UI (+1 / Shift -1), limitado por wireless.range (128 por padrão; 0 remove teto global). Reduzir suspende transferências fora do alcance sem apagar vínculos. Configurações wireless antigas em ufo-server.toml não são mais lidas; fonte autoritativa é config/ufo/wireless.toml no servidor. Os snapshots dos jobs existentes são mantidos.

O antigo wireless.buffs.saturationMachines foi substituído por wireless.buffs.dma.saturationMachines (10) e wireless.buffs.multiblock.saturationMachines (4). Valores antigos personalizados devem ser movidos explicitamente para as famílias desejadas.
