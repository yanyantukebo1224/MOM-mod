package dev.mom.analyzer;

import dev.mom.api.CompatibilityStatus;
import dev.mom.api.LoaderKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class JarAnalyzerTest {
    @TempDir Path temp;

    @Test void detectsFabricMetadataWithoutLoadingClasses() throws Exception {
        Path jar = temp.resolve("sample.jar");
        try (var out = new ZipOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new ZipEntry("fabric.mod.json"));
            out.write("{\"schemaVersion\":1,\"id\":\"sample\",\"version\":\"1.0\",\"entrypoints\":{\"main\":[\"sample.Main\"]}}".getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
            out.putNextEntry(new ZipEntry("sample/Main.class"));
            out.write(new byte[]{0, 1, 2});
            out.closeEntry();
        }
        var report = new JarAnalyzer().analyze(jar);
        assertEquals(CompatibilityStatus.ANALYZED, report.status());
        assertEquals("sample", report.mods().get(0).id());
        assertEquals(LoaderKind.FABRIC, report.mods().get(0).loader());
    }

    @Test void detectsForgeMetadata() throws Exception {
        Path jar = temp.resolve("forge-sample.jar");
        try (var out = new ZipOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new ZipEntry("META-INF/mods.toml"));
            String toml = """
                    [[mods]]
                    modId="sampleforge"
                    version="1.2.3"
                    [[dependencies.sampleforge]]
                    modId="minecraft"
                    versionRange="[1.20.1,1.21)"
                    """;
            out.write(toml.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
        var report = new JarAnalyzer().analyze(jar);
        assertEquals(CompatibilityStatus.ANALYZED, report.status());
        assertEquals("sampleforge", report.mods().get(0).id());
        assertEquals(LoaderKind.FORGE, report.mods().get(0).loader());
        assertEquals("[1.20.1,1.21)", report.mods().get(0).minecraftConstraints().get(0));
    }

    @Test void detectsNeoForgeMetadata() throws Exception {
        Path jar = temp.resolve("neo-sample.jar");
        try (var out = new ZipOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new ZipEntry("META-INF/neoforge.mods.toml"));
            String toml = """
                    [[mods]]
                    modId="sampleneo"
                    version="2.0.0"
                    """;
            out.write(toml.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
        var report = new JarAnalyzer().analyze(jar);
        assertEquals(CompatibilityStatus.ANALYZED, report.status());
        assertEquals("sampleneo", report.mods().get(0).id());
        assertEquals(LoaderKind.NEOFORGE, report.mods().get(0).loader());
    }

    @Test void rejectsNonJarInput() {
        var report = new JarAnalyzer().analyze(temp.resolve("not-a-jar.txt"));
        assertEquals(CompatibilityStatus.UNSUPPORTED, report.status());
    }
}
