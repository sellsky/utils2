package tk.bolovsrol.utils.threads;

/**
 * Объект, который может начинать свою деятельность и заканчивать её по команде.
 *
 * @see Suspendables
 */
public interface Suspendable {
    /** @return true, если объект ещё жив, иначе false. */
    boolean isAlive();

    /** Начать работу. */
    void start();

    /** Остановить работу. */
    void shutdown() throws InterruptedException, ShutdownException;

    /** @return название объекта для именования связанных тредов и т.п. */
    String getName();

}
