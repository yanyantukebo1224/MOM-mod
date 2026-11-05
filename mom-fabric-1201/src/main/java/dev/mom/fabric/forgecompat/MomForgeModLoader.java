package dev.mom.fabric.forgecompat;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads the deliberately small MOM Forge compatibility subset from
 * config/mom/forge-mods. Mods are arbitrary executable code: only install trusted JARs.
 */
public final class MomForgeModLoader {
    private static final Pattern MOD_BLOCK =
            Pattern.compile("(?s)\\[\\[mods\\]\\](.*?)(?=\\[\\[|\\z)");
    private static final Pattern MOD_ID =
            Pattern.compile("(?m)^\\s*modId\\s*=\\s*[\"']([A-Za-z0-9_.-]+)[\"']");
    private final Path directory;
    private URLClassLoader classLoader;

    public record LoadResult(int discoveredJars, int loadedMods, List<String> diagnostics) {
        public LoadResult { diagnostics = List.copyOf(diagnostics); }
    }

    public MomForgeModLoader(Path directory) { this.directory = directory; }

    public LoadResult load() {
        List<String> diagnostics = new ArrayList<>();
        List<Path> jars;
        try {
            Files.createDirectories(directory);
            try (var stream = Files.list(directory)) {
                jars = stream.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".jar"))
                        .sorted().toList();
            }
        } catch (IOException ex) {
            diagnostics.add("Cannot access Forge mod directory " + directory + ": " + ex.getMessage());
            diagnostics.forEach(s -> System.err.println("[MOM/Forge] " + s));
            return new LoadResult(0, 0, diagnostics);
        }

        List<ModJar> candidates = new ArrayList<>();
        List<URL> urls = new ArrayList<>();
        for (Path jar : jars) {
            try (ZipFile zip = new ZipFile(jar.toFile())) {
                ZipEntry metadata = zip.getEntry("META-INF/mods.toml");
                if (metadata == null) continue;
                String toml;
                try (var in = zip.getInputStream(metadata)) {
                    toml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
                List<String> ids = parseModIds(toml);
                if (ids.isEmpty()) {
                    diagnostics.add(jar.getFileName() + ": mods.toml contains no readable [[mods]] modId.");
                    continue;
                }
                candidates.add(new ModJar(jar, ids));
                urls.add(jar.toUri().toURL());
            } catch (Exception ex) {
                diagnostics.add(jar.getFileName() + ": cannot inspect archive: " + ex.getMessage());
            }
        }

        if (candidates.isEmpty()) {
            diagnostics.add("No Forge JARs found. Place trusted 1.20.1 Forge JARs in " + directory);
            diagnostics.forEach(s -> System.out.println("[MOM/Forge] " + s));
            return new LoadResult(jars.size(), 0, diagnostics);
        }

        classLoader = new URLClassLoader(urls.toArray(URL[]::new), MomForgeModLoader.class.getClassLoader());
        List<LoadedMod> loaded = new ArrayList<>();
        Set<String> loadedIds = new HashSet<>();
        for (ModJar candidate : candidates) {
            int before = loaded.size();
            scanAndConstruct(candidate, loaded, loadedIds, diagnostics);
            if (loaded.size() == before) {
                diagnostics.add(candidate.path().getFileName() + ": no matching, constructible @Mod class was found.");
            }
        }

        for (LoadedMod mod : loaded) {
            try { mod.bus().post(new FMLCommonSetupEvent()); }
            catch (RuntimeException ex) { diagnostics.add(mod.id() + ": common setup failed: " + ex); }
        }
        for (LoadedMod mod : loaded) {
            try { mod.bus().post(new FMLLoadCompleteEvent()); }
            catch (RuntimeException ex) { diagnostics.add(mod.id() + ": load-complete failed: " + ex); }
        }
        diagnostics.forEach(s -> System.out.println("[MOM/Forge] " + s));
        System.out.println("[MOM/Forge] Loaded " + loaded.size() + " Forge-style mod(s) from " + candidates.size() + " JAR(s).");
        return new LoadResult(jars.size(), loaded.size(), diagnostics);
    }

    private void scanAndConstruct(ModJar candidate, List<LoadedMod> loaded,
                                  Set<String> loadedIds, List<String> diagnostics) {
        try (ZipFile zip = new ZipFile(candidate.path().toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || !name.endsWith(".class") ||
                        name.equals("module-info.class") || name.endsWith("package-info.class")) continue;
                String className = name.substring(0, name.length() - 6).replace('/', '.');
                Class<?> type;
                try {
                    type = Class.forName(className, false, classLoader);
                } catch (LinkageError | ClassNotFoundException ex) {
                    continue;
                }
                Mod annotation = type.getAnnotation(Mod.class);
                if (annotation == null || !candidate.modIds().contains(annotation.value())) continue;
                if (!loadedIds.add(annotation.value())) {
                    diagnostics.add(candidate.path().getFileName() + ": duplicate mod id " + annotation.value() + " skipped.");
                    continue;
                }
                if (Modifier.isAbstract(type.getModifiers()) || type.isInterface()) {
                    diagnostics.add(annotation.value() + ": @Mod class is not constructible.");
                    continue;
                }
                try {
                    Constructor<?> constructor = type.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    IEventBus bus = new MomEventBus();
                    try (ModLoadingContextScope ignored = ModLoadingContextScope.enter(bus)) {
                        constructor.newInstance();
                    }
                    loaded.add(new LoadedMod(annotation.value(), bus));
                    System.out.println("[MOM/Forge] Constructed " + annotation.value() + " (" + type.getName() + ")");
                } catch (NoSuchMethodException ex) {
                    diagnostics.add(annotation.value() + ": only no-argument @Mod constructors are supported in this milestone.");
                } catch (ReflectiveOperationException | RuntimeException ex) {
                    diagnostics.add(annotation.value() + ": constructor failed: " + ex);
                }
            }
        } catch (IOException ex) {
            diagnostics.add(candidate.path().getFileName() + ": class scan failed: " + ex.getMessage());
        }
    }

    private static List<String> parseModIds(String toml) {
        List<String> ids = new ArrayList<>();
        Matcher blocks = MOD_BLOCK.matcher(toml);
        while (blocks.find()) {
            Matcher id = MOD_ID.matcher(blocks.group(1));
            if (id.find()) ids.add(id.group(1));
        }
        return ids;
    }

    private record ModJar(Path path, List<String> modIds) { }
    private record LoadedMod(String id, IEventBus bus) { }
}
