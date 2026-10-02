# UFO Future 3.0.0-beta.6

Esta beta torna o UFO Future compatível com o RaishxCore estável 0.2 e com as próximas versões 0.x.

## Destaques

- **Suporte ao RaishxCore 0.2:** o UFO Future agora declara a faixa de dependência `[0.2, 1.0)`, então a versão estável 0.2 e os próximos builds 0.x carregam sem o erro de dependência que rejeitava a 0.2. Nenhum código de jogo mudou; o RaishxCore 0.2 é uma correção do planner sobre o 0.1.0-beta.3.
- A revisão exata do Core usada pelos fluxos de build e release agora aponta para o commit da tag `v0.2`.

## Compatibilidade e verificação

- Minecraft 1.21.1, NeoForge 21.1.x e Applied Energistics 2 19.2.17 ou versão compatível da linha 19.x continuam obrigatórios. O RaishxCore `[0.2, 1.0)` é a faixa acompanhante.
- Os testes locais de release passaram: a suíte JUnit, incluindo o teste de contrato do metadata gerado, e o build do JAR de distribuição.
- Saves, receitas e progressão não mudam em relação ao 3.0.0-beta.5.
