# MOM — Multi-loader Optimization Middleware

MOM is an experimental Fabric-hosted compatibility project for inspecting and eventually adapting selected Forge and NeoForge mods.

**Target Minecraft range:** 1.20.1 through 26.3  
**Current milestone:** static metadata analysis, ASM bytecode reference scanning, and an explicit class-file remapping engine.

## Current status

| Component | Status |
|---|---|
| Shared API and compatibility report types | Implemented |
| Static JAR metadata analyzer | Initial implementation |
| ASM bytecode reference scanner | Implemented; static inspection only |
| Analyzer CLI | Implemented |
| Explicit ASM class-file remapper | Implemented; requires caller-supplied mappings |
| Forge and NeoForge metadata adapters | Initial implementation |
| Version-family assessment | Initial conservative implementation |
| Fabric 1.20.1 bootstrap | Initial runtime entrypoint |
| Fabric 26.3 bootstrap | Initial runtime entrypoint |
| Translation catalog and token validator | Initial implementation |
| Forge/NeoForge API bridge implementations | Not implemented |
| Automatic Forge-to-MOM remapping rules | Not implemented |
| Minecraft runtime integration and real mod execution | Not implemented |

## Architecture

- `mom-api`: shared descriptors and result types.
- `mom-analyzer`: metadata inspection, bytecode reference scanning, and CLI.
- `mom-transformer`: explicit ASM class-file remapping.
- `mom-forge` / `mom-neoforge`: loader-specific metadata adapters; API bridges remain future work.
- `mom-version-adapter`: conservative source/target version assessment.
- `mom-fabric-1201` / `mom-fabric-263`: separate Fabric bootstrap artifacts.
- `mom-translation`: locale catalogs, fallback resolution, and format-token validation.

The 1.20.1 and 26.3 Fabric targets are intentionally separate. The current bootstrap modules compile against Fabric Loader APIs but do not yet provide a complete mapped Minecraft runtime integration.

## Build

Requires JDK 25 and Gradle 9.6.0.

```bash
gradle clean test
gradle build
```

GitHub Actions runs unit tests and builds all modules on pushes and pull requests. Runtime JAR artifacts are uploaded when the workflow succeeds.

## Analyze a mod JAR

After building, run:

```bash
gradle :mom-analyzer:run --args="path/to/mod.jar"
```

The analyzer reports loader metadata, class-file version, referenced namespaces, and discovered loader API references. It does not load or execute classes from the inspected archive.

## Important compatibility note

A detected mod is not necessarily loadable. Bytecode reference scanning is not semantic analysis, and explicit remapping is not equivalent to implementing the target API. MOM does not yet emulate Forge/NeoForge event buses, registries, lifecycle, capabilities, networking, mixin behavior, or data migration. No Forge/NeoForge mod execution compatibility is claimed until a specific mod/version/game-version combination passes an actual Minecraft runtime test.

## License

MIT.
