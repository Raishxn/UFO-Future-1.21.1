# UFO Future 3.0.0-beta.1

The first public beta of the 3.0 line, focused on core functionality, save safety,
and release artifact cleanup. This is not yet a stable build: migration of a real
2.x world and production-modpack testing remain the final human gates.

## Main fixes

- Fixed the permanent `Saving World` hang caused by AE2 callbacks and visual-state
  updates promoting chunks during shutdown.
- AE2 cables now connect on every face of the Quantum Pattern Buffer and ME Massive
  Fluid Hatch, including the front.
- Recovery packages identify their resource and amount instead of displaying the
  raw `Wrapped Generic Stack` name.
- Missing bucket names and Brazilian Portuguese encoding were corrected.
- Pending inputs, outputs, and energy remain transactional across full networks,
  reloads, and machine removal.

## Assets and packaging

- Integrated the new machine and multiblock overlays.
- Asset provenance audit is clean: no old byte-identical matches and no
  all-rights-reserved asset remains in the package.
- Replaced the `star`/`space` models with original procedural geometry.
- Local texture backups are excluded from published runtime and sources JARs.

Back up old worlds before migration and include `latest.log`, exact versions, and
reproduction steps in bug reports.
