# Quantum Processor Assembler

O **Quantum Processor Assembler** e o multibloco de acabamento da linha de processadores. Ele recebe circuitos impressos e materiais de suporte para montar processadores finais em escala endgame.

## Recursos Principais

- Ate **27 jobs paralelos** no modo padrao
- **9 jobs paralelos** em Safe Mode
- Suporte a receitas universais de multibloco
- Integracao de autocrafting AE2 pelo **Quantum Pattern Buffer** ou **Quantum Pattern Proxy** vinculado
- Pull de ingredientes e push de outputs direto pela rede

## Pattern Buffer e Proxy

- A estrutura exige exatamente um Buffer ou Proxy.
- O Buffer armazena **72 patterns codificados**; um Proxy vinculado compartilha outro Buffer.
- Cada pattern usa uma thread livre em vez de travar a maquina inteira.

## Cadeia Da Fabrica

- **Quantum Slicer** cria as partes impressas
- **Quantum Processor Assembler** fecha os processadores
- **QMF** cobre conversoes pesadas no estilo DMA
