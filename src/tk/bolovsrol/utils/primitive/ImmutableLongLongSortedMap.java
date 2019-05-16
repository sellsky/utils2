package tk.bolovsrol.utils.primitive;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;

/**
 * Куцая карта только для чтения, хранящая список отсортированных ключей и список значений в двух массивах.
 * <p>
 * Быстренько ищет значение бинарным поиском.
 *
 * @param
 */
public class ImmutableLongLongSortedMap {

    public static final ImmutableLongLongSortedMap EMPTY = new ImmutableLongLongSortedMap(new long[0], new long[0]);

    private static class Entry {
        final long key;
        final long value;

        public Entry(long key, long value) {
            this.key = key;
            this.value = value;
        }
    }

    private final long[] keys;
    private final long[] values;

    protected ImmutableLongLongSortedMap(long[] keys, long[] values) {
        this.keys = keys;
        this.values = values;
    }

    public boolean containsKey(long key) {
        return Arrays.binarySearch(keys, key) >= 0;
    }

    public Long get(long key) {
        int i = Arrays.binarySearch(keys, key);
        return i >= 0 ? Long.valueOf(values[i]) : null;
    }

    public long get(long key, long defaultValue) {
        int i = Arrays.binarySearch(keys, key);
        return i >= 0 ? values[i] : defaultValue;
    }

    public Long getFloor(long key) {
        int i = Arrays.binarySearch(keys, key);
        if (i >= 0) {
            return Long.valueOf(values[i]);
        } else if (i == -1) { // перед первым вхождением
            return null;
        } else {
            return Long.valueOf(values[-i - 2]);
        }
    }

    public long getFloor(long key, long defaultValue) {
        int i = Arrays.binarySearch(keys, key);
        if (i >= 0) {
            return values[i];
        } else if (i == -1) { // перед первым вхождением
            return defaultValue;
        } else {
            return values[-i - 2];
        }
    }

    public boolean isEmpty() {
        return keys.length == 0;
    }

    public long[] values() {
        return values;
    }

    public long[] keys() {
        return keys;
    }


    // -------- фабрики
    @SuppressWarnings("unchecked") public static ImmutableLongLongSortedMap empty() {
        return EMPTY;
    }

    @SuppressWarnings("unchecked") public static ImmutableLongLongSortedMap from(SortedMap<Long, Long> source) {
        if (source == null) { return null; }
        if (source.comparator() != null) { return from((Map) source); }
        long[] keys = new long[source.size()];
        long[] values = new long[source.size()];
        int i = 0;
        for (Map.Entry<Long, Long> entry : source.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
            i++;
        }
        return new ImmutableLongLongSortedMap(keys, values);
    }

    @SuppressWarnings("unchecked") public static ImmutableLongLongSortedMap from(Map<Long, Long> source) {
        if (source == null) { return null; }
        Object[] buf = new Object[source.size()];
        int i = 0;
        for (Map.Entry<Long, Long> entry : source.entrySet()) {
            buf[i] = new Entry(entry.getKey(), entry.getValue());
            i++;
        }
        return sortAndCreateNew(buf);
    }

    public static <T> ImmutableLongLongSortedMap from(Collection<? extends T> source, ObjectToLongFunction<? super T> keyFunction, ObjectToLongFunction<? super T> valueFunction) {
        if (source == null) { return null; }
        Object[] buf = new Object[source.size()];
        int i = 0;
        for (T t : source) {
            buf[i] = new Entry(keyFunction.apply(t), valueFunction.apply(t));
            i++;
        }
        return sortAndCreateNew(buf);
    }

//    public static ImmutableLongLongSortedMap from(LongLongMap source) {
//        if (source == null) { return null; }
//        Object[] buf = new Object[source.size()];
//        IntContainer index = new IntContainer(0);
//        source.forEachKeyValue((key, value) -> buf[index.getAndInc()] = new Entry(key, value));
//        return sortAndCreateNew(buf);
//    }

    @SuppressWarnings("unchecked") private static ImmutableLongLongSortedMap sortAndCreateNew(Object[] buf) {
        Arrays.parallelSort(buf, Comparator.comparingLong(o -> ((Entry) o).key));
        long[] keys = new long[buf.length];
        long[] values = new long[buf.length];
        for (int i = buf.length - 1; i >= 0; i--) {
            Entry entry = (Entry) buf[i];
            keys[i] = entry.key;
            values[i] = entry.value;
        }
        return new ImmutableLongLongSortedMap(keys, values);
    }

}
