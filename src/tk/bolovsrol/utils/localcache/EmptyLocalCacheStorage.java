package tk.bolovsrol.utils.localcache;

import java.util.Collection;

/**
 * Вечно молодой, вечно пустой.
 *
 * @param <I>
 * @param <O>
 */
class EmptyLocalCacheStorage<I extends Comparable<? super I>, O> implements LocalCacheStorage<I, O> {

    @Override public void put(I id, O object) {
        // не хочу
    }

    @Override public void remove(I id) {
        // не буду
    }

    @Override public void removeAll(Collection<I> ids) {
        // ну вот ещё
    }

    @Override public O get(I id) {
        return null;
    }
}
