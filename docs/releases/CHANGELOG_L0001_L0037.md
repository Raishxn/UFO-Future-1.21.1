# Changelog técnico — L-0001 a L-0037

Resumo simples da grande correção 3.0 do UFO Future. Os marcos abaixo preservam, salvo menção explícita, IDs, saves, receitas e progressão existentes.

> **L-0038 não faz parte deste documento:** foi validado e aprovado separadamente pelo autor.

## Segurança, consistência e máquinas

- **L-0001 — Auditoria:** mapeados os riscos críticos de multiblocos, rede AE2, DMA, Stellar, células e performance; definido o plano de correção.
- **L-0002 — Pacotes:** todas as ações de máquina passaram a validar menu aberto, posição, distância, chunk e frequência no servidor.
- **L-0003 — Processos paralelos:** inputs, energia e outputs passaram a ser transacionais, persistentes e recuperáveis após rede cheia, reload ou quebra.
- **L-0004 — Células BigInteger:** corrigidos partition/inverter/fuzzy, cálculo de bytes, limites, NBT e suporte completo ao Cell Workbench.
- **L-0005 — DMA e fluidos:** múltiplos requisitos de fluido agora reservam e consomem a soma correta, sem gerar item com fluido insuficiente.
- **L-0006 — Diagnóstico:** adicionadas métricas e comandos de performance por tipo e por máquina.
- **L-0007 — Quantum Slicer:** iniciado o runtime multibloco 3.0, com invalidação por evento e compatibilidade do Slicer existente.
- **L-0008 — Estados:** formalizada a state machine dos controllers (`IDLE`, `RUNNING`, `PAUSED_NO_GRID`, `OUTPUT_BLOCKED` e outros).
- **L-0009 — Stellar outputs:** saídas do Stellar Nexus passaram a aguardar espaço na rede sem perda, duplicação ou reroll.
- **L-0010 — Falha térmica Stellar:** Safe Mode e falha local ficaram seguros por padrão, com grief e lava desativados na configuração padrão.

## Stellar Nexus, coolant e ports

- **L-0011 — Coolant Stellar:** consumo proporcional ao calor, sem desperdiçar volumes parciais inúteis.
- **L-0012 — Energia Stellar:** somente o `AE Energy Input Hatch` pode carregar o Nexus; diagnóstico mostra taxa e entrega real.
- **L-0013 — ThermalSystem:** unificada a matemática racional de calor/coolant do Stellar e dos controllers paralelos.
- **L-0014 — Porta de coolant Stellar:** somente o `ME Massive Fluid Hatch` formado fornece coolant ao Stellar.
- **L-0015 — Resfriamento idle:** Stellar dissipa calor parado e pode resfriar com coolant mesmo fora de uma receita.
- **L-0016 — Química:** ports químicos dos multiblocos passaram a usar planejamento `simulate → commit` transacional.
- **L-0017 — Itens:** roles de input/output foram explicitadas e o transporte de itens dos ports tornou-se transacional.

## Multiblocos, visualização e automação

- **L-0018 — QMF:** Quantum Matter Fabricator migrou para topology/definition compilada, scan rápido e invalidação orientada a eventos.
- **L-0019 — QPA:** Quantum Processor Assembler recebeu a mesma migração versionada, preservando sua geometria e saves.
- **L-0020 — Preview JEI:** viewer 3D nativo de multiblocos substituiu o caminho LDLib, com camadas, rotação, zoom, seleção e detalhes.
- **L-0021 — Auto-build:** adicionado auto-build seguro nas UIs Stellar/universais; não substitui blocos divergentes e consome inventário corretamente.
- **L-0022 — Cryoforge:** Quantum Cryoforge migrou para a definition compilada e para o ciclo event-driven.
- **L-0023 — Autocrafting:** patterns prometem apenas a saída garantida; bônus de catalyst viram byproducts persistentes e não travam CPUs AE2.

## Compatibilidade, jogador e performance

- **L-0024 — Dependências:** metadata do mod agora declara corretamente AE2, AE2AddonLib, GeckoLib e Mekanism; integrações opcionais continuam opcionais.
- **L-0025 — Lifecycle AE2:** removida a interceptação global indevida do lifecycle do AE2, preservando drives e upgrades das células UFO.
- **L-0026 — Voo:** criada política de ownership para não remover voo concedido por Creative, Spectator ou outros mods.
- **L-0027 — Armaduras:** efeitos de armadura são renovados somente quando necessário, reduzindo trabalho por tick sem perder bônus.
- **L-0028 — DMA quente:** partículas e busca de entidades foram cadenciadas; o hazard preserva dano, proteção e leitura visual com custo muito menor.
- **L-0029 — Runtime paralelo:** caches de nó AE2/catalyst, sync com janela e menos `setChanged()` reduziram custo de controllers ociosos.
- **L-0030 — Estruturas:** removido o polling periódico restante; quebra, chunks, explosões e pistões invalidam a estrutura por eventos.
- **L-0031 — Scan compilado:** matcher estrutural e reconciliação de parts foram compilados/otimizados; widgets universais receberam sprites definitivos.
- **L-0032 — Snapshot de GUI:** update do cliente deixou de enviar NBT persistente completo; a visualização de até 27 processos ficou compacta.
- **L-0033 — Storage batching:** transferências AE2 iguais no mesmo tick são agrupadas por chave, mantendo ordem e segurança transacional.

## Documentação, assets e manutenção

- **L-0034 — GuideME e organização:** guia refeito por progressão, com cenas geradas da definition canônica; assets e ferramentas receberam ownership documentado.
- **L-0035 — Tutorial legado:** removido integralmente o protótipo Ponder/tutorial duplicado, preservando GuideME, JEI, hologramas e auto-build.
- **L-0036 — CTM nativo:** casings Quantum/Entropy ganharam connected textures nativas NeoForge, sem LDLib; corrigido também o defeito inicial de blocos pretos.
- **L-0037 — Limpeza de resources:** changelogs históricos foram centralizados, duplicatas manual/gerada removidas e 134 texturas legadas sem consumidor foram limpas com testes de layout e aprovação visual dirigida.

## Validação geral

Os marcos concluídos foram acompanhados por testes Java/Gradle e, quando aplicável, por roteiros humanos em `.project-control/tests/testes-humanos-l-*.md`. O registro técnico detalhado — decisões, arquivos, métricas e evidências — permanece em `CONTINUITY_LEDGER.md`.
