# Quantum Slicer

O **Quantum Slicer** prepara componentes impressos em grande volume para linhas de processadores e circuitos.

## Comportamento Base

- Converte materiais de origem em partes impressas
- Usa o sistema universal de receitas de multibloco
- Suporta ate **27 jobs paralelos** no modo padrao
- Cai para **9 jobs paralelos** em Safe Mode
- Integra com AE2 via **Quantum Pattern Hatch**
- Puxa e devolve ingredientes e outputs direto pela ME

## Pattern Hatch

- O **Quantum Pattern Hatch** armazena **72 encoded patterns**
- Ele se vincula ao controller apos a montagem
- A AE2 pode empurrar jobs sem depender de inventario manual

## Posicao Na Fabrica

1. Materiais entram no **Quantum Slicer**
2. Os impressos vao para a ME
3. A AE2 pede os processadores finais ao **Quantum Processor Assembler**
