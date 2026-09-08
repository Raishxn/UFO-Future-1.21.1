# Proposta: Quantum Wireless — revisão de 7 de setembro de 2026

Status: especificação alvo; primeira etapa de infraestrutura implementada. Consulte [o estado real e as pendências](quantum-wireless-implementation.md). Esta revisão substitui a proposta anterior de interface por metas e de buffs exclusivos por família; não descreve todas as funcionalidades como já entregues.

## Decisões confirmadas pelo usuário

- Adicionar wireless à Quantum Pattern Hatch já existente. Não criar outra hatch/provider.
- Criar a Quantum Interface com a funcionalidade da Overloaded ME Interface do AE2 Lightning Tech, incluindo os widgets de auto-import, auto-export e wireless.
- Usar a textura fornecida pelo usuário: `src/main/resources/assets/ufo/textures/block/quantum_interface.png`, PNG RGBA de 16 × 16.
- Dar créditos ao AE2 Lightning Tech pelos widgets e pelas funcionalidades adaptadas.
- Desenvolver a proposta de escolha entre speed, energy discount e heat discount para DMA e multiblocos, com personalização para modpacks.

As fórmulas, valores padrão e regras de seleção abaixo continuam sendo sugestões, não decisões do usuário.

## Referência técnica e atribuição

Referência examinada: checkout local de AE2 Lightning Tech, commit `7e2e2726401e4ff4720e5f7efa9f4c0031b57c22`, em `/home/raishxn/MineProjects/AE2-Lightning-Tech`.

Origem: https://github.com/ae2lt/AE2-Lightning-Tech

Arquivos examinados:
- `client/OverloadedInterfaceScreen.java` e `client/TextureToggleButton.java`: widgets de modo, importação, exportação, velocidade e configuração de quantidade.
- `blockentity/OverloadedInterfaceBlockEntity.java`: modos, vínculos por face e agendamento das transferências.
- `logic/OverloadedInterfaceLogic.java`: configuração, armazenamento e integração com InterfaceLogic.
- `assets/ae2lt/ae2guide/overloaded-network/overloaded-interface.md`: comportamento documentado.
- `LICENSE`, `LICENSE_ASSETS.md` e README: código LGPL-3.0; assets CC BY-NC-SA 3.0.

Registrar cada arquivo efetivamente adaptado, commit de origem e alterações locais. Preservar avisos e acompanhar assets redistribuídos de sua atribuição e licença. Manter créditos visíveis no projeto e na documentação do mod, além de incluir os avisos necessários na distribuição. O inventário inicial está em `docs/credits/ae2-lightning-wireless.md`; atualmente ele identifica referências planejadas, não uma portabilidade concluída.

A textura quantum_interface.png foi fornecida pelo usuário e não deve ser atribuída automaticamente ao AE2LT. Os PNGs dos widgets têm origem separada da textura do bloco.

Preferir adaptação para o namespace UFO sem tornar AE2LT uma dependência obrigatória apenas para os botões. A referência utiliza detalhes internos de InterfaceLogic; compatibilidade com o AE2 19.2.17 do UFO deve ser verificada ao implementar. Não presumir que copiar as classes basta.

## Quantum Pattern Hatch existente

Preservar ID de registro, receita, inventário de 72 padrões, NBT, menu e vínculo estrutural dos mundos atuais. Adicionar modo local/wireless e gestão de destinos no mesmo bloco. A variante de cabo já existente deve receber comportamento coerente quando aplicável, sem inventar outro item de provider.

Separar o vínculo estrutural da hatch dos destinos de logística remota. Uma hatch integrante de um multiblock continua sendo parte física daquela estrutura. Ativar wireless não deve desmontá-la nem sobrescrever controllerPos com o endereço de um destino remoto.

No modo local, preservar o encaminhamento atual ao controlador vinculado. No modo wireless, encaminhar novos trabalhos apenas aos destinos selecionados. O controlador local só participa se explicitamente incluído, evitando encaminhamento inesperado. Jobs aceitos antes da troca de modo continuam pertencendo à máquina que os aceitou.

Cada execução de padrão vai inteira para um destino compatível. Distribuir múltiplas execuções entre destinos disponíveis; não dividir os ingredientes de uma única execução entre várias máquinas. Usar alternância ou balanceamento de carga com validação de receita, montagem, tier, vaga e espaço.

