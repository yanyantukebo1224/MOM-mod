package dev.mom.version;

import dev.mom.api.CompatibilityStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionProfilesTest {
    @Test void exactVersionIsOnlyAnalyzedNotVerified() {
        assertEquals(CompatibilityStatus.ANALYZED, VersionProfiles.assess("1.20.1", "1.20.1").status());
    }
    @Test void crossGenerationIsUnsupportedUntilAdapterExists() {
        assertEquals(CompatibilityStatus.UNSUPPORTED, VersionProfiles.assess("1.20.1", "26.3").status());
    }
}
