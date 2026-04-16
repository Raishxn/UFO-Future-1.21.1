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

### Implementations

- Ajustada a exibicao do JEI do `Stellar Nexus` para mostrar o coolant apenas como `MK1`, `MK2` ou `MK3`, com tooltip exibindo o nome completo do fluido.
- Ajustada a exibicao do JEI do `Stellar Nexus` para mostrar o fuel em formato abreviado dentro do box, com tooltip exibindo o nome completo do combustivel e a quantidade.
- Iniciada a base dos multiblocos `Entropic Convergence Engine` e `Entropic Assembler Matrix`, com PRD finalizado, notas tecnicas de estabilidade/performance e validador frio para cubo `7x7x7` com interior `5x5x5` preenchido por fields uniformes.

## [2.0.0]

### Notes

- Resumo detalhado disponivel em [UPDATE_LOG_PT-BR.md](/C:/Users/erick/OneDrive/Documents/UFO-Future-1.21.1-main/UPDATE_LOG_PT-BR.md) e [UPDATE_LOG_EN.md](/C:/Users/erick/OneDrive/Documents/UFO-Future-1.21.1-main/UPDATE_LOG_EN.md).

### Bug Fixes

- Consolidado nos arquivos de update log desta versao.

### Implementations

- Consolidado nos arquivos de update log desta versao.
