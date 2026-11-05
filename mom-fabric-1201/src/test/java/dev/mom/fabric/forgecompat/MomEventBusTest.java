package dev.mom.fabric.forgecompat;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MomEventBusTest {
    static final class Subscriber {
        final List<String> calls = new ArrayList<>();
        @SubscribeEvent(priority = EventPriority.HIGH)
        private void setup(FMLCommonSetupEvent event) { calls.add("setup"); }
    }

    static final class CancelableEvent extends Event {
        CancelableEvent() { super(true); }
    }

    @Test void invokesAnnotatedSubscribers() {
        MomEventBus bus = new MomEventBus();
        Subscriber subscriber = new Subscriber();
        bus.register(subscriber);
        assertFalse(bus.post(new FMLCommonSetupEvent()));
        assertEquals(List.of("setup"), subscriber.calls);
    }

    @Test void dispatchesTypedConsumers() {
        MomEventBus bus = new MomEventBus();
        int[] calls = {0};
        bus.addListener(EventPriority.NORMAL, FMLCommonSetupEvent.class, event -> calls[0]++);
        bus.post(new FMLCommonSetupEvent());
        assertEquals(1, calls[0]);
    }

    @Test void dispatchesForgeStyleMethodReferencesWithErasedConsumerType() {
        MomEventBus bus = new MomEventBus();
        Subscriber subscriber = new Subscriber();
        bus.addListener(subscriber::setup);
        bus.post(new FMLCommonSetupEvent());
        assertEquals(List.of("setup"), subscriber.calls);
    }

    @Test void skipsListenersWhenCanceledUnlessOptedIn() {
        MomEventBus bus = new MomEventBus();
        int[] calls = {0};
        bus.addListener(EventPriority.NORMAL, false, CancelableEvent.class, event -> calls[0]++);
        bus.addListener(EventPriority.LOW, true, CancelableEvent.class, event -> calls[0]++);
        CancelableEvent event = new CancelableEvent();
        event.setCanceled(true);
        assertTrue(bus.post(event));
        assertEquals(1, calls[0]);
    }

    @Test void commonSetupEnqueueWorkCompletesSynchronously() {
        int[] calls = {0};
        new FMLCommonSetupEvent().enqueueWork(() -> calls[0]++).join();
        assertEquals(1, calls[0]);
    }
}
