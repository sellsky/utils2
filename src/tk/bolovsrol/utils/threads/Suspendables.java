package tk.bolovsrol.utils.threads;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.time.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** Инструмент для работы с {@link Suspendable}-объектами. */
public final class Suspendables {

	public static final boolean LOG_SHUTDOWN = Cfg.getBoolean("log.shutdown", false);
	public static final Duration LOG_SHUTDOWN_INTERVAL = Cfg.getDuration("log.shutdown.interval", new Duration(10000L), Log.getInstance());

    private Suspendables() {
    }

    /**
     * Останавливает переданные {@link Suspendable} и записывает в лог варнинги об объектах, которые не смогли умереть.
     * <p/>
     * Элементы массива могут быть нуллами, такие элементы будут проигнорированы.
     *
     * @param log
     * @param threads
     * @throws InterruptedException
     * @see #shutdownAndLogSurvivors(tk.bolovsrol.utils.log.LogDome, java.util.Collection)
     * @see #shutdown(Suspendable[])
     */
    public static Map<Suspendable, Throwable> shutdownAndLogSurvivors(LogDome log, Suspendable... threads) throws InterruptedException {
        return shutdownAndLogSurvivors(log, Arrays.asList(threads));
    }

    /**
     * Останавливает переданные {@link Suspendable} и записывает в лог варнинги об объектах, которые не смогли умереть.
     * <p/>
     * Элементы коллекции могут быть нуллами, такие элементы будут проигнорированы.
     *
     * @param log
     * @param threads
     * @throws InterruptedException
     * @see #shutdownAndLogSurvivors(tk.bolovsrol.utils.log.LogDome, Suspendable...)
     * @see #shutdown(java.util.Collection)
     */
    public static Map<Suspendable, Throwable> shutdownAndLogSurvivors(LogDome log, Collection<? extends Suspendable> threads) throws InterruptedException {
        Map<Suspendable, Throwable> survivors = shutdown(threads);
        if (survivors != null && !survivors.isEmpty()) {
            for (Map.Entry<Suspendable, Throwable> entry : survivors.entrySet()) {
                if (entry != null) {
                    Suspendable survivor = entry.getKey();
                    Throwable exception = entry.getValue();
                    log.warning("Suspendable " + Spell.get(survivor.getName()) + " failed to shut down within timeout. ", exception);
                }
            }
        }
        return survivors;
    }

    /**
     * Асинхронно останавливает все {@link Suspendable} и возвращает управление,
     * когда все они остановятся или не смогут остановиться.
     * <p/>
     * Все объекты останавливаются параллельно.
     * <p/>
     * Возвращает массив объектов, которые не умерли, и исключение, с которым они не умерли.
     * <p/>
     * Элементы массива могут быть нуллами, такие элементы будут проигнорированы.
     *
     * @param suspendables
     * @see #shutdown(java.util.Collection)
     * @see #shutdownAndLogSurvivors(tk.bolovsrol.utils.log.LogDome, Suspendable...)
     */
    public static Map<Suspendable, Throwable> shutdown(Suspendable... suspendables) throws InterruptedException {
        return shutdown(Arrays.asList(suspendables));
    }

    /**
     * Останавливает переданные {@link Suspendable} и возвращает управление,
     * когда все они остановятся или не смогут остановиться.
     * <p/>
     * Переданные объекты останавливаются параллельно.
     * <p/>
     * Возвращает массив объектов, которые не умерли, и исключение, с которым они не умерли.
     * <p/>
     * Элементы коллекции могут быть нуллами, такие элементы будут проигнорированы.
     *
     * @param suspendables
     * @see #shutdown(Suspendable...)
     * @see #shutdownAndLogSurvivors(tk.bolovsrol.utils.log.LogDome, java.util.Collection)
     */
    public static Map<Suspendable, Throwable> shutdown(Collection<? extends Suspendable> suspendables) throws InterruptedException {
        if (suspendables == null || suspendables.isEmpty()) {
            return null;
//        } else if (suspendables.length == 1) {
//            try {
//                if (suspendables[0] != null) {
//                    if (suspendables[0].isAlive()) {
//                        suspendables[0].shutdown();
//                    }
//                }
//                return null;
//            } catch (ShutdownException e) {
//                return Collections.singletonMap(suspendables[0], (Exception) e);
//            }
        } else {
            List<Suspendable> victims = new ArrayList<>(suspendables.size());
            for (Suspendable thread : suspendables) {
                if (thread != null && thread.isAlive()) {
                    victims.add(thread);
                }
            }
            if (!victims.isEmpty()) {
                SerialKiller serialKiller = new SerialKiller(victims);
                serialKiller.murder();
                return serialKiller.getSurvivors();
            }
            return null;
        }
    }

    private static class SerialKiller {
        private final Object lock = new Object();
        private final List<Suspendable> victims;
        private List<Suspendable> killed = null;
        @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
        private Map<Suspendable, Throwable> survivors = null;

        public SerialKiller(List<Suspendable> threads) {
            this.victims = threads;
        }

        public void murder() throws InterruptedException {
            if (LOG_SHUTDOWN) {
                Log.trace("Killing suspendables " + Spell.get(victims));
            }
            synchronized (lock) {
                for (Suspendable victim : victims) {
                    new CarefulDeath(victim, this).start();
                }
                if (LOG_SHUTDOWN) {
                    while (!victims.isEmpty()) {
                        lock.wait(LOG_SHUTDOWN_INTERVAL.getMillis());
                        Log.trace("Shutdown progress follows");
                        for (Suspendable victim : victims) {
                            Log.trace(victim.getName() + ": isAlive=" + victim.isAlive());
                            if (victim instanceof Thread) {
                                Log.trace(Arrays.toString(((Thread) victim).getStackTrace()));
                            }
                        }
                    }
                } else {
                    while (!victims.isEmpty()) {
                        lock.wait();
                    }
                }
            }
            if (LOG_SHUTDOWN) {
                Log.trace("Killing complete.");
            }
        }

        public void killed(Suspendable victim) {
            synchronized (lock) {
                victims.remove(victim);
                if (killed == null) {
                    killed = new LinkedList<>();
                }
                killed.add(victim);
                lock.notifyAll();
            }
        }

        public void notKilled(Suspendable survivor, Throwable e) {
            synchronized (lock) {
                victims.remove(survivor);
                if (survivors == null) {
                    survivors = new LinkedHashMap<>();
                }
                survivors.put(survivor, e);
                lock.notifyAll();
            }
        }

        public Map<Suspendable, Throwable> getSurvivors() {
            return survivors;
        }
    }

    private static class CarefulDeath extends Thread {
        private final Suspendable victim;
        private final SerialKiller master;

        public CarefulDeath(Suspendable victim, SerialKiller master) {
            super("CarefulDeath-" + victim.getName());
            this.victim = victim;
            this.master = master;
        }

        @Override public void run() {
            try {
                victim.shutdown();
                master.killed(victim);
            } catch (Throwable e) {
                master.notKilled(victim, e);
            }
        }
    }

}
