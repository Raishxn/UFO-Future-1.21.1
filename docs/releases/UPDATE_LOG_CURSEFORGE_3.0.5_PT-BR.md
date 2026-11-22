# UFO Future 3.0.5

Corrige o lag pesado no grid do AE2 causado pelas casings do Stellar Nexus.

## Correções

- **Lag de grid do Stellar Nexus.** Entropy Singularity Casings e Entropy Assembler Core Casings não criam mais nós no grid do AE2. Num servidor com 15 Stellar Nexus, o `Grid.onLevelEndTick` do AE2 caiu de ~160 ms para ~5 ms por tick.
- **Mudança de comportamento:** as casings não conduzem mais a rede ME. Conecte a rede ME pelas hatches do Nexus.
- As casings agora restauram corretamente o vínculo com o controlador ao carregar o chunk. Mundos existentes continuam funcionando sem mudanças.

## Requisitos

Minecraft **1.21.1**, NeoForge **21.1.216+**, AE2 **19.2.17 ou 19.2.18** (`[19.2.17, 20)`), AE2 Addon Lib **1.0.3-1.21.1+** e RaishxCore **0.2.3** (`[0.2.3, 1.0)`). Substitua o JAR antigo do UFO.

Obrigado ao Immortalis202 pela correção (#35).
