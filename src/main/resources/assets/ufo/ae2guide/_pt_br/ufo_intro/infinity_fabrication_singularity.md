---
navigation:
  parent: ufo_intro/infrastructure.md
  title: Singularidade de Fabricação Infinita
  position: 57
---

# Singularidade de Fabricação Infinita

<BlockImage id="ufo:infinity_fabrication_singularity_controller" scale="4"></BlockImage>

A **Singularidade de Fabricação Infinita** é a solução endgame do UFO para
substituir máquinas que fabricam uma cópia por vez. Ela transforma uma tarefa
homogênea do AE2 em uma única rota agregada: pedir bilhões de itens iguais não
gera bilhões de ciclos de máquina.

Ela combina dois serviços dentro do próprio multibloco:

- sua própria biblioteca de patterns escalada pelos geradores de campo;
- seu próprio executor de fabricação agregada para pedidos massivos.

Ela não exige um Nexus de Computação Quântica separado. Apenas a Singularidade
formada e seu Quantum Grid Link precisam estar conectados à rede ME.

<GameScene zoom="1.75" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/infinity_fabrication_singularity.snbt" />
  <IsometricCamera yaw="35" pitch="25" />
</GameScene>

## Estrutura exata

A estrutura é um cubo de **7 × 7 × 7**. Ela requer:

- 1 Controlador da Singularidade de Fabricação Infinita;
- 1 Quantum Grid Link;
- 52 Revestimentos Hipermecânicos Quânticos;
- 25 Geradores de Campo Estelar, em qualquer combinação de MK1, MK2 e MK3;
- 36 Vidros Vibrantes de Quartzo;
- 12 Blocos de Quartzo;
- 12 Blocos de Fluix;
- 8 Revestimentos Banhados em Gráviton.

As outras 196 posições são ignoradas pela validação e podem permanecer como ar.
Conecte um cabo do AE2 à face externa do Quantum Grid Link. O Link consome um
canal e pelo menos 32 AE/t.

## Páginas de patterns

Abra o controlador para inserir os patterns codificados diretamente na fileira de
nove slots e use as setas para trocar de página. Cada posição de field adiciona
páginas conforme o tier instalado: **MK1 adiciona 1 página**, **MK2 adiciona 2
páginas** e **MK3 adiciona 4 páginas**. Com 25 fields, a capacidade total varia
entre 25 páginas / 225 patterns e 100 páginas / 900 patterns. Todos os Fields
devem usar o mesmo tier; misturar tiers invalida a estrutura.

Se os fields forem rebaixados, os patterns acima da nova capacidade são preservados
e continuam acessíveis para remoção; nenhum pattern codificado é apagado automaticamente.

## Crafting agregado

Em cada etapa compatível, a Singularidade valida uma cópia da receita, extrai de
uma vez todas as cópias disponíveis e promete as saídas correspondentes à CPU de
crafting do AE2 em uma operação limitada. As quantidades usam contadores de 64
bits; pedidos na casa dos bilhões permanecem uma rota, sem loop por item.

Até **128 rotas** podem ficar pendentes ao mesmo tempo. As rotas e suas saídas
prometidas persistem ao recarregar o mundo. Padrões de crafting, ferraria e
cortador de pedras são aceitos; padrões de processamento continuam usando suas
máquinas reais.

Patterns duplicados armazenados em páginas diferentes são publicados como uma única
rota de crafting deduplicada.

Ao habilitar o botão de um pattern, ele também entra em fabricação automática
contínua, como no Quantum Crafter: enquanto houver ingredientes na rede, a
Singularidade repete a receita. A diferença é que cada rodada consome todas as
cópias disponíveis de uma vez e cria uma única rota agregada, em vez de iterar
cópia por cópia. Desabilitar o pattern interrompe novas rodadas sem apagar saídas
que já estejam aguardando espaço na rede.

## Modos de operação

| Modo | Orçamento de rotas | Custo de AE | Uso indicado |
| --- | ---: | ---: | --- |
| Balanceado | 64/tick | 1× | Operação geral |
| Velocidade | 128/tick | 2× | Pico máximo de produção |
| Eficiência | 32/tick | 0,5× | Redes com energia limitada |

O modo é selecionado automaticamente pelos fields: **MK1 usa Eficiência**, **MK2
usa Balanceado** e **MK3 usa Velocidade**. Em estruturas mistas vale o menor tier
instalado; portanto, Velocidade exige que os 25 fields sejam MK3. Uma troca de
field afeta imediatamente novos despachos, sem cancelar jobs nem apagar saídas
que já estão na fila.

## Painel e diagnóstico

A interface mostra as páginas de patterns e os estados da estrutura, rede, jobs
ativos e rotas na fila. **Escanear estrutura** destaca blocos incorretos;
**Montar automaticamente** coloca apenas blocos ausentes para os quais o jogador
possui materiais.

- **Estrutura incompleta:** corrija a posição destacada pelo scanner.
- **Rede ME offline:** energize a rede, libere um canal e conecte a face externa do Grid Link.
- **Aguardando padrões:** insira um pattern codificado compatível em uma das páginas do controlador.
- **Rotas continuam na fila:** deixe espaço na rede ME para receber as saídas prometidas.
