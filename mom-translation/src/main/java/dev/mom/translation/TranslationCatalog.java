package dev.mom.translation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable-locale translation catalog.
 *
 * <p>The catalog deliberately does not perform machine translation. It stores
 * vetted key/value pairs supplied by a resource pack, mod, community bundle,
 * or a future optional translation provider.</p>
 */
public final class TranslationCatalog {
    private final String locale;
    private final Map<String, String> entries;

    public TranslationCatalog(String locale, Map<String, String> entries) {
        this.locale = requireText(locale, "locale");
        Objects.requireNonNull(entries, "entries");

        Map<String, String> copy = new LinkedHashMap<>();
        entries.forEach((key, value) -> {
            if (key != null && value != null) {
                copy.put(key, value);
            }
        });
        this.entries = Collections.unmodifiableMap(copy);
    }

    public String locale() {
        return locale;
    }

    public Optional<String> find(String translationKey) {
        return Optional.ofNullable(entries.get(translationKey));
    }

    public String translateOrKey(String translationKey) {
        return entries.getOrDefault(translationKey, translationKey);
    }

    public Map<String, String> entries() {
        return entries;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
