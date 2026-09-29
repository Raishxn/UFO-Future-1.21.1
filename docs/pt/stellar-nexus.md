# Stellar Nexus

O **Stellar Nexus** e o multibloco final de simulacao do UFO Future. Ele consome AE, fluidos raros e materiais de alto tier para gerar outputs em escala extrema.

## Identidade Da Maquina

- Multibloco massivo
- Le itens e fluidos direto da ME
- Carrega um buffer interno de **200B AE**
- Consome combustivel ao iniciar e coolant durante a operacao
- Usa calor, safe mode e overclock como eixos principais
- Exige os hatches de entrada e saida de itens, um ME Massive Fluid Hatch para coolant externo e um FE Energy Input Hatch para energia externa

## Programas de simulacao

A beta 5 mantém as **13 simulações originais** com os mesmos tipos de recursos, mas aumenta as quantidades de saída: **280 milhões a 1,625 bilhão de itens** e **107 a 375 milhões de mB** por ciclo. Os blocos de minério foram removidos das saídas; os metais aparecem processados.

Uma nova simulação **Stellar Omnibus (MK3)** reúne os recursos dessas receitas, com duas substituições em **64 saídas de itens e 17 de fluidos**. Ela requer Advanced AE, ExtendedAE, Mega Cells, Mekanism, Mekanism Generators e Applied Flux para que todos os recursos opcionais existam. Na Omnibus, pedra e obsidiana dão lugar aos três lingotes do UFO Future, materiais avançados e fluidos de fusão. As receitas de Ethylene e Sky Steel continuam disponíveis em MK2.

No JEI, as saídas de itens percorrem primeiro os três slots de cada quadro da esquerda para a direita, depois descem para a faixa seguinte. Mundos com uma seleção do catálogo experimental são remapeados para uma receita original ao carregar.

## Tiers Dos Field Generators

As quatro posicoes de field generator precisam estar todas no mesmo tier:

- **MK1**
- **MK2**
- **MK3**

Misturar tiers invalida a estrutura.

## Escada De Coolant

- **Gelid Cryotheum** = baixa eficiencia
- **Stable Coolant** = eficiencia media
- **Temporal Fluid** = eficiencia extrema

## Safe Mode

O Safe Mode e a opcao segura para automacao.

- **2x** custo de AE
- **2x** consumo de combustivel
- **2.5x** consumo de coolant
- Desligamento automatico em vez de explosao

## Overclock

- **5x** mais velocidade
- **8x** custo de AE
- **5x** combustivel
- **5x** calor
- **5x** coolant

Esse comportamento tambem vale para receitas customizadas.

## Politica de superaquecimento

Com Safe Mode desligado, o calor maximo causa uma falha local com dano e efeitos visuais. **A destruicao de blocos vem desativada por padrao.** O administrador pode ativar uma onda destrutiva limitada em `config/ufo/server.toml` (`stellar.explosion.enableBlockGrief`), com limites de raio, blocos por tick, total de blocos e dimensoes permitidas. Lava e explosoes secundarias tem opcoes separadas.
