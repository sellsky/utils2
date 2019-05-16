package tk.bolovsrol.utils.threads;

import tk.bolovsrol.utils.QuitException;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.syncro.VersionParking;

/**
 * Тред, выполняющий работу, когда его пнут.
 * <p>
 * В наследнике достаточно заимплементить метод {@link #work()},
 * в котором выполнять периодическое действие.
 * <p>
 * Также нужен внешний активный фактор, который будет пинать тред, вызывая {@link #poke()}, дабы инициировать итерацию работы.
 */
public abstract class PokeableThread extends HaltableThread implements Pokeable {

    protected final VersionParking vp;
    private volatile int version;

    protected LogDome log;

    protected PokeableThread(String name, LogDome log) {
        this(name, log, new VersionParking());
    }

    protected PokeableThread(String name, LogDome log, VersionParking vp) {
        super(name);
        this.vp = vp;
        this.log = log;
        this.version = vp.getVersion();
    }

    protected PokeableThread(String name, long shutdownTimeout, LogDome log) {
        this(name, shutdownTimeout, log, new VersionParking());
    }

    protected PokeableThread(String name, long shutdownTimeout, LogDome log, VersionParking vp) {
        super(name, shutdownTimeout);
        this.vp = vp;
        this.log = log;
        this.version = vp.getVersion();
    }

    public LogDome getLog() {
        return log;
    }

    public void setLog(LogDome log) {
        this.log = log;
    }

    @Override public void run() {
        try {
            while (!isInterrupted()) {
                try {
                    version = park(version);
                } catch (InterruptedException e) {
                    throw new QuitException(e);
                }
                try {
                    work();
                } catch (InterruptedException | QuitException e) {
                    throw e;
                } catch (UnexpectedBehaviourException e) {
                    log.warning(e);
                } catch (Throwable e) {
                    log.exception(e);
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
     * Парковка треда до следующей версии. В наследниках можно перегрузить, добавив ещё какие-нибудь условия.
     *
     * @param version текущая версия
     * @return новая версия
     * @throws InterruptedException
     */
    protected int park(int version) throws InterruptedException {
        return vp.park(version);
    }

    /**
     * Будит тред и вызывает его метод {@link #work()}.
     * Если тред сейчас активен, то он выполнит ещё одну итерацию, как только закончит текущую.
     */
    @Override public void poke() {
        vp.nextVersion();
    }

    /**
     * Выполняет полезную работу.
     * <p>
     * Предполагается, что при выполнении полезной работы этот метод
     * напишет в лог много интересных слов.
     * <p>
     * А если работы не оказалось, то он промолчит.
     * <p>
     * Метод может выкинуть исключение, оно будет записано в лог, на чём выполнение итерации завершится.
     *
     * @throws InterruptedException
     * @see #poke()
     */
    protected abstract void work() throws Exception;

}
