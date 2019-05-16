package tk.bolovsrol.utils.localcache;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Хранилище, основанное на охране памяти.
 * <p>
 * Все кэши при создании оказываются зарегистрированы у смотрителя,
 * который просыпается при увеличении размеров кэша и проверяет разницу между
 * {@link Runtime#maxMemory()} (доступная системе память, управляемая параметром  -Xmx джава-машины)
 * и {@link Runtime#totalMemory()} (аллоцированная память).
 * <p>
 * Если паяти остаётся меньше заданного, смотритель начинает грохать кэши целиком
 * в условно произвольном порядке, пока памяти не станет меньше порога.
 */
class WatchedLocalCacheStorage<I extends Comparable<? super I>, O> implements LocalCacheStorage<I, O> {

    private static final WatchedLocalCacheWatcher WATCHER = WatchedLocalCacheWatcher.newFromCfg();

    /** Хранилище данных. */
    private ConcurrentMap<I, O> data;

    public WatchedLocalCacheStorage() {
        reset();
        WATCHER.register(this);
    }

    public void reset() {
        data = new ConcurrentHashMap<>(64, 0.9f, 16);
    }

    @Override public void put(I id, O object) {
        data.put(id, object);
        WATCHER.poke();
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
