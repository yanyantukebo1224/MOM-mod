# MOM — Multi-loader Optimization Middleware

MOM is an experimental Fabric-hosted compatibility project for inspecting and eventually adapting selected Forge and NeoForge mods.

**Target Minecraft range:** 1.20.1 through 26.3  
**Current milestone:** static JAR analysis, compatibility metadata, and two Fabric bootstrap artifacts.

## Current status

| Component | Status |
|---|---|
| Shared API and compatibility report types | Implemented |
| Static JAR analyzer (Fabric / Forge / NeoForge metadata) | Initial implementation |
| Forge and NeoForge metadata adapters | Initial implementation |
| Version-family assessment | Initial conservative implementation |
| Fabric 1.20.1 bootstrap | Initial runtime entrypoint |
| Fabric 26.3 bootstrap | Initial runtime entrypoint |
| Translation catalog and token validator | Initial implementation |
| Forge/NeoForge API emulation and mod execution | Not implemented |
| Minecraft internals / bytecode version conversion | Not implemented |

## Architecture

- `mom-api`: shared descriptors and result types.
- `mom-analyzer`: inspects JAR metadata without executing archive classes.
- `mom-forge` / `mom-neoforge`: loader-specific metadata adapters.
- `mom-version-adapter`: conservative source/target version assessment.
- `mom-fabric-1201` / `mom-fabric-263`: separate Fabric bootstrap artifacts.
- `mom-translation`: locale catalogs, fallback resolution, and format-token validation.

The 1.20.1 and 26.3 Fabric targets are intentionally separate. Fabric's 26.3 toolchain uses a newer Loom/Gradle generation and Java 25; the current bootstrap modules only compile against Fabric Loader APIs and do not yet use mapped Minecraft classes.

## Build

Requires JDK 25 and Gradle 9.6.0.

```bash
gradle clean test
gradle build
```

GitHub Actions runs unit tests and builds all modules on pushes and pull requests. The two runtime JARs are uploaded as a workflow artifact.

## Important compatibility note

A detected mod is not necessarily loadable. This analyzer does not yet resolve all bytecode references, mixin targets, core transformers, event semantics, registries, capabilities, or data migrations. Forge/NeoForge mod execution remains future work; no compatibility claim is made until a specific mod/version/game-version combination passes runtime tests.

## License

MIT.
