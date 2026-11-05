package dev.mom.analyzer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

class BytecodeInspectorTest {
    @TempDir Path temp;

    @Test void detectsLoaderReferencesWithoutDefiningClasses() throws Exception {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "sample/Mod", null,
                "java/lang/Object", null);
        MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC, "register",
                "(Lnet/minecraftforge/eventbus/api/IEventBus;)V", null, null);
        method.visitCode();
        method.visitTypeInsn(Opcodes.NEW, "net/minecraftforge/common/MinecraftForge");
        method.visitInsn(Opcodes.DUP);
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraftforge/common/MinecraftForge",
                "<init>", "()V", false);
        method.visitInsn(Opcodes.POP);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(2, 2);
        method.visitEnd();
        writer.visitEnd();

        Path jar = temp.resolve("fixture.jar");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new ZipEntry("sample/Mod.class"));
            out.write(writer.toByteArray());
            out.closeEntry();
        }

        try (ZipFile zip = new ZipFile(jar.toFile())) {
            BytecodeSummary result = new BytecodeInspector().inspect(zip);
            assertEquals(1, result.classCount());
            assertTrue(result.referencedNamespaces().contains("forge"));
            assertTrue(result.loaderApiReferences().contains("net.minecraftforge.common.MinecraftForge"));
            assertTrue(result.warnings().isEmpty());
        }
    }
}
