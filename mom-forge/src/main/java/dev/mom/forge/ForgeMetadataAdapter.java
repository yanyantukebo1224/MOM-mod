package dev.mom.forge;

import dev.mom.api.LoaderKind;
import dev.mom.api.ModDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Conservative parser for the common META-INF/mods.toml fields. */
public final class ForgeMetadataAdapter {
    private static final Pattern BLOCK = Pattern.compile("(?s)\\[\\[mods]](.*?)(?=\\[\\[|\\z)");
    private static final Pattern FIELD = Pattern.compile("(?m)^\\s*(modId|version)\\s*=\\s*[\"']([^\"']+)[\"']");
    private static final Pattern MC_RANGE = Pattern.compile("(?s)\\[\\[dependencies\\.[^]]+]](.*?)(?=\\[\\[|\\z)");

    public List<ModDescriptor> parse(String toml) {
        List<ModDescriptor> result = new ArrayList<>();
        Matcher blocks = BLOCK.matcher(toml);
        while (blocks.find()) {
            String id = null, version = "unknown";
            Matcher fields = FIELD.matcher(blocks.group(1));
            while (fields.find()) {
                if (fields.group(1).equals("modId")) id = fields.group(2);
                if (fields.group(1).equals("version")) version = fields.group(2);
            }
            if (id != null) result.add(new ModDescriptor(id, version, LoaderKind.FORGE,
                    minecraftRanges(toml), List.of(), List.of(),
                    List.of("Partial TOML parser: conditional dependency semantics are not evaluated.")));
        }
        return List.copyOf(result);
    }

    private static List<String> minecraftRanges(String toml) {
        List<String> result = new ArrayList<>();
        Matcher blocks = MC_RANGE.matcher(toml);
        while (blocks.find()) {
            String block = blocks.group(1);
            if (block.contains("\"minecraft\"") || block.contains("'minecraft'")) {
                Matcher range = Pattern.compile("versionRange\\s*=\\s*[\"']([^\"']+)[\"']").matcher(block);
                if (range.find()) result.add(range.group(1));
            }
        }
        return List.copyOf(result);
    }
}
