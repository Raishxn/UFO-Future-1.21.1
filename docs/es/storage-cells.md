# Celdas de Almacenamiento

UFO Future extiende el sistema de almacenamiento de AE2 con dos nuevas series — **Enana Blanca** (ítems) y **Estrella de Neutrones** (fluidos) — con capacidades BigInteger. También añade **Celdas Infinitas** para almacenamiento ilimitado.

---

## Celdas de Ítems Enana Blanca

| Nivel | Capacidad | Componente | ID |
|-------|----------|-----------|-----|
| **Echo** | 40M bytes | Matriz Phase Shift | `ufo:white_dwarf_cell_echo` |
| **Beacon** | 100M bytes | Matriz Hyper Dense | `ufo:white_dwarf_cell_beaco` |
| **Nexus** | 250M bytes | Matriz Tesseract | `ufo:white_dwarf_cell_nexus` |
| **Core** | 750M bytes | Matriz Event Horizon | `ufo:white_dwarf_cell_core` |
| **Singularity** | 2 147 483 647 bytes | Matriz Cosmic String | `ufo:white_dwarf_cell_singularity` |

**Carcasa**: `ufo:white_dwarf_item_cell_housing`

---

## Celdas de Fluido Estrella de Neutrones

| Nivel | Capacidad | Componente | ID |
|-------|----------|-----------|-----|
| **Echo** | 40M bytes | Matriz Phase Shift | `ufo:neutron_star_reservoir_echo` |
| **Beacon** | 100M bytes | Matriz Hyper Dense | `ufo:neutron_star_reservoir_beaco` |
| **Nexus** | 250M bytes | Matriz Tesseract | `ufo:neutron_star_reservoir_nexus` |
| **Core** | 750M bytes | Matriz Event Horizon | `ufo:neutron_star_reservoir_core` |
| **Singularity** | 2 147 483 647 bytes | Matriz Cosmic String | `ufo:neutron_star_reservoir_singularity` |

**Carcasa**: `ufo:neutron_fluid_cell_housing`

---

## Camaras quimicas Pulsar (opcionales)

La tercera serie BigInteger almacena quimicos de Mekanism en los niveles **Echo (40M), Beacon (100M), Nexus (250M), Core (750M) y Singularity (2 147 483 647 bytes)**. La carcasa es `ufo:pulsar_cell_housing`; las celdas usan `ufo:pulsar_chamber_echo`, `..._beaco`, `..._nexus`, `..._core` y `..._singularity`.

Esta serie requiere la integracion con Mekanism. Sin ella, las camaras y la carcasa quedan ocultas en Creativo y JEI/EMI, pero los ID registrados permanecen para mundos existentes.

## Celda Infinity Genesis

La **Infinity Genesis Cell** (`ufo:infinity_genesis_cell`) aprende tipos de recursos cuando se insertan. Luego ofrece almacenamiento AE2 ilimitado para esos tipos y admite configuracion de particion, inverter y fuzzy en la Cell Workbench. Las Infinity Cells de recurso fijo siguen siendo objetos separados.

## Celdas Infinitas

Almacenamiento **ilimitado** de un solo recurso. Incluye: Agua, Lava, Piedra, Arena, Obsidiana, Vidrio, y más. También 16 colores de tinte y recursos de Mekanism.

Las recetas de Infinity Cells ahora usan el **QMF** y varian segun recurso y nivel. Consulta JEI/EMI para cada receta actual.

---

*Ver también: [DMA](dma.md) · [Progresión](progression.md) · [Materiales](materials.md)*
