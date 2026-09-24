# Quantum Wireless

**Quantum Wireless** is the first playable wireless transfer layer for AE2-connected UFO machines. It currently centers on the Quantum Interface, Quantum Wireless Tool, Pattern Hatch and Pattern Buffer/Proxy links. It does not transfer FE power to machines.

## Quantum Interface

The interface has **36 configuration slots across two pages of 18**. It can stock resources from the ME network, and configured slots can continuously supply a linked destination face. Select local or wireless operation, import/export behavior and I/O speed in its interface. The network needs its own resources and power.

## Linking a destination

1. Connect and power the Quantum Interface on an ME network.
2. Enable wireless mode at the source. Use the **Quantum Wireless Tool** on that source to select it; sneak-use the source to toggle the mode.
3. Use the tool on a destination face, such as the DMA input or a multiblock coolant hatch, to add or remove that link.
4. Configure the interface slots and export behavior. Coolant must be present in the ME network and still enters the machine through its physical hatch.

Links use a configurable range and do not force-load chunks. The tool works with the source and destination in the same dimension; unloaded destinations wait until they are available.

## Pattern links and limits

A **Quantum Pattern Hatch** can link to a DMA. A **Quantum Pattern Buffer** can link to **Quantum Pattern Proxies** in the universal multiblock line. This link distributes patterns; it is separate from the physical structure connection.

This is an initial implementation. Remote eject, import filters, FE transfer and full destination editing are not part of this workflow. For current server-side settings, check `config/ufo/wireless.toml`.
