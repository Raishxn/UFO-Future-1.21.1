# Quantum Wireless

**Quantum Wireless** es la primera etapa jugable de transferencia inalambrica para maquinas UFO conectadas a AE2. Usa la Quantum Interface, la Quantum Wireless Tool y los enlaces de Pattern Hatch y Pattern Buffer/Proxy. No transmite FE a las maquinas.

## Quantum Interface

La interfaz tiene **36 espacios de configuracion en dos paginas de 18**. Repone recursos desde ME, y los espacios configurados pueden abastecer continuamente una cara vinculada. Elige operacion local o wireless, importacion/exportacion y velocidad de I/O en la interfaz. La red necesita sus propios recursos y energia.

## Vincular un destino

1. Conecta y alimenta la Quantum Interface en una red ME.
2. Activa wireless en el origen. Usa la **Quantum Wireless Tool** en el origen para seleccionarlo; agachate y usa la herramienta en el origen para alternar el modo.
3. Usa la herramienta en una cara de destino, como la entrada del DMA o el hatch de coolant del multibloque, para agregar o quitar el enlace.
4. Configura los espacios y la exportacion. El coolant debe existir en ME y sigue entrando a la maquina por el hatch fisico.

Los enlaces tienen alcance configurable y no fuerzan la carga de chunks. La herramienta usa origen y destino en la misma dimension; los destinos descargados esperan hasta estar disponibles.

## Patrones y limites

Una **Quantum Pattern Hatch** puede vincularse a un DMA. Un **Quantum Pattern Buffer** puede vincularse a **Quantum Pattern Proxies** de la linea universal de multibloques. Este enlace distribuye patrones y es independiente de la conexion fisica de la estructura.

Esta es una implementacion inicial. EJECT remoto, filtros de importacion, transferencia FE y edicion completa de destinos no forman parte del flujo actual. La configuracion del servidor esta en `config/ufo/wireless.toml`.
