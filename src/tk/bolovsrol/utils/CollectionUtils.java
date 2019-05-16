package tk.bolovsrol.utils;

import tk.bolovsrol.utils.function.ThrowingBiConsumer;
import tk.bolovsrol.utils.function.ThrowingBiFunction;
import tk.bolovsrol.utils.function.ThrowingConsumer;
import tk.bolovsrol.utils.function.ThrowingFunction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> List<T> fill(List<T> list, int count, Supplier<T> supplier) {
        if (list == null) {return null;}
        while (count > 0) {
            list.add(supplier.get());
            count--;
        }
        return list;
    }

    public static <T, C extends Collection<T>, E extends Throwable> C forEach(C collection, ThrowingConsumer<T, E> consumer) throws E {
        for (T t : collection) {
            consumer.accept(t);
        }
        return collection;
    }

    public static <K, V, M extends Map<K, V>, E extends Throwable> M forEach(M map, ThrowingBiConsumer<K, V, E> consumer) throws E {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            consumer.accept(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static <V, T, C extends Collection<T>, E extends Throwable> C map(Collection<V> source, ThrowingFunction<V, T, E> mapper, C target) throws E {
        if (source == null) {
            return null;
        }
        for (V v : source) {
            target.add(mapper.apply(v));
        }
        return target;
    }

    public static <V, K, T, M extends Map<K, T>, E1 extends Exception, E2 extends Exception> M map(Collection<V> source, ThrowingFunction<V, K, E1> keyMapper, ThrowingFunction<V, T, E2> valueMapper, M target) throws E1, E2 {
        if (source == null) {
            return null;
        }
        for (V v : source) {
            target.put(keyMapper.apply(v), valueMapper.apply(v));
        }
        return target;
    }

    public static <K, V, T, C extends Collection<T>, E extends Throwable> C map(Map<K, V> source, ThrowingBiFunction<K, V, T, E> mapper, C target) throws E {
        if (source == null) {
            return null;
        }
        for (Map.Entry<K, V> entry : source.entrySet()) {
            target.add(mapper.apply(entry.getKey(), entry.getValue()));
        }
        return target;
    }


}
