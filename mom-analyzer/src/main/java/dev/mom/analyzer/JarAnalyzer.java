package dev.mom.analyzer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.mom.api.AnalysisReport;
import dev.mom.api.CompatibilityStatus;
import dev.mom.api.LoaderKind;
import dev.mom.api.ModDescriptor;
import dev.mom.forge.ForgeMetadataAdapter;
import dev.mom.neoforge.NeoForgeMetadataAdapter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Static, non-executing first-pass JAR analyzer. Archive classes are never loaded. */
public final class JarAnalyzer {
    private final ForgeMetadataAdapter forgeAdapter = new ForgeMetadataAdapter();
    private final NeoForgeMetadataAdapter neoForgeAdapter = new NeoForgeMetadataAdapter();

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
            if (forge != null) mods.addAll(forgeAdapter.parse(read(zip, forge)));
            if (neo != null) mods.addAll(neoForgeAdapter.parse(read(zip, neo)));
            if (mods.isEmpty()) {
                findings.add("No recognized Fabric, Forge, or NeoForge metadata was found.");
                return new AnalysisReport(jar, CompatibilityStatus.UNKNOWN, mods, findings, errors);
            }
            findings.add("Archive contains " + countClasses(zip) + " class file(s).");
            findings.add("Bytecode references, mixin targets, and runtime behavior are not yet analyzed.");
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
