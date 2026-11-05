package dev.mom.neoforge;

import dev.mom.api.LoaderKind;
import dev.mom.api.ModDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Conservative parser for common META-INF/neoforge.mods.toml metadata. */
public final class NeoForgeMetadataAdapter {
    private static final Pattern BLOCK = Pattern.compile("(?s)\\[\\[mods]](.*?)(?=\\[\\[|\\z)");
    private static final Pattern FIELD = Pattern.compile("(?m)^\\s*(modId|version)\\s*=\\s*[\"']([^\"']+)[\"']");

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
            if (id != null) result.add(new ModDescriptor(id, version, LoaderKind.NEOFORGE,
                    List.of(), List.of(), List.of(),
                    List.of("Partial TOML parser: dependency features and NeoForge-specific metadata are not fully modeled.")));
        }
        return List.copyOf(result);
    }
}
