# UFO Future 3.0.0-alpha.2

A hardening alpha. It adds no new machines: it tightens modular armor, makes the
GameTest gates actually fail on a broken mod, and moves the companion planner to a
new RaishxCore release. This release also attaches the matching **RaishxCore**
jar next to the UFO jar.

## Dependencies

- **RaishxCore updated to `0.1.0-alpha.4`** (required, downloaded from its own
  repository). See that release for the planner work: consumer-declared missing
  weights, an exact oracle for the differential corpus, and `routeChoiceLinks` in
  the planner diagnostics.
- **Applied Energistics 2:** 19.2.17 or newer within the 19.x line.
- **AE2 Addon Lib:** 1.0.3 for Minecraft 1.21.1 or a compatible 1.x version.
- **Mekanism remains optional.**

## Changed

- **Modular armor caps are server-authoritative.** All 15 armor settings can now be
  limited per server or modpack through `armor.moduleCaps.*` in
  `ufo-server.toml`, tightening the design maximum without editing the mod. The
  server enforces the cap at the client-to-server boundary and on every effect read;
  the client still shows the item's uncapped design range.
- **Armor clock is dimension-independent.** Cadences and the translocator cooldown
  use the overworld's game time, so they no longer jump when the player changes
  dimension.
- **Cloak scanning is budgeted.** The cloak re-scans nearby entities every 5 ticks
  instead of every tick.
- **Thermal suit isolated from UFO modules**, so its behavior no longer mixes with
  the modular armor.
- **GuideME guide in simplified Chinese:** core guide, navigation and machine
  operation pages.
- **RaishxCore integration** follows `0.1.0-alpha.4`; the declared pin and the
  verified revision move together.

## Fixed

- **Armor setting clamp overflow (security).** A crafted packet with an extreme
  value could overflow the clamps and flip to the opposite end of the range (for
  example, the most negative value yielding the maximum speed). The arithmetic now
  bounds first and is pinned by unit tests at the wire extremes.
- **Release artifacts cleaned:** the datagen hash cache no longer leaks into the
  published sources jar, and the runtime jar ships the README alongside the license
  and credits.

## Build and test hardening

- Every GitHub Action is pinned by commit SHA, with Dependabot updating the pins in
  grouped weekly pull requests.
- The GameTest, soak and load workflows now assert NeoForge's completion banner.
  `runGameTestServer` exits 0 even when the mod fails to load, so a broken jar could
  have passed CI and been published; that failure mode is now caught.
