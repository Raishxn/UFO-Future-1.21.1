# 🗺️ UFO Future — Roadmap de Updates Futuras

> Propostas de 2026-09-06, baseadas no estado `3.0.0-alpha.1` e no `CONTINUITY_LEDGER.md`.
> Itens marcados **[pendente]** já constam no ledger (só foram organizados aqui);
> **[novo]** são propostas desta rodada.
>
> **Referências de design** (o padrão de "agrega de verdade"):
> [Neo ECO AE Extension](https://www.curseforge.com/minecraft/mc-mods/neo-eco-ae-extension)
> — três multiblocos grandes e independentes (crafting, storage e computação);
> [AE2 Lightning Tech](https://www.curseforge.com/minecraft/mc-mods/ae2-lightning-tech)
> — energia capturada de fenômenos do mundo (já portamos seu preview/auto-build).

---

## 1. Multiblocos — a linha que agrega

### 1.1 Re-imaginação da família entrópica como trio NeoECO **[pendente — decisão do autor]**

O ledger já registra que `EntropicAssemblerMatrixBE` será excluída e a família
entrópica inteira re-imaginada como multibloco. A proposta concreta é seguir o
modelo NeoECO e dividir em **três estruturas independentes**, cada uma com a
escada MK1/MK2/MK3 e field generators que o mod já tem:

| Estrutura | Papel | Base técnica que reaproveita |
|-----------|-------|------------------------------|
| **Multibloco de Computação** (novo Entropic Convergence Engine) | Crafting CPU físico: blocos de crafting-storage e co-processadores instalados na própria estrutura, fila de mais de 1 job por MK | Orçamento de execução por tick já mapeado no AE2 (`getCoProcessors() + 1`); matemática agregada segura (decisão arquitetural nº 6) |
| **Multibloco de Crafting** (nova Entropic Assembler Matrix) | Aceita patterns AE2 direto na estrutura, throughput e paralelismo por MK | Quantum Pattern Hatch (72 patterns) e o contrato de autocrafting da L-0023 |
| **Multibloco de Armazenamento — "Stellar Vault"** | MEStorage gigante: células BigInteger/Infinity instaladas como blocos físicos na estrutura, byte-pool agregado, particionável | `BigCellCapacityMath`, `BigIntegerLimits`, `IPartitionList` já corrigidos na L-0004 |

- A Vault é o único dos três que **não existe em nenhuma forma hoje** — é o
  equivalente direto do multibloco de storage do NeoECO e fecha o trio.
- O orçamento de execução por tick do multibloco de computação serve também
  como solução estrutural do **P0 de autocraft massivo** (item 4.1).
- Cada substituição precisa do marco próprio com política de IDs/saves, como o
  ledger já exige.

### 1.2 Ion Storm Collector — energia capturada de fenômenos **[novo]**

Multibloco no espírito do AE2 Lightning Tech, com identidade UFO: um coletor
que converte **fenômenos do mundo** em energia AE — raios de tempestade como
fonte principal, com possibilidade de capturar explosões próximas e eventos
dimensionais como fontes secundárias de eficiência diferente.

- **Energia de pico, não constante**: tempestades geram bursts que carregam o
  buffer AE do multibloco (complemento natural do Event Horizon Energy Cell,
  que hoje só armazena).
- **Safe por padrão**: nenhum grief sem opt-in do servidor — reusa o padrão de
  design do `StellarExplosionPolicy` (L-0010), que já resolve exatamente esse
  problema para o Stellar.
- MK1/MK2/MK3 aumentam raio de captação, taxa de conversão e buffer; coolant
  opcional no OC, como no resto da linha.

### 1.3 Quantum Stock Vault — stocking multibloco **[novo]**

Estrutura (ou part, se preferir começar pequena) que **mantém estoque-alvo** no
ME network: "manter 256 de cada ingrediente de processor", puxando do storage e
guardando num buffer ME-native interno.

- Automatiza o abastecimento de autocrafting sem fileira de export buses.
- MK tiers = número de alvos monitorados e taxa de reposição.
- Encaixa no abastecimento explícito novo: o jogador define o alvo, a máquina
  só repõe pelo hatch.

### 1.4 Quantum Disassembler — reciclagem **[novo]**

Máquina ou multibloco que decompõe equipamentos UFO (armadura, multi-tool,
células antigas, catalysts obsoletos) de volta em componentes, com percentual
de retorno e custo de energia/coolant. A "disassembly" já apareceu como recorte
fora de escopo do rebalance de receitas — como feature própria ela fecha o loop
de progressão e dá destino a sobras de upgrades.

---

## 2. Rede ME e integrações

### 2.1 Side config real dos hatches **[pendente]**

`PacketChangeSideConfig` existe, está protegido pela `MachinePacketGuard`, mas
continua no-op — a mecânica de lados nunca foi implementada. É a pendência de
UX mais antiga do core.

### 2.2 Ownership/claims nos controllers **[pendente]**

`MachinePacketGuard` já autentica menu/posição/distância, mas não existe
política de gameplay de dono/equipe. É pré-requisito para habilitar com
segurança o modo destrutivo opt-in em servidores ( Stellar explosivo, Ion
Collector ) — reconhecido como limite consciente na L-0010.

### 2.3 Stellar Level Emitter — threshold BigInteger **[novo]**

Level emitter que lê **quantidades agregadas** das células BigInteger e dispara
autocraft/redstone em thresholds de bilhões. Hoje não há trigger bom para
"quando eu tiver menos de 5B de Pulsar Matter, crafta mais" — e esse fluxo é o
coração do endgame do mod. Naturalmente emparelha com o Stellar Vault (1.1).

### 2.4 FE Output Port **[novo]**

Port que devolve o buffer AE como FE/RF para o mundo (Mekanism, Applied Flux,
máquinas externas). O Event Horizon Cell já aceita FE de entrada; a saída fecha
o ciclo de energia bidirecional que o mod promete no README.

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

Linha de material gate (ex.: "Black Hole Matter") para os multiblocos novos do
item 1 — o trio entrópico e o Ion Collector precisam de um degrau acima de
Pulsar para não encostar no teto atual da progressão. Aproveita a cadeia de
matéria existente (Neutronium → Proto → Dark Matter).

### 3.6 Tools/scanner **[pendente]** — melhorias do multi-tool e do scanner de
estrutura citadas como fora de escopo do rebalance, pendentes de recorte.

### 3.7 Armadura modular UFO + remoção da Astral Nexus **[novo — proposta em discussão]**

Transformar a UFO Armor na armadura modular do mod (padrão AdvancedAE/AE2
Lightning Tech: peça base + slots + cartões de módulo + energia) e remover a
Astral Nexus, cujos poderes viram módulos top-tier pagos.

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
| Energia | Storm Harvester | peitoral | 3 | Gera RF em tempestade/raios (gêmeo do Ion Collector); taxa |
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
| AE2 | Stock Sentinel | capacete | 2–3 | Monitora alvo de estoque na rede e notifica/autocrafta; lista de alvos (emparelha com o item 2.3) |

**Backlog divertido (uma linha cada)**: Entropy Sink (converter lixo em RF,
opt-in), Holo Cloak (invisibilidade para outros players), Apocalypse Beacon
(chamar o Type-A como aliado), Warp Module (teleporte sem pérola).

**Decisões abertas**: destino do Thermal Resistor Exosuit (nicho sobrepõe com
Pyro Barrier + DMA Thermal Shield), política de deprecação dos 4 IDs da Astral
e se a bancada abre a trilha de telas do autor.

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

### 4.4 GameTests **[pendente — risco conhecido]**
175 testes puros, zero GameTests. Movimento por pistão, unload de chunk e
sobreposição de estruturas estão descobertos. Novos multiblocos devem entrar
com GameTests desde o primeiro recorte.

### 4.5 ME Addon Toolkit no RaishxCore **[pendente — direção confirmada]**
Extrair a infraestrutura genérica (definição compilada, holograma, auto-build,
widget de supply, ports) para o Core, com o modid já renomeado para
`raishxcore`. Só depois de validar o widget de supply (4.2).

### 4.6 Trilha de UI do autor **[pendente — pausada por decisão]**
Telas próprias, UI-0002 e o mockup JEI (`/home/raishxn/MineProjects/printUI/
mockup-jei-multiblocos-universal-v1.png`) só com nova autorização. O snapshot
`getCoolantStatus`/`getUpgradeStatus` já expõe os dados que a tela vai
consumir.

### 4.7 Crédito GTO Project no LICENSE.md **[pendente — apontado no ledger]**
README credita o GTO Project pelas folhas CTM; LICENSE.md não. Correção
aditiva de uma linha.

---

## Ordem sugerida

1. **4.1 → 4.2 → 4.4** — estabilizar (P0 + validação de supply + GameTests).
2. **1.1 (trio entrópico)** — maior valor agregado; o multibloco de computação
   resolve o P0 estruturalmente e a Vault fecha o modelo NeoECO.
3. **2.3 + 1.2** — ecossistema de storage (Level Emitter BigInteger) e energia
   capturada (Ion Collector), ambos alimentados pelo que já existe.
4. **1.3 + 1.4 + 3.5** — Stock Vault, Disassembler e o material Supermassive
   como novo degrau de progressão.
5. **2.1 + 2.2 + 4.5 + 4.6** — UX e fundação em paralelo, conforme janela do
   autor (side config → ownership → ME Addon Toolkit → telas próprias).
6. **3.1–3.6** — conteúdo de progressão espaçado entre os itens acima.
7. **3.7 (armadura modular)** — linha paralela de conteúdo: o sistema de
   módulos pode começar após o passo 2, contanto que a fundação do passo 1
   esteja fechada; a remoção da Astral Nexus acompanha o primeiro release com
   módulos.
