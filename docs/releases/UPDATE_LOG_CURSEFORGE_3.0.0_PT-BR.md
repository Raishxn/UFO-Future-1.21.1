# UFO Future 3.0.0

A primeira versão oficial da linha 3.0 reúne as correções das betas e as melhorias da infraestrutura AE2, multiblocos e progressão endgame.

## Destaques da linha 3.0

- Quantum Computation Nexus com lanes compartilhadas entre jobs, controle de despachos por tick e recuperação gradual após reload.
- Quantum Pattern Fabrication Matrix com entrega de múltiplos lotes por tier.
- Células Infinity e Infinity Genesis com quantidades visíveis corretamente junto a outros armazenamentos ME.
- Melhorias na construção automática, prévia de estruturas, receitas e guia.

## Correções desta versão

- Corrigida a desconexão ao abrir o status de crafting do AE2 19.2.18. A formatação de armazenamento e coprocessadores continua compatível com 19.2.17.
- Requer RaishxCore **0.2.1**: corrigido `reusable input amount must be positive` em ciclos de receitas que consomem todo o estoque de trabalho.
- O log agora distingue limite de tempo, operações e profundidade, registrando item, quantidade e métricas do planejamento.

## Alternativa temporária para problemas de autocrafting

Feche o jogo ou pare o servidor e edite `config/raishxcore/core.toml`:

```toml
[planner]
enabled = false
```

Altere a opção existente e mantenha as demais configurações. Reinicie para usar o planejador padrão do AE2. Em multiplayer, o administrador deve editar a configuração do servidor. Para reativar o Core após uma atualização, use `enabled = true` e reinicie. A opção já existia no beta9.

## Limitação conhecida

O timeout relatado ao pedir mais de três Enhancement Cores ou Infinity Templates ainda precisa ser reproduzido com os padrões e estoque da rede afetada. A correção do ingrediente reutilizável não confirma a resolução desse timeout. Se persistir, envie o novo log e o diagnóstico `/raishxcore planner` logo após a falha.

## Requisitos

Minecraft **1.21.1**, NeoForge **21.1.216+**, AE2 **19.2.17 ou 19.2.18** (faixa declarada: `[19.2.17, 20)`; outras versões 19.x não foram verificadas nesta correção), AE2 Addon Lib **1.0.3-1.21.1+** e RaishxCore **0.2.1** (`[0.2.1, 1.0)`). Substitua os JARs antigos do UFO e do RaishxCore pelos novos.
