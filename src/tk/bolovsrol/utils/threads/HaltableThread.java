package tk.bolovsrol.utils.threads;

import tk.bolovsrol.utils.QuitException;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.time.Duration;

/** Тред, который можно безопасно останавливать. */
public abstract class HaltableThread extends Thread implements Suspendable {

    public static final long DEFAULT_SHUTDOWN_TIMEOUT = 0L;

    /** Таймаут для остановки. Если тред не сдохнет в течение таймаута, будет выкинут ThreadShutdownException. */
	private long shutdownTimeout = Cfg.getLong("shutdown.timeout", DEFAULT_SHUTDOWN_TIMEOUT, Log.getInstance());

    /**
     * Создаёт тред с бесконечным таймаутом остановки.
     *
     * @see Thread#Thread()
     */
    protected HaltableThread() {
    }

    /**
     * Создаёт тред с бесконечным таймаутом остановки и указанным именем.
     *
     * @param name имя треда
     * @see Thread#Thread(String)
     */
    protected HaltableThread(String name) {
        super(name);
    }

    /**
     * Создаёт тред с указанным таймаутом остановки (мс).
     *
     * @param shutdownTimeout таймаут остановки
     * @see Thread#Thread()
     */
    protected HaltableThread(long shutdownTimeout) {
        setShutdownTimeout(shutdownTimeout);
    }

    /**
     * Создаёт тред с указанными таймаутом остановки (мс) и именем.
     *
     * @param name            имя треда
     * @param shutdownTimeout таймаут остановки
     * @see Thread#Thread(String)
     */
    protected HaltableThread(String name, long shutdownTimeout) {
        super(name);
        setShutdownTimeout(shutdownTimeout);
    }

    /**
     * Запоминаем таймаут остановки.
     *
     * @param shutdownTimeout
     */
    public final void setShutdownTimeout(long shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    /**
     * Аккуратно останавливает работу треда-объекта.
     * <p/>
     * Метод следует вызывать из контрольного треда.
     * <p/>
     * Метод передаёт своему объекту interrupt() и ждёт, что тред остановится
     * в течение заданного таймаута указанного при создании объекта времени.
     * Если тред не остановился, выкидывает {@link ShutdownException}.
     *
     * @throws InterruptedException ожидание смерти треда прервано извне.
     * @throws ShutdownException    тред не умер в течение таймаута.
     * @see #getShutdownTimeout()
     */
    @Override public void shutdown() throws InterruptedException, ShutdownException {
        synchronized (this) {
            this.interrupt();
            this.join(shutdownTimeout);
        }
        if (this.isAlive()) {
            throw new ShutdownException(this);
        }
    }

    /**
     * Возвращает величину таймаута (мс), в течение которого тред
     * должен остановиться, когда настанет такая необходимость.
     *
     * @return таймаут, мс
     * @see #shutdown()
     */
    public long getShutdownTimeout() {
        return shutdownTimeout;
    }

    /**
     * Делает штатную паузу в работе, а если в это время тред прервут, выкидывает QuitException.
     *
     * @param duration
     * @throws QuitException
     */
    protected void pause(Duration duration) throws QuitException {
        pause(duration.getMillis());
    }

    /**
     * Делает штатную паузу в работе, а если в это время тред прервут, выкидывает QuitException.
     *
     * @param durationMillis
     * @throws QuitException
     */
    protected void pause(long durationMillis) throws QuitException {
        try {
            Thread.sleep(durationMillis);
        } catch (InterruptedException e) {
            throw new QuitException(e);
        }
    }
}
