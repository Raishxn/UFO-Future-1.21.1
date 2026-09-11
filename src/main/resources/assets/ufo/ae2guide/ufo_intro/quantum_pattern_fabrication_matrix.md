---
navigation:
  parent: ufo_intro/infrastructure.md
  title: Quantum Pattern Fabrication Matrix
  position: 56
item_ids:
  - ufo:quantum_pattern_fabrication_matrix_controller
---

# Quantum Pattern Fabrication Matrix

<BlockImage id="ufo:quantum_pattern_fabrication_matrix_controller" scale="4"></BlockImage>

The **Quantum Pattern Fabrication Matrix** is a field-scaled repository and virtual
assembler for AE2 crafting patterns. It accepts **Crafting Patterns**, **Smithing
Table Patterns**, and **Stonecutting Patterns**. Processing Patterns belong in
Pattern Buffers and processing machines and are rejected by the Matrix. Its
controller owns the pattern inventory and the Quantum Grid Link connects that
inventory to an ME network.

<GameScene zoom="1.8" background="transparent" interactive={true}>
  <ImportStructure src="../assets/assemblies/quantum_pattern_fabrication_matrix.snbt" />
  <DiamondAnnotation pos="1.5 0.5 6.5" color="#b268ff">Controller — front of structure</DiamondAnnotation>
  <DiamondAnnotation pos="5.5 0.5 6.5" color="#58e6ff">Quantum Grid Link — cable side</DiamondAnnotation>
  <IsometricCamera yaw="35" pitch="25" />
</GameScene>

## Exact structure

The footprint is **7 blocks wide, 5 blocks high and 9 blocks deep**. It requires:

- 1 Quantum Pattern Fabrication Matrix Controller
- 1 Quantum Grid Link
- 127 Quantum Hyper-Mechanical Casings
- 44 Quartz Vibrant Glass
- 22 Stellar Field Generators, in any mixture of MK1, MK2 and MK3
- 16 Quartz Blocks
- 15 Fluix Blocks

Place the controller with its illuminated face toward the player. The structure is
built behind that face; the Grid Link is on the opposite side. Sneak-use the
controller to preview the hologram, or use **Auto-build** from its dashboard.

## Formed versus online

These are separate states:

- **Structure complete** means every required structural position matches.
- **ME grid online** additionally requires an AE2 cable on the outward face of the
  Quantum Grid Link, network power and one available channel.

A complete structure remains formed while its ME cable is disconnected. Its
dashboard reports **STRUCTURE COMPLETE • ME GRID OFFLINE** until the link becomes
active. Use **Scan Structure** to highlight an actual structural mismatch.

## Field-scaled capacity

Every installed Field Generator contributes independently:

| Field Generator | Slots per block | 22 equal Fields |
| --- | ---: | ---: |
| MK1 | 256 | 5,632 |
| MK2 | 512 | 11,264 |
| MK3 | 1,024 | 22,528 |

Mixed tiers are supported. For example, ten MK1, ten MK2 and two MK3 Fields give
`10 × 256 + 10 × 512 + 2 × 1024 = 9,728` pattern slots.

Downgrading Fields never deletes patterns. Existing entries above the new capacity
remain available for removal, while new insertions wait until enough capacity is
restored.

## Pattern management

The pattern-grid button directly below the priority button opens the Pattern
Management screen. It provides:

- insertion and removal of Crafting, Smithing Table, and Stonecutting Patterns;
- shift-click transfer between the library and player inventory;
- server-side pages of 54 slots with mouse-wheel or scrollbar navigation;
- search by the display names of pattern inputs and outputs on the loaded page;
- output-item rendering while the encoded pattern stays stored intact.

When the Quantum Grid Link is online, the Matrix is also listed as one compact
provider in AE2's **Pattern Access Terminal**. Empty capacity is not transmitted as
tens of thousands of placeholder slots: the terminal receives the stored patterns
plus one writable position, preventing large-capacity Matrices from stalling the
server.

The button can be opened even when the structure or ME network is offline, which
keeps recovery of stored patterns possible.

## Automatic upload

After successfully encoding a Crafting, Smithing Table, or Stonecutting Pattern in
an AE2 Pattern Encoding Terminal—or a compatible Tianshu terminal—connected to the
same grid, the encoded pattern is moved into an online Matrix automatically.
Processing Patterns remain in the terminal output slot and are not uploaded.

1. Online Matrices are ordered by **Pattern Priority**, highest first.
2. Equal priorities use a stable position order.
3. A full Matrix is skipped and the next eligible Matrix is tried.
4. If none can accept the pattern, it remains in the terminal output slot.

The priority button uses AE2's native priority editor. Priority affects automatic
upload selection; it does not change the number of slots.

## Dashboard controls

- **Guide** opens this page.
- **Scan Structure** validates the current orientation and highlights mistakes.
- **Auto-build** places missing structural blocks without replacing occupied blocks.
- **Pattern Management** opens the searchable repository.
- **Pattern Priority** selects which Matrix receives encoded patterns first.

The dashboard's Field panel lists the live MK1/MK2/MK3 counts and the library bar
shows occupied slots against the capacity calculated from those Fields.
