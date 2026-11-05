package dev.mom.api;

/** Coarse result of static compatibility inspection; ANALYZED is not a loadability guarantee. */
public enum CompatibilityStatus {
    ANALYZED,
    UNKNOWN,
    UNSUPPORTED
}
