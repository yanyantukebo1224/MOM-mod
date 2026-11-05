package net.minecraftforge.eventbus.api;

/** Minimal Forge-style event base class. */
public class Event {
    private final boolean cancelable;
    private boolean canceled;

    public Event() { this(false); }

    protected Event(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public boolean isCancelable() { return cancelable; }
    public boolean isCanceled() { return canceled; }

    public void setCanceled(boolean canceled) {
        if (!cancelable) throw new IllegalStateException("Event is not cancelable");
        this.canceled = canceled;
    }
}
