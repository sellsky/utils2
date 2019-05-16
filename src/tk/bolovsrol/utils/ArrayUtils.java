package tk.bolovsrol.utils;

import tk.bolovsrol.utils.function.ThrowingBiConsumer;
import tk.bolovsrol.utils.function.ThrowingConsumer;
import tk.bolovsrol.utils.function.ThrowingFunction;
import tk.bolovsrol.utils.time.Duration;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/** Инструменты для преобразования массивов. */
public final class ArrayUtils {
    private ArrayUtils() {
    }

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final Duration[] EMPTY_DURATION_ARRAY = new Duration[0];

    /**
     * Добавляет элемент в конец массива.
     *
     * @param array
     * @param suffix
     * @return новый массив: {array[0], array[1]...array[array.length-1], suffix}
     */
    public static long[] appendSuffix(long[] array, long suffix) {
        long[] result = new long[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = suffix;
        return result;
    }

    /**
     * Добавляет элемент в начало массива.
     *
     * @param array
     * @param prefix
     * @return новый массив: {prefix, array[0], array[1] ... array[array.length-1]}
     */
    public static long[] appendPrefix(long[] array, long prefix) {
        long[] result = new long[array.length + 1];
        result[0] = prefix;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }

    /**
     * Склеивает массивы.
     *
     * @return новый массив, состоящий из последовательно склеенных переданных массивов
     */
    public static long[] join(long[]... longs) {
        int size = 0;
        for (long[] b : longs) {
            size += b.length;
        }
        long[] result = new long[size];
        int ptr = 0;
        for (long[] b : longs) {
            System.arraycopy(b, 0, result, ptr, b.length);
            ptr += b.length;
        }
        return result;
    }

    /**
     * Склеивает массивы.
     *
     * @return новый массив, состоящий из последовательно склеенных переданных массивов
     */
    public static byte[] join(byte[]... bytes) {
        int size = 0;
        for (byte[] b : bytes) {
            size += b.length;
        }
        byte[] result = new byte[size];
        int ptr = 0;
        for (byte[] b : bytes) {
            System.arraycopy(b, 0, result, ptr, b.length);
            ptr += b.length;
        }
        return result;
    }

    /**
     * Склеивает переданные массивы.
     * <p>
     * Если передан нул либо все переданные массивы нулы, возвращает нул.
     *
     * @return новый массив, состоящий из последовательно склеенных переданных массивов
     */
    @SuppressWarnings("unchecked") @SafeVarargs public static <T> T[] join(T[]... arrays) {
        if (arrays == null) { return null; }

        int size = 0;
        Class<?> componentType = null;
        for (T[] array : arrays) {
            if (array != null) {
                size += array.length;
                if (componentType == null) { componentType = array.getClass().getComponentType(); }
            }
        }

        if (componentType == null) { return null; }

        T[] result = (T[]) Array.newInstance(componentType, size);
        int ptr = 0;
        for (T[] array : arrays) {
            if (array != null) {
                System.arraycopy(array, 0, result, ptr, array.length);
                ptr += array.length;
            }
        }
        return result;
    }

    /**
     * К каждому массиву из <code>suffixes</code> приклеивает указанный <code>prefix</code>.
     * Возвращает получившееся в виде {@link List списка}.
     * <p>
     * Если <code>suffixes</code> нул, возвращает тоже нул.
     *
     * @param prefix префикс
     * @param suffixes коллекция суффиксов
     * @return суффиксы с приклеенным префиксом или нул
     */
    public static List<byte[]> prepend(byte[] prefix, Collection<byte[]> suffixes) {
        if (suffixes == null) {
            return null;
        }
        List<byte[]> targets = new ArrayList<>(suffixes.size());
        for (byte[] suffix : suffixes) {
            byte[] target = new byte[prefix.length + suffix.length];
            System.arraycopy(prefix, 0, target, 0, prefix.length);
            System.arraycopy(suffix, 0, target, prefix.length, suffix.length);
            targets.add(target);
        }
        return targets;
    }

    //------ Object[]

    /**
     * Добавляет массиву объект suffix в конец массива.
     * <p>
     * Тип нового массива будет таким же, как тип переданного массива.
     * Suffix должен быть совместимого класса.
     *
     * @param array
     * @param suffix
     * @return новый массив
     */
    public static Object[] appendSuffix(Object[] array, Object suffix) {
        Object[] result =
            (Object[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = suffix;
        return result;
    }

    /**
     * Добавляет массиву объект prefix в начало массива.
     * <p>
     * Тип нового массива будет таким же, как тип переданного массива.
     * Suffix должен быть совместимого класса.
     *
     * @param array
     * @param prefix
     * @return новый массив
     */
    public static Object[] appendPrefix(Object[] array, Object prefix) {
        Object[] result =
            (Object[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        result[0] = prefix;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }

    /**
     * Склеивает массивы first и last последовательно.
     * <p>
     * Тип нового массива будет таким же, как тип массива first.
     *
     * @param first
     * @param last
     * @return новый массив
     */
    public static Object[] append(Object[] first, Object[] last) {
        Object[] result =
            (Object[]) Array.newInstance(first.getClass().getComponentType(), first.length + last.length);
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(last, 0, result, first.length, last.length);
        return result;
    }


    public static boolean contains(final int[] array, final int key) {
        Arrays.sort(array);
        return Arrays.binarySearch(array, key) != -1;
    }

    /**
     * Перебирает все элементы массива, если один из них равен (через ==),
     * возвращает истину. Иначе ложь.
     *
     * @param haystack стог
     * @param needle иголка
     * @param <T>
     * @return true, если массив содержит искомый элемент, иначе false
     */
    public static <T> boolean containsIdentity(T[] haystack, T needle) {
        if (haystack == null) { return false; }
        for (T t : haystack) {
            if (t == needle) { return true; }
        }
        return false;
    }

    public static int indexOf(byte[] array, byte[] sequence, int from) {
        return indexOf(array, sequence, from, array.length);
    }

    public static int indexOf(byte[] array, byte[] sequence, int from, int to) {
        if (sequence.length == 0) {
            return from;
        }
        int limit = to - sequence.length;
        int i = from;
        while (true) {
            if (i > limit) {
                return -1;
            }
            int u = i;
            int o = 0;
            while (array[u] == sequence[o]) {
                o++;
                if (o == sequence.length) {
                    return i;
                }
                u++;
            }
            i++;
        }
    }

    public static int indexOf(int[] array, int[] sequence, int from) {
        return indexOf(array, sequence, from, array.length);
    }

    public static int indexOf(int[] array, int[] sequence, int from, int to) {
        if (sequence.length == 0) {
            return from;
        }
        int limit = to - sequence.length;
        int i = from;
        while (true) {
            if (i >= limit) {
                return -1;
            }
            int u = i;
            int o = 0;
            while (array[u] == sequence[o]) {
                o++;
                if (o == sequence.length) {
                    return i;
                }
                u++;
            }
            i++;
        }
    }


    public static int lastIndexOf(byte[] array, byte[] sequence, int from) {
        return lastIndexOf(array, sequence, from, 0);
    }

    public static int lastIndexOf(byte[] array, byte[] sequence, int from, int to) {
        if (sequence.length == 0) {
            return from;
        }
        int pos = from - sequence.length;
        while (true) {
            if (pos < to) {
                return -1;
            }
            int cmpArrayPos = pos;
            int smpSequencePos = 0;
            while (array[cmpArrayPos] == sequence[smpSequencePos]) {
                smpSequencePos++;
                if (smpSequencePos == sequence.length) {
                    return pos;
                }
                cmpArrayPos++;
            }
            pos--;
        }
    }

    public static int lastIndexOf(int[] array, int[] sequence, int from) {
        return lastIndexOf(array, sequence, from, 0);
    }

    public static int lastIndexOf(int[] array, int[] sequence, int from, int to) {
        if (sequence.length == 0) {
            return from;
        }
        int pos = from - sequence.length;
        while (true) {
            if (pos < to) {
                return -1;
            }
            int cmpArrayPos = pos;
            int smpSequencePos = 0;
            while (array[cmpArrayPos] == sequence[smpSequencePos]) {
                smpSequencePos++;
                if (smpSequencePos == sequence.length) {
                    return pos;
                }
                cmpArrayPos++;
            }
            pos--;
        }
    }


    public static short getShortBE(byte[] array, int pos) {
        return (short) ((array[pos] & 0xff) << 8 | array[pos + 1] & 0xff);
    }

    public static short getShortLE(byte[] array, int pos) {
        return (short) ((array[pos + 1] & 0xff) << 8 | array[pos] & 0xff);
    }

    public static void putShortBE(byte[] array, int pos, short value) {
        array[pos] = (byte) (value >> 8);
        array[pos + 1] = (byte) value;
    }

    public static void putShortLE(byte[] array, int pos, short value) {
        array[pos + 1] = (byte) (value >> 8);
        array[pos] = (byte) value;
    }

    public static int getIntBE(byte[] array, int pos) {
        return (array[pos] & 0xff) << 24
            | (array[pos + 1] & 0xff) << 16
            | (array[pos + 2] & 0xff) << 8
            | array[pos + 3] & 0xff;
    }

    public static int getIntLE(byte[] array, int pos) {
        return (array[pos + 3] & 0xff) << 24
            | (array[pos + 2] & 0xff) << 16
            | (array[pos + 1] & 0xff) << 8
            | array[pos] & 0xff;
    }

    public static void putIntBE(byte[] array, int pos, int value) {
        array[pos] = (byte) (value >> 24);
        array[pos + 1] = (byte) (value >> 16);
        array[pos + 2] = (byte) (value >> 8);
        array[pos + 3] = (byte) value;
    }

    public static void putIntLE(byte[] array, int pos, int value) {
        array[pos + 3] = (byte) (value >> 24);
        array[pos + 2] = (byte) (value >> 16);
        array[pos + 1] = (byte) (value >> 8);
        array[pos] = (byte) value;
    }

    public static long getLongBE(byte[] array, int pos) {
        return (long) (array[pos] & 0xff) << 56
            | (long) (array[pos + 1] & 0xff) << 48
            | (long) (array[pos + 2] & 0xff) << 40
            | (long) (array[pos + 3] & 0xff) << 32
            | (long) (array[pos + 4] & 0xff) << 24
            | (long) (array[pos + 5] & 0xff) << 16
            | (long) (array[pos + 6] & 0xff) << 8
            | (long) (array[pos + 7] & 0xff);
    }

    public static long getLongLE(byte[] array, int pos) {
        return (long) (array[pos + 7] & 0xff) << 56
            | (long) (array[pos + 6] & 0xff) << 48
            | (long) (array[pos + 5] & 0xff) << 40
            | (long) (array[pos + 4] & 0xff) << 32
            | (long) (array[pos + 3] & 0xff) << 24
            | (long) (array[pos + 2] & 0xff) << 16
            | (long) (array[pos + 1] & 0xff) << 8
            | (long) (array[pos] & 0xff);
    }

    public static void putLongBE(byte[] array, int pos, long value) {
        array[pos] = (byte) (value >> 56);
        array[pos + 1] = (byte) (value >> 48);
        array[pos + 2] = (byte) (value >> 40);
        array[pos + 3] = (byte) (value >> 32);
        array[pos + 4] = (byte) (value >> 24);
        array[pos + 5] = (byte) (value >> 16);
        array[pos + 6] = (byte) (value >> 8);
        array[pos + 7] = (byte) value;
    }

    public static void putLongLE(byte[] array, int pos, long value) {
        array[pos + 7] = (byte) (value >> 56);
        array[pos + 6] = (byte) (value >> 48);
        array[pos + 5] = (byte) (value >> 40);
        array[pos + 4] = (byte) (value >> 32);
        array[pos + 3] = (byte) (value >> 24);
        array[pos + 2] = (byte) (value >> 16);
        array[pos + 1] = (byte) (value >> 8);
        array[pos] = (byte) value;
    }

    /**
     * Наполняет все поля массива результатам суплаера.
     * Возвращает этот массив, чтобы его можно было в параметрах создавать.
     *
     * @param array
     * @param supplier
     * @param <T>
     * @return переданный массив
     */
    public static <T> T[] setAll(T[] array, Supplier<T> supplier) {
        for (int i = array.length - 1; i >= 0; i--) {
            array[i] = supplier.get();
        }
        return array;
    }

    public static <T, E extends Exception> void forEachIndex(T[] fields, ThrowingConsumer<Integer, E> consumer) throws E {
        if (fields == null) { return; }
        int len = fields.length;
        for (int i = 0; i < len; i++) {
            consumer.accept(i);
        }
    }

    public static <T, E extends Exception> void setEach(T[] fields, ThrowingFunction<Integer, T, E> function) throws E {
        if (fields == null) { return; }
        int len = fields.length;
        for (int i = 0; i < len; i++) {
            fields[i] = function.apply(i);
        }
    }

    public static <T, E extends Exception> void forEach(T[] fields, ThrowingBiConsumer<Integer, T, E> consumer) throws E {
        if (fields == null) { return; }
        int len = fields.length;
        for (int i = 0; i < len; i++) {
            consumer.accept(i, fields[i]);
        }
    }

    public static <V, T, C extends Collection<T>, E extends Exception> C map(V[] source, ThrowingFunction<V, T, E> mapper, Supplier<C> targetSupplier) throws E {
        if (source == null) {
            return null;
        }
        C target = targetSupplier.get();
        for (V v : source) {
            target.add(mapper.apply(v));
        }
        return target;
    }

}
