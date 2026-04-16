# PRD: Apiario Estelar e Centrifuga Estelar

## 1. Resumo

Esta proposta adiciona uma nova linha de processamento apicola de endgame ao UFO Future baseada em dois multiblocos:

- **Apiario Estelar**
- **Centrifuga Estelar**

O objetivo do design e simples:

- integrar **UFO Future + AE2 + Productive Bees**
- preservar a fantasia de operar muitas abelhas dentro de uma unica maquina industrial
- entregar **output em escala extrema**
- manter o sistema **seguro para TPS** mesmo em modpacks grandes

A ideia central e evitar simular grandes quantidades de entidades de abelha vivas. Em vez disso, os multiblocos tratam as abelhas como perfis de producao de alto throughput gerenciados pelo controller, e empurram saidas em lote diretamente para o AE2 por hatches massivos.

## 2. Objetivos do Produto

Adicionar um sistema apicola de endgame que:

- seja dramaticamente mais forte que setups normais de Productive Bees
- use os tiers de field do UFO Future como principal modelo de escalonamento
- suporte automacao massiva com AE2 sem spam de inventario
- consiga gerar **milhoes de combs ou comb blocks por minuto** no late game
- evite perda catastrófica de TPS causada por automacao cheia de entidades
- crie um loop industrial limpo em duas etapas:
  - **Apiario** para gerar comb, comb block e honey
  - **Centrifuga** para processamento massivo posterior

## 3. Fantasia do Jogador

O jogador deve sentir que:

- construiu um reator apicola em escala estelar
- comprimiu populacoes inteiras de abelhas em uma camara de campo controlada
- industrializou o Productive Bees muito alem da progressao normal
- conectou todo o sistema diretamente ao AE2 para throughput absurdo

Isso deve parecer mais um complexo industrial de pos-endgame do que um apiario melhorado.

## 4. Lugar na Progressao

Este sistema deve ficar firmemente no late game ou pos-endgame, depois que o jogador ja tiver:

- infraestrutura AE2 estavel
- producao confiavel de coolants
- automacao do DMA
- progressao do QMF e dos multiblocos quanticos
- acesso confortavel a materiais altos do UFO
- boa producao de processors

Papel recomendado na progressao:

- Productive Bees normal = coleta de especies e automacao early/mid
- Apiario Estelar = geracao extrema de comb e honey
- Centrifuga Estelar = refinamento extremo e manuseio massivo em escala AE2

## 5. Identidade das Maquinas

## 5.1 Apiario Estelar

O **Apiario Estelar** e o gerador de alta producao.

Responsabilidades:

- manter muitas abelhas dentro de hatches dedicados
- converter essas abelhas em grande volume de comb e honey
- opcionalmente compactar parte da producao em comb block em tiers altos ou com upgrades
- usar tiers de field, calor e recover mode como principais sistemas de balanceamento

## 5.2 Centrifuga Estelar

A **Centrifuga Estelar** e o processador massivo.

Responsabilidades:

- puxar combs ou comb blocks selecionados diretamente da rede AE
- processa-los com alto throughput paralelo
- mandar itens e fluidos de volta ao AE2 pelos hatches massivos

As duas maquinas juntas formam uma cadeia industrial limpa.

## 6. Conceito Estrutural

## 6.1 Estrutura do Apiario Estelar

Footprint recomendado:

- **3x3x3** para uma versao compacta
- **5x5x5** para uma versao mais premium de endgame

Regras estruturais recomendadas:

- casca externa feita de casings dedicados
- nucleo interno reservado para componentes de field do UFO
- ate **4 Bee Matrix Hatches**
- cada hatch armazena ate **32 abelhas**
- cada hatch e limitado a **uma unica especie**
- inclui infraestrutura de AE e output na casca

Componentes recomendados da casca:

- controller
- blocos de casing
- hatch de energia AE
- ME massive output hatch
- opcionalmente ME massive fluid output hatch
- reservatorio de honey ou hatch de tanque interno
- ate 4 bee hatches

## 6.2 Estrutura da Centrifuga Estelar

Footprint recomendado:

- **5x5x5** ou uma estrutura vertical **3x3x5**

Componentes recomendados da casca:

- controller
- blocos de casing
- hatch de energia AE
- **ME Massive Comb Input Bus**
- ME massive output hatch
- ME massive fluid output hatch
- estrutura interna de field

## 7. Modelo de Tiers por Field

Esta linha de maquinas usa os tiers de field do UFO como principal modelo de escalonamento.

Tiers sugeridos:

- **Tier 0** = sem field blocks
- **Tier 1**
- **Tier 2**
- **Tier 3**

Intencao do design:

- tiers mais altos aumentam producao
- tiers mais altos aumentam velocidade
- tiers mais altos reduzem a pressao termica

Isso mantem o sistema alinhado com o UFO Future em vez de adicionar outra linguagem paralela de upgrades como food-only ou floral-only.

## 8. Modelo de Escalonamento Recomendado

O escalonamento deve parecer exponencial, mas ainda legivel.

Exemplo inicial:

