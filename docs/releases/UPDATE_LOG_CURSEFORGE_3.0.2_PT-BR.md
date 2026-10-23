# UFO Future 3.0.2

Abelhas de fragmento estelar, energia com peso menor nos crafts e correções de desempenho do early game no servidor.

## Conteúdo novo

- **Abelhas de fragmento estelar para o Productive Bees** (somente com o mod instalado). As abelhas White Dwarf, Neutron Star e Pulsar saem de uma cadeia de conversão: dê um lingote de fragmento a uma Netherite Bee para obter a White Dwarf Bee, a uma White Dwarf Bee para a Neutron Star Bee e a uma Neutron Star Bee para a Pulsar Bee. Elas não se reproduzem entre si, precisam do bloco do fragmento como flor e o favo dá pepitas do fragmento com chance baixa.
- **Chances configuráveis.** `config/ufo/common.toml`, seção `compat.productivebees.<família>`: `conversionChance`, `nuggetChance` e `nuggetMax`. Edite o arquivo e rode `/reload`; datapacks e KubeJS continuam podendo sobrescrever os mesmos ids de receita.

| Abelha | Chance de conversão por lingote | Chance de pepita por favo |
|---|---:|---:|
| White Dwarf | 8% | 15% |
| Neutron Star | 5% | 10% |
| Pulsar | 3% | 6% |

## Balanceamento

- **Energia pesa menos nos crafts.** Nova opção de servidor `machines.recipeEnergyMultiplier` (padrão **0.25**) multiplica o custo de AE das receitas da Dimensional Matter Assembler e dos multiblocos paralelos (QMF, Cryoforge, Slicer, Processor Assembler). JEI e guia mostram o custo já multiplicado. Use `1.0` para voltar ao custo anterior.

## Desempenho do servidor (TPS)

- **DMA com Applied Flux:** toda DMA trabalhando extraía `Integer.MAX_VALUE` FE da rede ME e devolvia a sobra em todo tick (duas operações completas de storage por DMA por tick). Agora puxa só o que cabe no buffer e não mexe na rede com o buffer cheio.
- **DMA sem energia** não consulta mais a rede todo tick: desacelera o tick (até uma vez por segundo) e volta à velocidade normal no primeiro tick com energia.
- Novo teste de carga automatizado do early game: frota de 64 DMAs com receita real em todo build, que falha se uma DMA sem energia consultar a rede todo tick ou se DMAs com buffer cheio mexerem no FE do Applied Flux.

## Requisitos

Minecraft **1.21.1**, NeoForge **21.1.216+**, AE2 **19.2.17 ou 19.2.18** (`[19.2.17, 20)`), AE2 Addon Lib **1.0.3-1.21.1+** e RaishxCore **0.2.2** (`[0.2.2, 1.0)`). Substitua o JAR antigo do UFO; o RaishxCore 0.2.2 continua o mesmo.
