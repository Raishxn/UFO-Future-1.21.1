# PRD: Nexus Black Hole Accelerator

## 1. Resumo

O **Nexus Black Hole Accelerator** e um novo multibloco de endgame focado em **geracao extrema de energia AE**. A fantasia central da maquina e criar um **sol artificial** dentro do nucleo, forcar seu colapso com **Dark Matter** e mantelo estavel com **Temporal Fluid**, transformando esse colapso controlado em producao massiva de energia.

Ele nao deve competir com geradores comuns. Ele deve ser o equivalente energetico do Stellar Nexus: uma infraestrutura absurda, perigosa e transformadora, que muda completamente a escala da fabrica do jogador.

## 2. Objetivo de Produto

Adicionar um multibloco que:

- entregue um novo payoff de endgame focado exclusivamente em energia
- consuma materiais de assinatura do UFO Future em escala relevante
- use risco real, manutencao e colapso como parte da identidade
- aproveite a fantasia de estrela artificial virando buraco negro
- mantenha progressao clara entre `mk1`, `mk2` e `mk3`

## 3. Fantasia do Jogador

O jogador deve sentir que:

- construiu uma infraestrutura proibida e perigosa
- iniciou uma estrela artificial no centro do multibloco
- colapsou essa estrela em um buraco negro controlado
- agora alimenta toda a base com um projeto que parece impossivel

O momento visual chave e:

1. energizar o multibloco
2. injetar metais estelares e Temporal Fluid
3. ver um pequeno sol surgir no centro
4. inserir Dark Matter
5. assistir o sol colapsar para um buraco negro
6. entrar em geracao massiva de AE

## 4. Lugar na Progressao

Este multibloco deve ficar **no mesmo patamar do capstone industrial do mod**, depois da fase de:

- DMA estabilizado
- producao consistente de coolants
- Quantum Matter Fabricator
- Quantum Slicer
- Quantum Processor Assembler
- acesso confortavel a `white_dwarf_matter`, `neutron_star_matter`, `pulsar_matter` e `dark_matter`

Regra de progressao:

- `mk1` = primeiro reator energetico realmente transformador
- `mk2` = energia suficiente para sustentar fabricas absurdas sem gargalo
- `mk3` = energia praticamente ilimitada para o contexto do mod

## 5. Identidade de Design

O Nexus Black Hole Accelerator deve seguir estes pilares:

- **alto risco**: falha tem consequencia real
- **alto custo de startup**: ligar deve ser um evento, nao um clique barato
- **alto custo sustentado**: o jogador precisa manter estabilidade
- **alto payoff**: a recompensa precisa justificar o absurdo
- **leitura clara**: apesar da fantasia extrema, o loop deve ser facil de entender

## 6. Estrutura e Tiering

### 6.1 Regra recomendada

Use uma unica familia de maquina com tres tiers:

- `mk1`
- `mk2`
- `mk3`

### 6.2 Recomendacao de implementacao

Para manter o design legivel e coerente com o restante do mod:

- a estrutura base e construida como `mk1`
- `mk2` e `mk3` sao liberados por upgrades estruturais e componentes internos
- o footprint pode permanecer igual entre tiers
- o tier e definido por um conjunto unico de blocos de campo/estabilizacao, sem misturar tiers

Isso preserva a ideia do fa de "so ser buildable como mk1" no sentido de onboarding, sem perder a progressao real de `mk2` e `mk3`.

### 6.3 Escala de custo estrutural

Seguir a regra ja usada no balanceamento do repo:

- `mk2 ~= 10x` custo total do `mk1`
- `mk3 ~= 50x` custo total do `mk2`

Importante:

- essa escala vale para **custo de construcao**
- o **startup consumivel** pode usar uma escala menor e mais legivel

## 7. Loop de Funcionamento

## 7.1 Estado inativo

O multibloco montado fica carregando energia interna e aguardando os insumos de ignicao.

## 7.2 Fase 1: Stellar Seed

Para iniciar o nucleo:

- consumir `10G AE` armazenados no multibloco
- consumir `3 stacks` de `white_dwarf_fragment_ingot`
- consumir `3 stacks` de `neutron_star_fragment_ingot`
- consumir `3 stacks` de `pulsar_fragment_ingot`
- consumir `8000 mB` de `Temporal Fluid`

Resultado:

