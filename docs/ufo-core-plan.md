# Plano — UFO Core 3.0

## Objetivo

Construir uma fundação reutilizável para addons tecnológicos integrados ao AE2:
multiblocos, ports, widgets, preview, sync e progressão de escala extrema. O Core
deve permitir máquinas próprias sem copiar controllers, telas ou lógica de
transação a cada addon.

Desde `0.1.0-alpha.1`, ele é um mod separado em `UFO-Core-1.21.1`, com modid
`ufocore`. O UFO Future é o primeiro consumidor real e declara o Core como
dependência obrigatória. A API permanece alpha até um segundo addon validar os
contratos sem precisar de forks.

## Princípios

- Autoridade e transações permanecem no servidor; a tela apenas apresenta estado
  compactado e pede ações autorizadas.
- Uma `MultiblockDefinition` canônica alimenta formação, scan, membership,
  auto-build, preview JEI e cenas GuideME.
- APIs públicas são pequenas, documentadas e independentes de telas, recipes ou
  conteúdo de um addon específico.
- Integrações com AE2, Mekanism, JEI e EMI ficam em adaptadores; o núcleo não usa
  esses tipos na API matemática.
- Escala numérica nunca cria loops por unidade. Todas as operações são agregadas,
  saturadas quando uma API externa exigir `long` e verificadas antes do commit.
- Widgets seguem o visual AE2/UFO, mas não possuem nomes ou regras de uma máquina
  concreta.

## Estrutura implementada

```text
UFO-Core-1.21.1
  api.amount       números exatos, ratios, encoding e formatação
  api.tier         tiers data-oriented
  api.multiblock   definição, roles e estados/resultados puros
  api.port         ports agregados de energia, fluidos, itens e chemicals
  api.transaction  planejamento determinístico de transferências
  api.network      identidade estável de ações de máquina
  client           widgets AE2/UFO reutilizáveis
  neoforge         validação server-side e rate limit de packets
UFO-Future-1.21.1  máquinas, recipes, progressão e assets do addon atual
```

O Core produz `ufocore-0.1.0-alpha.1.jar`; o addon produz seu artefato separado.
No desenvolvimento, um composite build substitui a coordenada Maven do Core
pelo projeto irmão automaticamente.

## Superfície pública proposta

### `ufo-core-api`

- `amount`: `UfoAmount` para quantidades inteiras exatas e não negativas,
  `UfoRatio` para taxas/eficiências fracionárias e formatadores de magnitude.
- `tier`: definição data-driven de tier, requisito, custo, multiplicador e
  apresentação; conteúdo decide a curva, o Core apenas a avalia com segurança.
- `multiblock`: definitions, roles, resultados de scan, estados runtime e planos
  de auto-build; sem blocos/receitas próprios.
- `transaction`: reserva, commit parcial, saldo pendente, refund e resultados
  determinísticos para recursos agregados.
- `sync`: records compactos de visualização, sem NBT de save nem classes cliente.

### `ufo-core-neoforge`

- scanner compilado, membership index e invalidação por eventos já comprovados;
- guard/rate-limit de packets de máquina e lifecycle de sessões de auto-build;
- adaptadores de storage AE2, fluidos NeoForge e chemicals Mekanism;
- config de limites, telemetria e migrações de dados;
- bridges que convertem valores do Core para limites de APIs externas sem perder
  saldo interno ou fazer cast silencioso.

### `ufo-core-client`

- `UfoToolbar`: barra lateral AE2 com layout, exclusão JEI e ciclo de vida seguro
  em resize;
- `UfoStateIconButton`, botões de atlas e widgets de ação/estado reutilizáveis;
- campos centralizados, barras de progresso/energia/calor e tooltips compactos;
- modelos de preview, holograma e adaptadores JEI/EMI opcionais;
- contratos de snapshot: cliente nunca lê o NBT persistente para desenhar uma
  máquina.

## Escala numérica e tiers

O Core usará dois tipos autoritativos, não `double`:

| Necessidade | Tipo | Regra |
|---|---|---|
| Itens, energia, fluido, capacidade e outputs | `UfoAmount` (`BigInteger`) | Inteiro exato, não negativo, com fast path `long` quando possível. |
| Taxas minúsculas, eficiência, calor por mB e multiplicadores | `UfoRatio` (numerador/denominador `BigInteger`) | Fração reduzida; arredondamento só no limite da transação. |

Tiers não serão um enum fixo. Cada família declara uma curva: base, fator,
limites, requisitos e texto de apresentação. Assim uma máquina pode usar
MK.I–III, enquanto endgame usa tiers exponenciais sem forçar todo addon a ter a
mesma tabela. A interface mostra notação curta (`1.25 Qa`, `4.0e18`) e tooltip
exato; o servidor conserva o valor preciso.

AE2 e NeoForge continuam recebendo apenas valores dentro de seus contratos
`long`/`int`. Se uma transferência extrema exceder o limite externo, o Core a
divide em batches transacionais limitados por tick; não reduz nem duplica o
saldo.

## O que fica fora do Core

- receitas, loot, balanceamento e IDs de conteúdo;
- modelos/texturas específicos de um addon;
- a topologia e o propósito da futura Entropic Assembler Matrix;
- políticas específicas de explosão, coolant ou catalyst do Stellar Nexus;
- compatibilidade opcional que ainda não tenha consumidor real.

## Estado e próxima ordem de execução

1. **Concluído:** scaffold separado, metadata, wrapper, testes, sources JAR e
   publicação Maven local.
2. **Concluído:** `amount`, `ratio`, tiers, definições de multibloco, transações e
   ports neutros, todos com testes de contrato.
3. **Concluído:** packet guard/rate-limit e widgets universais extraídos; Stellar
   e as telas universais já consomem as implementações do Core.
4. **Próximo:** extrair scanner, membership index e lifecycle de auto-build em
   etapas pequenas, preservando NBT, packets e comportamento já aprovado.
5. **Próximo:** criar snapshots universais, barras/medidores e preview no cliente.
6. **Criar o primeiro addon novo:** definir uma máquina/progressão que
   realmente use o Core e revele lacunas da API.
7. **Congelar API v1:** após `ufo-future` e um segundo addon consumirem o Core sem
   forks, estabilizar `com.raishxn.ufocore.api` como API 1.0.

## Critérios para cada extração

- Compilação, testes, datagen e cliente continuam verdes.
- O comportamento de save, packet e recipe existente é idêntico.
- O módulo extraído tem ao menos um teste puro de contrato e um consumidor real.
- Nenhum pacote `api` expõe classes de Minecraft, NeoForge, AE2 ou GUI quando um
  record/adapter próprio resolve o acoplamento.
- Mudanças de API exigem versionamento, migration note e teste de compatibilidade.

## Direção para a Entropic Assembler Matrix

Ela não deve virar apenas “mais uma CPU maior”. Antes de projetá-la, precisamos
decidir a função que falta ao ecossistema:

- **Lattice Computer:** throughput/memória/segurança de autocrafting em módulos;
- **Matter Compression Matrix:** transformação de quantidades enormes com
  `UfoAmount`, compressão e decomposição controladas;
- **Stellar Foundry:** produção térmica modular, coils/campo/cooling loop e risco;
- **Entropy chain:** conversão poderosa com resíduos, contenção e trade-offs.

A recomendação inicial é a **Matter Compression Matrix**: ela prova a matemática
de escala extrema do Core, não concorre diretamente com CPUs AE2 fortes e cria
um papel claro para inputs/outputs muito grandes. A decisão final deve vir antes
de criar bloco, recipe ou save para a Matrix.