Os multiblocos já possuem contratos de aceitação de planos. O DMA precisa de um adaptador de trabalhos que preserve a execução exata e evite mistura com processamento automático independente. Não prender os insumos de uma encomenda inteira em um único DMA ocupado.

## Quantum Interface: paridade funcional como objetivo

A Quantum Interface terá o fluxo da Overloaded ME Interface, em vez de exigir cadastrar mínimo/alvo de coolant por máquina. A configuração da interface determina o que abastecer, e os destinos vinculados recebem os recursos.

| Função da referência | Comportamento pretendido no UFO |
| --- | --- |
| 36 slots de configuração | Configurar recursos e quantidades com a mesma experiência |
| Normal / Wireless | Atender vizinhos ou destinos remotos vinculados |
| Auto-export OFF / AUTO | Exportar conforme a configuração |
| Auto-import OFF / AUTO / EJECT | Desligar, importar ativamente ou aceitar ejeção passiva |
| Quantidade / fornecimento ilimitado | Quantidade configurada ou abastecimento contínuo, limitado pelo estoque real |
| I/O Normal / Fast | Agendamento adaptativo, com modo rápido |
| Filtro de importação | Restringir os recursos recolhidos |
| Vínculo por face | Permitir faces diferentes de um mesmo destino |
| FE com cartão de indução/AppFlux | Incluir essa integração opcional na paridade pretendida |

A referência documenta até 1.024 vínculos por face, mesma dimensão e alcance padrão de 128 blocos configurável, inclusive sem limite de distância quando configurado como zero. Usar isso como referência de paridade, substituindo a proposta anterior de 16/32 destinos. Capacidade de cadastro não significa examinar todas as faces em todos os ticks: manter orçamento de operações, backoff e medições em escala.

A referência usa estratégias por tipos de chave para I/O e trata FE separadamente. Confirmar suporte efetivo de itens/fluidos nos adaptadores de destino; não prometer qualquer recurso de addon sem validar sua estratégia. EJECT exige integração de recepção passiva, não é apenas outro nome para AUTO.

### Fluxo do coolant

1. Vincular as faces/destinos a abastecer.
2. Configurar o coolant na interface, com quantidade ou modo ilimitado.
3. Ativar wireless e auto-export.
4. A interface obtém o coolant da rede/buffer conforme a configuração e insere nos tanques aceitos pelos destinos.

O coolant continua sendo um recurso real: precisa estar disponível. Configurar o slot não cria fluido. Colocar uma quantidade física sem configurar reposição só fornece estoque finito; o fluxo exato de depósito/buffer deve seguir o comportamento da referência.

Para UFO, o adaptador identifica o tanque de coolant do DMA ou as hatches físicas do controlador. Respeitar capacidade, identidade do fluido e limites de inserção. Auto-import não deve drenar coolant ou insumos reservados para autocrafting; retornar apenas as saídas elegíveis. O jogador não precisa escrever uma regra térmica separada por máquina para abastecimento básico.

O tanque de coolant do DMA é separado do tanque de fluido de receita. A seleção correta é responsabilidade da integração. Um resto de 16 mB de Gelid pode ser completado com mais Gelid; não será descartado ou convertido em resfriamento gratuito.

Distribuir exportação entre conexões sem favorecer permanentemente a primeira. Respeitar as mesmas opções da referência antes de introduzir modos extras de abastecimento. Trocas automáticas de tipo de coolant e metas percentuais não fazem parte do fluxo básico confirmado.

### Importação e retorno de crafting

A interface pode recolher resultados pelo auto-import. O roteamento da hatch deve coordenar-se com esse retorno e com as saídas já existentes dos multiblocos: cada pilha ou fluido possui um único caminho responsável por transferi-lo.

Não extrair o mesmo resultado pelo provider e pela interface simultaneamente. Não prometer ao autocrafting bônus aleatórios como saída garantida. Rede cheia ou desligada conserva resultados em buffer persistente/máquina e exibe o bloqueio.

Onde houver retorno automático pela própria hatch, sua seleção precisa ser explícita e mutuamente coordenada com auto-import da interface. A experiência não deve obrigar o jogador a configurar dois importadores para a mesma máquina.

## Buffs customizáveis para ambas as famílias