| Tier | Multiplicador de Producao | Multiplicador de Velocidade | Multiplicador de Calor |
|---|---:|---:|---:|
| Tier 0 | `1x` | `1x` | `1.00x` |
| Tier 1 | `4x` | `3x` | `0.85x` |
| Tier 2 | `20x` | `8x` | `0.70x` |
| Tier 3 | `100x` | `20x` | `0.50x` |

Observacao importante:

Se producao e velocidade escalam agressivamente ao mesmo tempo, o throughput total sobe muito mais rapido do que parece. Por exemplo, `100x` de producao e `20x` de velocidade criam efetivamente um salto de `2000x` no throughput.

Isso e aceitavel para endgame, mas precisa ser um payoff deliberado.

## 9. Modelo dos Bee Hatches

Cada **Bee Matrix Hatch** deve:

- aceitar ate **32 abelhas**
- aceitar apenas **uma especie por vez**
- mostrar quantidade de bees instaladas
- mostrar identidade da especie
- mostrar a contribuicao estimada dessa hatch no output

Uma especie por hatch e recomendado porque:

- simplifica o balanceamento
- simplifica a GUI
- reduz edge cases
- facilita prever o output

Com 4 hatches, o controller pode suportar ate:

- `128` bees no total
- `4` especies ativas ao mesmo tempo

## 10. Modelo de Producao

O Apiario Estelar nao deve simular um processo ativo por bee.

Em vez disso, ele deve:

- ler as bees instaladas em cada hatch
- agregar a producao efetiva total por hatch e especie
- operar a maquina em ciclos
- gerar saidas em lote ao final de cada ciclo

Logica recomendada:

1. Ler todas as hatches validas
2. Resolver cada hatch em um perfil de producao
3. Agregar outputs
4. Aplicar multiplicadores do tier de field
5. Aplicar upgrades da maquina
6. Gerar resultados de item e fluido em lote
7. Injetar todos os outputs no AE2 com o menor numero possivel de operacoes

Isso preserva a fantasia de “muitas abelhas trabalhando ao mesmo tempo” sem criar o custo de TPS de muitas entidades vivas.

## 11. Principais Outputs

O Apiario Estelar deve focar em:

- combs
- comb blocks
- honey
- subprodutos secundarios opcionais dependendo da especie

Regra recomendada:

- o output base e **comb**
- tier alto ou upgrades especializados podem converter parte do output para **comb block**
- honey e sempre gerado como fluido secundario e armazenado em tanque interno ou exportado diretamente

Isso mantem o modelo de saida facil de ler.

## 12. Upgrades Especiais

A maquina deve ter sua propria familia dedicada de upgrades.

Esses upgrades devem ser mais fortes que upgrades atuais no nivel Omega dentro da sua especialidade, mas apenas para esta linha de maquinas.

Filosofia recomendada:

- nao “mais Omega que Omega”
- e sim “Omega-tier, mas especializado em apicultura”

Possiveis temas de upgrade:

- aumentar a quantidade efetiva de bees por hatch
- melhorar conversao de comb para comb block
- melhorar chance de subprodutos
- reduzir duracao do recover
- melhorar eficiencia do honey

Direcao recomendada de crafting:

- craftados a partir de **upgrade Omega + catalysts**
- montados no **DMA** ou no **QMF**

## 13. Sistema de Calor

O sistema de calor deve ser mais simples que o modelo de falha do DMA ou do Stellar Nexus.

Comportamento recomendado:

- o calor sobe enquanto a maquina esta ativa
- tiers de field reduzem a pressao termica
- parallels altos e throughput agressivo aumentam o calor mais rapido
- atingir calor maximo **nao** explode a maquina
- atingir calor maximo faz a maquina entrar em **Recover Mode**

Isso sustenta melhor a fantasia de algo “OP, mas estavel” do que destruicao catastrofica.

## 14. Recover Mode

Quando o calor maximo e atingido:

- a maquina para imediatamente
- entra em **Recover Mode**
- permanece offline por **10 minutos**
- esse timer de recover e salvo em NBT
- quebrar e recolocar o controller nao pode resetar esse timer

Motivos recomendados para esse design:

- impede abuso
- evita a frustracao de explosoes
- ainda cria um custo real para forcar a maquina demais
- preserva a identidade do calor como sistema de balanceamento relevante

Extensao opcional:

- futuras versoes podem escalar a duracao do recover pela severidade
- mas a primeira implementacao deve preferir um timer fixo simples

## 15. Paralelismo

A maquina deve suportar throughput muito alto, mas o paralelismo precisa continuar agregado.

Interpretacao recomendada:

- parallels nao sao “uma thread por bee”
- parallels sao “quantos lotes de producao a maquina resolve por ciclo”

Isso importa porque threading por bee viraria um hot path muito rapido.

Meta recomendada de throughput:

- late game deve atingir confortavelmente **milhoes de combs ou comb blocks por minuto**

Isso e aceitavel se a implementacao continuar:

- baseada em ciclos
- agregada
- nativa ao AE2
- com baixa alocacao

## 16. Conceito de GUI do Apiario Estelar

Secoes recomendadas da GUI do controller:

