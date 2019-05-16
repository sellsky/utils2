package tk.bolovsrol.utils.syncro;

import tk.bolovsrol.utils.time.Duration;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Одни треды паркуются тут в ожидании изменения версии, другой (другие) изменяют версию, рапарковывая всех.
 * Для собственно остановки тредов использует {@link LockSupport}.
 */
public class VersionParking {

    /**
     * Хранилище запаркованных тредов.
     * Предполагается, что парковку используют несколько потоков, так что тупой перебор при распарковке потока выглядит оптимальным решением.
     */
    private final ConcurrentLinkedQueue<Thread> park = new ConcurrentLinkedQueue<>();

    /** Текущая версия. */
    private AtomicInteger version = new AtomicInteger(0);

    public VersionParking() {
    }

    /** @return текущая версия парковки. */
    public int getVersion() {
        return version.get();
    }

    /** Изменяет версию парковки и распарковывает все запаркованные треды. */
    public void nextVersion() {
        version.incrementAndGet();
        park.forEach(LockSupport::unpark);
    }

    /**
     * Возвращает управление, когда версия станет не version или тред прервут.
     * <p>
     * В первом случае возвращает новую версию, в последнем — выкидывает исключение.
     *
     * @param version версия, с которой нужно уйти
     * @return новая версия
     * @throws InterruptedException
     */
    public int park(int version) throws InterruptedException {
        Thread currentThread = Thread.currentThread();
        park.add(currentThread);
        try {
            while (this.version.get() == version) {
                LockSupport.park(this);
                if (currentThread.isInterrupted()) {
                    throw new InterruptedException();
                }
            }
        } finally {
            park.remove(currentThread);
        }
        return this.version.get();
    }

    /**
     * Возвращает управление, когда версия станет не version или наступит указанное время (в миллисекундах) или тред прервут.
     * <p>
     * В первых двух случаях возвращает актуальную версию, в последнем — выкидывает исключение.
     *
     * @param version версия, с которой нужно уйти
     * @param deadline время, после которого нужно вернуть управление.
     * @return новая версия
     * @throws InterruptedException
     */
    public int parkUntil(int version, long deadline) throws InterruptedException {
        Thread currentThread = Thread.currentThread();
        park.add(currentThread);
        try {
            while (this.version.get() == version && System.currentTimeMillis() < deadline) {
                LockSupport.parkUntil(this, deadline);
                if (currentThread.isInterrupted()) {
                    throw new InterruptedException();
                }
            }
        } finally {
            park.remove(currentThread);
        }
        return this.version.get();
    }

    /**
     * Возвращает управление, когда версия станет не version
     * или пройдёт указанное время (в миллисекундах) или тред прервут.
     * <p>
     * В первых двух случаях возвращает актуальную версию, в последнем — выкидывает исключение.
     *
     * @param version версия, с которой нужно уйти
     * @param millis максимальное время, через которое нужно вернуть управление
     * @return новая версия
     * @throws InterruptedException
     */
    public int parkMillis(int version, long millis) throws InterruptedException {
        return parkUntil(version, System.currentTimeMillis() + millis);
    }

    /**
     * Возвращает управление, когда версия станет не version
     * или пройдёт указанное время или тред прервут.
     * <p>
     * В первых двух случаях возвращает актуальную версию, в последнем — выкидывает исключение.
     *
     * @param version версия, с которой нужно уйти
     * @param duration максимальное время, через которое нужно вернуть управление
     * @return новая версия
     * @throws InterruptedException
     */
    public int parkDuration(int version, Duration duration) throws InterruptedException {
        return parkMillis(version, duration.getMillis());
    }

    /**
     * Метод для удобства. Паркует тред от версии к версии до тех пор,
     * когда переданный {@link Supplier} при очередном вызове возвратит не нул.
     *
     * @param supplier генератор результата
     * @return то, что сгенерировал суплаер
     * @throws InterruptedException
     */
    public <R> R parkWhileNull(Supplier<R> supplier) throws InterruptedException {
        int version = this.version.get();
        R result;
        while ((result = supplier.get()) == null) {
            version = park(version);
        }
        return result;
    }

    /**
     * Метод для удобства. Паркует тред от версии к версии до тех пор,
     * когда переданный {@link BooleanSupplier} при очередном вызове возвратит false.
     *
     * @param supplier генератор результата
     * @throws InterruptedException
     */
    public void parkWhile(BooleanSupplier supplier) throws InterruptedException {
        int version = this.version.get();
        while (supplier.getAsBoolean()) {
            version = park(version);
        }
    }
}
