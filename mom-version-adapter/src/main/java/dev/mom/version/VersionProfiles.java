package dev.mom.version;

import dev.mom.api.CompatibilityStatus;
import java.util.List;

/**
 * Explicitly conservative generation profiles. This is a routing table, not
 * a promise that arbitrary Minecraft internals can be translated.
 */
public final class VersionProfiles {
    private VersionProfiles() { }

    public static VersionCompatibility assess(String source, String target) {
        if (source == null || target == null || source.isBlank() || target.isBlank()) {
            return new VersionCompatibility(String.valueOf(source), String.valueOf(target),
                    CompatibilityStatus.UNKNOWN, List.of("Both versions must be specified."));
        }
        if (source.equals(target)) {
            return new VersionCompatibility(source, target, CompatibilityStatus.ANALYZED,
                    List.of("Exact game-version match; loader/API compatibility still requires separate analysis."));
        }
        String sourceFamily = family(source);
        String targetFamily = family(target);
        if (sourceFamily.equals(targetFamily)) {
            return new VersionCompatibility(source, target, CompatibilityStatus.UNKNOWN,
                    List.of("Same broad version family; no automatic bytecode or data conversion is implied."));
        }
        return new VersionCompatibility(source, target, CompatibilityStatus.UNSUPPORTED,
                List.of("Cross-generation conversion is not implemented. A dedicated version adapter and tests are required."));
    }

    private static String family(String version) {
        if (version.startsWith("1.20")) return "legacy-1.20";
        if (version.startsWith("1.21")) return "legacy-1.21";
        if (version.startsWith("26.")) return "modern-26";
        return "unknown";
    }
}
