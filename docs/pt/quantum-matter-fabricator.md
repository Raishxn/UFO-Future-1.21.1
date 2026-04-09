# Quantum Matter Fabricator

O **Quantum Matter Fabricator (QMF)** e a evolucao multibloco do DMA. Ele existe para processamento em massa, producao paralela e integracao real com autocrafting do AE2.

## Diferenciais

- ate **8 threads paralelas** de processamento
- aceita tanto recipes nativas do **QMF** quanto recipes do **DMA**
- puxa ingredientes direto da rede ME
- devolve outputs direto para o armazenamento ME
- suporta automacao do AE2 pelo **Quantum Pattern Hatch**

O QMF e o lugar certo para receitas grandes, caras ou repetitivas demais para continuar no DMA.

## Quantum Pattern Hatch

O **Quantum Pattern Hatch** e o pattern provider dedicado dessa familia de multiblocos.

- armazena ate **64 encoded patterns**
- se vincula ao controller quando a estrutura monta
- expoe o controller ao AE2 como maquina de crafting
- deixa o AE2 despachar trabalho direto para as threads do multibloco

## Modelo de Paralelismo

Cada controller possui **8 threads**.

- as threads podem rodar a mesma recipe ou receitas diferentes
- um pattern enviado pelo AE2 ocupa uma thread livre
- threads ociosas ainda podem auto-iniciar jobs validos da ME
- energia, fluidos e itens sao puxados direto da rede

## Uso Ideal

Use o QMF para:

- recipes gigantes compativeis com DMA
- conversoes grandes de materia
- producao em rede de longa duracao
- recipes com contagens muito altas de ingrediente

## Veja Tambem

- [Quantum Slicer](quantum-slicer.md)
- [Quantum Processor Assembler](quantum-processor-assembler.md)
- [Tiers de Multibloco](multiblock-tiers.md)
