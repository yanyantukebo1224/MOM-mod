package dev.mom.analyzer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.mom.api.AnalysisReport;
import dev.mom.api.CompatibilityStatus;
import dev.mom.api.LoaderKind;
import dev.mom.api.ModDescriptor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Static, non-executing first-pass JAR analyzer. Archive classes are never loaded. */
public final class JarAnalyzer {
    private static final Pattern TOML_MOD_BLOCK = Pattern.compile("(?s)\\[\\[mods\\]\\](.*?)(?=\\[\\[|\\z)");
    private static final Pattern TOML_FIELD = Pattern.compile("(?m)^\\s*(modId|version)\\s*=\\s*[\"']([^\"']+)[\"']");
    private static final Pattern TOML_DEPENDENCY = Pattern.compile("(?s)\\[\\[dependencies\\.([A-Za-z0-9_.-]+)\\]\\](.*?)(?=\\[\\[|\\z)");
    private static final Pattern TOML_DEP_MOD_ID = Pattern.compile("(?m)^\\s*modId\\s*=\\s*[\"']minecraft[\"']");
    private static final Pattern TOML_VERSION_RANGE = Pattern.compile("(?m)^\\s*versionRange\\s*=\\s*[\"']([^\"']+)[\"']");

    public AnalysisReport analyze(Path jar) {
        List<ModDescriptor> mods = new ArrayList<>();
        List<String> findings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        if (jar == null || !jar.toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
            return new AnalysisReport(jar == null ? Path.of(".") : jar,
                    CompatibilityStatus.UNSUPPORTED, mods, findings, List.of("Input must be a .jar file."));
        }
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry fabric = zip.getEntry("fabric.mod.json");
            ZipEntry forge = zip.getEntry("META-INF/mods.toml");
            ZipEntry neo = zip.getEntry("META-INF/neoforge.mods.toml");
            if (fabric != null) {
                try (var reader = new InputStreamReader(zip.getInputStream(fabric), StandardCharsets.UTF_8)) {
                    mods.add(parseFabric(JsonParser.parseReader(reader).getAsJsonObject()));
                }
            }
            if (forge != null) mods.addAll(parseToml(read(zip, forge), LoaderKind.FORGE));
            if (neo != null) mods.addAll(parseToml(read(zip, neo), LoaderKind.NEOFORGE));
            if (mods.isEmpty()) {
                findings.add("No recognized Fabric, Forge, or NeoForge metadata was found.");
                return new AnalysisReport(jar, CompatibilityStatus.UNKNOWN, mods, findings, errors);
            }
            int classCount = countClasses(zip);
            findings.add("Archive contains " + classCount + " class file(s).");
            if (classCount > 0) findings.add("Bytecode references and injection targets are not yet analyzed.");
            for (ModDescriptor mod : mods) {
                if (!mod.mixinConfigs().isEmpty()) findings.add(mod.id() + " declares mixin configuration(s): " + mod.mixinConfigs());
                if (mod.loader() != LoaderKind.FABRIC) findings.add(mod.id() + " needs a " + mod.loader() + " runtime bridge; detection is not loadability.");
                findings.addAll(mod.warnings());
            }
            return new AnalysisReport(jar, CompatibilityStatus.ANALYZED, mods, findings, errors);
        } catch (IOException | RuntimeException ex) {
            errors.add(ex.getClass().getSimpleName() + ": " + String.valueOf(ex.getMessage()));
            return new AnalysisReport(jar, CompatibilityStatus.UNSUPPORTED, mods, findings, errors);
        }
    }

    private static ModDescriptor parseFabric(JsonObject json) {
        String id = string(json, "id", "unknown");
        String version = string(json, "version", "unknown");
        List<String> entrypoints = new ArrayList<>();
        JsonElement ep = json.get("entrypoints");
        if (ep != null && ep.isJsonObject()) ep.getAsJsonObject().entrySet().forEach(e -> {
            if (e.getValue().isJsonArray()) e.getValue().getAsJsonArray().forEach(v -> entrypoints.add(v.toString()));
            else entrypoints.add(e.getValue().toString());
        });
        List<String> mixins = new ArrayList<>();
        JsonElement mx = json.get("mixins");
        if (mx != null && mx.isJsonArray()) mx.getAsJsonArray().forEach(v -> mixins.add(v.isJsonPrimitive() ? v.getAsString() : v.toString()));
        List<String> constraints = new ArrayList<>();
        JsonElement depends = json.get("depends");
        if (depends != null && depends.isJsonObject()) {
            JsonElement mc = depends.getAsJsonObject().get("minecraft");
            if (mc != null) constraints.add(mc.toString());
        }
        return new ModDescriptor(id, version, LoaderKind.FABRIC, constraints, entrypoints, mixins, List.of());
    }

    private static List<ModDescriptor> parseToml(String text, LoaderKind loader) {
        List<String> constraints = new ArrayList<>();
        Matcher dependency = TOML_DEPENDENCY.matcher(text);
        while (dependency.find()) {
            Matcher id = TOML_DEP_MOD_ID.matcher(dependency.group(2));
            Matcher range = TOML_VERSION_RANGE.matcher(dependency.group(2));
            if (id.find() && range.find()) constraints.add(range.group(1));
        }
        List<ModDescriptor> result = new ArrayList<>();
        Matcher blocks = TOML_MOD_BLOCK.matcher(text);
        while (blocks.find()) {
            String id = null, version = "unknown";
            Matcher fields = TOML_FIELD.matcher(blocks.group(1));
            while (fields.find()) {
                if (fields.group(1).equals("modId")) id = fields.group(2);
                if (fields.group(1).equals("version")) version = fields.group(2);
            }
            if (id != null) result.add(new ModDescriptor(id, version, loader, constraints,
                    List.of(), List.of(), List.of("TOML support is conservative; conditional dependency semantics are not evaluated.")));
        }
        return result;
    }

    private static String read(ZipFile zip, ZipEntry entry) throws IOException {
        try (var in = zip.getInputStream(entry)) { return new String(in.readAllBytes(), StandardCharsets.UTF_8); }
    }

    private static int countClasses(ZipFile zip) {
        int count = 0;
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) if (entries.nextElement().getName().endsWith(".class")) count++;
        return count;
    }

    private static String string(JsonObject json, String key, String fallback) {
        JsonElement value = json.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : fallback;
    }
}
