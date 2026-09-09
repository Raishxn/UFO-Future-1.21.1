---
navigation:
  parent: ufo_intro/quantum_hatches.md
  title: Rede e Bônus Wireless
  position: 1
---

# Rede e Bônus Wireless

A rede wireless distribui patterns e insumos entre máquinas sem cabos de transferência em cada destino. A origem continua precisando de uma rede ME energizada. Ela não fornece energia de operação à máquina nem carrega chunks.

## Montando a rede

1. Para multiblocos, conecte o Quantum Pattern Buffer à rede ME e coloque nele os patterns de processamento. Instale e vincule um Pattern Proxy em cada multibloco adicional. Para DMAs, continue usando o Quantum Pattern Hatch legado.
2. Ative wireless na UI da hatch.
3. Selecione a hatch com a [Quantum Wireless Tool](quantum_wireless_tool.md).
4. Clique na face de entrada de cada máquina. Repetir o clique na mesma face remove o vínculo.
5. Solicite a receita pelo terminal ME. Os resultados precisam retornar à rede por uma saída existente ou Auto Import da [Quantum Interface](quantum_interface.md).

A ferramenta mostra a origem e a prévia em amarelo; linhas e faces azuis identificam vínculos existentes. As máquinas devem estar carregadas, na mesma dimensão e dentro do alcance. Outra rede ME não pode ser usada como ponte.

Para coolant, configure uma Quantum Interface ligada à rede e ative Auto Export. Selecione essa interface e vincule as faces de entrada de coolant. Ela fornece apenas logística: **o Pattern Buffer concede buffs aos multiblocos e o Pattern Hatch legado concede buffs aos DMAs**.

## Alcance ajustável

Cada hatch/interface começa com **32 blocos**. A engrenagem de alcance na barra lateral permite adicionar 1 bloco por clique ou retirar 1 com Shift+clique. O ajuste é salvo individualmente e respeita o máximo do servidor.

O alcance limita criação e operação dos vínculos. Reduzir o alcance suspende os destinos mais distantes sem apagar os vínculos; aumentar novamente permite retomá-los. O servidor verifica a distância, inclusive quando a ferramenta já tinha uma origem selecionada.

## Buffs automáticos

Configure uma vez no arquivo **config/ufo/wireless.toml** da instância/servidor. Não existem perfis, pontos nem modos de bônus na UI. Velocidade, desconto de energia e desconto térmico são independentes e atuam juntos.

O DMA e os multiblocos possuem limites separados. Os padrões iniciais de cada família são:

- Velocidade: até +20%.
- Energia por execução: até −10%.
- Calor produtivo por execução: até −15%.

Contam máquinas distintas, carregadas, vinculadas e não criativas que avançaram o processamento de uma receita nos últimos **20 ticks (um segundo)**. Podem executar patterns diferentes e receber insumos pelo wireless, por cabos ou manualmente. Estar conectada, receber coolant ou esperar insumos/energia não conta como atividade produtiva. Multiblocos precisam estar montados. Várias faces ou threads da mesma máquina contam uma vez. Uma máquina recebe fatores neutros; a força cresce linearmente até **10 DMAs** ou **4 multiblocos**. As famílias são contadas separadamente. Com 2 multiblocos, os valores padrão dão aproximadamente +6,67% de velocidade, −3,33% de energia e −5% de calor. Com 3, dão +13,33%, −6,67% e −10%.

A hatch precisa estar com wireless habilitado e nó ME ativo. Hatches diferentes não acumulam buffs: prevalece o bônus mais forte aplicável. Cada máquina fixa os fatores no início da receita (antes da reserva de energia nos multiblocos) e os mantém até terminar, inclusive ao salvar. A primeira rodada após inatividade pode não ter bônus; receitas seguintes aproveitam a atividade crescente. A contagem de atividade reinicia após recarregar o mundo.

O painel da hatch mostra conexões e contagens/fatores ativos na ordem **DMA / Multi**, disponíveis para novas receitas. O widget do DMA/controller mostra os buffs aplicados aos seus trabalhos; em multiblocos, exibe a média dos trabalhos reservados/iniciados não pausados. Entre receitas, mostra os fatores disponíveis para a próxima execução em vez de voltar a 1x. O tooltip dos efeitos identifica se são fatores das receitas atuais ou uma prévia da próxima receita.

## Configuração

Exemplo de parâmetros em config/ufo/wireless.toml:

```toml
[wireless]
range = 128
maxLinks = 1024

[wireless.buffs]
enabled = true

[wireless.buffs.dma]
saturationMachines = 10
maxSpeedBonus = 0.20
maxEnergyDiscount = 0.10
maxHeatDiscount = 0.15

[wireless.buffs.multiblock]
saturationMachines = 4
maxSpeedBonus = 0.20
maxEnergyDiscount = 0.10
maxHeatDiscount = 0.15
```

range é o teto permitido para os ajustes locais (0 remove apenas esse teto). Cada origem começa em 32. Um valor 0.20 representa 20%. Para desativar um efeito, coloque seu valor em 0. Reinicie a instância/servidor depois de editar para aplicar de forma previsível. Em multiplayer, os valores usados para processamento são os do servidor.

A velocidade preserva a energia total anterior ao desconto, podendo exigir mais potência por tick. O calor produtivo é compensado pela velocidade antes do desconto térmico. Resfriamento passivo e eficiência do coolant não mudam. Receitas de um tick não ficam menores.

Compatíveis: DMA, Quantum Matter Fabricator, Quantum Slicer, Quantum Processor Assembler e Quantum Cryoforge. Stellar Nexus e máquinas de outros mods não recebem esses buffs.

O antigo wireless.buffs.saturationMachines compartilhado foi substituído pelas chaves das duas famílias. Se você personalizou o valor antigo, copie o valor desejado para cada nova seção; os novos padrões são 10 e 4.

O antigo activityTicks e o histórico de envio por pattern não são mais usados. Mantenha seus limites por família e os tetos dos efeitos; não é necessário configurar outra métrica.
