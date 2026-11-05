package dev.mom.translation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslationResolverTest {
    @Test
    void earlierCatalogHasHigherPriority() {
        TranslationCatalog official = new TranslationCatalog("ja_jp",
                Map.of("item.example", "公式訳"));
        TranslationCatalog community = new TranslationCatalog("ja_jp",
                Map.of("item.example", "コミュニティ訳", "item.other", "追加訳"));

        TranslationResolver resolver = new TranslationResolver(List.of(official, community));

        assertEquals("公式訳", resolver.translateOrKey("item.example"));
        assertEquals("追加訳", resolver.translateOrKey("item.other"));
        assertEquals("item.unknown", resolver.translateOrKey("item.unknown"));
    }
}
