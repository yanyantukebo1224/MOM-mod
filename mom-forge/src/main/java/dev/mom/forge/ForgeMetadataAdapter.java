package dev.mom.forge;

import dev.mom.api.LoaderKind;
import dev.mom.api.ModDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Conservative parser for common META-INF/mods.toml metadata. */
public final class ForgeMetadataAdapter {
    private static final Pattern BLOCK = Pattern.compile("(?s)\\[\\[mods\\]\\](.*?)(?=\\[\\[|\\z)");
    private static final Pattern FIELD = Pattern.compile("(?m)^\\s*(modId|version)\\s*=\\s*[\"']([^\"']+)[\"']");
    private static final Pattern DEPENDENCY = Pattern.compile("(?s)\\[\\[dependencies\\.[^\\]]+\\]\\](.*?)(?=\\[\\[|\\z)");
    private static final Pattern MINECRAFT_ID = Pattern.compile("(?m)^\\s*modId\\s*=\\s*[\"']minecraft[\"']");
    private static final Pattern VERSION_RANGE = Pattern.compile("(?m)^\\s*versionRange\\s*=\\s*[\"']([^\"']+)[\"']");

    public List<ModDescriptor> parse(String toml) {
        List<String> minecraftRanges = new ArrayList<>();
        Matcher dependencies = DEPENDENCY.matcher(toml);
        while (dependencies.find()) {
            Matcher id = MINECRAFT_ID.matcher(dependencies.group(1));
            Matcher range = VERSION_RANGE.matcher(dependencies.group(1));
            if (id.find() && range.find()) minecraftRanges.add(range.group(1));
        }

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
                    minecraftRanges, List.of(), List.of(),
                    List.of("Partial TOML parser: conditional dependency semantics are not evaluated.")));
        }
        return List.copyOf(result);
    }
}
