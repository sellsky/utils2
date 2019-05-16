package tk.bolovsrol.utils.syncro;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Обеспечивает синхронизацию в порядке очерёдности регистрации ключей.
 * <p>
 * Так, пока не освободятся ключи, которые были запрошены, к примеру,
 * первым и вторым, третий и последующие будут ждать.
 * <p>
 * Если необходимости разнести регистрацию и запирание нет, проще использовать {@link java.util.concurrent.locks.ReentrantLock}.
 *
 * @see LockQueuedSynchronizer
 * @see java.util.concurrent.locks.ReentrantLock
 */
public class UniQueuedSynchronizer {

    /** Собственно синхронизатор. */
    final VersionParking parking = new VersionParking();

    /** Самый позднозарегистрированный ключ или нул, зарегистрированных не закрытых ключей нет. */
    final AtomicReference<QueuedKey> tail = new AtomicReference<>();

    /**
     * Создаёт новый ключ {@link QueuedKey} и регистрирует его в очереди синхронизации.
     * <p>
     * Перед входом в критическую секцию нужно синхронизировать ключ
     * методом {@link QueuedKey#synchronize()},
     * а по завершению критической секции освободить ключ
     * методом {@link QueuedKey#release()}.
     * <p>
     * Например, в простейшем случае:
     * <pre>
     * QueuedKey key;
     * try {
     *     key = lockQueuedSynchronizer.register(lock);
     *     // ... некритический код ...
     *     key.synchronize();
     *     // ... критическая секция ...
     * } finally {
     *     key.release();
     * }
     * </pre>
     *
     * @return ключ синхронизации
     * @see #enter()
     * @see QueuedKey#synchronize()
     */
    public QueuedKey register() {
        QueuedKey key = new QueuedKey(this);
        key.head = tail.getAndSet(key);
        return key;
    }

    /**
     * Создаёт новый ключ {@link QueuedKey}
     * и немедленно выполняет синхронизацию. Два в одном!
     * <p>
     * Регистрирует ключ для синхронизации.
     * Возвращает управление в синхронизированном по ключу контексте.
     * После выхода из критической секции нужно освободить ключ
     * методом {@link QueuedKey#release()}.
     * <p>
     * Например, в простейшем случае:
     * <pre>
     * QueuedKey key = uniQueuedSynchronizer.enter();
     * try {
     *     // ... критическая секция ...
     * } finally {
     *     key.release();
     * }
     * </pre>
     *
     * @return ключ
     * @throws InterruptedException ожидание синхронизации прервано
     * @see #register()
     */
    public QueuedKey enter() throws InterruptedException {
        QueuedKey key = register();
        try {
            key.synchronize();
        } catch (InterruptedException e) {
            key.release();
            throw e;
        }
        return key;
    }

    /**
     * Интерфейс для {@link #perform()}.
     *
     * @param <E>
     */
    @FunctionalInterface public interface Performer<E extends Exception> {
        void perform() throws E;
    }

    /**
     * «Синтаксический сахар».
     * Дожидается синхронизации и вызывает {@link Performer#perform()} переданному объекту (один раз, да)
     * в синхронизированном контексте, после чего выходит из синхронизации.
     *
     * @param performer
     * @param <E>
     * @throws InterruptedException
     * @throws E
     */
    public <E extends Exception> void perform(Performer<E> performer) throws InterruptedException, E {
        try (QueuedKey ignored = enter()) {
            performer.perform();
        }
    }

    /**
     * Обрабатывает событие: ключ отпущен.
     *
     * @param queuedKey
     */
    void keyReleased(QueuedKey queuedKey) {
        parking.nextVersion();
        tail.compareAndSet(queuedKey, null);
    }

    /**
     * Ждём, когда ключ и все его предшественники будут отпущены или будут нулом.
     *
     * @param queuedKey
     * @throws InterruptedException
     */
    void waitForRelease(QueuedKey queuedKey) throws InterruptedException {
        while (queuedKey != null) {
            int version = parking.getVersion();
            while (!queuedKey.released) {
                version = parking.park(version);
            }
            queuedKey = queuedKey.head;
        }
    }

}
