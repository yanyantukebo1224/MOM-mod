package dev.mom.api;

import java.util.List;
import java.util.Objects;

public record ModDescriptor(
        String id,
        String version,
        LoaderKind loader,
        List<String> minecraftConstraints,
        List<String> entrypoints,
        List<String> mixinConfigs,
        List<String> warnings
) {
    public ModDescriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(loader, "loader");
        minecraftConstraints = List.copyOf(minecraftConstraints);
        entrypoints = List.copyOf(entrypoints);
        mixinConfigs = List.copyOf(mixinConfigs);
        warnings = List.copyOf(warnings);
    }
}
