# MOM — Multi-loader Optimization Middleware

MOM is an experimental Fabric-hosted compatibility project for inspecting and adapting a deliberately selected subset of Forge 1.20.1 mods.

## Current milestone

**MOM 0.3.0 — first Forge runtime vertical slice.**

- Static JAR metadata analysis and ASM bytecode reference scanning.
- Explicit ASM class-file remapping primitive.
- Fabric 1.20.1 Loom development runtime.
- Experimental Forge-style mod discovery from `config/mom/forge-mods`.
- Initial compatibility shims for `@Mod`, `IEventBus`, `@SubscribeEvent`, `FMLJavaModLoadingContext`, `FMLCommonSetupEvent`, and `FMLLoadCompleteEvent`.
- A tiny Forge-API fixture JAR and automated loader/event-bus tests.

## Build

Requires JDK 25 and Gradle 9.8.0.

```bash
gradle clean test
gradle build
gradle :mom-fabric-1201:remapJar
gradle :mom-fabric-1201:forgeFixtureJar
gradle :mom-fabric-1201:runClient
```

The normal runtime artifact is produced under `mom-fabric-1201/build/libs/`. The separate `*-forge-fixture.jar` is only a test mod.

## Try the Forge compatibility slice

1. Install Fabric Loader for Minecraft 1.20.1.
2. Put the MOM runtime JAR in the Fabric instance's `mods` directory.
3. Put a trusted Forge 1.20.1 mod JAR in `config/mom/forge-mods/`.
4. Start the game and inspect the log for `[MOM/Forge]`.

The CI artifact also contains a small `momfixture` JAR that exercises the currently supported subset. It is not a gameplay mod.

## Compatibility boundary — read before testing

This is **not full Forge compatibility**. The first runtime slice currently supports:
- A class annotated with `@net.minecraftforge.fml.common.Mod` whose ID appears in `META-INF/mods.toml`.
- A no-argument mod constructor.
- Obtaining the mod event bus during construction.
- Registering subscribers with `IEventBus.register` and `@SubscribeEvent`.
- Explicitly typed `IEventBus.addListener(EventClass.class, consumer)`.
- Synchronous dispatch of MOM's common-setup and load-complete lifecycle events.

It does not yet implement registries, deferred registers, MinecraftForge gameplay events, capabilities, networking, configs, Dist/DistExecutor, access transformers, coremods, mixin remapping, Forge services, mod dependency resolution, or the full Forge EventBus API. Many real Forge mods will not load yet. Mods using Forge classes outside this subset can fail during class loading or construction.

Forge mod JARs execute code inside the game process. Only test JARs from sources you trust. Use a separate game instance and back up saves.

The runtime loader scans the dedicated `config/mom/forge-mods` directory so Fabric's own mod discovery remains separate. It does not automatically convert every file in the normal `mods` folder.

## Project modules

- `mom-api`: shared compatibility report types.
- `mom-analyzer`: static metadata and bytecode analysis CLI.
- `mom-transformer`: explicit ASM remapping primitive.
- `mom-forge` / `mom-neoforge`: metadata adapters.
- `mom-version-adapter`: conservative Minecraft version-family assessment.
- `mom-fabric-1201`: active Loom runtime and first Forge compatibility slice.
- `mom-fabric-263`: separate early bootstrap for Minecraft 26.3.
- `mom-translation`: translation catalogs and token validation.

No NeoForge runtime compatibility is claimed yet.
