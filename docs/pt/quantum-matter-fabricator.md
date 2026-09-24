# Quantum Matter Fabricator

O **Quantum Matter Fabricator (QMF)** e a evolucao multibloco do DMA. Ele existe para processamento em massa, paralelismo e autocrafting com AE2.

## Comportamento Base

- Ate **27 threads paralelas** no modo padrao
- **9 threads paralelas** no Safe Mode
- Aceita receitas nativas de **QMF** e receitas de **DMA**
- Puxa ingredientes direto da ME
- Envia outputs de volta para o armazenamento ME
- Usa o **Quantum Pattern Buffer** ou um **Quantum Pattern Proxy** vinculado para automacao

## Pattern Buffer e Proxy

- A estrutura exige exatamente um Buffer ou Proxy.
- O Buffer armazena ate **72 patterns codificados** e atende ao proprio controller.
- Um Proxy vinculado compartilha outro Buffer com este controller.
- A AE2 pode despachar jobs direto para as threads livres.
- O antigo **Quantum Pattern Hatch** pertence ao DMA de bloco unico.

## Modelo Paralelo

Cada thread livre pode executar uma copia de receita.

- Um pattern empurrado pela AE2 reserva uma thread
- Threads ociosas ainda podem iniciar jobs validos da rede
- Itens e fluidos da receita vem da ME. Forneca coolant por fora ao ME Massive Fluid Hatch e FE ao FE Energy Input Hatch.
