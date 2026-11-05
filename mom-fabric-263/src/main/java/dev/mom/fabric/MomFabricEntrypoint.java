package dev.mom.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class MomFabricEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        var loader = FabricLoader.getInstance();
        System.out.println("[MOM] Bootstrap active on Fabric / Minecraft 26.3.");
        System.out.println("[MOM] Loaded Fabric mods: " + loader.getAllMods().size());
        System.out.println("[MOM] Forge/NeoForge execution bridges are not enabled yet.");
    }
}
