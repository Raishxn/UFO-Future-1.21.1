# Quantum Slicer

O **Quantum Slicer** prepara componentes impressos em grande volume para linhas de processadores e circuitos.

## Comportamento Base

- Converte materiais de origem em partes impressas
- Usa o sistema universal de receitas de multibloco
- Suporta ate **27 jobs paralelos** no modo padrao
- Cai para **9 jobs paralelos** em Safe Mode
- Integra com AE2 via **Quantum Pattern Buffer** ou **Quantum Pattern Proxy** vinculado
- Puxa e devolve ingredientes e outputs direto pela ME

## Pattern Buffer e Proxy

- A estrutura exige exatamente um Buffer ou Proxy.
- O Buffer armazena **72 patterns codificados**; um Proxy vinculado compartilha outro Buffer.
- A AE2 pode enviar jobs sem depender de inventario manual.

## Posicao Na Fabrica

1. Materiais entram no **Quantum Slicer**
2. Os impressos vao para a ME
3. A AE2 pede os processadores finais ao **Quantum Processor Assembler**
