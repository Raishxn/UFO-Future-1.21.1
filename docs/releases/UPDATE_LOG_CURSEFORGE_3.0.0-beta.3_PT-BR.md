# UFO Future 3.0.0-beta.3

Esta beta melhora as telas do Stellar Nexus e inclui correções no resgate do Reality Anchor, no alcance wireless e na migração das configurações.

## Destaques

- Refeitas as interfaces do controller e das receitas Stellar Nexus no JEI/EMI. A tela comporta até 81 saídas de itens e 18 de fluidos.
- O nome da simulação aparece no cabeçalho do EMI. O tier do campo, a duração e o custo em AE ficam na borda superior da receita.
- A moldura interna do EMI agora acompanha a textura inteira, e o ícone do controller fica ao lado dela.
- Corrigidos o tanque de coolant e o alinhamento dos textos no controller.
- Corrigido o resgate do Reality Anchor após cair no vazio.
- O Wireless Tool segue os alcances de `config/ufo/wireless.toml`, inclusive em vínculos antigos que ainda usavam o padrão de 32 blocos. Alcances escolhidos manualmente são preservados.
- As configurações do UFO foram reunidas em `config/ufo/`, com migração automática dos valores existentes.
- Wiki atualizada em quatro idiomas, incluindo a restauração das páginas em chinês simplificado.

## Compatibilidade e verificação

- O JSON de receitas KubeJS do Stellar Nexus continua igual. Um GameTest de desenvolvimento carregou uma receita por script com 81 saídas de itens e 18 de fluidos.
- O build de release passou nos 41 GameTests obrigatórios do UFO com e sem Mekanism, nos 14 GameTests do Core e nos dois testes curtos de soak.
- Compilado com RaishxCore 0.1.0-beta.2; não é necessária uma nova versão do Core.
- Minecraft 1.21.1, NeoForge 21.1.x e Applied Energistics 2 19.2.17 ou compatível na linha 19.x continuam obrigatórios. Mekanism continua opcional.
