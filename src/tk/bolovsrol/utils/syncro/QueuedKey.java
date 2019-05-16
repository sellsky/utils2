package tk.bolovsrol.utils.syncro;

/** Ключ упорядоченной синхронизации. */
public class QueuedKey implements AutoCloseable {

    private final UniQueuedSynchronizer owner;

    /** Когда тут будет true, ключ станет свободен. */
    volatile boolean released = false;

    /** Когда все головы освободятся, наступит наша очередь. Нижестоящие не будут трогать нашу голову до тех пор, когда мы сделаемся released. */
    QueuedKey head;

    QueuedKey(UniQueuedSynchronizer sync) {
        this.owner = sync;
    }

    /**
     * Возвращает управление в синхронизированном по ключу контексте.
     * После выхода из критической секции нужно освободить ключ
     * методом {@link QueuedKey#release()}.
     * <p>
     * Если во время ожидания синхронизации тред {@link Thread#interrupt() прервут},
     * метод выкинет {@link InterruptedException}, а ключ останется в исходном
     * несинхронизированном состоянии. Такой ключ нужно синхронизировать
     * заново или освободить.
     * <p>
     * В простейшем случае:
     * <pre>
     * QueuedKey key = ...
     * try {
     *     key.synchronize();
     *     // ... критическая секция ...
     * } finally {
     *     key.release();
     * }
     * </pre>
     *
     * @throws InterruptedException тред прерван во время ожидания синхронизации
     */
    public void synchronize() throws InterruptedException {
        owner.waitForRelease(head);
    }

    /**
     * Освобождает ключ, чтобы следующие ключи тоже могли синхронизироваться.
     * <p>
     * Ключ одноразовый, после освобождения ни на что не годен, можно выкинуть.
     * <p>
     * Можно освободить ключ до синхронизации, очередь это не нарушит.
     */
    public void release() {
        released = true;
        owner.keyReleased(this);
    }

    /**
     * Освобождает ключ. Делает то же,, что {@link #release()},
     * просто такой метод хочет {@link java.lang.AutoCloseable}.
     *
     * @throws Exception
     */
    @Override public void close() {
        release();
    }
}
