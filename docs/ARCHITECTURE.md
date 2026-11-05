# MOM architecture

## Two independent compatibility dimensions

MOM separates two problems that must not be conflated:

1. **Loader compatibility:** Forge/NeoForge metadata, lifecycle, event bus, registries, capabilities/attachments, configuration, and loader APIs.
2. **Game-version compatibility:** Minecraft class/member changes, mappings, data formats, registry changes, resource formats, and changed behavior.

A mod can pass one dimension and fail the other. Compatibility is therefore recorded against a tuple:

`(source loader, source Minecraft version, target Minecraft version, mod identity/version)`.

## Planned pipeline

1. **Discovery** — enumerate candidate JARs and dependency declarations.
2. **Static analysis** — inspect metadata, entrypoints, declared mixins, access wideners/transformers, and referenced classes.
3. **Compatibility planning** — select loader adapter and source-to-target version adapter.
4. **Transformation** — apply only registered, version-scoped transformations.
5. **Runtime integration** — expose supported loader APIs through Fabric-hosted bridges.
6. **Verification** — validate transformed artifacts and run controlled integration tests.
7. **Diagnostics** — report supported, patched, unsupported, and unverified features separately.

## Module boundaries

- `mom-api`: stable internal contracts and compatibility result types.
- `mom-analyzer`: safe JAR/metadata inspection; no class execution.
- `mom-transformer`: deterministic, testable bytecode/resource transformations.
- `mom-forge`: Forge-specific metadata and API bridge.
- `mom-neoforge`: NeoForge-specific metadata and API bridge.
- `mom-version-*`: game-version-specific adapters.
- `mom-fabric-*`: Fabric runtime integration, split by incompatible Loom/game generations.
- `mom-translation`: locale discovery, catalogs, fallback, and validation.

## Version strategy

Do not force Minecraft 1.20.1 and 26.3 into one Loom configuration. Keep source sets/builds separated where the toolchain or remapping model differs, while sharing plain-Java modules where possible.

The initial repository build intentionally contains no Minecraft dependency. This keeps core logic testable and avoids pretending that the runtime bridge already exists.
