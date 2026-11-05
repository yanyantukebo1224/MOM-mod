package dev.mom.translation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolves translations in priority order. Earlier catalogs always win.
 */
public final class TranslationResolver {
    private final List<TranslationCatalog> catalogs;

    public TranslationResolver(List<TranslationCatalog> catalogs) {
        this.catalogs = List.copyOf(Objects.requireNonNull(catalogs, "catalogs"));
    }

    public Optional<String> find(String key) {
        Objects.requireNonNull(key, "key");
        return catalogs.stream()
                .map(catalog -> catalog.find(key))
                .flatMap(Optional::stream)
                .findFirst();
    }

    public String translateOrKey(String key) {
        return find(key).orElse(key);
    }
}
