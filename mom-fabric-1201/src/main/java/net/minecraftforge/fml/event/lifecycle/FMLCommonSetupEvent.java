package net.minecraftforge.fml.event.lifecycle;

import net.minecraftforge.eventbus.api.Event;

import java.util.concurrent.CompletableFuture;

/** MOM's synchronous common-setup lifecycle event. */
public class FMLCommonSetupEvent extends Event {
    /** Runs setup work immediately on MOM's synchronous lifecycle thread. */
    public CompletableFuture<Void> enqueueWork(Runnable work) {
        work.run();
        return CompletableFuture.completedFuture(null);
    }
}
