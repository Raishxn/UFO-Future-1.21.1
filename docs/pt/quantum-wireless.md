# Quantum Wireless

**Quantum Wireless** e a primeira etapa jogavel de transferencia sem fio para maquinas UFO conectadas a AE2. Ela usa a Quantum Interface, a Quantum Wireless Tool e os vinculos da Pattern Hatch e do Pattern Buffer/Proxy. Nao transmite FE para as maquinas.

## Quantum Interface

A interface tem **36 slots de configuracao em duas paginas de 18**. Ela repoe recursos da rede ME, e slots configurados podem abastecer continuamente uma face vinculada. Escolha operacao local ou wireless, importacao/exportacao e velocidade de I/O na interface. A rede precisa ter recursos e energia proprios.

## Vinculando um destino

1. Conecte e alimente a Quantum Interface numa rede ME.
2. Ative wireless na origem. Use a **Quantum Wireless Tool** na origem para seleciona-la; agache e use a ferramenta na origem para alternar o modo.
3. Use a ferramenta numa face de destino, como a entrada do DMA ou a hatch de coolant do multibloco, para adicionar ou remover o vinculo.
4. Configure os slots e a exportacao. O coolant precisa existir na rede ME e continua entrando na maquina pela hatch fisica.

Os vinculos usam alcance configuravel e nao carregam chunks a forca. A ferramenta usa origem e destino na mesma dimensao; destinos descarregados aguardam ate ficarem disponiveis.

## Patterns e limites

Uma **Quantum Pattern Hatch** pode se vincular a um DMA. Um **Quantum Pattern Buffer** pode se vincular a **Quantum Pattern Proxies** da linha universal de multiblocos. Esse vinculo distribui patterns e e separado da conexao fisica da estrutura.

Esta e uma implementacao inicial. EJECT remoto, filtros de importacao, transferencia FE e edicao completa dos destinos nao fazem parte do fluxo atual. As configuracoes do servidor ficam em `config/ufo/wireless.toml`.
