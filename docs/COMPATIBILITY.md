# Compatibility policy

## Target range

Minecraft 1.20.1 through 26.3.

This is a target range, not a claim that every intermediate release or every mod is already supported.

## Compatibility states

- **Detected:** metadata/JAR could be inspected.
- **Analyzed:** known dependencies and transformation-sensitive features were identified.
- **Loadable:** the loader bridge accepted the mod.
- **Runtime-tested:** a controlled launch and selected behavior tests passed.
- **Verified:** the specific mod/version/game-version combination passed its declared test suite.
- **Unsupported:** a known blocker prevents safe execution.
- **Unknown:** insufficient evidence.

## Test strategy

- Unit tests for metadata parsing, version mapping, transformation rules, and translation.
- Fixture JARs for malformed or unusual metadata.
- Golden tests for deterministic bytecode/resource transformations.
- Dedicated game instances for each supported Minecraft/Loom generation.
- Smoke tests followed by mod-specific behavior tests.
- Crash reports must include the exact source loader, source game version, target game version, and adapter versions.

Never mark a mod as verified solely because the game reaches the title screen.
