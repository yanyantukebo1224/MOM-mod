package net.minecraftforge.common;

import dev.mom.fabric.forgecompat.MomEventBus;
import net.minecraftforge.eventbus.api.IEventBus;

/** Forge global event bus facade. Only the event-bus subset is currently implemented. */
public final class MinecraftForge {
    public static final IEventBus EVENT_BUS = new MomEventBus();

    private MinecraftForge() { }
}
