# Montador de Materia Dimensional

O **Montador de Materia Dimensional (DMA)** continua sendo a primeira maquina avancada do UFO Future, mas agora ele faz parte de uma progressao maior de multiblocos em vez de tentar resolver sozinho todo craft extremo do endgame.

## Comportamento Base

- crafting shapeless com grade de 9 slots
- entrada opcional de fluido de receita
- tanque dedicado de coolant para controle termico
- catalisadores para velocidade, eficiencia, estabilidade e comportamento avancado de output
- funcionamento alimentado por AE2

O DMA e a maquina flexivel de bloco unico para progressao inicial, media e para recipes menores ou mais experimentais.

## Separacao dos Tanques

O DMA tem duas responsabilidades de fluido bem diferentes:

- **tanque de fluido base**: consumido apenas quando a recipe pede fluid input
- **tanque de coolant**: usado apenas para retirar calor da maquina

Coolants nao devem aparecer como fluid input normal de receitas do DMA. Eles pertencem apenas ao sistema de refrigeracao.

## Progressao de Coolants

A progressao desejada agora e:

1. **Gelid Cryotheum** no inicio
2. **Stable Coolant** para automacao sustentada
3. **Temporal Fluid** para cargas extremas de endgame

Isso ficou mais importante porque as configuracoes fortes agora dependem de sair do Gelid Cryotheum em vez de trata-lo como solucao eterna.

## Calor e Falha

O DMA gera calor enquanto trabalha e esfria lentamente quando fica ocioso. A maquina passa por tres estados:

- **zona segura** abaixo de 50%
- **zona de perigo** acima de 50%, com avisos visuais e dano proximo
- **meltdown** em 100%, seguido de contagem regressiva e explosao destrutiva se o resfriamento nao recuperar

Se voce empilhar catalisadores agressivos sem melhorar o coolant, o DMA vai cobrar esse erro.

## Onde o DMA Para

Receitas gigantes de endgame nao devem mais ser resolvidas inflando o DMA singleblock. Esse papel agora vai para a linha de multiblocos:

- [Quantum Matter Fabricator](quantum-matter-fabricator.md)
- [Quantum Slicer](quantum-slicer.md)
- [Quantum Processor Assembler](quantum-processor-assembler.md)

## Formato JSON de Receita

As recipes do DMA continuam usando o tipo `ufo:dimensional_assembly`.

A estrutura exata da recipe e os exemplos de script continuam documentados em [Receitas KubeJS](kubejs-recipes.md). Esta pagina fica focada no comportamento da maquina e na progressao.

## Veja Tambem

- [Catalisadores](catalysts.md)
- [Tiers de Multibloco](multiblock-tiers.md)
- [Progressao](progression.md)
