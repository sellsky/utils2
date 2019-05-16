package tk.bolovsrol.utils.localcache;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.syncro.VersionParking;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TimeUtils;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Смотрит за хардлинк-кэшами и грохает произвольные, когда становится мало доступной памяти.
 * <pre>
 * availableMemory = {@link Runtime#maxMemory()} - {@link Runtime#totalMemory()} + {@link Runtime#freeMemory()}
 * </pre>
 */
class WatchedLocalCacheWatcher extends Thread {

    public static WatchedLocalCacheWatcher newFromCfg() {
        long threshold = WatchedLocalCacheWatcher.parseThreshold(Cfg.get("localcache.watched.threshold"));
        Duration errorSleep = Cfg.getDuration("localcache.watched.errorSleep", new Duration(TimeUtils.MS_IN_SECOND), Log.getInstance());
        boolean trace = Cfg.getBoolean("localcache.watched.trace", false);
        Log.hint("WatchedLocalCache threshold " + threshold + " (" + Spell.getHuman(threshold) + ") bytes, error sleep " + Spell.get(errorSleep) + ", trace " + (trace ? "enabled" : "disabled"));
        return new WatchedLocalCacheWatcher(threshold, errorSleep, trace);
    }

    private static long parseThreshold(String src) {
        if (src != null && !src.isEmpty()) {
            char lastChar = src.charAt(src.length() - 1);
            if (Character.isDigit(lastChar)) {
                return Long.parseLong(src);
            }
            long base = Long.parseLong(src.substring(0, src.length() - 1));
            switch (lastChar) {
            case '%':
                return Runtime.getRuntime().maxMemory() * base / 100L;
            case 'K':
            case 'k':
                return base * 1024L;
            case 'M':
            case 'm':
                return base * 1024L * 1024;
            case 'G':
            case 'g':
                return base * 1024L * 1024 * 1024;
            default:
                Log.warning("Unexpected threshold value " + Spell.get(src) + " ignored, using default threshold");
            }
        }
        // default threshold: 1Gb or ¼ of maxMemory, what less
        return Math.min(1073741824L, Runtime.getRuntime().maxMemory() >> 2);
    }

    private final VersionParking parking = new VersionParking();
    private final ConcurrentLinkedQueue<WatchedLocalCacheStorage<?, ?>> primary = new ConcurrentLinkedQueue<>();
    private final Queue<WatchedLocalCacheStorage<?, ?>> secondary = new ArrayDeque<>();

    /** Пока доступно меньше памяти, чем тут указано (в байтах), то надо грохать кэши. */
    private final long threshold;
    private final Duration errorSleep;
    private final boolean trace;

    public WatchedLocalCacheWatcher(long threshold, Duration errorSleep, boolean trace) {
        super("LocalCacheWatcher");
        this.threshold = threshold;
        this.errorSleep = errorSleep;
        this.trace = trace;
        setDaemon(true);
        start();
    }

    @Override public void run() {
        try {
            while (!isInterrupted()) {
                int version = parking.getVersion();
                watch();
                parking.park(version);
            }
        } catch (InterruptedException e) {
            // quit
        }
    }

    private void watch() throws InterruptedException {
        Runtime r = Runtime.getRuntime();
        long availableMemory;
        while ((availableMemory = r.maxMemory() - r.totalMemory() + r.freeMemory()) < threshold) {
            do {
                WatchedLocalCacheStorage<?, ?> container = primary.poll();
                if (container == null) {
                    Log.warning("Memory low. LocalCache cleanup did not freed enough memory. Available " + Spell.getHuman(availableMemory) + " bytes. Will retry in " + Spell.get(errorSleep)
                        + " maxMem=" + Spell.getHuman(r.maxMemory()) + " totalMem=" + Spell.getHuman(r.totalMemory()) + " freeMem=" + Spell.getHuman(r.freeMemory())
                    );
                    Thread.sleep(errorSleep.getMillis());
                    break;
                } else {
                    if (trace) { Log.trace("Available memory " + Spell.getHuman(availableMemory) + " bytes. Dropping arbitrary cache"); }
                    container.reset();
                    secondary.add(container);
                    r.gc();
                }
            } while ((availableMemory = r.maxMemory() - r.totalMemory() + r.freeMemory()) < threshold);
            if (trace) { Log.trace("Available memory increased to " + Spell.getHuman(availableMemory) + " bytes. Well done."); }
            primary.addAll(secondary);
            secondary.clear();
        }
    }

    public void register(WatchedLocalCacheStorage wlcc) { primary.add(wlcc); }

    public void poke() {parking.nextVersion();}
}
