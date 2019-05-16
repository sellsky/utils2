package tk.bolovsrol.utils.threads;

import tk.bolovsrol.utils.QuitException;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.time.Duration;

/**
 * Квантовый тред повыполняется-повыполняется, да поспит.
 * <p>
 * Тред пишет в лог сообщения о том, что он жив, если в лог давно ни о чём не писали.
 * <p>
 * Достаточно заимплементить метод {@link #work()},
 * в котором выполнять периодическое действие.
 */
public abstract class IterationThread extends HaltableThread {

    public static final long DEFAULT_ITERATION_SLEEP = 3000L;
    public static final long DEFAULT_ERROR_SLEEP = 60000L;
    public static final long DEFAULT_ALIVE_NOTIFY_TIMEOUT = 300000L;
    public static final String DEFAULT_ALIVE_NOTIFY_MESSAGE = "Nothing to do.";

    protected LogDome log;
    protected boolean sleepBeforeFirstIteration;
    protected long iterationSleep;
    protected long errorSleep;
    protected long aliveNotifyTimeout;
    protected String aliveNotifyMessage;

    protected IterationThread(String name) {
        this(name, Log.getInstance());
    }

    protected IterationThread(String name, LogDome log) {
        this(name, log, false, null, null, null, null);
    }

    protected IterationThread(String name, LogDome log, boolean sleepBeforeFirstIteration, Duration iterationSleepOrNull, Duration errorSleepOrNull) {
        this(name, log, sleepBeforeFirstIteration, iterationSleepOrNull, errorSleepOrNull, null, null);
    }

    protected IterationThread(String name, LogDome log, boolean sleepBeforeFirstIteration, Duration iterationSleepOrNull, Duration errorSleepOrNull, Duration aliveNotifyTimeoutOrNull, String aliveNotifyMessageOrNull) {
        super(name);
        this.log = log;
        this.sleepBeforeFirstIteration = sleepBeforeFirstIteration;
        this.iterationSleep = retrieveDurationVar(log, iterationSleepOrNull, "iteration.sleep.ms", DEFAULT_ITERATION_SLEEP);
        this.errorSleep = retrieveDurationVar(log, errorSleepOrNull, "error.sleep.ms", DEFAULT_ERROR_SLEEP);
        this.aliveNotifyTimeout = retrieveDurationVar(log, aliveNotifyTimeoutOrNull, "alive.notify.timeout.ms", DEFAULT_ALIVE_NOTIFY_TIMEOUT);
        this.aliveNotifyMessage = retrieveStringVar(aliveNotifyMessageOrNull, "alive.notify.message", DEFAULT_ALIVE_NOTIFY_MESSAGE);
    }

    private static long retrieveDurationVar(LogDome log, Duration explicitValue, String confKey, long defaultValue) {
        if (explicitValue != null) {
            return explicitValue.getMillis();
        } else {
			return Cfg.getLong(confKey, defaultValue, log);
		}
	}

    private static String retrieveStringVar(String explicitValue, String confKey, String defaultValue) {
        if (explicitValue != null) {
            return explicitValue;
        } else {
            return Cfg.get(confKey, defaultValue);
        }
    }

    public LogDome getLog() {
        return log;
    }

    public void setLog(LogDome log) {
        this.log = log;
    }

    public long getIterationSleep() {
        return iterationSleep;
    }

    public void setIterationSleep(long iterationSleep) {
        this.iterationSleep = iterationSleep;
    }

    public long getErrorSleep() {
        return errorSleep;
    }

    public void setErrorSleep(long errorSleep) {
        this.errorSleep = errorSleep;
    }

    public long getAliveNotifyTimeout() {
        return aliveNotifyTimeout;
    }

    public void setAliveNotifyTimeout(long aliveNotifyTimeout) {
        this.aliveNotifyTimeout = aliveNotifyTimeout;
    }

    public String getAliveNotifyMessage() {
        return aliveNotifyMessage;
    }

    public void setAliveNotifyMessage(String aliveNotifyMessage) {
        this.aliveNotifyMessage = aliveNotifyMessage;
    }

    public boolean isSleepBeforeFirstIteration() {
        return sleepBeforeFirstIteration;
    }

    public void setSleepBeforeFirstIteration(boolean sleepBeforeFirstIteration) {
        this.sleepBeforeFirstIteration = sleepBeforeFirstIteration;
    }

    @Override
    public void run() {
        log.info(getName() + " started up.");
        try {
            if (iterationSleep <= 0L) {
                work();
            } else {
                if (sleepBeforeFirstIteration) {
                    sleepAfterIteration();
                }
                while (!isInterrupted()) {
                    try {
                        work();
                        notifyAlive();
                        sleepAfterIteration();
                    } catch (InterruptedException | QuitException e) {
                        throw e;
                    } catch (UnexpectedBehaviourException e) {
                        log.warning(e);
                        try {
                            sleepAfterError();
                        } catch (InterruptedException ee) {
                            throw new QuitException(ee);
                        }
                    } catch (Throwable e) {
                        log.exception(e);
                        try {
                            sleepAfterError();
                        } catch (InterruptedException ee) {
                            throw new QuitException(ee);
                        }
                    }
                }
            }
        } catch (QuitException e) {
            log.trace(e.getMessage());
        } catch (Throwable e) {
            log.exception(e);
        } finally {
            log.info(getName() + " stopped.");
        }
    }

    /**
     * Спит {@link #getErrorSleep()} мс после того, как выполнение work() завершилось ошибкой.
     *
     * @throws InterruptedException
     */
    protected void sleepAfterError() throws InterruptedException {
        Thread.sleep(errorSleep);
    }

    /**
     * Спит {@link #getIterationSleep()} мс после очередного штатного выполнения work().
     * <p>
     * Считается, что тред можно безопасно останавливать в это время.
     * Если тред прервать, он выкинет QuitException.
     *
     * @throws QuitException тред прерван
     */
    protected void sleepAfterIteration() throws QuitException {
        try {
            synchronized (this) {
                // it's ok if we wake up earlier
                //noinspection WaitNotInLoop,UnconditionalWait
                this.wait(iterationSleep);
            }
        } catch (InterruptedException e) {
            throw new QuitException(e);
        }
    }

    /**
     * Выполняет полезную работу.
     * <p>
     * Предполагается, что при выполнении полезной работы этот метод
     * напишет в лог много интересных слов.
     * <p>
     * А если работы не оказалось, то он промолчит.
     *
     * @throws InterruptedException
     */
    protected abstract void work() throws Exception;

    /** Уведомляет в лог о том, что сервис жив, даже если он ничего и не пишет. */
    protected void notifyAlive() {
        try {
            if (aliveNotifyTimeout > 0L && System.currentTimeMillis() - log.getLatestDate().getTime() > aliveNotifyTimeout) {
                log.info(aliveNotifyMessage);
            }
        } catch (NullPointerException e) {
            // сюда попадаем, если ещё в лог ничего не писали: log.getLatestDate() вернул null.
            // такая ситуация маловероятна, но мало ли.
            // раз сервис ничего не пишет, то и мы тут не будем выступать.
        }
    }

}
