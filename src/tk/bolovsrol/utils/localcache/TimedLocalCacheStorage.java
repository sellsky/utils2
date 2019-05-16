package tk.bolovsrol.utils.localcache;

import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Хранилище, забывающее установленное ему значение по истечении заданного таймаута.
 *
 * @param <I>
 * @param <O>
 */
public class TimedLocalCacheStorage<I extends Comparable<? super I>, O> implements LocalCacheStorage<I, O> {

    private static final Timer TIMER = new Timer("TimedLocalCacheStorageExpirator", true);

    /** Хранилище данных. */
    private ConcurrentMap<I, O> data = new ConcurrentHashMap<>(64, 0.9f, 16);

    private final long timeout;

    private class Killer extends TimerTask {
        private final I id;
        private final O value;

        public Killer(I id, O value) {
            this.id = id;
            this.value = value;
        }

        @Override public void run() {
            data.remove(id, value);
        }
    }

    public TimedLocalCacheStorage(long timeout) {
        this.timeout = timeout;
    }

    @Override public void put(I id, O object) {
        data.put(id, object);
        TIMER.schedule(new Killer(id, object), new Date(System.currentTimeMillis() + timeout));
    }

    @Override public void remove(I id) {
        data.remove(id);
    }

    @Override public void removeAll(Collection<I> ids) {
        data.keySet().removeAll(ids);
    }

    @Override public O get(I id) {
        return data.get(id);
    }

}
