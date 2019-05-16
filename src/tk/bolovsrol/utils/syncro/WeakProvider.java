package tk.bolovsrol.utils.syncro;

import tk.bolovsrol.utils.log.Log;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * При необходимости создаёт объект, соответствующий переданному ключу, хранит его в слабой ссылке {@link WeakReference}
 * и отдаёт при предъявлении такого ключа.
 * <p>
 * Таким образом этот класс гарантирует, что для каждого ключа в каждый момент времени будет актуален только один объект.
 *
 * @param <K>
 * @param <V>
 */
public class WeakProvider<K, V> {

    /** Очередь для освободившихся ссылок на синхрозаторы. */
    private static final ReferenceQueue<Object> RQ = new ReferenceQueue<>();

    /** Чистилка карты синхрониаторов от освободившихся ссылок. */
    private static final Thread CLEANER = new Thread(() -> {
        try {
            while (true) {
                ((MappedWeakReference) RQ.remove()).discard();
            }
        } catch (InterruptedException ignored) {
            // ok
        } catch (Throwable e) {
            Log.exception(e);
        }
    }, "RegisteredWeakReferenceCleaner");

    static {
        WeakProvider.CLEANER.setDaemon(true);
        WeakProvider.CLEANER.start();
    }

    /** Слабая ссылка с юни-синхронизатором, которая умеет удалять себя из карты. */
    public static class MappedWeakReference<K, V> extends WeakReference<V> {
        private final K key;
        private final Map<K, MappedWeakReference<K, V>> map;

        public MappedWeakReference(K key, V value, Map<K, MappedWeakReference<K, V>> map) {
            super(value, RQ);
            this.key = key;
            this.map = map;
        }

        public void discard() {
            map.remove(key, this);
        }
    }

    private final ConcurrentMap<K, MappedWeakReference<K, V>> keyToRegisteredWeakReference = new ConcurrentHashMap<>();
    private final Function<K, V> generator;

    public WeakProvider(Function<K, V> generator) {
        this.generator = generator;
    }

    public V get(K key) {
        while (true) {
            MappedWeakReference<K, V> reference = keyToRegisteredWeakReference.computeIfAbsent(key, k -> new MappedWeakReference<K, V>(k, generator.apply(k), keyToRegisteredWeakReference));
            V value = reference.get();
            if (value == null) {
                // ссылка уже испортилась, но ещё не удалилась. Удалим, и всё заново.
                keyToRegisteredWeakReference.remove(key, reference);
            } else {
                return value;
            }
        }

    }

}
