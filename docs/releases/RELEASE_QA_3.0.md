# UFO Future 3.0 release QA

The author reports completing both human checks: a real 2.x save upgrade and
load testing in the actual modpack with player activity. No item loss or other
unexpected change was observed. The author reports that the server stayed at
20 TPS with the machines running and accelerated. The workload's exact machine
count, client count, MSPT and logs have not yet been attached to this document;
the procedures below remain the reference for preserving that evidence.
The already approved Grid Link, energy-supply, viewer and idle-lifecycle checks
remain closed.

## Author report — 2026-09-24

- Real 2.x world migration: performed; no missing items or other unexpected
  differences observed by the author.
- Real-modpack load test: performed; the author reports 20 server TPS with the
  machines running and accelerated. Exact workload details and profiler output
  are pending transcription here.
- In the 2026-09-21 UFO playtest session, the author reported testing six QMFs,
  six Quantum Slicers, five Quantum Processor Assemblers and five Quantum
  Cryoforges in parallel, then testing acceleration with Warden Soul and four
  Chrono T3 upgrades. On 2026-09-24 the author clarified that server TPS stayed
  at 20 even with the machines running and accelerated. The reported 66–102,
  55–105 and 58–91 ranges were controller timing readings for QMF, Slicer and
  Processor respectively;
  they are not server TPS readings. No numerical Spark TPS/MSPT result was found
  in that session transcript.
- This report is a human observation, not a claim that exact resource balances,
  traffic rates or the 100+ simultaneously active machine count have been
  independently verified from artifacts.

## Current beta.1 result — 2026-09-18

- Unit/build/release-JAR verification: passed.
- Full dependency GameTests: 34/34 passed.
- Reduced dependency run without Mekanism: 34/34 passed.
- Short idle soak: 2/2 passed; 3 machines for 200 measured ticks at 0.388 and
  0.249 ms/tick average.
- Short active-load smoke: 1/1 passed; 3 machines, 907 measured ticks at 0.646
  wall ms/tick, exact stock/FE and zero scans outside power-change windows.
- Every server run saved overworld, Nether, End and AE2 spatial storage and shut
  down normally; the dedicated `Saving World` reproduction also passed.
- Artifact: `ufo-3.0.0-beta.1.jar`; local backups and datagen caches absent.

## Automated gates

Run sequentially: UFO's composite build writes to the same Core outputs.

```sh
cd ../RaishxCore
./gradlew test --rerun build runGameTestServer
cd ../UFO-Future-1.21.1
./gradlew test --rerun build runGameTestServer
./gradlew runGameTestServer -PwithoutMekanism
./gradlew runSoakGameTestServer -PsoakMachines=3 -PsoakTicks=200
./gradlew runLoadGameTestServer -PloadMachines=3 -PloadTicks=200
./gradlew runLoadGameTestServer
```

Build/datagen require the complete development fixture. The reduced dependency
flag is for the appropriate test/client variant. All JavaCompile tasks fail on
warnings except the documented NeoForge construction `this-escape` exception.

The separate `Active load` workflow defaults to 102 Singularities and 10,000
active ticks, after 700 ticks of rejecting recipe products. It uploads JSON
ledgers, controller timing samples, native Minecraft JFR and the server log.
This test uses actual encoded stone-brick crafting patterns and automatic
controller/Grid Link ticking, finite FE supplied through the local hatch and a
controlled MEStorage implementation. No pending outputs are seeded.

It checks bounded blocked routes, exact stone/bricks and energy conservation,
continued production after opening storage, a physical power-block outage and
reconnection, and no scans outside the bounded power-change windows. Those
windows may each cause one event-driven neighbor invalidation; they are not an
idle zero-scan assertion. Startup requires active chunks, AE2 readiness and 40
initialized ticks without scans. The closed idle soak implementation is separate.

The headless result is not live-modpack TPS or player traffic. Its accelerated
GameTest wall intervals include server work; controller p95/p99 cover the last
up to 256 recorded calls. Minecraft JFR includes its native server tick-time
and packet events, with no player packets in a headless fleet.