Proposta: a hatch oferece um perfil de sinergia com três eixos, disponíveis tanto para DMA quanto para multiblocos:
- Speed: aumenta velocidade de processamento.
- Energy discount: reduz energia total por execução.
- Heat discount: reduz geração ativa de calor, sem aumentar eficiência do fluido nem apagar calor acumulado.

O jogador distribui um orçamento de 100 pontos entre esses eixos, por grupo de destinos. Presets facilitam o uso: Velocidade, Economia, Térmico e Personalizado. A interface de logística não concede um segundo bônus acumulável.

### Tetos iniciais sugeridos

| Família | 100 pontos em speed | 100 pontos em energy | 100 pontos em heat |
| --- | --- | --- | --- |
| DMA | Até +20% velocidade | Até -10% energia | Até -15% calor ativo |
| Multiblocos | Até +20% velocidade | Até -10% energia | Até -15% calor ativo |

Esses tetos iguais simplificam a primeira experiência. O modpack pode diferenciar DMA, multiblocos e controladores específicos. Não há rendimento extra, alteração de tier nem novas vagas de paralelismo.

Proposta de escala pela fábrica:
`s = min(1, max(0, (N - 1) / 15))`

Com frações de pontos pS, pE e pH, cuja soma é no máximo 1:
- Velocidade = `1 + 0,20 × s × pS`.
- Energia por execução = `1 - 0,10 × s × pE`.
- Calor ativo = `1 - 0,15 × s × pH`, aplicado sobre a geração ativa já ajustada para a velocidade.

| Fábrica | Perfil | Velocidade | Energia | Calor ativo* |
| --- | --- | --- | --- | --- |
| 10 elegíveis | 100% speed | +12% | Sem desconto | Sem desconto |
| 10 elegíveis | 100% energy | Sem ganho | -6% | Sem desconto |
| 10 elegíveis | 100% heat | Sem ganho | Sem desconto | -9% |
| 10 elegíveis | 50% speed / 25% energy / 25% heat | +6% | -1,5% | -2,25% |
| 16 elegíveis | 50% speed / 25% energy / 25% heat | +10% | -2,5% | -3,75% |

*Desconto térmico é relativo à geração após compensação de velocidade, não necessariamente menor calor por tick que uma máquina sem buff. No exemplo misto de 16 máquinas, `1,10 × 0,9625 = 1,05875`: aproximadamente +5,875% de calor ativo por tick, mas menor calor por trabalho idealizado. O valor real deve ser medido no motor térmico de cada máquina.

Não habilitar automaticamente os três tetos completos ao mesmo tempo. Um orçamento comum torna a escolha significativa. O modpack pode liberar uma política independente, mas isso deve ser explícito e validado, não o padrão.

### O que conta em N

Máquinas únicas, válidas, carregadas e compatíveis com o mesmo padrão normalizado, que aceitaram trabalho dele nos últimos 60 segundos; trabalhos ainda ativos também contam. Montagem é exigida quando aplicável. Faces e threads não multiplicam a contagem. Criativo fica fora. Essa janela continua sendo uma hipótese a medir.

Agrupar receitas permite organizar a UI, mas não permite que máquinas executando padrões baratos aumentem o bônus de um único destino executando outro padrão caro. O perfil pode ser compartilhado pelo grupo enquanto N é calculado por padrão compatível.

Fixar perfil e multiplicadores na aceitação de cada execução. Alterar pontos afeta novos trabalhos, não recalcula uma receita já paga ou em andamento. Troca de perfil não deve permitir receber desconto de energia no início e velocidade máxima no final do mesmo trabalho.

### Composição e custo real

Aplicar os fatores após ajustes de tier/catalisadores, com mínimos e tratamento de criativo preservados. Não acumular perfis de várias hatches na mesma execução. Aceleração preserva energia total antes do desconto escolhido, por isso pode aumentar a potência necessária.

Exemplo de 16 máquinas no perfil misto: potência sustentada idealizada `1,10 × 0,975 = 1,0725`, aproximadamente +7,25%, apesar do desconto de 2,5% por receita. Mostrar energia por receita e consumo por tick separadamente.

DMA e multiblocos geram calor por atividade. Acelerar sem compensação pode dar desconto térmico implícito por trabalho. Ajustar a parcela ativa para a velocidade e então aplicar heat discount, com acumuladores fracionários persistentes. Não multiplicar resfriamento passivo, temperatura existente ou calor externo.