- renderiza um **sol artificial** no centro
- a maquina entra em estado de pre-colapso
- ainda nao gera energia total

## 7.3 Fase 2: Black Hole Collapse

Para entrar no modo de geracao real:

- consumir `1x Dark Matter`

Resultado:

- o sol colapsa visualmente em um buraco negro
- a maquina passa a gerar energia em modo ativo
- o sistema de estabilidade passa a degradar continuamente

## 7.4 Reinicio

Se o reator desligar por falta de estabilidade, desligamento manual ou falha:

- o processo de ignicao precisa ser refeito
- o custo de startup e consumido novamente

Isso impede que o multibloco vire energia gratis apos a primeira partida.

## 8. Sistema Central: Stellar Field Stability

### 8.1 Substituicao do HU

Em vez de HU/heat como estatistica principal, esta maquina usa:

- **Stellar Field Stability (SFS)** de `100%` a `0%`

Leitura para o jogador:

- `100%` = buraco negro totalmente contido
- `50%` = zona de risco
- `25%` = alertas visuais e sonoros fortes
- `0%` = falha catastrófica

### 8.2 Regra de degradacao

A estabilidade cai continuamente enquanto a maquina esta ativa.

Recomendacao inicial:

- `mk1 normal`: `1%` a cada `5s`
- `mk1 overclock`: `3%` a cada `5s`
- `mk2`: mesma logica, mas com multiplicadores internos maiores e consumo maior
- `mk3`: mesma logica, com maior throughput e maior punicao em falha

Esse ritmo e intencionalmente perceptivel, mas nao opressivo.

### 8.3 Uso do Temporal Fluid

O `Temporal Fluid` e o estabilizador principal da maquina.

Regra recomendada:

- cada `1000 mB` de `Temporal Fluid` restaura `10%` de SFS

Comportamento:

- restauracao automatica quando o SFS cai abaixo de um threshold configuravel
- prioridade de consumo maior em `Overclock`
- se nao houver fluido suficiente, a maquina continua rodando e degrada ate falhar

### 8.4 Thresholds recomendados de automacao

- acima de `60%`: operacao segura
- entre `60%` e `30%`: alerta amarelo
- abaixo de `30%`: alerta vermelho
- abaixo de `10%`: sirene, particulas instaveis, tela tremendo

## 9. Modos de Operacao

## 9.1 Normal Mode

Modo padrao, melhor eficiencia por recurso.

## 9.2 Overclock Mode

Overclock deve ser mantido porque combina com a fantasia do mod.

Regras recomendadas:

- gera `10x` mais energia
- consome `5x` mais `Temporal Fluid`
- degrada `3x` mais estabilidade
- acelera animacao e efeitos visuais

Observacao:

- aqui mantemos sua proposta de `10x` output com `5x` consumo, mas usando SFS como sistema de risco principal

## 10. Output de Energia

## 10.1 Meta de geracao

Recomendacao base:

| Tier | Normal | Overclock |
|---|---:|---:|
| MK1 | `20M AE/t` | `200M AE/t` |
| MK2 | `40M AE/t` | `400M AE/t` |
| MK3 | `100M AE/t` | `1B AE/t` |

## 10.2 Justificativa

- `mk1` ja precisa parecer um divisor de aguas
- `mk2` dobra o ganho e melhora a sustentacao industrial
- `mk3` precisa parecer absurdamente acima da curva

O salto entre tiers nao vem so do output. Ele tambem vem de:

- custo estrutural
- custo de startup
- custo de manutencao
- severidade da falha

## 11. Escala de Startup por Tier

Para o **startup consumivel**, usar uma escala mais simples que a estrutural:

| Tier | AE inicial | Ingotos estelares | Temporal Fluid | Dark Matter |
|---|---:|---:|---:|---:|
| MK1 | `10G AE` | `9 stacks totais` | `8000 mB` | `1` |
| MK2 | `20G AE` | `18 stacks totais` | `16000 mB` | `2` |
| MK3 | `50G AE` | `45 stacks totais` | `40000 mB` | `5` |

Distribuicao dos ingotos por tier:

- 1/3 `white_dwarf_fragment_ingot`
- 1/3 `neutron_star_fragment_ingot`
- 1/3 `pulsar_fragment_ingot`

Essa escala preserva sua ideia original de `2x` e `5x` para startup.

## 12. Falha e Explosao

### 12.1 Gatilho

