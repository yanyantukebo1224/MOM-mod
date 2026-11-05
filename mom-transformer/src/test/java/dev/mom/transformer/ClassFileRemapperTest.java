package dev.mom.transformer;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClassFileRemapperTest {
    @Test void remapsReferencedTypesAndDescriptors() {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "sample/Mod", null,
                "java/lang/Object", null);
        writer.visitField(Opcodes.ACC_PRIVATE, "bus",
                "Lnet/minecraftforge/eventbus/api/IEventBus;", null, null).visitEnd();
        writer.visitEnd();

        byte[] transformed = new ClassFileRemapper().remap(writer.toByteArray(), Map.of(
                "net/minecraftforge/eventbus/api/IEventBus", "dev/mom/bridge/event/EventBus"
        ));
        String[] name = new String[1];
        String[] descriptor = new String[1];
        new ClassReader(transformed).accept(new org.objectweb.asm.ClassVisitor(Opcodes.ASM9) {
            @Override public void visit(int version, int access, String className, String signature,
                                        String superName, String[] interfaces) {
                name[0] = className;
            }
            @Override public org.objectweb.asm.FieldVisitor visitField(int access, String fieldName,
                    String desc, String signature, Object value) {
                descriptor[0] = desc;
                return null;
            }
        }, 0);
        assertEquals("sample/Mod", name[0]);
        assertEquals("Ldev/mom/bridge/event/EventBus;", descriptor[0]);
    }
}
