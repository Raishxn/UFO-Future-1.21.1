---
navigation:
  parent: ufo_intro/infrastructure.md
  title: Quantum Computation Nexus
  position: 55
item_ids:
  - ufo:quantum_computation_nexus_controller
  - ufo:quantum_grid_link
---

# Quantum Computation Nexus

<BlockImage id="ufo:quantum_computation_nexus_controller" scale="4"></BlockImage>

The **Quantum Computation Nexus** combines all UFO Crafting Storage and
Co-Processor blocks in its cavity into one shared AE2 computation pool.

<GameScene zoom="1.45" background="transparent" interactive={true}>
  <ImportStructure src="../assets/assemblies/quantum_computation_nexus.snbt" />
  <DiamondAnnotation pos="8.5 2.5 7.5" color="#80c6ff">Quantum Computation Nexus Controller</DiamondAnnotation>
  <DiamondAnnotation pos="8.5 2.5 11.5" color="#58e6ff">Quantum Grid Link</DiamondAnnotation>
  <IsometricCamera yaw="35" pitch="25" />
</GameScene>

## Construction

Build the shell shown above and install the desired crafting modules in the
internal module space. At least one UFO Crafting Storage is required for the CPU
to become available to the ME crafting service. Co-Processors add crafting lanes.

The valid module space is the completely enclosed cavity behind the glass. Blocks
outside the shell are never counted. Recognised modules remain physically present,
visible and keep their contents while the Nexus owns their computation capacity.

Connect the **Quantum Grid Link** to a powered ME network through its outward
face. The Link consumes one channel and publishes the pool to that grid; internal
modules never become individual CPUs and the controller does not accept ME cables.

## Requirements

- One **Quantum Computation Nexus Controller**
- One **Quantum Grid Link** in the marked casing position
- At least one UFO **Crafting Storage** module
- Any number of supported UFO **Co-Processor** and additional storage modules
- One free ME channel and at least **32 AE/t** for the Grid Link

Only the outward face of the Grid Link accepts cable. Its overlay points toward
the cable side. If the Link says `unlinked`, check the shell; if it says `offline`,
the structure is valid but the connected ME network has no usable power/channel.

## How It Works

When idle, the Nexus appears in the Crafting Terminal as **one CPU** containing
all currently free capacity. Starting a job reserves only that plan's required
bytes and creates one temporary virtual CPU. All remaining bytes stay available
for simultaneous jobs. Co-Processors are one shared dispatch budget, divided
fairly between active jobs rather than duplicated for every job.

### Infinite computation mode

Installing at least **25 Cosmic String Crafting Storages** (the 1 TiB tier) and
**25 Cosmic String Co-Processors** (65,536 lanes each) activates the Nexus'
infinite computation mode. Both
Crafting Storage and Co-Processors are then advertised as **infinite** to AE2.
The controller displays `∞`, while its tooltips continue to report the physical
capacity installed in the cavity.

Both requirements must be met using the highest-tier modules. Lower-tier modules
still add their normal finite capacity but do not count toward this unlock.

After a job finishes and its held items have returned to the network, its virtual
CPU disappears and its reservation returns to the idle pool. The controller
reports physical totals and active jobs, never dozens of physical CPU entries.

Breaking the shell, controller or Grid Link safely detaches the CPU. Stored module
contents are not converted into a separate inventory: the original blocks remain
in place.

## Controller

The controller screen reports structure, grid and CPU status separately. It also
shows exact total storage, parallel lanes and the number of recognised modules.
In infinite mode the main metrics become `∞`; hover them to inspect their physical totals.

- **Open Guide** returns to this page.
- **Scan Multiblock Structure** reports and highlights missing blocks.
- **Auto-build Structure** places missing shell blocks when the player has the
  required materials. It never replaces occupied positions.
- **CPU Priority** opens AE2's native priority editor. Higher-priority Nexus
  pools are preferred when AE2 automatically chooses a crafting CPU.

Virtual job state and its exact reservation are persisted so active crafting can
be reconstructed after the world is reloaded.

## Troubleshooting

- **Structure offline:** use **Scan Multiblock Structure** and repair the highlighted position.
- **Grid offline:** connect a powered AE2 cable to the Grid Link overlay and provide one channel.
- **CPU offline:** install at least one supported Crafting Storage module in the enclosed cavity.
- **Module not counted:** confirm it is inside the glass-enclosed cavity, not merely inside the preview bounds.
