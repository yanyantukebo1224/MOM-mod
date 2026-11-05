package dev.mom.fabric.forgecompat;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/** Small synchronous event bus implementing the explicitly supported Forge subset. */
public final class MomEventBus implements IEventBus {
    private record Handler(Class<? extends Event> eventType, EventPriority priority,
                           boolean receiveCanceled, long sequence, Consumer<Event> callback) { }

    private final CopyOnWriteArrayList<Handler> handlers = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public void register(Object target) {
        if (target == null) throw new NullPointerException("target");
        for (Class<?> type = target.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
                if (annotation == null) continue;
                if (method.getParameterCount() != 1 ||
                        !Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    throw new IllegalArgumentException("@SubscribeEvent method must accept exactly one Event: " + method);
                }
                @SuppressWarnings("unchecked")
                Class<? extends Event> eventType = (Class<? extends Event>) method.getParameterTypes()[0];
                method.setAccessible(true);
                Object receiver = Modifier.isStatic(method.getModifiers()) ? null : target;
                add(eventType, annotation.priority(), annotation.receiveCanceled(),
                        event -> invoke(method, receiver, event));
            }
        }
    }

    /**
     * Forge's generic Consumer overload erases its event type in bytecode.
     * Dispatching to the small set of lifecycle events and ignoring bridge casts
     * lets ordinary method references such as addListener(this::commonSetup) work.
     * Mods needing precise dispatch should use the typed overload.
     */
    @Override
    public <T extends Event> void addListener(Consumer<T> listener) {
        if (listener == null) throw new NullPointerException("listener");
        add(Event.class, EventPriority.NORMAL, false, event -> {
            try {
                @SuppressWarnings("unchecked") T typed = (T) event;
                listener.accept(typed);
            } catch (ClassCastException wrongEventType) {
                // The erased Consumer bridge rejects lifecycle events of another type.
            }
        });
    }

    @Override
    public <T extends Event> void addListener(Class<T> eventType, Consumer<T> listener) {
        addListener(EventPriority.NORMAL, false, eventType, listener);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, Class<T> eventType,
                                               Consumer<T> listener) {
        addListener(priority, false, eventType, listener);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled,
                                               Class<T> eventType, Consumer<T> listener) {
        if (eventType == null || listener == null || priority == null) throw new NullPointerException();
        add(eventType, priority, receiveCanceled, event -> listener.accept(eventType.cast(event)));
    }

    @Override
    public boolean post(Event event) {
        if (event == null) throw new NullPointerException("event");
        List<Handler> snapshot = new ArrayList<>(handlers);
        snapshot.sort(Comparator.comparing((Handler h) -> h.priority().ordinal())
                .thenComparingLong(Handler::sequence));
        for (Handler handler : snapshot) {
            if (!handler.eventType().isAssignableFrom(event.getClass())) continue;
            if (event.isCanceled() && !handler.receiveCanceled()) continue;
            handler.callback().accept(event);
        }
        return event.isCanceled();
    }

    private void add(Class<? extends Event> type, EventPriority priority, boolean receiveCanceled,
                     Consumer<Event> callback) {
        handlers.add(new Handler(type, priority, receiveCanceled, sequence.getAndIncrement(), callback));
    }

    private static void invoke(Method method, Object receiver, Event event) {
        try {
            method.invoke(receiver, event);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException("Forge-style event handler failed: " + method, cause);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Could not invoke Forge-style event handler: " + method, ex);
        }
    }
}
