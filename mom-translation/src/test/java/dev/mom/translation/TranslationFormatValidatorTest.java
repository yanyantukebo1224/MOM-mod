package dev.mom.translation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranslationFormatValidatorTest {
    @Test
    void acceptsReorderedPlaceholdersAndFormattingCodes() {
        var result = TranslationFormatValidator.validate(
                "Hello {player}: %1$s §aREADY",
                "こんにちは {player}: §a準備完了 %1$s");

        assertTrue(result.valid());
        assertTrue(result.missingTokens().isEmpty());
        assertTrue(result.unexpectedTokens().isEmpty());
    }

    @Test
    void detectsMissingPlaceholder() {
        var result = TranslationFormatValidator.validate(
                "Found %1$d items for {player}",
                "{player} のアイテム");

        assertFalse(result.valid());
        assertTrue(result.missingTokens().contains("%1$d"));
    }
}
