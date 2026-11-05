package dev.mom.fabric;

import dev.mom.fabric.forgecompat.MomForgeModLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class MomFabricEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        var loader = FabricLoader.getInstance();
        System.out.println("[MOM] Fabric bootstrap active on Minecraft 1.20.1.");
        System.out.println("[MOM] Loaded Fabric mods: " + loader.getAllMods().size());
        var forgeDirectory = loader.getConfigDir().resolve("mom").resolve("forge-mods");
        System.out.println("[MOM] Scanning Forge compatibility directory: " + forgeDirectory);
        new MomForgeModLoader(forgeDirectory).load();
        System.out.println("[MOM] Supported Forge API subset: @Mod, IEventBus, @SubscribeEvent, common setup and load complete.");
        System.out.println("[MOM] This is an experimental subset, not full Forge compatibility.");
    }
}
