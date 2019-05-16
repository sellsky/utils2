package tk.bolovsrol.utils.primitive;

import tk.bolovsrol.utils.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Function;

/**
 * Карта только для чтения, хранящая упорядоченный список ключей в одном массиве, а значений во втором.
 *
 * @param <V>
 */
public class ImmutableIntObjectSortedMap<V> {

    public static final ImmutableIntObjectSortedMap EMPTY = new ImmutableIntObjectSortedMap(new int[0], new Object[0]);

    private static class Entry<V> {
        final int key;
        final V value;

        public Entry(int key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int[] keys;
    private final V[] values;

    protected ImmutableIntObjectSortedMap(int[] keys, V[] values) {
        this.keys = keys;
        this.values = values;
    }

    public V get(int key) {
        int i = Arrays.binarySearch(keys, key);
        return i >= 0 ? values[i] : null;
    }

    public V getFloor(int key) {
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

    // -------- фабрики
    @SuppressWarnings("unchecked") public static <V> ImmutableIntObjectSortedMap<V> empty() {
        return (ImmutableIntObjectSortedMap<V>) EMPTY;
    }

    @SuppressWarnings("unchecked") public static <V> ImmutableIntObjectSortedMap<V> from(SortedMap<Integer, V> source) {
        if (source == null) { return null; }
        if (source.comparator() != null) { return from((Map) source); }
        int[] keys = new int[source.size()];
        V[] values = (V[]) new Object[source.size()];
        int i = 0;
        for (Map.Entry<Integer, V> entry : source.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
            i++;
        }
        return new ImmutableIntObjectSortedMap<>(keys, values);
    }

    @SuppressWarnings("unchecked") public static <V> ImmutableIntObjectSortedMap<V> from(Map<Integer, V> source) {
        if (source == null) { return null; }
        Object[] buf = new Object[source.size()];
        int i = 0;
        for (Map.Entry<Integer, V> entry : source.entrySet()) {
            buf[i] = new Entry<>(entry.getKey(), entry.getValue());
            i++;
        }
        return sortAndCreateNew(buf);
    }

    public static <V, T> ImmutableIntObjectSortedMap<V> from(Collection<? extends T> source, ObjectToIntFunction<? super T> keyFunction, Function<? super T, V> valueFunction) {
        if (source == null) { return null; }
        Object[] buf = new Object[source.size()];
        int i = 0;
        for (T t : source) {
            buf[i] = new Entry<>(keyFunction.apply(t), valueFunction.apply(t));
            i++;
        }
        return sortAndCreateNew(buf);
    }

    @SuppressWarnings("unchecked")
    private static <V> ImmutableIntObjectSortedMap<V> sortAndCreateNew(Object[] buf) {
        Arrays.parallelSort(buf, Comparator.comparingInt(o -> ((Entry<V>) o).key));
        int[] keys = new int[buf.length];
        for (int i = buf.length - 1; i >= 0; i--) {
            Entry<V> entry = (Entry<V>) buf[i];
            keys[i] = entry.key;
            buf[i] = entry.value;
        }
        return new ImmutableIntObjectSortedMap<>(keys, (V[]) buf);
    }
}