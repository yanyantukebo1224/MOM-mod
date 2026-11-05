package dev.mom.fabric.forgecompat;

import net.minecraftforge.eventbus.api.IEventBus;

public final class ModLoadingContextScope implements AutoCloseable {
    private static final ThreadLocal<IEventBus> CURRENT = new ThreadLocal<>();

    private ModLoadingContextScope(IEventBus bus) { CURRENT.set(bus); }

    public static ModLoadingContextScope enter(IEventBus bus) {
        return new ModLoadingContextScope(bus);
    }

    public static IEventBus current() { return CURRENT.get(); }

    @Override public void close() { CURRENT.remove(); }
}
