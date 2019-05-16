package tk.bolovsrol.utils.threads;

import tk.bolovsrol.utils.log.Log;

import java.util.Date;

/**
 * Главный Выключатель включает, а затем выключает
 * все зарегистрировавшиеся объекты хором.
 * <p/>
 * Это просто статическая версия {@link Switch Sыключателя}.
 *
 * @see Switch
 */
public class MainSwitch {

    private static final Switch SWITCH = new Switch(Log.getInstance());

    private MainSwitch() {}

    /**
     * Регистрирует желающего запуститься и остановиться по команде.
     *
     * @param suspendable желающий
     * @see #on()
     * @see #off()
     */
    public static void register(Suspendable suspendable) {
        SWITCH.register(suspendable);
    }

    public static boolean unregister(Suspendable suspendable) {
        return SWITCH.unregister(suspendable);
    }

    /**
     * По очереди запускает зарегистрированные объекты.
     * <p/>
     * Возвращает управление синхронно.
     *
     * @see #off()
     */
    public static void on() {
        SWITCH.on();
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
    public static void off() throws InterruptedException {
        SWITCH.off();
    }

    /**
     * Возвращает дату крайнего переключения. Если переключений ещё не было, то null.
     *
     * @return дата переключения или null.
     */
    public static Date getSwitchDate() {
        return SWITCH.getSwitchDate();
    }
}