Se a maquina atingir `0% SFS`, ocorre falha de contencao.

### 12.2 Consequencia por tier

Usar sua referencia de severidade relativa ao Draconic Reactor:

| Tier | Severidade sugerida |
|---|---|
| MK1 | `25%` da explosao de referencia |
| MK2 | `50%` da explosao de referencia |
| MK3 | `100%` da explosao de referencia |

### 12.3 Telegrafia de risco

Antes de explodir, o jogo precisa deixar claro:

- distorcao visual no centro
- aumento agressivo de particulas
- sirene
- mensagens no HUD
- flash na estrutura

O jogador precisa sentir que morreu por ignorar sinais, nao por falta de clareza.

## 13. Custos de Construcao

Os custos exatos podem ser refinados depois, mas o PRD deve travar a filosofia:

- usar componentes tierados anteriores como sub-arvores obrigatorias
- consumir `dark_matter`, `event_horizon_component_matrix`, `charged_enriched_neutronium_sphere`, `quantum_anomaly` e processadores avancados
- usar casings dedicadas de alto tier
- pedir infraestrutura previa, nao apenas materia-prima flat

Recomendacao de familias de componentes:

- controladores/core de aceleracao
- aneis de contencao gravitacional
- estabilizadores de campo
- injetores de materia escura
- emissores AE de extracao

## 14. Interface e Feedback

O multibloco precisa mostrar no GUI:

- energia gerada por tick
- modo atual `Normal` ou `Overclock`
- `SFS`
- consumo atual de `Temporal Fluid`
- status do nucleo: `Idle`, `Seeded`, `Collapsed`, `Critical`
- tempo ativo desde a ignicao

Feedback visual no mundo:

- sol brilhante na fase inicial
- colapso para esfera escura com lente gravitacional
- particulas e distorcao aumentando conforme o SFS cai

## 15. Regras de Balanceamento

Para este multibloco funcionar bem no UFO Future:

- o build precisa ser muito mais caro que um gerador comum
- a manutencao precisa impedir operacao totalmente gratis
- `Temporal Fluid` precisa virar sink real de endgame
- `Dark Matter` precisa ser o gatilho de prestigio, nao um ingrediente decorativo
- o reward precisa ser absurdamente alto, porque o lock-in tambem e

## 16. Decisoes Fechadas Recomendadas

Estas sao as decisoes que eu recomendo fixar agora:

- a maquina usa **SFS**, nao HU, como risco principal
- `Temporal Fluid` estabiliza o reator e evita o colapso
- `Dark Matter` e obrigatoria para transformar o sol em buraco negro
- `mk1` entrega `20M AE/t`
- `Overclock` entrega `10x` output, `5x` consumo e `3x` degradacao
- tiers de construcao seguem `1x -> 10x -> 500x`
- tiers de startup seguem `1x -> 2x -> 5x`

## 17. Pontos em Aberto

Estes pontos ainda precisam de decisao antes da implementacao final:

1. O reator vai exportar energia direto para a rede ME, para hatches proprios, ou para ambos?
2. O `Temporal Fluid` sera o unico insumo sustentado ou tambem havera `Stable Coolant` como suporte secundario?
3. O `Overclock` sera liberado desde o `mk1` ou so a partir do `mk2`?
4. A falha vai destruir so a estrutura e arredores, ou tambem criar um dano persistente temporario no local?

## 18. Criterios de Aceite

O PRD esta atendido quando:

- existe uma progressao clara de `mk1`, `mk2` e `mk3`
- o jogador entende visualmente a transicao de sol para buraco negro
- o startup e caro o suficiente para parecer um ritual industrial
- `Temporal Fluid` se torna um gargalo real de manutencao
- o output e alto o bastante para justificar o projeto
- a falha e perigosa, mas previsivel e telegráfica
- o multibloco nao invalida o early/mid game, apenas redefine o endgame

## 19. Recomendacao Final

A melhor fusao entre sua ideia e a do fa e:

- manter o **sol artificial** como fase de ignicao
- usar **Dark Matter** para o colapso em buraco negro
- usar **Temporal Fluid** como estabilizador principal
- trocar o conceito de heat/HU por **Stellar Field Stability**
- manter `Overclock` como modo de alto risco e alto payoff

Isso preserva a fantasia forte do conceito, encaixa melhor na progressao atual do mod e cria um multibloco memoravel de verdade.
