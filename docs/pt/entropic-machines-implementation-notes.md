# Entropic Machines: Notas de Implementacao

## Objetivo

Registrar os principais riscos de performance e estabilidade para:

- `Entropic Convergence Engine`
- `Entropic Assembler Matrix`

Estas notas complementam o PRD e devem orientar a implementacao.

---

## Hotspots principais

## 1. Scan de estrutura `7x7x7`

Risco:

- validar cubo inteiro em todo tick
- revalidar a partir de qualquer bloco clicado sem cache
- fazer muitos `getBlockState` e `isLoaded` sob spam de bloco update

Guardrails:

- scan apenas sob `dirty flag`, clique do jogador, load e eventos de quebra/colocacao
- nunca revalidar por tick enquanto a estrutura estiver estavel
- usar `FieldTieredCubeValidator` apenas em caminhos frios
- manter `anchor` canonico para evitar re-scan desnecessario

## 2. Multibloco sem controller visivel

Risco:

- qualquer bloco clicado tentar descobrir a estrutura inteira toda hora
- abrir menu em estado parcial
- menu abrir sem AE conectado

Guardrails:

- resolver `anchor/master` de forma deterministica
- abrir interface apenas se:
  - cubo estiver valido
  - tier interno estiver uniforme
  - houver no valido com grid AE

## 3. Integracao AE2 crafting CPU

Risco:

- recalcular estado "infinito" em loops quentes
- usar numeros gigantes e causar overflow ou narrowing
- spam de atualizacao de GUI

Guardrails:

- tratar `infinito` como estado especial, nao so numero gigante
- formatar `∞` na GUI/listagem, nao no core logico cru
- cachear dados derivados do CPU e invalidar apenas em mudanca estrutural/estado
- limitar sync cliente para mudancas reais

## 4. Throughput massivo de patterns

Risco:

- criar milhares de estados de processo simultaneos
- varrer lista inteira de recipes/patterns toda hora
- custo por tick crescer linearmente com capacidade maxima

Guardrails:

- usar filas compactadas e batches
- separar `pattern capacity` de `active execution slots`
- manter cache de recipe lookup/index por id
- nunca iterar sobre milhares de slots ativos por tick se a fila puder ser agregada

## 5. Persistencia e recuperacao

Risco:

- estrutura formada perder `anchor`
- estado de tier voltar incorreto ao recarregar chunk
- bloco clicado apontar para master invalido

Guardrails:

- persistir `anchor`, `tier`, `formed` e metadados minimos
- revalidar em load se estrutura estiver marcada como formada
- nunca confiar cegamente em dado cliente para abrir/acessar a maquina

---

## Decisoes tecnicas recomendadas

## A. Anchor invisivel e canonico

O multibloco precisa de um `master` interno, mas sem parecer controller.

Recomendacao:

- usar o canto minimo do cubo formado como `anchor` canonico
- qualquer bloco do cubo deve conseguir resolver esse anchor

## B. Tier por interior uniforme

Regra fechada:

- `5x5x5` interno completo com `Field T1` -> `MK1`
- `5x5x5` interno completo com `Field T2` -> `MK2`
- `5x5x5` interno completo com `Field T3` -> `MK3`
- interior misto ou incompleto -> estrutura sem tier valido

## C. Sem tick loop caro

Essas maquinas devem parecer absurdas para o jogador, mas nao para o servidor.

Recomendacao:

- custo estrutural e de validacao em caminhos frios
- custo de producao em batches agregados
- custo de GUI apenas em estado derivado cacheado

---

## Ordem de implementacao recomendada

1. Estrutura `7x7x7` e deteccao de tier uniforme
2. Arquitetura de `anchor/master` invisivel
3. Abertura de interface por qualquer bloco formado e conectado
4. Integracao AE2 do `Entropic Convergence Engine`
5. Formatacao `∞` no terminal/status
6. Batch execution do `Entropic Assembler Matrix`
