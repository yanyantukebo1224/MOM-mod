package dev.mom.analyzer;

import java.util.List;
import java.util.Set;

/** Immutable summary of class-file structure and loader API references. */
public record BytecodeSummary(
        int classCount,
        int maxClassMajorVersion,
        int mixinAnnotationCount,
        Set<String> referencedNamespaces,
        List<String> loaderApiReferences,
        List<String> warnings
) {
    public BytecodeSummary {
        referencedNamespaces = Set.copyOf(referencedNamespaces);
        loaderApiReferences = List.copyOf(loaderApiReferences);
        warnings = List.copyOf(warnings);
    }
}
