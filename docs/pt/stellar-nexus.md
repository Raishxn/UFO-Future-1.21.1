# Stellar Nexus

O **Stellar Nexus** e o multibloco final de simulacao do UFO Future. Ele consome quantidades absurdas de AE, fluidos raros e materiais de alto tier para produzir outputs em escala extrema ao longo de ciclos longos.

Esta pagina foca na logica e no modelo de controle da maquina, nao em listas fixas de recipes.

## Identidade da Maquina

- multibloco massivo
- le itens e fluidos direto da rede ME
- carrega um buffer interno enorme de AE enquanto fica montado
- consome fuel no inicio e coolant durante a execucao
- usa calor, safe mode e overclock como mecanicas centrais de balanceamento

## Tiers dos Field Generators

As quatro posicoes de field precisam ser do mesmo tier:

- **MK1**
- **MK2**
- **MK3**

Misturar tiers invalida a estrutura. O tier define acesso a recipes e influencia carga e resfriamento.

## Escada de Coolants

O Stellar Nexus agora segue a progressao correta:

- **Gelid Cryotheum** = baixa eficiencia
- **Stable Coolant** = eficiencia media
- **Temporal Fluid** = eficiencia extrema

A maquina nao deve mais tratar agua como coolant normal de progressao.

## Safe Mode

O Safe Mode e a opcao segura para automacao.

Quando ativado, a maquina:

- consome mais AE
- consome mais fuel
- consome mais coolant
- desliga em vez de explodir ao atingir calor maximo

Seguranca custa caro de proposito.

## Modo Overclock

O controller ja suporta **Overclock Mode**.

Efeitos:

- **5x mais rapido**
- **10x** custo de AE
- **5x** consumo de fuel
- **5x** geracao de calor
- **5x** consumo de coolant

Isso vale tambem para recipes custom adicionadas por KubeJS, porque o comportamento esta na logica da maquina.

## Explosao Catastrofica

Se o Safe Mode estiver desligado e a maquina atingir calor maximo, o Stellar Nexus entra numa sequencia real de destruicao em vez de uma explosao cosmetica leve.

A implementacao atual:

- expande em camadas para evitar um pico instantaneo gigante
- destroi blocos reais ao redor do controller
- converte o nucleo interno em lava
- incendeia a zona externa
- causa dano repetido nas entidades proximas conforme a onda se propaga

## Lugar na Progressao

- **DMA** cuida do crafting avancado flexivel
- **multiblocos quantum** cuidam das linhas em massa via AE2
- **Stellar Nexus** cuida das simulacoes extremas e da geracao final

## Veja Tambem

- [Montador de Materia Dimensional](dma.md)
- [Quantum Matter Fabricator](quantum-matter-fabricator.md)
- [Tiers de Multibloco](multiblock-tiers.md)
