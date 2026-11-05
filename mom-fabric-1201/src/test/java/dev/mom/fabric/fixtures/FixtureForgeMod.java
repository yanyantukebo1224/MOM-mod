package dev.mom.fabric.fixtures;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** A deliberately tiny Forge-API fixture used to verify the MOM compatibility slice. */
@Mod("momfixture")
public final class FixtureForgeMod {
    public static int constructed;
    public static int commonSetupCalls;

    public FixtureForgeMod() {
        constructed++;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(FMLCommonSetupEvent.class, this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        commonSetupCalls++;
    }
}
