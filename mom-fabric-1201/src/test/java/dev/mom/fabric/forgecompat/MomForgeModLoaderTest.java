package dev.mom.fabric.forgecompat;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class MomForgeModLoaderTest {
    @TempDir Path temp;

    @Mod("momfixture")
    public static final class FixtureForgeMod {
        static int constructed;
        static int setupCalls;

        public FixtureForgeMod() {
            constructed++;
            IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
            bus.addListener(FMLCommonSetupEvent.class, event -> setupCalls++);
        }
    }

    @Test void discoversConstructsAndRunsCommonSetupFromForgeMetadataJar() throws Exception {
        FixtureForgeMod.constructed = 0;
        FixtureForgeMod.setupCalls = 0;
        Path jar = temp.resolve("fixture.jar");
        String classEntry = FixtureForgeMod.class.getName().replace('.', '/') + ".class";
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new ZipEntry("META-INF/mods.toml"));
            out.write("""
                    modLoader="javafml"
                    loaderVersion="[47,)"
                    [[mods]]
                    modId="momfixture"
                    version="1.0.0"
                    """.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
            out.putNextEntry(new ZipEntry(classEntry));
            try (var in = FixtureForgeMod.class.getResourceAsStream("/" + classEntry)) {
                assertNotNull(in);
                in.transferTo(out);
            }
            out.closeEntry();
        }

        MomForgeModLoader.LoadResult result = new MomForgeModLoader(temp).load();
        assertEquals(1, result.loadedMods());
        assertEquals(1, FixtureForgeMod.constructed);
        assertEquals(1, FixtureForgeMod.setupCalls);
    }
}
