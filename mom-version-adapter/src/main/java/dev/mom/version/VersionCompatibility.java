package dev.mom.version;

import dev.mom.api.CompatibilityStatus;
import java.util.List;

public record VersionCompatibility(
        String sourceMinecraft,
        String targetMinecraft,
        CompatibilityStatus status,
        List<String> reasons
) {
    public VersionCompatibility { reasons = List.copyOf(reasons); }
}
