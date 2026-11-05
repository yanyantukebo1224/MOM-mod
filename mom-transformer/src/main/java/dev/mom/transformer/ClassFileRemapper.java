package dev.mom.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.util.Map;
import java.util.Objects;

/**
 * Applies explicit internal-name remapping to class files.
 * It does not invent Forge/NeoForge compatibility mappings; callers must provide
 * mappings backed by a concrete MOM bridge implementation.
 */
public final class ClassFileRemapper {
    public byte[] remap(byte[] classFile, Map<String, String> internalNameMappings) {
        Objects.requireNonNull(classFile, "classFile");
        Objects.requireNonNull(internalNameMappings, "internalNameMappings");
        ClassReader reader = new ClassReader(classFile);
        ClassWriter writer = new ClassWriter(0);
        Remapper remapper = new Remapper() {
            @Override public String map(String internalName) {
                return internalNameMappings.getOrDefault(internalName, internalName);
            }
        };
        reader.accept(new ClassRemapper(writer, remapper), 0);
        return writer.toByteArray();
    }
}
