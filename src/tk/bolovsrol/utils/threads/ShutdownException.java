package tk.bolovsrol.utils.threads;

import tk.bolovsrol.utils.Spell;

/**
 * Выбрасывается, когда {@link Suspendable} не смог зашатдауниться самостоятельно
 * в течение отведённого таймаута.
 */
public class ShutdownException extends Exception {
    private final Suspendable suspendable;

    public ShutdownException(HaltableThread haltableThread) {
        super("Thread " + Spell.get(haltableThread.getName()) + " couldn't shut down within " + haltableThread.getShutdownTimeout() + " ms. Stack trace: " + Spell.get(haltableThread.getStackTrace()));
        this.suspendable = haltableThread;
    }

    public ShutdownException(String message, Suspendable suspendable) {
        super(message);
        this.suspendable = suspendable;
    }

    public ShutdownException(Suspendable suspendable) {
        super("Suspendable " + Spell.get(suspendable) + " failed to shut down.");
        this.suspendable = suspendable;
    }

    public Suspendable getSuspendable() {
        return suspendable;
    }
}
