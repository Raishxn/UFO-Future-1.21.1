# UFO Future 3.0.0

The first official 3.0 release brings together the beta fixes and improvements to AE2 infrastructure, multiblocks and endgame progression.

## 3.0 highlights

- Quantum Computation Nexus with lanes shared between jobs, per-tick dispatch control and gradual recovery after reload.
- Quantum Pattern Fabrication Matrix with multiple batch deliveries per tier.
- Infinity and Infinity Genesis cells with correct resource visibility alongside other ME storage.
- Improvements to automatic construction, structure previews, recipes and the guide.

## Fixes in this release

- Fixed the disconnect when opening AE2 19.2.18 crafting status. Storage and coprocessor formatting remains compatible with 19.2.17.
- Requires RaishxCore **0.2.1**: fixes `reusable input amount must be positive` in recipe feedback cycles that consume their entire working stock.
- Planner logs now distinguish time, operation and depth limits, including the target, amount and request metrics.

## Temporary autocrafting workaround

Close the game or stop the server and edit `config/raishxcore/core.toml`:

```toml
[planner]
enabled = false
```

Change the existing setting and keep the other settings. Restart to use AE2's built-in planner. On multiplayer servers, the administrator must edit the server configuration. To re-enable the Core planner after an update, set `enabled = true` and restart. This switch already existed in beta9.

## Known limitation

The reported timeout when requesting more than three Enhancement Cores or Infinity Templates still needs reproduction with the affected network's patterns and inventory. The reusable-input fix does not establish that this timeout is resolved. If it persists, provide the new log and `/raishxcore planner` diagnostics immediately after the failure.

## Requirements

Minecraft **1.21.1**, NeoForge **21.1.216+**, AE2 **19.2.17 or 19.2.18** (declared range: `[19.2.17, 20)`; other 19.x versions were not verified for this fix), AE2 Addon Lib **1.0.3-1.21.1+** and RaishxCore **0.2.1** (`[0.2.1, 1.0)`). Replace both old UFO and RaishxCore JARs.
