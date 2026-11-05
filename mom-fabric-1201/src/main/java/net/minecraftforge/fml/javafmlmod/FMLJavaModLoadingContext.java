package net.minecraftforge.fml.javafmlmod;

import dev.mom.fabric.forgecompat.ModLoadingContextScope;
import net.minecraftforge.eventbus.api.IEventBus;

/** Context available while MOM constructs a Forge-style @Mod class. */
public final class FMLJavaModLoadingContext {
    private static final FMLJavaModLoadingContext INSTANCE = new FMLJavaModLoadingContext();

    private FMLJavaModLoadingContext() { }

    public static FMLJavaModLoadingContext get() { return INSTANCE; }

    public IEventBus getModEventBus() {
        IEventBus bus = ModLoadingContextScope.current();
        if (bus == null) {
            throw new IllegalStateException("The mod event bus is only available during MOM mod construction");
        }
        return bus;
    }
}
