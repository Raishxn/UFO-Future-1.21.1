# Linha atual de multiblocos

UFO Future tem oito maquinas multibloco. As cinco primeiras processam recursos e usam o sistema de campo MK1–MK3; as tres ultimas oferecem infraestrutura de autocrafting AE2 em grande escala.

| Maquina | Funcao principal |
|---|---|
| [Quantum Matter Fabricator](quantum-matter-fabricator.md) | Receitas DMA e QMF em lote, com 27 threads paralelas (9 no Safe Mode) |
| [Quantum Slicer](quantum-slicer.md) | Producao em massa de componentes impressos |
| [Quantum Processor Assembler](quantum-processor-assembler.md) | Montagem final de processadores |
| [Quantum Cryoforge](quantum-cryoforge.md) | Processamento criogenico, incluindo a linha de Stable Coolant |
| [Stellar Nexus](stellar-nexus.md) | Simulacoes estelares finais com buffer de 200 bilhoes de AE |
| [Quantum Computation Nexus](quantum-computation-nexus.md) | Reune modulos de armazenamento de crafting e co-processadores UFO em CPUs virtuais AE2 |
| [Quantum Pattern Fabrication Matrix](quantum-pattern-fabrication-matrix.md) | Biblioteca pesquisavel de patterns e montador virtual para crafting, smithing e stonecutting |
| [Infinity Fabrication Singularity](infinity-fabrication-singularity.md) | Maquina final de crafting com ate 128 rotas persistentes de patterns |

## Patterns e acesso a rede

Cada multibloco universal de processamento exige exatamente um **Quantum Pattern Buffer** ou **Quantum Pattern Proxy**. O Buffer armazena **72 patterns codificados** e atende ao proprio controller. Um Proxy vinculado permite que outro controller use o mesmo Buffer. O antigo **Quantum Pattern Hatch** pertence ao fluxo do DMA de bloco unico.

As maquinas de processamento puxam os ingredientes das receitas da rede ME e devolvem os resultados pelos hatches ME. Forneca coolant externamente ao **ME Massive Fluid Hatch** e FE ao **FE Energy Input Hatch**. A energia da rede ME nao alimenta esses controllers automaticamente. Consulte [tiers de multibloco](multiblock-tiers.md) para acesso as receitas e bonus de eficiencia. O [guia KubeJS](kubejs-recipes.md) documenta receitas personalizadas.

Para requisitos de instalacao e a lista completa de recursos, consulte o [README do projeto](https://github.com/Raishxn/UFO-Future-1.21.1#readme).
