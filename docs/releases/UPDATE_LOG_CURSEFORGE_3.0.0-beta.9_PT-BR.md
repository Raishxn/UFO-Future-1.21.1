# UFO Future 3.0.0-beta.9

## Correções

- Corrigido o Quantum Computation Nexus ser mais lento que CPUs de crafting AE2 equivalentes. As lanes de co-processadores instaladas agora são compartilhadas de forma justa entre todos os jobs ativos, em vez de limitadas a uma janela fixa de 2.048 operações para o pool inteiro. Com quatro jobs e 16.384 lanes, o Nexus iguala quatro CPUs AE2 equivalentes em vez de rodar a cerca de um oitavo do throughput delas.
- O throttle de energia por buffer baixo virou opcional. Por padrão o Nexus se comporta como o AE2 e os addons de CPU, que não reduzem o despacho quando o buffer está baixo. O administrador pode reativá-lo com `nexus.energyThrottle`.
- O crafting do Nexus não consome mais a energia armazenada da rede por padrão: os jobs só precisam que o Grid Link esteja energizado (32 AE/t). O custo nativo por padrão do AE2 pode ser restaurado com `nexus.ignorePatternEnergy=false`.

## Adicionado

- Ajuste de servidor `nexus.maxPatternDispatchesPerTick` (padrão 16.384) limita o orçamento de despachos do Nexus por tick e protege a TPS do servidor em pools muito grandes. Jobs carregados do disco rampeiam em quatro ticks, então um craft retomado não drena o buffer inteiro de uma vez.
- Novo teste de carga/TPS do agendador no pipeline de release.

## Validação

- GameTests de regressão comparam o Nexus com quatro CPUs equivalentes independentes: paridade exata (32.776 despachos em oito rodadas) com poder cheio e com buffer simulado em 1%.
- O novo teste de carga roda 16 jobs em 1.638.400 lanes pelo caminho real do RaishxCore, despachando o orçamento completo a cada tick (~16.465 despachos por tick, 16,5 milhões em 1.000 ticks). O tick médio ficou em ~23 ms contra o orçamento de 50 ms na máquina de desenvolvimento.
- Testes unitários, 57 GameTests, o teste de carga ativa e o pipeline de release (GameTests com e sem Mekanism, datagen, soak ocioso e carga TPS do Nexus) passam.

## Requisitos

- Minecraft **1.21.1**, NeoForge 21.1.x, Applied Energistics 2 **19.2.17 ou 19.x compatível** e RaishxCore **0.2 ou 0.x compatível** (`[0.2, 1.0)`).
- Esta continua sendo uma versão **beta**. Sem mudanças de receitas ou progressão em relação à beta.8.
- A velocidade efetiva depende dos módulos instalados, dos providers de padrões, dos ingredientes disponíveis e do espaço para resultados.
