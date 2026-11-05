package dev.mom.api;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record AnalysisReport(
        Path file,
        CompatibilityStatus status,
        List<ModDescriptor> mods,
        List<String> findings,
        List<String> errors
) {
    public AnalysisReport {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(status, "status");
        mods = List.copyOf(mods);
        findings = List.copyOf(findings);
        errors = List.copyOf(errors);
    }
}
