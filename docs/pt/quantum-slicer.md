# Quantum Slicer

O **Quantum Slicer** e o multibloco de preparo de componentes impressos. Ele cuida da etapa de corte em linhas grandes de circuitos e processadores.

## Papel da Maquina

- converte materiais fonte em partes impressas em massa
- usa o sistema universal de recipes de multibloco
- suporta ate **8 jobs paralelos**
- integra com AE2 pelo **Quantum Pattern Hatch**

Ele foi feito para throughput, nao para recipes manuais pequenas.

## Posicao na Fabrica

O fluxo normal e:

1. blocos e materiais entram no **Quantum Slicer**
2. os printed outputs vao para a rede ME
3. o AE2 solicita os processadores finais ao **Quantum Processor Assembler**

## Automacao

- os encoded patterns ficam no hatch, nao no controller
- o controller consome direto da rede ME
- as threads paralelas deixam um unico slicer atender varias familias de circuitos ao mesmo tempo

## Veja Tambem

- [Quantum Processor Assembler](quantum-processor-assembler.md)
- [Quantum Matter Fabricator](quantum-matter-fabricator.md)
