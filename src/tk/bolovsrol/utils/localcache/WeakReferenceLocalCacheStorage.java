package tk.bolovsrol.utils.localcache;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WeakReferenceLocalCacheStorage<I extends Comparable<? super I>, O> implements LocalCacheStorage<I, O> {

    /** Хранилище данных. */
    private final ConcurrentMap<I, CachedObjectReference<I, O>> data = new ConcurrentHashMap<>(64, 0.9f, 16);

    @Override public void put(I id, O o) {
        data.put(id, new CachedObjectReference<>(id, data, o, WeakReferenceCleaner.getInstance().getQueue()));
    }

    @Override public void remove(I id) {
        CachedObjectReference<I, O> sr = data.remove(id);
        if (sr != null) {
            sr.clear();
        }
    }

    @Override public void removeAll(Collection<I> ids) {
        for (I id : ids) {
            remove(id);
        }
    }

    @Override public O get(I id) {
        CachedObjectReference<I, O> sr = data.get(id);
        return sr == null ? null : sr.get();
    }
}
