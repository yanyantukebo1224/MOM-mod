# MOM — Multi-loader Optimization Middleware

MOM is an experimental compatibility layer that aims to run selected Forge and NeoForge mods on a Fabric-based Minecraft runtime, while also handling differences between Minecraft versions.

**Target range:** Minecraft 1.20.1 through 26.3  
**Initial engineering milestone:** build a reliable, testable core before attempting loader or game-class emulation.

## Project status

This repository is at the foundation stage. It does **not** yet load Forge or NeoForge mods. Compatibility is a per-mod, per-version engineering problem; a successful JAR scan is not proof that a mod can run.

| Area | Status |
|---|---|
| Core architecture and module boundaries | In progress |
| Translation catalog / validation core | Initial implementation |
| Mod metadata and JAR analysis | Planned |
| Forge compatibility adapter | Planned |
| NeoForge compatibility adapter | Planned |
| Minecraft version adapters | Planned |
| Fabric runtime integration | Planned |
| Automated game-level compatibility tests | Planned |

## Design principles

1. **Fabric is the host runtime.** MOM does not replace Fabric Loader.
2. **Separate loader compatibility from game-version compatibility.**
3. **Detect before transforming.** Analyze mod metadata and bytecode before attempting compatibility work.
4. **Prefer explicit adapters and tested patches** over broad, unsafe bytecode rewriting.
5. **Translation is layered and offline-first.** Existing mod translations take priority; MOM/community translations fill gaps; automatic translation is an optional provider, never a required network dependency.
6. **Report unsupported behavior clearly.** Do not silently claim compatibility.

## Repository layout

- `mom-translation` — language catalog, fallback resolution, and format-token validation.
- `docs/ARCHITECTURE.md` — compatibility pipeline and module boundaries.
- `docs/TRANSLATION.md` — translation data flow and safety rules.
- `docs/COMPATIBILITY.md` — support policy and test strategy.

## Build

Requires JDK 25 and Gradle 9.7.1 for the initial development toolchain.

```bash
gradle test
gradle build
```

The initial build is a pure-Java foundation. Minecraft/Fabric dependencies will be introduced in version-specific runtime modules after the target matrix is pinned.

## Compatibility expectations

MOM will not promise that every old mod can run on every new Minecraft version. Mods that depend on removed game internals, native libraries, mixin injection points, core transformers, or loader-specific lifecycle behavior may require dedicated adapters or may remain unsupported.

## License

MIT. See [LICENSE](LICENSE).
