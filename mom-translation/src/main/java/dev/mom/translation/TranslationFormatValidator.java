package dev.mom.translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks that a translated string preserves formatting/control tokens from
 * its source. It is intentionally conservative: a mismatch is a review
 * warning, not proof that the translation is invalid.
 */
public final class TranslationFormatValidator {
    private static final Pattern TOKEN = Pattern.compile(
            "%(?:[0-9]+\\$)?[-#+ 0,(<]*[0-9]*(?:\\.[0-9]+)?[a-zA-Z]"
                    + "|\\{[A-Za-z0-9_.-]+}"
                    + "|§."
    );

    private TranslationFormatValidator() {
    }

    public static ValidationResult validate(String source, String translated) {
        List<String> sourceTokens = tokens(source);
        List<String> translatedTokens = tokens(translated);

        List<String> missing = difference(sourceTokens, translatedTokens);
        List<String> unexpected = difference(translatedTokens, sourceTokens);
        return new ValidationResult(missing.isEmpty() && unexpected.isEmpty(),
                missing, unexpected);
    }

    private static List<String> tokens(String value) {
        List<String> result = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(value);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        Collections.sort(result);
        return result;
    }

    private static List<String> difference(List<String> expected, List<String> actual) {
        List<String> remaining = new ArrayList<>(actual);
        List<String> difference = new ArrayList<>();
        for (String token : expected) {
            if (!remaining.remove(token)) {
                difference.add(token);
            }
        }
        return List.copyOf(difference);
    }

    public record ValidationResult(
            boolean valid,
            List<String> missingTokens,
            List<String> unexpectedTokens
    ) {
        public ValidationResult {
            missingTokens = List.copyOf(missingTokens);
            unexpectedTokens = List.copyOf(unexpectedTokens);
        }
    }
}
