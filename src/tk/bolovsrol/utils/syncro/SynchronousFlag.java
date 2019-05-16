package tk.bolovsrol.utils.syncro;

/** Флажок, который можно устанавливать и который можно забирать, сбрасывая. */
public class SynchronousFlag {
    private volatile boolean raised = false;
    private final Object lock = new Object();

    /**
     * Создаёт флаг в сброшенном состоянии.
     */
    public SynchronousFlag() {
        this(false);
    }

    /**
     * Создаёт флаг, изначальное состояние флага определяется параметром.
     *
     * @param raised поднят ли флаг изначально
     */
    public SynchronousFlag(boolean raised) {
        this.raised = raised;
    }

    /** Устанавливает флаг, если он ещё не установлен. */
    public void raise() {
        synchronized (lock) {
            raised = true;
            lock.notifyAll();
        }
    }

    /** Возвращает true, если флаг поднят, иначе false. */
    public boolean isRaised() {
        synchronized (lock) {
            return raised;
        }
    }

    /**
     * Сбрасывает флаг.
     *
     * @return прежнее состояние флага, true, если был поднят, иначе false
     */
    public boolean drop() {
        boolean result;
        synchronized (lock) {
            result = raised;
            raised = false;
        }
        return result;
    }

    /**
     * Блокирует выполнение треда до тех пор, когда флаг будет поднят.
     * Оставляет флаг в поднятом состоянии.
     *
     * @throws InterruptedException
     */
    public void waitForRise() throws InterruptedException {
        synchronized (lock) {
            while (!raised) {
                lock.wait();
            }
        }
    }

    /**
     * Блокирует выполнение треда до тех пор, когда флаг будет поднят.
     * Сбрасывает флаг.
     *
     * @throws InterruptedException
     */
    public void waitForRiseAndDrop() throws InterruptedException {
        synchronized (lock) {
            while (!raised) {
                lock.wait();
            }
            raised = false;
        }
    }

    /**
     * Ожидает поднятия флага либо истечения таймаута, что раньше.
     *
     * @param timeout
     * @return true, если поднят флаг, false, если истёк таймаут, а флаг так и не поднят
     * @throws InterruptedException
     */
    public boolean waitForRiseAndDrop(long timeout) throws InterruptedException {
        long timeToWake = System.currentTimeMillis() + timeout;
        synchronized (lock) {
            while (true) {
                if (raised) {
                    raised = false;
                    return true;
                }
                long timeToSleep = timeToWake - System.currentTimeMillis();
                if (timeToWake <= 0) {
                    return false;
                }
                lock.wait(timeToSleep);
            }
        }
    }
}
