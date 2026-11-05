package dev.mom.analyzer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads class-file metadata without defining or executing classes.
 * This is a reference scanner, not a bytecode transformer or compatibility proof.
 */
public final class BytecodeInspector {
    private static final long MAX_CLASS_BYTES = 16L * 1024 * 1024;
    private static final long MAX_TOTAL_CLASS_BYTES = 128L * 1024 * 1024;

    public BytecodeSummary inspect(ZipFile zip) throws IOException {
        Set<String> namespaces = new TreeSet<>();
        Set<String> loaderRefs = new TreeSet<>();
        List<String> warnings = new ArrayList<>();
        int[] counts = new int[2]; // classes, mixin annotations
        int[] maxVersion = {0};
        long totalBytes = 0;

        var entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
            if (entry.getSize() > MAX_CLASS_BYTES) {
                warnings.add("Skipped oversized class: " + entry.getName());
                continue;
            }
            if (entry.getSize() > 0 && totalBytes + entry.getSize() > MAX_TOTAL_CLASS_BYTES) {
                warnings.add("Stopped scanning after the 128 MiB class-byte safety limit.");
                break;
            }
            byte[] bytes;
            try (var in = zip.getInputStream(entry)) {
                bytes = in.readNBytes((int) MAX_CLASS_BYTES + 1);
            }
            if (bytes.length > MAX_CLASS_BYTES) {
                warnings.add("Skipped oversized class: " + entry.getName());
                continue;
            }
            totalBytes += bytes.length;
            try {
                ClassReader reader = new ClassReader(bytes);
                maxVersion[0] = Math.max(maxVersion[0], reader.readShort(6));
                reader.accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override public void visit(int version, int access, String name, String signature,
                                                String superName, String[] interfaces) {
                        addInternal(name, namespaces, loaderRefs);
                        addInternal(superName, namespaces, loaderRefs);
                        if (interfaces != null) for (String itf : interfaces) addInternal(itf, namespaces, loaderRefs);
                    }

                    @Override public FieldVisitor visitField(int access, String name, String descriptor,
                                                              String signature, Object value) {
                        addDescriptor(descriptor, namespaces, loaderRefs);
                        if (value instanceof Type type) addType(type, namespaces, loaderRefs);
                        return null;
                    }

                    @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                                String signature, String[] exceptions) {
                        addDescriptor(descriptor, namespaces, loaderRefs);
                        if (exceptions != null) for (String ex : exceptions) addInternal(ex, namespaces, loaderRefs);
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override public void visitTypeInsn(int opcode, String type) {
                                addInternal(type, namespaces, loaderRefs);
                            }
                            @Override public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                                addInternal(owner, namespaces, loaderRefs);
                                addDescriptor(desc, namespaces, loaderRefs);
                            }
                            @Override public void visitMethodInsn(int opcode, String owner, String name,
                                                                  String desc, boolean isInterface) {
                                addInternal(owner, namespaces, loaderRefs);
                                addDescriptor(desc, namespaces, loaderRefs);
                            }
                            @Override public void visitMultiANewArrayInsn(String descriptor, int dimensions) {
                                addDescriptor(descriptor, namespaces, loaderRefs);
                            }
                            @Override public void visitLdcInsn(Object value) {
                                if (value instanceof Type type) addType(type, namespaces, loaderRefs);
                            }
                        };
                    }

                    @Override public org.objectweb.asm.AnnotationVisitor visitAnnotation(String descriptor,
                                                                                           boolean visible) {
                        if (descriptor.contains("/Mixin;")) counts[1]++;
                        addDescriptor(descriptor, namespaces, loaderRefs);
                        return null;
                    }
                }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                counts[0]++;
            } catch (IllegalArgumentException ex) {
                warnings.add("Could not parse class " + entry.getName() + ": " + ex.getMessage());
            }
        }
        return new BytecodeSummary(counts[0], maxVersion[0], counts[1], namespaces,
                List.copyOf(loaderRefs), warnings);
    }

    private static void addDescriptor(String descriptor, Set<String> namespaces, Set<String> loaderRefs) {
        if (descriptor == null || descriptor.isEmpty()) return;
        try {
            Type type = descriptor.startsWith("(") ? Type.getMethodType(descriptor) : Type.getType(descriptor);
            addType(type, namespaces, loaderRefs);
        } catch (IllegalArgumentException ignored) {
            // Generic signatures and malformed optional metadata are not descriptors.
        }
    }

    private static void addType(Type type, Set<String> namespaces, Set<String> loaderRefs) {
        switch (type.getSort()) {
            case Type.ARRAY -> addType(type.getElementType(), namespaces, loaderRefs);
            case Type.OBJECT -> addInternal(type.getInternalName(), namespaces, loaderRefs);
            case Type.METHOD -> {
                for (Type arg : type.getArgumentTypes()) addType(arg, namespaces, loaderRefs);
                addType(type.getReturnType(), namespaces, loaderRefs);
            }
            default -> { }
        }
    }

    private static void addInternal(String name, Set<String> namespaces, Set<String> loaderRefs) {
        if (name == null) return;
        if (name.startsWith("net/minecraft/")) namespaces.add("minecraft");
        if (name.startsWith("net/minecraftforge/")) { namespaces.add("forge"); loaderRefs.add(name.replace('/', '.')); }
        if (name.startsWith("net/neoforged/")) { namespaces.add("neoforge"); loaderRefs.add(name.replace('/', '.')); }
        if (name.startsWith("net/fabricmc/")) { namespaces.add("fabric"); loaderRefs.add(name.replace('/', '.')); }
        if (name.startsWith("org/spongepowered/asm/mixin/")) namespaces.add("mixin");
    }
}
