---
navigation:
  parent: ufo_intro/infrastructure.md
  title: Matriz de Patterns Quanticos
  position: 56
---

# Matriz de Fabricacao de Patterns Quanticos

<BlockImage id="ufo:quantum_pattern_fabrication_matrix_controller" scale="4"></BlockImage>

A **Matriz de Fabricacao de Patterns Quanticos** e um repositorio e montador virtual
para patterns de crafting do AE2 cuja capacidade cresce com os Field Generators. Ela
aceita **Crafting Patterns**, **Smithing Table Patterns** e **Stonecutting Patterns**.
Processing Patterns pertencem aos Pattern Buffers e maquinas de processamento e sao
rejeitados pela Matriz. O controller e o dono do inventario e o Quantum Grid Link
conecta esse inventario a rede ME.

<GameScene zoom="1.8" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/quantum_pattern_fabrication_matrix.snbt" />
  <DiamondAnnotation pos="1.5 0.5 6.5" color="#b268ff">Controller — frente da estrutura</DiamondAnnotation>
  <DiamondAnnotation pos="5.5 0.5 6.5" color="#58e6ff">Quantum Grid Link — lado do cabo</DiamondAnnotation>
  <IsometricCamera yaw="35" pitch="25" />
</GameScene>

## Estrutura exata

O volume mede **7 blocos de largura, 5 de altura e 9 de profundidade**. Sao
necessarios:

- 1 Controller da Matriz de Fabricacao de Patterns Quanticos
- 1 Quantum Grid Link
- 127 Quantum Hyper-Mechanical Casings
- 44 Quartz Vibrant Glass
- 22 Stellar Field Generators, em qualquer mistura de MK1, MK2 e MK3
- 16 Quartz Blocks
- 15 Fluix Blocks

Coloque o controller com a face iluminada voltada para o jogador. A estrutura e
construida atras dessa face e o Grid Link fica no lado oposto. Use o controller
agachado para visualizar o holograma ou use **Montagem automatica** na tela.

## Estrutura formada e rede online

Sao dois estados diferentes:

- **Estrutura completa** significa que todas as posicoes obrigatorias estao certas.
- **Rede ME online** exige tambem um cabo AE2 na face externa do Quantum Grid Link,
  energia na rede e um canal disponivel.

A estrutura continua formada sem o cabo. Nesse caso a tela mostra **ESTRUTURA
COMPLETA • REDE ME OFFLINE**. Use **Escanear estrutura** para destacar somente
erros estruturais reais.

## Capacidade dos Fields

Cada Field Generator instalado contribui separadamente:

| Field Generator | Espacos por bloco | 22 Fields iguais |
| --- | ---: | ---: |
| MK1 | 256 | 5.632 |
| MK2 | 512 | 11.264 |
| MK3 | 1.024 | 22.528 |

Tiers misturados sao aceitos. Por exemplo, dez MK1, dez MK2 e dois MK3 fornecem
`10 × 256 + 10 × 512 + 2 × 1024 = 9.728` espacos para patterns.

Reduzir o tier dos Fields nunca apaga patterns. Os patterns que excederem a nova
capacidade continuam acessiveis para retirada, mas novas insercoes aguardam ate a
capacidade ser restaurada.

## Gerenciamento de patterns

O botao com a grade de patterns, logo abaixo do botao de prioridade, abre o
gerenciamento. A tela oferece:

- insercao e retirada de Crafting, Smithing Table e Stonecutting Patterns;
- transferencia com shift-clique entre a biblioteca e o inventario do jogador;
- paginas de 54 slots sincronizadas pelo servidor, navegadas pela roda do mouse ou barra de rolagem;
- busca pelos nomes dos itens de entrada ou saida do pattern na pagina carregada;
- exibicao do item produzido sem modificar o pattern armazenado.

Quando o Quantum Grid Link esta online, a Matriz tambem aparece como um unico
provider compacto no **Pattern Access Terminal** do AE2. A capacidade vazia nao e
enviada como dezenas de milhares de slots: o terminal recebe os patterns
armazenados e uma posicao livre, evitando travamentos em Matrizes de alta capacidade.

O botao funciona mesmo com a estrutura ou a rede ME offline, garantindo que os
patterns armazenados sempre possam ser recuperados.

## Envio automatico ao codificar

Depois de codificar com sucesso um Crafting, Smithing Table ou Stonecutting Pattern
no Pattern Encoding Terminal do AE2 ou no terminal Tianshu compativel conectado ao
mesmo grid, o pattern e movido automaticamente para uma Matriz online. Processing
Patterns permanecem no slot de saida do terminal e nao sao enviados para a Matriz.

1. As Matrizes sao ordenadas por **Prioridade dos patterns**, da maior para a menor.
2. Prioridades iguais usam uma ordem estavel baseada na posicao.
3. Uma Matriz cheia e ignorada e a proxima elegivel e testada.
4. Se nenhuma puder receber, o pattern permanece no slot de saida do terminal.

O botao de prioridade abre o editor nativo do AE2. A prioridade controla o destino
do envio automatico e nao altera a capacidade.

## Controles da tela

- **Guia** abre esta pagina.
- **Escanear estrutura** valida a orientacao atual e destaca erros.
- **Montagem automatica** coloca blocos ausentes sem substituir blocos ocupados.
- **Gerenciamento de patterns** abre o repositorio pesquisavel.
- **Prioridade dos patterns** define qual Matriz recebe patterns primeiro.

O painel de Fields mostra as quantidades MK1/MK2/MK3 em tempo real. A barra da
biblioteca mostra os espacos ocupados e a capacidade calculada a partir dos Fields.
