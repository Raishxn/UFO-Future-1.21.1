# Changelog

Este arquivo passa a ser a lista principal de bugs corrigidos e implementacoes do UFO Future.

Fluxo combinado daqui para frente:

- Toda correcao entra em `Bug Fixes`
- Toda feature nova entra em `Implementations`
- Tudo novo vai primeiro para `Unreleased`
- Quando fecharmos uma versao, movemos os itens de `Unreleased` para a secao da versao
- Os arquivos [UPDATE_LOG_PT-BR.md](/C:/Users/erick/OneDrive/Documents/UFO-Future-1.21.1-main/UPDATE_LOG_PT-BR.md) e [UPDATE_LOG_EN.md](/C:/Users/erick/OneDrive/Documents/UFO-Future-1.21.1-main/UPDATE_LOG_EN.md) podem continuar sendo usados como resumo publico mais elaborado

## [Unreleased]

### Bug Fixes

- Corrigida a sincronizacao de progresso dos multiblocos paralelos no controller, evitando casos em que crafts de fluidos continuavam processando no servidor enquanto a tela ficava travada em valores como `0.0/15.0s`.
- Corrigida a conectividade AE2 dos multiblocos entropicos para seguir o padrao do `ExtendedAE`, com casings AE reais, node multiblock e canal compartilhado por estrutura.
- Revertida a troca indevida das texturas antigas de `entropy_assembler_core_casing`, `entropy_assembler_core_casing_base`, `entropy_computer_condensation_matrix` e `entropy_singularity_casing`, preservando os outros multiblocos que dependiam delas.

### Implementations

- Ajustada a exibicao do JEI do `Stellar Nexus` para mostrar o coolant apenas como `MK1`, `MK2` ou `MK3`, com tooltip exibindo o nome completo do fluido.
- Ajustada a exibicao do JEI do `Stellar Nexus` para mostrar o fuel em formato abreviado dentro do box, com tooltip exibindo o nome completo do combustivel e a quantidade.
- Iniciada a base dos multiblocos `Entropic Convergence Engine` e `Entropic Assembler Matrix`, com PRD finalizado, notas tecnicas de estabilidade/performance e validador frio para cubo `7x7x7` com interior `5x5x5` preenchido por fields uniformes.
- Criados os novos blocos exclusivos `Entropic Assembler Casing` e `Entropic Convergence Casing`, que agora sao as unicas cascas validas desses dois multiblocos, com receitas, modelos e texturas proprias.
- Alinhada a interacao para abrir a GUI por qualquer bloco da estrutura formada e conectada, sem controller visivel no shell.
- Reduzido o custo de revalidacao estrutural, mantendo rescan periodico mais lento quando montado e resposta imediata via dirty flag quando a estrutura muda.

## [2.0.0]

### Notes

- Resumo detalhado disponivel em [UPDATE_LOG_PT-BR.md](/C:/Users/erick/OneDrive/Documents/UFO-Future-1.21.1-main/UPDATE_LOG_PT-BR.md) e [UPDATE_LOG_EN.md](/C:/Users/erick/OneDrive/Documents/UFO-Future-1.21.1-main/UPDATE_LOG_EN.md).

### Bug Fixes

- Consolidado nos arquivos de update log desta versao.

### Implementations

- Consolidado nos arquivos de update log desta versao.
