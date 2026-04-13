# Quantum Processor Assembler

O **Quantum Processor Assembler** e o multibloco de acabamento da linha de processadores. Ele recebe circuitos impressos e materiais de suporte para montar processadores finais em escala endgame.

## Recursos Principais

- Ate **27 jobs paralelos** no modo padrao
- **9 jobs paralelos** em Safe Mode
- Suporte a receitas universais de multibloco
- Integracao de autocrafting AE2 pelo **Quantum Pattern Hatch**
- Pull de ingredientes e push de outputs direto pela rede

## Pattern Hatch

- O **Quantum Pattern Hatch** armazena **72 encoded patterns**
- Expoe o controller para a AE2 como maquina de crafting
- Cada pattern usa uma thread livre em vez de travar a maquina inteira

## Cadeia Da Fabrica

- **Quantum Slicer** cria as partes impressas
- **Quantum Processor Assembler** fecha os processadores
- **QMF** cobre conversoes pesadas no estilo DMA
