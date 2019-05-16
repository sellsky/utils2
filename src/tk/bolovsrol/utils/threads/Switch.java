package tk.bolovsrol.utils.threads;

import tk.bolovsrol.utils.log.LogDome;

import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

/**
 * Выключатель включает, а затем выключает
 * все зарегистрировавшиеся объекты хором.
 * <p/>
 * Такие объекты должны реализовать интерфейс {@link Suspendable}
 * (треды, например, можно пронаследовать от {@link HaltableThread})
 * и зарегистрироваться в {@link Switch#register(Suspendable)}.
 *
 * @see Suspendable
 * @see HaltableThread
 * @see #register(Suspendable)
 */
public class Switch {

    private final LogDome log;
    private final LinkedList<Suspendable> suspendables = new LinkedList<Suspendable>();
    private boolean isOn = false;
    private Date switchDate = null;

    public Switch(LogDome log) {
        this.log = log;
    }

    /**
     * Регистрирует желающего запуститься и остановиться по команде.
     *
     * @param suspendable желающий
     * @see #on()
     * @see #off()
     */
    public void register(Suspendable suspendable) {
        synchronized (suspendables) {
            suspendables.add(suspendable);
        }
    }

    public boolean unregister(Suspendable suspendable) {
        synchronized (suspendables) {
            return suspendables.remove(suspendable);
        }
    }

    /**
     * По очереди запускает зарегистрированные объекты.
     * <p/>
     * Возвращает управление синхронно.
     *
     * @see #off()
     */
    public void on() {
        if (isOn) {
            throw new IllegalStateException("Main Switch is already on.");
        }
        synchronized (suspendables) {
            for (Suspendable suspendable : suspendables) {
                if (!suspendable.isAlive()) {
                    suspendable.start();
                }
            }
        }
        switchDate = new Date();
        isOn = true;
    }

    /**
     * Останавливает зарегистрированные объекты все скопом, асинхронно.
     * <p/>
     * Возвращает управление, когда все модули остановлены.
     * <p/>
     * После этого метода все зарегистрированные модули разрегистрируются.
     *
     * @return объекты, которые не смогли остановиться
     * @throws InterruptedException
     * @see Suspendables#shutdown(Suspendable[])
     * @see #on()
     */
    public Map<Suspendable, Throwable> off() throws InterruptedException {
        if (!isOn) {
            throw new IllegalStateException("Main Switch is already off.");
        }
        isOn = false;
        switchDate = new Date();
        Suspendable[] victims;
        synchronized (suspendables) {
            victims = suspendables.toArray(new Suspendable[suspendables.size()]);
            suspendables.clear();
        }
        return Suspendables.shutdownAndLogSurvivors(log, victims);
    }

    /**
     * Возвращает дату крайнего переключения. Если переключений ещё не было, то null.
     *
     * @return дата переключения или null.
     */
    public Date getSwitchDate() {
        return switchDate;
    }
}