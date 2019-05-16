package tk.bolovsrol.utils.syncro;

/**
 * Обеспечивает синхронизацию в порядке очерёдности регистрации
 * ключей для каждого из замков.
 * <p>
 * Так, пока не освободятся ключи, которые были запрошены, к примеру,
 * первым и вторым, третий и последующие ключи для того же замка будут ждать.
 * <p>
 * Замки сопоставляются по {@link Object#equals(Object) равенству}, не по идентичности.
 * <p>
 * Если необходимости разнести регистрацию и запирание нет, проще использовать {@link MultiReentrantLock}.
 *
 * @see UniQueuedSynchronizer
 * @see MultiReentrantLock
 */
public class LockQueuedSynchronizer<O> {

    private final WeakProvider<O, UniQueuedSynchronizer> unis = new WeakProvider<>(o -> new UniQueuedSynchronizer());

    /**
     * Создаёт новый ключ {@link QueuedKey} и регистрирует его
     * в очереди синхронизации указанного замка.
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
     * @param lock замок, по равенству которого выполняется синхронизация
     * @return ключ синхронизации
     * @see #enter(Object)
     * @see QueuedKey#synchronize()
     */
    public QueuedKey register(final O lock) {
        return unis.get(lock).register();
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
     * QueuedKey key = lockQueuedSynchronizer.enter(lock);
     * try {
     *     // ... критическая секция ...
     * } finally {
     *     key.release();
     * }
     * </pre>
     * либо используя автоматическое закрытие
     * <pre>
     * try (QueuedKey ignored = lockQueuedSynchronizer.enter(lock)) {
     *     // ... критическая секция ...
     * }
     * </pre>
     *
     * @param lock замок, по равенству которого выполняется синхронизация
     * @return ключ
     * @throws InterruptedException ожидание синхронизации прервано
     * @see #register(Object)
     */
    public QueuedKey enter(O lock) throws InterruptedException {
        return unis.get(lock).enter();
    }
}
