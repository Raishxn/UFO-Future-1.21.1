# Organização de texturas e assets

## Árvore oficial

- `textures/block/`: texturas usadas por models e renderers de blocos. Famílias
  com contrato próprio ficam em subpastas (`dma/`, `drive/`, `energycell/`,
  `fluid/`, `multiblock/`, `obj/` e `qmf/`); as texturas do renderer de crafting
  do AE2 permanecem diretamente em `block/` porque esse é o path resolvido pelo
  provider em runtime.
- `textures/item/`: texturas usadas por models de itens. O conjunto de ferramentas
  UFO possui um único owner em `textures/item/ufoset/`; animações ficam em
  `textures/item/ufoset/animations/`.
- `textures/entity/`: texturas de entidades e renderers.
- `textures/gui/` e `textures/guis/`: interfaces UFO e assets compatíveis com o
  loader de telas do AE2. A consolidação desses dois caminhos exige alterar os
  consumidores e será feita separadamente.
- `textures/models/armor/`: layers de armaduras equipadas.
- `ae2guide/`: páginas e imagens exclusivas do GuideME.
- `tools/`: fontes/conversores que não devem entrar no JAR.

## Regras

1. O caminho do PNG é API de resource pack. Não mover uma textura runtime sem
   atualizar todos os models, `.mcmeta` e renderers que a referenciam.
2. Arquivos editáveis (`.pdn`, `.xcf`, `.kra`, `.psd`) e exportações de trabalho
   não pertencem a `src/main/resources`.
3. Duplicatas byte a byte só podem ser removidas depois que todos os consumidores
   apontarem para um caminho canônico.
4. Sufixo `_ctm.png` não ativa connected textures sozinho. É preciso um renderer
   que conheça o layout e uma configuração que aponte para esse arquivo.
5. Um caminho não pode existir simultaneamente em `src/main/resources` e
   `src/generated/resources`. O datagen é o owner de recipes, loot tables e models
   simples; arquivos manuais são reservados a geometrias e telas que ele não gera.
6. `textures/block/crafting/`, `textures/ufoset/` e animações soltas diretamente
   em `textures/item/` são layouts legados e não devem voltar.

## Limpeza L-0037

- Foram removidas somente duplicatas e exportações sem consumidor. Nenhuma
  textura runtime ativa foi movida, portanto os resource locations publicados
  continuam válidos.
- A árvore antiga `block/crafting/` duplicava o material realmente requisitado
  em `block/`; a árvore `textures/ufoset/` duplicava o owner canônico
  `textures/item/ufoset/`.
- Models manuais ocultos por models gerados e recipes/loot tables manuais ocultos
  pela precedência do datagen foram removidos. Cada resource agora possui um
  único owner.
- O asset `block/multiblock/coolant_fluid_hatch_overlay.png` foi preservado para
  o marco de coolant hatch já planejado, mesmo ainda sem consumidor runtime.

## Decisão visual L-0038

- O coolant externo reutiliza o ID, bloco, model e overlay já publicados do
  `ME Massive Fluid Hatch`; não foi criado um segundo hatch.
- `coolant_fluid_hatch_overlay.png` continua reservado e sem consumidor. Ele não
  é replacement correto para um bloco que preserva simultaneamente suas funções
  ME, química e de coolant, portanto substituir o overlay existente seria uma
  mudança visual enganosa e desnecessária.

## Contrato CTM integrado em 2026-09-03

- `ufo:connected_texture` é o loader de geometria NeoForge pertencente ao UFO.
- `ufo:same_block` conecta apenas vizinhos do mesmo bloco, tanto em estruturas
  formadas quanto desmontadas; isso é apresentação, não regra de multibloco.
- `entropy_singularity_casing_ctm.png` e
  `quantum_hyper_mechanical_casing_ctm.png` são folhas compactas 32×32.
- Cada folha é uma grade 4×4 de tiles de 8×8. Cada face do cubo é dividida em
  quatro quadrantes; duas bordas incidentes e a diagonal escolhem o tile.
- As texturas-base 16×16 são o fallback sem `ModelData`, a partícula e os models
  vanilla explícitos de inventário. Se o sprite CTM referenciado estiver ausente,
  o loader também usa a textura-base.
- Os models de bloco declaram `base`, `ctm` e `particle` em namespace `ufo`.
  Resource packs podem substituir esses paths ou o próprio model JSON.
- O metadata `ldlib` que apontava para `gtocore:` foi removido após a geração do
  replacement. Não existe dependência runtime adicional nem atlas manual: os
  materiais declarados pelo model são costurados no atlas de blocos pelo NeoForge.

O cálculo ocorre somente durante rebuild de geometria do chunk e os quads são
cacheados por face e máscaras de borda/canto. Não há tick, packet, block entity,
propriedade de blockstate ou alteração de save/topologia associada ao CTM.