## Human gate 1: copy of an actual 2.x world

Follow [the migration guide](MIGRATION_2.1-to-3.0.md). Test a complete copy,
including `data/ae_universal_cell_data`, with the normal modpack dependencies.
Keep GeckoLib when AdvancedAE requires it.

1. In the original 2.x world, record exact amounts in representative item/fluid/
   chemical cells and inventories. Include a UUID-backed `beaco` cell. Record
   running jobs, promised outputs and buffered resources before making the copy.
2. Open the copy with the UFO/Core 3.0 JARs. Compare retained blocks/items and cell
   UUIDs/balances. The documented removed Entropic Assembler content and
   Apocalypse entity/egg are exceptions, not retained content.
3. Replace the designated coolant/energy supply positions on old affected
   structures. Check old in-flight recipe IDs against the current recipe before
   resuming; rebalanced recipes need their own compatibility comparison.
4. Resume representative machines, record completed/pending resources, then
   save, quit and reopen the copy. Compare the total owned resources across
   cells, machine buffers, pending outputs and drops. There must be no unexplained
   loss, duplicate result, regenerated cell UUID or unexpected missing content.

Return the source UFO version, target UFO/Core commits, mod versions, before/
after amounts and `latest.log`. A screenshot alone does not establish a large
cell's exact balance. Synthetic legacy NBT fixtures prove reader contracts;
they do not replace this whole-world comparison.

## Human gate 2: actual modpack load and players

Use a copied/test world with the production JARs. Warm the chunks, AE2 grids and
structures before starting the window. Record how many machines are actually
running and what recipes/pattern quantities they execute. A placed idle machine
is not an active processor. For the 100+ machine release load target, include
at least 100 simultaneously active machines in the measured workload.

Capture a three-minute window for each relevant scenario: active processing,
blocked outputs followed by drain, and reconnect while work remains. Reuse
existing machines; this is a load test, not a repeat of the approved Grid Link
correctness script. Include normal terminal/GUI/player activity during the
window. Record the number of connected clients.

```text
/ufo debug perf reset
/jfr start
```

The reference modpack already includes Spark. For actual TPS/MSPT and individual
stall reports, also run the server commands below:

```text
/spark tps
/spark tickmonitor --threshold-tick 50
/spark profiler start
```

Run the scenario for three minutes, then:

```text
/ufo debug perf export active-processing
/jfr stop
/spark tps
/spark profiler stop --save-to-file
/spark tickmonitor
```

Use distinct scenario labels for `blocked-output-drain` and `active-reconnect`.
Collect the JSON under `ufo-diagnostics`, the JFR and its native JSON summary
under `debug`, the saved `.sparkprofile` under Spark's config directory, and
`latest.log`. Spark tickmonitor toggles off with the final command.
Command semantics: [official Spark documentation](https://spark.lucko.me/docs/Command-Usage).
Reset begins a new global metrics window; record
its exact start/end and do not combine unrelated windows.

For packet traffic, use a dedicated server and real network clients: an
integrated singleplayer connection does not provide a representative wire
traffic measurement. Minecraft JFR reports Minecraft packet bytes, not TCP/
compression/encryption overhead. Machine `syncBytes` cover only instrumented
UFO synchronization. Neither number alone is total link bandwidth.

The full-server tick-work budget is 50 ms/tick for 20 TPS. Report measured tick
average/p95/p99 available from the actual tick monitor/profile, worst stalls,
machine costs, packet bytes/rates and client count. Native Minecraft JFR
ServerTickTime events sample a moving tick average; their percentiles are
percentiles of those samples, not individual-tick p95/p99. Machine percentile
samples have their separate <=256-call window. Do not conflate these scopes.

Traffic has no approved numerical ceiling yet: preserve the measurement
and workload description for the author to set that ceiling. Do not call an
unmeasured traffic budget approved.

If a gate fails, retain its exact workload, logs/recording and resource balances
for reproduction. Tag/release publication remains a separate authorized action;
this QA procedure publishes neither.
