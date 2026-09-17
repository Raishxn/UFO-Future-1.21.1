# UFO Future 3.0.0-alpha.2

Um alpha de endurecimento. Não adiciona máquinas novas: aperta a armadura modular,
faz os gates de GameTest realmente falharem quando o mod não carrega e move o
planner companheiro para um novo release do RaishxCore. Este release também anexa o
jar do **RaishxCore** ao lado do jar do UFO.

## Dependências

- **RaishxCore atualizado para `0.1.0-alpha.4`** (obrigatório, baixado no repositório
  dele). Veja aquele release para o trabalho do planner: pesos de faltante
  declarados pelo consumidor, um oráculo exato para o corpus diferencial e
  `routeChoiceLinks` no diagnóstico do planner.
- **Applied Energistics 2:** 19.2.17 ou mais novo dentro da linha 19.x.
- **AE2 Addon Lib:** 1.0.3 para Minecraft 1.21.1 ou um 1.x compatível.
- **Mekanism continua opcional.**

## Mudanças

- **Os limites da armadura modular são autoritativos no servidor.** Os 15 ajustes
  podem ser limitados por servidor ou modpack em `armor.moduleCaps.*` no
  `ufo-server.toml`, apertando o máximo de projeto sem editar o mod. O servidor
  aplica o limite na fronteira cliente-servidor e em toda leitura de efeito; o
  cliente continua mostrando o alcance de projeto sem o limite.
- **O relógio da armadura não depende da dimensão.** Cadências e o cooldown do
  translocador usam o tempo do overworld, então não saltam mais quando o jogador
  troca de dimensão.
- **A varredura do cloak tem orçamento.** O cloak re-escaneia entidades próximas a
  cada 5 ticks em vez de a cada tick.
- **Traje térmico isolado dos módulos do UFO**, sem misturar comportamento com a
  armadura modular.
- **Guia do GuideME em chinês simplificado:** guia principal, navegação e operação
  de máquinas.
- **Integração com o RaishxCore** acompanha o `0.1.0-alpha.4`; o pin declarado e a
  revisão verificada andam juntos.

## Correções

- **Overflow do clamp das configurações de armadura (segurança).** Um pacote
  forjado com valor extremo estourava os limites e invertia para o lado oposto da
  faixa (por exemplo, o valor mais negativo virava a velocidade máxima). A
  aritmética agora limita antes e está fixada por testes de unidade nos extremos.
- **Artefatos de release limpos:** o cache de hash do datagen não vaza mais para o
  sources jar, e o jar de runtime leva o README junto da licença e dos créditos.

## Endurecimento de build e testes

- Toda GitHub Action está presa por SHA de commit, com o Dependabot atualizando os
  pins em pull requests semanais agrupados.
- Os workflows de GameTest, soak e load agora exigem o banner de conclusão do
  NeoForge. O `runGameTestServer` sai com código 0 mesmo quando o mod falha ao
  carregar, então um jar quebrado podia passar no CI e ser publicado; esse modo de
  falha agora é pego.
