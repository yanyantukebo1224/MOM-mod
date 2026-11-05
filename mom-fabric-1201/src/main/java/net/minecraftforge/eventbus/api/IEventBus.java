package net.minecraftforge.eventbus.api;

import java.util.function.Consumer;

/** Supported MOM subset of Forge IEventBus. */
public interface IEventBus {
    void register(Object target);
    boolean post(Event event);

    <T extends Event> void addListener(Consumer<T> listener);

    <T extends Event> void addListener(Class<T> eventType, Consumer<T> listener);

    <T extends Event> void addListener(EventPriority priority, Class<T> eventType, Consumer<T> listener);

    <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled,
                                       Class<T> eventType, Consumer<T> listener);
}
