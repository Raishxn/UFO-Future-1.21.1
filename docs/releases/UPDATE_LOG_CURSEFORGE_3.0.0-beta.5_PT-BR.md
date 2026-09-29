# UFO Future 3.0.0-beta.5

Esta beta melhora a montagem automática dos multiblocos e revisa as simulações do Stellar Nexus a partir do retorno dos jogadores.

## Destaques

- **Compatibilidade com o Advanced AE:** a ponte de CPUs compartilhadas do RaishxCore não substitui mais retornos que outros add-ons de CPU estendem. Com o Advanced AE instalado, o Quantum Computer volta a aparecer na lista de CPUs e a receber itens, com o planner ligado ou desligado. Esta versão exige o RaishxCore 0.1.0-beta.3.
- A montagem automática reconhece peças já válidas, instala os hatches de serviço obrigatórios quando há uma posição de casing compatível e avisa quando a estrutura continua incompleta. As prévias e as informações do JEI também foram ajustadas.
- Corrigidos os atributos genéticos das abelhas de sucata, caixa de sucata e bola de matéria. Gerar uma delas podia derrubar o mundo quando o Productive Bees consultava a tolerância ao clima.
- As 13 simulações originais do Stellar Nexus voltam com saídas especializadas. Cada uma produz 10 vezes mais itens e 50 vezes mais fluidos que na beta.4; blocos de minério bruto foram retirados das saídas.
- A nova simulação MK3 **Stellar Omnibus** reúne 64 saídas de itens e 17 de fluidos. Ela troca pedregulho e obsidiana por lingotes do UFO Future e materiais avançados, além de incluir Transcending Matter, Bose–Einstein Condensate, deutério, trítio e combustível D-T.
- No JEI/EMI, as saídas de itens percorrem três slots em cada quadro, da esquerda para a direita, antes de descer para a próxima linha. Mundos com uma receita selecionada do catálogo consolidado experimental são remapeados para uma simulação original ao carregar.

## Compatibilidade e verificação

- As simulações originais mantêm as condições dos mods opcionais. A Stellar Omnibus exige Advanced AE, ExtendedAE, Mega Cells, Mekanism, Mekanism Generators e Applied Flux, pois reúne recursos de todos eles.
- Minecraft 1.21.1, NeoForge 21.1.x e Applied Energistics 2 19.2.17 ou versão compatível da linha 19.x continuam obrigatórios. RaishxCore 0.1.0-beta.3 é a versão acompanhante; a ponte de CPUs compartilhadas agora compõe com o Quantum Computer do Advanced AE.
- Os testes locais de release passaram: JAR de distribuição, 49 GameTests com o conjunto completo e 49 sem Mekanism (incluindo a regressão do ovo de abelha no Productive Bees 13.13.5 e a regressão da ponte de CPUs compartilhadas com o Quantum Computer), dois testes curtos de estabilidade ociosa e um teste curto de carga ativa. O fluxo de release repete as verificações de distribuição e compatibilidade.

Esta é uma beta para avaliar balanceamento e compatibilidade de mundos. Relate problemas de disponibilidade de receitas, saída de recursos ou montagem automática com o log completo e passos para reproduzir.
