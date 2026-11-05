package dev.mom.analyzer;

import dev.mom.api.AnalysisReport;

import java.nio.file.Path;
import java.util.zip.ZipFile;

/** Command-line entry point for inspecting a mod JAR. */
public final class JarAnalysisCli {
    private JarAnalysisCli() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: gradle :mom-analyzer:run --args=\"<path-to-mod.jar>\"");
            System.exit(64);
        }
        Path path = Path.of(args[0]);
        AnalysisReport report = new JarAnalyzer().analyze(path);
        System.out.println("MOM static analysis: " + path.toAbsolutePath());
        System.out.println("Status: " + report.status());
        report.mods().forEach(mod -> {
            System.out.println("Mod: " + mod.id() + " " + mod.version() + " [" + mod.loader() + "]");
            System.out.println("Minecraft constraints: " + mod.minecraftConstraints());
            System.out.println("Entrypoints: " + mod.entrypoints());
            System.out.println("Mixin configs: " + mod.mixinConfigs());
        });
        try (ZipFile zip = new ZipFile(path.toFile())) {
            BytecodeSummary bytecode = new BytecodeInspector().inspect(zip);
            System.out.println("Class files scanned: " + bytecode.classCount());
            System.out.println("Maximum class major version: " + bytecode.maxClassMajorVersion());
            System.out.println("Mixin annotations: " + bytecode.mixinAnnotationCount());
            System.out.println("Referenced namespaces: " + bytecode.referencedNamespaces());
            System.out.println("Loader API references: " + bytecode.loaderApiReferences().size());
            bytecode.loaderApiReferences().stream().limit(30).forEach(ref -> System.out.println("  " + ref));
            bytecode.warnings().forEach(warning -> System.out.println("WARNING: " + warning));
        }
        report.findings().forEach(finding -> System.out.println("INFO: " + finding));
        report.errors().forEach(error -> System.err.println("ERROR: " + error));
        if (report.status() == dev.mom.api.CompatibilityStatus.UNSUPPORTED) System.exit(2);
    }
}