- bee hatches instaladas
- especie por hatch
- quantidade de bees por hatch
- upgrades da maquina
- outputs previstos
- tanque de honey
- barra de calor
- timer de recover quando travado
- tier atual de field

Melhoria opcional de UX:

- mostrar output estimado por minuto para a configuracao atual

## 17. Conceito Central da Centrifuga Estelar

A **Centrifuga Estelar** deve ser feita especificamente para manuseio massivo com AE2.

Diferente de centrifugas simples com poucos slots de input e output, esta maquina deve:

- puxar grandes quantidades de combs ou comb blocks da rede AE2
- processa-los com alto throughput paralelo
- mandar itens e fluidos de volta diretamente ao AE2

Esta maquina existe porque, quando o Apiario chega em escala absurda, centrifugas normais baseadas em slots se tornam o verdadeiro gargalo.

## 18. ME Massive Comb Input Bus

O **ME Massive Comb Input Bus** e uma hatch especial para a Centrifuga Estelar.

A GUI dela deve:

- mostrar os combs e comb blocks atualmente disponiveis na rede ME
- permitir que o jogador escolha quais tipos sao validos como input do multibloco
- funcionar como camada de selecao e filtro, nao como inventario normal

Regra recomendada de implementacao:

- **nao** reescanear a rede inteira a cada frame

Em vez disso, cachear a lista visivel e atualiza-la:

- ao abrir a GUI
- quando o jogador clicar em refresh
- ou em um intervalo espaçado

Sem essa protecao, a propria UI pode virar um hotspot de performance em redes AE grandes.

## 19. Modelo de Processamento da Centrifuga Estelar

Logica recomendada:

1. Ler os filtros de comb selecionados no input bus
2. Puxar grandes lotes da rede AE
3. Resolver outputs de centrifuga em lote
4. Agregar outputs identicos
5. Inserir todos os outputs de item pelos hatches massivos
6. Inserir todos os outputs de fluido pelos hatches massivos de fluido

Isso deve suportar paralelos muito altos sem depender de muitos slots fisicos de inventario.

## 20. Principios de Performance

Esta proposta so sera bem-sucedida se continuar segura para TPS.

Regras duras:

- nao simular entidades de abelha vivas dentro do multibloco
- nao avaliar cada bee a cada tick como um processo independente
- nao inserir outputs um item por vez
- nao consultar a rede AE inteira a cada frame da GUI
- nao reconstruir listas caras quando nada mudou

Guardrails recomendados:

- processamento por ciclos a cada `20` ou `40` ticks
- perfis de producao em cache
- listas da GUI em cache
- mapas agregados de output
- uma ou poucas chamadas de insercao no AE2 por tipo de output

## 21. Questoes em Aberto

Estes pontos devem ser resolvidos antes da implementacao:

- As maquinas devem ter footprint final **3x3x3** ou **5x5x5**?
- O Apiario deve produzir apenas **comb**, ou tambem suportar modo direto de **comb block**?
- O honey deve ser apenas um buffer/subproduto, ou tambem um recurso de upkeep da maquina?
- Os tiers de field devem exigir que as quatro posicoes internas sejam do mesmo tier, como outros multiblocos do UFO?
- O Apiario deve usar apenas upgrades proprios do UFO, ou tambem expor widgets de upgrade no estilo do Productive Bees?
- O recover deve ser sempre fixo em **10 minutos**, ou depois pode escalar com a severidade do overheat?
- A Centrifuga deve processar **comb** e **comb block** desde o primeiro release, ou lancar primeiro com um formato?

## 22. Possiveis Contradicoes

Estes sao os principais pontos onde o conceito pode ficar inconsistente internamente:

- Querer um sistema “simples” e ao mesmo tempo reproduzir profundamente o comportamento nativo de upgrades do Productive Bees pode adicionar complexidade desnecessaria.
- Dizer que os novos upgrades sao “mais fortes que Omega” pode embaralhar o papel do Omega se esses upgrades nao forem claramente especializados.
- Uma versao Tier 0 forte demais pode invalidar cedo demais a progressao normal do Productive Bees.
- Throughput muito alto com muitos subprodutos especificos por especie pode deslocar o gargalo real da maquina para a sincronizacao de GUI e insercao AE2.

## 23. Principais Riscos

Principais riscos de implementacao:

- acoplamento excessivo aos internals do Productive Bees em vez de criar uma camada de producao propria do UFO
- fazer a GUI consultar o AE2 de forma agressiva demais
- deixar o calor cosmetico em vez de relevante
- permitir que quebrar e recolocar o controller burle o recover mode
- balancear o Tier 3 tao alto que Tier 1 e Tier 2 fiquem irrelevantes

## 24. Recomendacao

Direcao recomendada de implementacao:

- construir o **Apiario Estelar** primeiro
- usar **uma especie por hatch**
- usar **producao agregada por ciclos**
- usar **tiers de field de 0 a 3**
- usar **calor + recover mode**
- depois construir a **Centrifuga Estelar** como solucao dedicada de pos-processamento

Esse caminho preserva a fantasia desejada enquanto mantem a arquitetura amigavel para modpacks grandes e fabricas em escala AE2.
