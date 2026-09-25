# Stellar Nexus

O **Stellar Nexus** e o multibloco final de simulacao do UFO Future. Ele consome AE, fluidos raros e materiais de alto tier para gerar outputs em escala extrema.

## Identidade Da Maquina

- Multibloco massivo
- Le itens e fluidos direto da ME
- Carrega um buffer interno de **200B AE**
- Consome combustivel ao iniciar e coolant durante a operacao
- Usa calor, safe mode e overclock como eixos principais
- Exige os hatches de entrada e saida de itens, um ME Massive Fluid Hatch para coolant externo e um FE Energy Input Hatch para energia externa

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
