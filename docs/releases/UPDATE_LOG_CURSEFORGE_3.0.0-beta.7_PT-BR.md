# UFO Future 3.0.0-beta.7

## Correções

- Corrigido o desaparecimento de recursos no terminal ME quando células Infinity ou Infinity Genesis forneciam o mesmo recurso que armazenamentos externos, como gavetas ou uma interface de transmutação. As células agora anunciam a mesma quantidade das células criativas do AE2 (2.147.483.647 por recurso), evitando overflow ao somar as fontes. A extração continua infinita.
- As receitas do DMA agora aparecem no guia GuideME dentro do jogo, incluindo quantidades de itens, requisitos e produtos de fluidos, energia e tempo de processamento.
- Corrigida a tradução do nome de células Infinity sem um recurso válido.
- Adicionada a descrição de construção automática do Structure Scanner em modo criativo.
- Corrigida a validação da navegação do guia para verificar vínculos de itens duplicados separadamente por idioma.

## Adições

- Adicionadas 21 páginas do guia em chinês simplificado e vínculos de itens nas páginas traduzidas. Obrigado a **yongaishide** pela contribuição no PR #24!
- Adicionados testes de regressão para células Infinity junto a fontes externas, fontes infinitas duplicadas, prioridades, retirada/recolocação de células e extração infinita.

## Manutenção

- Atualizado o Gradle Actions de 6.3.0 para 6.4.0 (PR #23).

## Requisitos

- Minecraft **1.21.1**, NeoForge **21.1.x**, Applied Energistics 2 **19.2.17 ou versão compatível da linha 19.x** e RaishxCore **0.2 ou versão compatível da linha 0.x** (`[0.2, 1.0)`).
- Esta continua sendo uma versão **beta**. Não há mudanças de receitas ou progressão em relação à beta.6.