Receitas de um tick respeitam o teto de execução real; buffs não criam saídas adicionais. Testar arredondamento do progresso, energia e calor para que bônus pequenos tenham comportamento consistente.

Custos de logística são separados dos buffs. Transferências em lote precisam ser eficientes para receitas bulk. Medir economia líquida incluindo o custo wireless; não anunciar o desconto bruto como economia total da fábrica.

## Personalização para modpacks

Dois níveis:
1. Jogador: escolhe o perfil/alocação dentro das permissões do servidor.
2. Autor do modpack/servidor: define disponibilidade, tetos, curva, orçamento e política de composição.

Proposta de configuração de servidor:
- Ativar/desativar sinergia sem desativar wireless.
- Permitir ou bloquear escolha do jogador; fixar presets.
- Tetos independentes de speed, energy discount e heat discount por família.
- Overrides por ID de controlador; política por receita/tag pode ser uma extensão de datapack.
- Pontos totais, eixos habilitados, limiar de máquinas, saturação e janela de atividade.
- Alcance, vínculos máximos, custos e orçamento de operações da logística.
- Permitir política alternativa de bônus independentes, opt-in.
- Regras de recarga documentadas: configurações novas afetam novas execuções; jobs existentes mantêm o snapshot, salvo regra administrativa explícita.

Chaves e valores são uma proposta de schema, ainda não existem no mod. Implementar pela configuração de servidor já utilizada pelo UFO e sincronizar o necessário à UI. Validar valores finitos, não negativos, descontos menores que 100% e limites que evitem overflow. Uma configuração local do cliente não concede buffs.

Não usar KubeJS como dependência obrigatória. Exposição por datapack/API de balanceamento pode ser acrescentada para packs que precisem de granularidade por receita.

## Interface e arte

Reutilizar/adaptar os mesmos widgets AE2LT para wireless, auto-import, auto-export e velocidade de I/O, com estados, tooltips e créditos corretos. Velocidade de I/O é velocidade de transferência; speed de sinergia é velocidade da receita. Mostrar em áreas diferentes para não confundir.

A Quantum Interface usa quantum_interface.png fornecida pelo usuário. A hatch existente mantém identidade visual e recebe os controles adicionais. A alocação de buffs fica em uma seção própria da hatch: pontos disponíveis, três eixos, presets e preview do ganho real para o grupo/padrão.

Para coolant, exibir mB com precisão suficiente: `16 / 16.000 mB`. Estados de logística incluem cheio, sem estoque, sem energia, face incompatível, fora de alcance e chunk descarregado.

## Integridade e desempenho

- Simulação não consome, aceitação transfere propriedade, resultados pendentes persistem.
- Não perder material ao alternar modos, desligar a rede, remover destinos ou reiniciar.
- Identidade do destino impede que outro bloco nas mesmas coordenadas herde um vínculo.
- Revalidar acesso e pertencimento ao grid; vínculo remoto não dá acesso entre redes alheias.
- Suspender destinos descarregados, sem carregar chunks automaticamente.
- Destinos por face para logística; identidade de controlador/máquina para sinergia.
- Adaptadores respeitam restrições de entrada/saída, reserva de crafting e tanques físicos.
- Orçamento por rodada, cache invalidável e backoff, sem varredura do mundo.
- Avaliar 10, 32, 100 máquinas e a capacidade máxima de vínculos da referência. Medir vazão e custo por tick.

## Sequência de implementação

1. Inventário preciso de widgets, assets e dependências da referência; avisos e atribuição por arquivo.
2. Núcleo de vínculos/transferência adaptado ao UFO e estados sincronizados de UI.
3. Quantum Interface: registro/modelo/textura/menu, paridade de import/export, slots, quantidades, filtro, wireless e integração opcional FE/AppFlux.
4. Wireless na hatch existente e integração DMA/multiblocos; preservar estruturas e dados antigos.
5. Perfil customizável de buffs, config de servidor, snapshots por trabalho e matemática fracionária.
6. Testes de conservação, reconexão, reinício, conflitos de import/export, tiers, catalisadores e escala; validação visual dentro do jogo.

Paridade funcional é o objetivo. Se uma função da referência não puder ser adaptada sem dependências adicionais, registrar a diferença concreta antes de declarar a interface equivalente. A proposta anterior de metas por máquina e limite fixo de 32 destinos não deve reaparecer silenciosamente na implementação.
