package tk.bolovsrol.utils.primitive;

import tk.bolovsrol.utils.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Function;

/**
 * Куцая карта только для чтения, хранящая список отсортированных ключей и список значений в двух массивах.
 * <p>
 * Быстренько ищет значение бинарным поиском.
 *
 * @param <V>
 */
public class ImmutableLongObjectSortedMap<V> {

    public static final ImmutableLongObjectSortedMap EMPTY = new ImmutableLongObjectSortedMap(new long[0], new Object[0]);

    private static class Entry<V> {
        final long key;
        final V value;

        public Entry(long key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final long[] keys;
    private final V[] values;

    protected ImmutableLongObjectSortedMap(long[] keys, V[] values) {
        this.keys = keys;
        this.values = values;
    }

    public boolean containsKey(long key) {
        return Arrays.binarySearch(keys, key) >= 0;
    }

    public V get(long key) {
        int i = Arrays.binarySearch(keys, key);
        return i >= 0 ? values[i] : null;
    }

    public V getFloor(long key) {
        int i = Arrays.binarySearch(keys, key);
        if (i >= 0) {
            return values[i];
        } else if (i == -1) { // перед первым вхождением
            return null;
        } else {
            return values[-i - 2];
        }
    }

    public boolean isEmpty() {
        return keys.length == 0;
    }

    public V[] values() {
        return values;
    }

    public long[] keys() {
        return keys;
    }


    // -------- фабрики
    @SuppressWarnings("unchecked") public static <V> ImmutableLongObjectSortedMap<V> empty() {
        return (ImmutableLongObjectSortedMap<V>) EMPTY;
    }

    @SuppressWarnings("unchecked") public static <V> ImmutableLongObjectSortedMap<V> from(SortedMap<Long, ? extends V> source) {
        if (source == null) { return null; }
        if (source.comparator() != null) { return from((Map) source); }
        long[] keys = new long[source.size()];
        V[] values = (V[]) new Object[source.size()];
        int i = 0;
        for (Map.Entry<Long, ? extends V> entry : source.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
            i++;
        }
        return new ImmutableLongObjectSortedMap<>(keys, values);
    }

    @SuppressWarnings("unchecked") public static <V> ImmutableLongObjectSortedMap<V> from(Map<Long, ? extends V> source) {
        if (source == null) { return null; }
        Object[] buf = new Object[source.size()];
        int i = 0;
        for (Map.Entry<Long, ? extends V> entry : source.entrySet()) {
            buf[i] = new Entry<>(entry.getKey(), entry.getValue());
            i++;
        }
        return sortAndCreateNew(buf);
    }

    public static <V, T> ImmutableLongObjectSortedMap<V> from(Collection<? extends T> source, ObjectToLongFunction<? super T> keyFunction, Function<? super T, V> valueFunction) {
        if (source == null) { return null; }
        Object[] buf = new Object[source.size()];
        int i = 0;
        for (T t : source) {
            buf[i] = new Entry<>(keyFunction.apply(t), valueFunction.apply(t));
            i++;
        }
        return sortAndCreateNew(buf);
    }

    @SuppressWarnings("unchecked") @NotNull
    private static <V> ImmutableLongObjectSortedMap<V> sortAndCreateNew(Object[] buf) {
        Arrays.parallelSort(buf, Comparator.comparingLong(o -> ((Entry<V>) o).key));
        long[] keys = new long[buf.length];
        for (int i = buf.length - 1; i >= 0; i--) {
            Entry<V> entry = (Entry<V>) buf[i];
            keys[i] = entry.key;
            buf[i] = entry.value;
        }
        return new ImmutableLongObjectSortedMap<>(keys, (V[]) buf);
    }
}
