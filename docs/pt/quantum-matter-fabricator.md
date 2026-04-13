# Quantum Matter Fabricator

O **Quantum Matter Fabricator (QMF)** e a evolucao multibloco do DMA. Ele existe para processamento em massa, paralelismo e autocrafting com AE2.

## Comportamento Base

- Ate **27 threads paralelas** no modo padrao
- **9 threads paralelas** no Safe Mode
- Aceita receitas nativas de **QMF** e receitas de **DMA**
- Puxa ingredientes direto da ME
- Envia outputs de volta para o armazenamento ME
- Usa o **Quantum Pattern Hatch** para automacao

## Quantum Pattern Hatch

- Armazena ate **72 encoded patterns**
- Vincula ao controller quando a estrutura monta
- Expoe o multibloco para a AE2 como maquina de crafting
- Deixa a AE2 despachar jobs direto para as threads livres

## Modelo Paralelo

Cada thread livre pode executar uma copia de receita.

- Um pattern empurrado pela AE2 reserva uma thread
- Threads ociosas ainda podem iniciar jobs validos da rede
- Itens, fluidos e AE vem direto da ME
