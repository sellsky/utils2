package tk.bolovsrol.utils.box;

import tk.bolovsrol.utils.containers.Lazy;
import tk.bolovsrol.utils.function.ThrowingBiConsumer;
import tk.bolovsrol.utils.function.ThrowingBiFunction;
import tk.bolovsrol.utils.function.ThrowingBiPredicate;
import tk.bolovsrol.utils.function.ThrowingSupplier;
import tk.bolovsrol.utils.function.ThrowingTriConsumer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * Коробка с картой, экспериментальная версия.
 */
public class MBox<K, V> {

    private static final MBox EMPTY = new MBox(null);

    @SuppressWarnings("unchecked")
    public static <K, V> MBox<K, V> empty() {
        return (MBox<K, V>) EMPTY;
    }

    public static <K, V> MBox<K, V> with(Map<K, V> m) {
        return m == null || m.isEmpty() ? empty() : new MBox<>(m);
    }

    private final Map<K, V> m;

    public MBox(Map<K, V> m) {
        this.m = m;
    }

    public Map<K, V> get() {
        return m;
    }

    public <T extends Throwable> Map<K, V> getOr(ThrowingSupplier<Map<K, V>, T> defaultSupplier) throws T {
        return m == null ? defaultSupplier.get() : m;
    }

    public <T extends Throwable> Map<K, V> getOrDie(Supplier<T> exceptionSupplier) throws T {
        if (m == null) {
            throw exceptionSupplier.get();
        }
        return m;
    }

    public <T extends Throwable> MBox<K, V> orDie(Supplier<T> exceptionSupplier) throws T {
        if (m == null) {
            throw exceptionSupplier.get();
        }
        return this;
    }

    /**
     * Создаёт новую коробку с элементами, одобренными (если <code>excludeFiltered</code> = true)
     * или забракованными (если <code>excludeFiltered</code> = false) фильтром.
     *
     * @param filter   фильтрующий предикат
     * @param specimen переносить в новую коробку элементы, для которых filter вернул соответствующее значение
     * @param <T>
     * @return
     * @throws T
     */
    private <T extends Throwable> MBox<K, V> filter(ThrowingBiPredicate<K, V, T> filter, boolean specimen) throws T {
        if (m == null) {
            return empty();
        }
        boolean removeFiltered = !specimen;
        Lazy<LinkedHashMap<K, V>> r = new Lazy<>(() -> new LinkedHashMap<K, V>(m.size()));
        for (Map.Entry<K, V> entry : m.entrySet()) {
            if (filter.test(entry.getKey(), entry.getValue()) ^ removeFiltered) {
                r.get().put(entry.getKey(), entry.getValue());
            }
        }
        return r.isEmpty() ? empty() : r.get().size() == m.size() ? this : MBox.with(r.get());
    }

    /**
     * Выбирает в новую коробку элементы, для которых filter
     *
     * @param filter
     * @param <T>
     * @return
     * @throws T
     */
    public <T extends Throwable> MBox<K, V> select(ThrowingBiPredicate<K, V, T> filter) throws T {
        return filter(filter, true);
    }

    public <T extends Throwable> MBox<K, V> remove(ThrowingBiPredicate<K, V, T> filter) throws T {
        return filter(filter, false);
    }

    public MBox<K, V> removeNullValues() {
        return filter((k, v) -> v == null, false);
    }

    public MBox<K, V> sort() {
        if (m == null || (m instanceof SortedMap && ((SortedMap) m).comparator() == null)) {
            return this;
        }
        return new MBox<>(new TreeMap<>(m));
    }

    public MBox<K, V> sort(Comparator<K> comparator) {
        if (m == null || (m instanceof SortedMap && Objects.equals(((SortedMap) m).comparator(), comparator))) {
            return this;
        }
        SortedMap<K, V> n = new TreeMap<>(comparator);
        n.putAll(m);
        return new MBox<>(n);
    }

    public <Z, X, TZ extends Throwable, TX extends Throwable> MBox<Z, X> map(ThrowingBiFunction<K, V, Z, TZ> keyMapper, ThrowingBiFunction<K, V, X, TX> valueMapper) throws TZ, TX {
        if (m == null) {
            return empty();
        }
        Map<Z, X> result = new LinkedHashMap<>(m.size());
        for (Map.Entry<K, V> entry : m.entrySet()) {
            result.put(keyMapper.apply(entry.getKey(), entry.getValue()), valueMapper.apply(entry.getKey(), entry.getValue()));
        }
        return new MBox<>(result);
    }

    public <X, TX extends Throwable> CBox<X> toCollection(ThrowingBiFunction<K, V, X, TX> valueMapper) throws TX {
        if (m == null) {
            return CBox.empty();
        }
        List<X> result = new ArrayList<>(m.size());
        for (Map.Entry<K, V> entry : m.entrySet()) {
            result.add(valueMapper.apply(entry.getKey(), entry.getValue()));
        }
        return new CBox<>(result);
    }

    public <Y, TY extends Throwable, T extends Throwable> Y collect(ThrowingSupplier<Y, TY> targetSupplier, ThrowingTriConsumer<Y, K, V, T> collator) throws TY, T {
        if (m == null) {
            return null;
        }
        Y result = targetSupplier.get();
        for (Map.Entry<K, V> entry : m.entrySet()) {
            collator.accept(result, entry.getKey(), entry.getValue());
        }
        return result;

    }

    public <Y, TY extends Throwable, T extends Throwable> Box<Y> reduce(ThrowingSupplier<Y, TY> targetSupplier, ThrowingTriConsumer<Y, K, V, T> consumer) throws TY, T {
        if (m == null) {
            return Box.empty();
        }
        Y result = targetSupplier.get();
        for (Map.Entry<K, V> entry : m.entrySet()) {
            consumer.accept(result, entry.getKey(), entry.getValue());
        }
        return Box.with(result);
    }

    public <E extends Throwable> MBox<K, V> forEach(ThrowingBiConsumer<K, V, E> consumer) throws E {
        if (m != null) {
            for (Map.Entry<K, V> entry : m.entrySet()) {
                consumer.accept(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }


}
