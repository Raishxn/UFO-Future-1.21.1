---
navigation:
  parent: ufo_intro/infrastructure.md
  title: Nexus de Computacao Quantica
  position: 55
---

# Nexus de Computacao Quantica

<BlockImage id="ufo:quantum_computation_nexus_controller" scale="4"></BlockImage>

O **Nexus de Computacao Quantica** combina todos os Armazenamentos de Crafting e
Co-Processadores UFO de sua cavidade em um unico pool de computacao para o AE2.

<GameScene zoom="1.45" background="transparent" interactive={true}>
  <ImportStructure src="../../assets/assemblies/quantum_computation_nexus.snbt" />
  <DiamondAnnotation pos="7.5 2.5 9.5" color="#80c6ff">Controller do Nexus</DiamondAnnotation>
  <DiamondAnnotation pos="11.5 2.5 9.5" color="#58e6ff">Quantum Grid Link</DiamondAnnotation>
  <IsometricCamera yaw="35" pitch="25" />
</GameScene>

## Construcao

Monte a carcaça mostrada acima e instale os modulos desejados dentro da cavidade
totalmente fechada pelos vidros. E necessario pelo menos um Armazenamento de
Crafting UFO. Co-Processadores adicionam linhas de crafting paralelo.

Somente a cavidade interna e valida. Blocos colocados fora da carcaça nao sao
contados. Os modulos reconhecidos continuam fisicamente presentes, visiveis e
preservam seus dados enquanto o Nexus utiliza sua capacidade de computacao.

## Requisitos

- Um **Controller do Nexus de Computacao Quantica**
- Um **Quantum Grid Link** na posicao indicada
- Pelo menos um **Armazenamento de Crafting UFO**
- Quantos Co-Processadores e armazenamentos adicionais couberem na cavidade
- Um canal ME livre e pelo menos **32 AE/t** para o Grid Link

## Conexao com o grid

Conecte o cabo AE2 somente na face externa indicada pelo overlay do **Quantum
Grid Link**. O Link publica o pool no grid consumindo um unico canal; os modulos
internos nao viram CPUs individuais e o controller nao aceita cabos diretamente.

Se o Link informar `desvinculado`, verifique a estrutura. Se informar `offline`,
a estrutura esta valida, mas falta energia ou canal no grid conectado.

## Funcionamento

Quando ocioso, o Nexus aparece no Terminal de Crafting como **uma unica CPU** com
toda a capacidade ainda livre. Ao iniciar um job, o pool reserva somente os bytes
exigidos pelo plano e cria uma CPU virtual temporaria. O restante do armazenamento
continua disponivel para outros jobs. Os Co-Processadores formam uma capacidade
compartilhada, repartida de forma justa entre os jobs ativos.

### Modo de computacao infinita

Instalar pelo menos **25 Armazenamentos de Crafting Cosmic String** (tier de 1 TiB)
e **25 Co-Processadores Cosmic String** (65.536 linhas cada) ativa o modo de
computacao infinita do Nexus. O
Armazenamento de Crafting e os Co-Processadores passam a ser publicados como
**infinitos** para o AE2. O controller mostra `∞`, mas os tooltips continuam
informando a capacidade fisica instalada na cavidade.

Os dois requisitos precisam ser cumpridos com modulos do ultimo tier. Modulos de
tiers inferiores continuam somando sua capacidade normal, mas nao contam para o desbloqueio.

Quando o job termina e seus itens pendentes voltam ao grid, sua CPU virtual
desaparece e a reserva retorna ao pool. A tela mostra o total fisico e a quantidade
atual de jobs, nunca dezenas de CPUs fisicas.

Quebrar a carcaça, o controller ou o Grid Link desconecta a CPU com seguranca.
Os blocos internos nao sao transformados em um inventario virtual: permanecem
visiveis no mundo.

## Tela do controller

No modo infinito, as duas metricas principais exibem `∞`. Passe o mouse sobre
elas para consultar os totais fisicos dos modulos instalados.

- **Abrir guia** retorna para esta pagina.
- **Escanear multibloco** informa e destaca blocos ausentes.
- **Montagem automatica** coloca apenas blocos ausentes que o jogador possui e
  nunca substitui uma posicao ocupada.
- **Prioridade da CPU** abre o editor nativo de prioridade do AE2. Nexus com
  prioridade maior sao escolhidos primeiro na selecao automatica de CPU.

## Solucao de problemas

- **Estrutura offline:** escaneie a estrutura e repare a posicao destacada.
- **Grid offline:** conecte um cabo energizado ao overlay do Grid Link e libere um canal.
- **CPU offline:** coloque ao menos um Armazenamento de Crafting suportado na cavidade.
- **Modulo nao contado:** confirme que ele esta dentro da cavidade fechada, nao apenas nos limites da pre-visualizacao.
