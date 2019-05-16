package tk.bolovsrol.utils.syncro;

import tk.bolovsrol.utils.function.ThrowingRunnable;
import tk.bolovsrol.utils.function.ThrowingSupplier;

import java.util.concurrent.locks.Lock;

/**
 * Одноразовое запирание замка {@link Lock}. Творческое развитие {@link tk.bolovsrol.utils.rezvyakov.Locker} с более благозвучным названием.
 * <p>
 * Например,
 * <pre>
 *     try (Locked locked = new Locked(lock)) { // ехал лок через лок :)
 *        ...
 *     }
 * </pre>
 * вместо
 * <pre>
 *     lock.lock();
 *     try {
 *         ...
 *     } finally {
 *         lock.unlock();
 *     }
 * </pre>
 * или, скажем,
 * <pre>
 *     Object result = Locked.call(readLock.lock(), () -> action.calculateResult(foo, bar));
 * </pre>
 * вместо
 * <pre>
 *     Object result;
 *     readWriteLock.readLock().lock();
 *     try {
 *         result = action.calculateResult(foo, bar);
 *     } finally {
 *         readWriteLock.readLock().unlock();
 *     }
 * </pre>
 */
public final class Locked implements AutoCloseable {

    private Lock lock;

    /**
     * Запирает переданный Lock.
     *
     * @param lock
     */
    public Locked(Lock lock) { (this.lock = lock).lock(); }

    /**
     * Отпирает замок. Можно вызывать несколько раз, повторные вызовы ничего не делают.
     *
     * @throws IllegalMonitorStateException
     */
    @Override public void close() throws IllegalMonitorStateException {
        if (this.lock != null) {
            this.lock.unlock();
            this.lock = null;
        }
    }

    /**
     * Выполняет переданное действие в залоченном контексте.
     *
     * @param lock лок
     * @param action действие
     */
    public static <E extends Throwable> void run(Lock lock, ThrowingRunnable<E> action) throws E {
        try (Locked l = new Locked(lock)) {
            action.run();
        }
    }

    /**
     * Выполняет переданное действие в залоченном контексте и возвращает результат.
     *
     * @param lock лок
     * @param action действие
     * @param <V> тип результата
     * @param <E> тип исключений
     * @return результат действия
     * @throws E исключение, выкинутое действием
     */
    public static <V, E extends Throwable> V call(Lock lock, ThrowingSupplier<V, E> action) throws E {
        try (Locked l = new Locked(lock)) {
            return action.get();
        }
    }

}