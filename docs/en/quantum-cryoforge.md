# Quantum Cryoforge

The **Quantum Cryoforge** is the cryogenic stage of the universal multiblock recipe line. It produces materials such as Stable Coolant for later machines.

## Operation

- Runs up to **27 parallel jobs**, or **9** in Safe Mode.
- Uses MK1–MK3 recipe requirements and the shared [tier bonus](multiblock-tiers.md).
- Accepts custom `ufo:universal_multiblock` recipes with `machine: 'quantum_cryoforge'`; see [KubeJS recipes](kubejs-recipes.md).
- Requires one Quantum Pattern Buffer or a linked Proxy for AE2 pattern automation.
- Pulls recipe inputs from ME and returns outputs there. Supply coolant externally through the **ME Massive Fluid Hatch** and FE through the **FE Energy Input Hatch**.

Use the in-game structure preview or Structure Scanner to find the required hatch positions.
