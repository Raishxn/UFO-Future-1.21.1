# 🗺️ UFO Future — Roadmap de Updates Futuras

> Escopo revisado em 2026-09-11 para conter somente direções confirmadas pelo
> autor. Ideias exploratórias não aprovadas foram retiradas.
>
> **Referências de design** (o padrão de "agrega de verdade"):
> [Neo ECO AE Extension](https://www.curseforge.com/minecraft/mc-mods/neo-eco-ae-extension)
> — três multiblocos grandes e independentes (crafting, storage e computação);
> [AE2 Lightning Tech](https://www.curseforge.com/minecraft/mc-mods/ae2-lightning-tech)
> — energia capturada de fenômenos do mundo (já portamos seu preview/auto-build).

---

## 1. Multiblocos — a linha que agrega

### 1.1 Trio endgame de autocrafting **[concluído]**

A re-imaginação foi concluída como três estruturas independentes, todas ligadas
à mesma rede ME por Quantum Grid Link:

| Estrutura | Papel implementado |
|-----------|--------------------|
| **Quantum Computation Nexus** | Pool compartilhado de crafting storages e co-processadores, com CPUs virtuais dinâmicas e modo infinito |
| **Quantum Pattern Fabrication Matrix** | Biblioteca escalável de padrões e assembler virtual para crafting, ferraria e stonecutting |
| **Infinity Fabrication Singularity** | Executor agregado de até 128 rotas persistentes; substitui o loop por cópia em pedidos de bilhões de itens |

O problema prioritário era completar o pipeline `computação → padrões → execução`
e eliminar o P0 de autocraft massivo sem duplicar a infraestrutura BigInteger já
existente.

---

## 2. Rede ME e integrações

### 2.1 Side config real dos hatches **[adiada; superfície incompleta removida]**

O antigo `PacketChangeSideConfig` era um no-op sem UI chamadora nem estado
persistente e foi removido no hardening pós-3.0-alpha.1. A mecânica deve voltar
somente como um recorte completo: contrato por port, persistência, sync, UI,
capability filtering e GameTests.

### 2.2 Ownership/claims nos controllers **[pendente]**

`MachinePacketGuard` já autentica menu/posição/distância, mas não existe
política de gameplay de dono/equipe. É pré-requisito para habilitar com
segurança o modo destrutivo opt-in em servidores (Stellar explosivo) —
reconhecido como limite consciente na L-0010.

### Backlog rápido (ideias menores, sem prioridade definida)

- **Stellar Pattern Encoder** — codificar patterns a partir de itens do network
  (o ExtendedAE provou a demanda; versão UFO poderia ler direto do QMF).
- **Terminal portátil tier UFO** — access terminal wireless com alcance/buffer
  próprios e visual da linha.
- **Blueprint de estrutura** — item que grava um multibloco formado e projeta o
  holograma em outro lugar (o holograma + auto-build já existem; falta a parte
  "copiar de uma estrutura pronta").
- **Integração de drops do Apocalypse Type-A** — dar papel na progressão ao
  entity que hoje é decorativo (drop exclusivo de um catalyst ou do Ion
  Collector).

---

## 3. Progressão e conteúdo

### 3.1 Dimensional Catalyst **[pendente]** — item citado no endgame do README,
nunca fechado como recorte próprio.

### 3.2 Coolant T3 da Cryoforge **[pendente]** — terceiro degrau da escada
Gelid → Stable → Temporal, com fluido novo da linha temporal.

### 3.3 Scrap boxes **[pendente]** — trio Scrap/UU/Liquid Starlight aguardando
métricas de energia antes de reavaliar (A-0001).

### 3.4 Novas simulações Stellar **[pendente]** — rebalance/expansão do
conjunto de simulações; medição antes de rebalancear (L-0045).

### 3.5 Material Supermassive acima de Pulsar **[novo]**

Linha de material gate (ex.: "Black Hole Matter") para conteúdo futuro que
precise de um degrau acima de Pulsar sem encostar no teto atual da progressão.
Aproveita a cadeia de matéria existente (Neutronium → Proto → Dark Matter).

### 3.6 Tools/scanner **[pendente]** — melhorias do multi-tool e do scanner de
estrutura citadas como fora de escopo do rebalance, pendentes de recorte.

### 3.7 Armadura modular UFO + remoção da Astral Nexus **[implementada; hardening pendente]**

A UFO Armor já foi transformada em armadura modular: peça base, slots, 16
cartões craftáveis, configuração server-authoritative e consumo de energia. A
remoção/deprecação definitiva da Astral Nexus continua pendente. Antes do
próximo release, o sistema modular ainda precisa de testes por módulo, limites
configuráveis pelo servidor e profiling multiplayer dos módulos que pesquisam
entidades em área.

**Estado atual verificado no código (2026-09-06)**

- `AstralNexusArmorItem` é creative-only (nenhuma receita gerada) mas com
  efeito real em `AstralNexusEvents`: cancela todo dano, cancela morte, reflete
  ×1.000.000, ar cheio, night vision, water breathing, step assist, imunidade
  radiação Mekanism e participa do voo — tudo sem custo de energia.
- `UfoArmorItem` já tem `ModDataComponents.ENERGY` + IEnergyStorage (400 RF/s
  por peça com set completo), voo via `FlightOwnershipPolicy` (L-0026) e
  efeitos via `ArmorEffectRefreshPolicy` (L-0027).
- Migração: manter os IDs da UFO Armor (zero save-migration); Astral sai da
  creative tab imediatamente e os 4 IDs podem ficar uma versão sem efeito.

**Regras do sistema**

- Slots por peça: capacete 2, peitoral 3, calça 2, botas 2 (9 base) +
  Expansion Frame (+1 slot, cap de 4 por peça). Um módulo não stacka na mesma
  peça; pares exclusivos declarados (ex.: Aegis ↔ Thorns Field).
- Configuração: defaults na bancada; toggles/valores in-game por keybind +
  scroll (mesmo padrão do multi-tool), com clamp server-side; custos e caps
  expostos no `ufo-common.toml` para modpack developers.
- Gating: MK1 = White Dwarf, MK2 = Neutron Star, MK3 = Pulsar/Dark Matter;
  top-tier usa o material Supermassive do item 3.5 e processores
  Tesseract/Event Horizon/Cosmic como custo.

**Catálogo de módulos (proposta)**

| Família | Módulo | Peça | MKs | Efeito / configurável |
|---------|--------|------|-----|------------------------|
| Energia | Capacitor Core | todas | 1–3 | Buffer RF 10M/1B/100B; passivo |
| Energia | AE Link | peitoral | 2–3 | Carrega wireless da rede AE2 (MK3 = range infinito); toggle |
| Energia | Flux Intake | peitoral | 1–3 | Entrada FE externa + Applied Flux; taxa máx. configurável |
| Movimento | Graviton Flight | peitoral | 1–3 | MK1 glide, MK2 voo com cap de velocidade, MK3 voo criativo; velocidade configurável |
| Movimento | Astral Wings | peitoral | 2–3 | Visual das asas (`AstralNexusWingsLayer` reaproveitado) + glide + airdash; força do dash |
| Movimento | Kinetic Servos | calça | 1–3 | Speed I–III + jump boost; níveis configuráveis |
| Movimento | Shock Absorber | botas | 1–3 | Cancela fall damage com custo proporcional; altura máxima |
| Movimento | Gravity Step | botas | 1–2 | Step assist + edge guard ao sneaking; toggle |
| Combate | Singularity Aegis | peitoral | 1–3 | Absorve 75/95/100% do dano, custo RF por hit; reflect off/thorns/×1M (opt-in de servidor, espírito do StellarExplosionPolicy) |
| Combate | Death Denial | capacete | 3 | Nega a morte consumindo carga grande + cooldown; recarrega da rede |
| Combate | Thorns Field | peitoral | 1–2 | Reflete % configurável; exclusivo com Aegis |
| Combate | Ferrofluid Weave | calça | 2 | Imunidade a knockback; toggle |
| Combate | Tool Auto-Charge | qualquer | 1–3 | Recarrega multi-tool/arma RF do inventário; threshold % |
| Utilidade | Magnet Core | capacete | 1–3 | Raio de coleta 4/8/16/32 + blacklist; toggle |
| Utilidade | Auto-Feed | capacete | 1–3 | MK1 come do inventário, MK2+ saturação sintética por RF; alvo de fome |
| Utilidade | Miner's Overdrive | capacete | 1–3 | Haste I–IV; nível; sinergia com auto-smelt do multi-tool |
| Utilidade | Pyro Barrier | peitoral | 1–2 | Imunidade fogo/lava, custo por segundo em lava; toggle |
| Utilidade | DMA Thermal Shield | peitoral | 1–2 | Proteção térmica do DMA (ponte do Exosuit); MK2 operar em Hazard |
| Utilidade | Rad Scrubber | capacete | 1–3 | Imunidade Mekanism + limpa radiação em raio 2/4/8; raio |
| Utilidade | Spatial Anchor | peitoral | 3 | Chunk loading em raio 1–3, RF/s por chunk; gating Spatial Fluid/Primordial Matter |
| Utilidade | Void Swimmer | capacete | 1–2 | Water breathing + swim speed + visão subaquática; toggle |
| Utilidade | Builder's Reach | calça | 2–3 | Alcance estendido com clamp server-side; valor |
| AE2 | Wireless ME Access | peitoral | 2–3 | Terminal ME sem cabo (integra AE2WTLib se presente); MK3 sem range |

**Backlog divertido (uma linha cada)**: Entropy Sink (converter lixo em RF,
opt-in), Holo Cloak (invisibilidade para outros players), Apocalypse Beacon
(chamar o Type-A como aliado), Warp Module (teleporte sem pérola).

**Decisões abertas**: destino do Thermal Resistor Exosuit (nicho sobrepõe com
Pyro Barrier + DMA Thermal Shield), política de deprecação dos 4 IDs da Astral
e se a bancada abre a trilha de telas do autor.

### 3.8 Abelhas UFO (Productive Bees) **[concluído]**

Rosto decidido em 2026-09-11: manter o trio Bola de Matéria / Sucata / Caixa de
Sucata — eles cobrem exatamente os dois loops de volume do pack (matter balls
para UU-matter/células infinity e a cadeia scrap → scrap box). Itens de
progressão (processadores, matrices, catalysts) continuam exclusivos de
máquina e não viram abelha.

- **Buff**: genes de produtividade e tolerância a clima no máximo
  (VERY_HIGH) e centrifugação em dobro (32–64 matter balls, 64–128 scrap,
  4–8 scrap boxes por favo).
- **Obtenção só no QMF** (MK2): receitas caras emitem o
  `spawn_egg_configurable_bee` do Productive Bees com o componente
  `entity_data` da abelha. Com `selfbreed: false`, cada abelha extra custa
  outra rodada de QMF — abelha forte é investimento de multibloco.
- Guarda `mod_loaded: productivebees` em tudo: sem o mod no pack, nada é
  carregado.

---

## 4. Fundação — pré-requisitos que destravam o resto

### 4.1 P0 — autocraft massivo derruba o servidor **[pendente — teste do autor em andamento]**
Pedidos de autocraft com requisitos de centenas de milhões. Primeiro passo é o
repro coletado pelo autor; a solução estrutural é orçamento agregado por tick
no novo multibloco de computação (1.1). **Antes de qualquer conteúdo novo.**

### 4.2 Validação in-game do abastecimento explícito **[pendente]**
Hatches de coolant/energia externos + `MultiblockSupplyWidget` implementados em
2026-09-05, aguardando o roteiro de teste do autor
(`.project-control/audits/teste-abastecimento-explicito.md`).

### 4.3 Performance do Stellar idle **[pendente]**
14,90 µs/tick medidos → alvo exploratório de 5 µs. Não bloqueia conteúdo, mas
qualquer multibloco novo deve nascer já dentro do runtime unificado.

### 4.4 GameTests **[em evolução]**
Em 2026-09-16, 229 testes unitários e 28 GameTests passaram localmente. A suíte
cobre unload/reload físico, unload parcial, recuperação do Grid Link, outputs
pendentes, migração de saves e isolamento da Thermal Suit em relação a energia
e módulos UFO. Movimento por pistão e sobreposição de estruturas continuam
descobertos. Novos multiblocos devem entrar com GameTests desde o primeiro recorte.

### 4.5 Extração completa do ME Addon Toolkit para o RaishxCore **[próxima prioridade]**
Concluir a extração da infraestrutura genérica: definição/matcher compilado e
constraints, scanner, índice de membership e invalidação, holograma, auto-build,
viewer, widgets/snapshots, ports e guards de packet. O Core mantém contratos
neutros; topologias, receitas e balanceamento continuam no UFO Future.

### 4.6 Trilha de UI do autor **[pendente — pausada por decisão]**
Telas próprias, UI-0002 e o mockup JEI (`/home/raishxn/MineProjects/printUI/
mockup-jei-multiblocos-universal-v1.png`) só com nova autorização. O snapshot
`getCoolantStatus`/`getUpgradeStatus` já expõe os dados que a tela vai
consumir.

### 4.7 Crédito GTO Project no LICENSE.md **[pendente — apontado no ledger]**
README credita o GTO Project pelas folhas CTM; LICENSE.md não. Correção
aditiva de uma linha.

### 4.8 GuideME em chinês simplificado **[em andamento — 27/48 páginas]**

Três recortes concluídos para `zh_cn`: índice, entrada, navegação principal,
diagnóstico, QMF, Quantum Slicer, Quantum Processor Assembler, Stellar Nexus,
suas 13 subpáginas operacionais, mega crafting, containment e ferramentas.
Essas páginas usam terminologia consistente com `zh_cn.json` e possuem teste
contra regressão para placeholders sem texto Han. Links de capítulos ainda não
traduzidos usam o fallback da página-base até o lote correspondente ser concluído.

Próximos lotes, nesta ordem:

1. DMA, Cryoforge, hatches, células, armaduras, materiais, catalisadores e fragmentos;
2. trio de autocrafting endgame;
3. sistemas complementares (campos, abelhas e wireless);
4. revisão terminológica e navegação final dentro do jogo.

---

## Ordem sugerida

1. **Validar o trio 1.1 no `runClient` e no save real**, incluindo migração,
   repetição agregada e proibição absoluta de Fields mistos.
2. **4.5 — extração completa para o RaishxCore**, mantendo testes de contrato e
   validando o UFO a cada recorte migrado.
3. **4.1 → 4.2 → 4.4** — fechar o P0, validação de supply e cobertura GameTest.
4. **2.1 + 2.2 + 4.6** — side config, ownership e telas quando autorizadas.
5. **3.1–3.6** — conteúdo de progressão espaçado entre os itens acima.
6. **3.7 (armadura modular)** — linha paralela de conteúdo: o sistema de
   módulos pode começar após o passo 2, contanto que a fundação do passo 1
   esteja fechada; a remoção da Astral Nexus acompanha o primeiro release com
   módulos.
