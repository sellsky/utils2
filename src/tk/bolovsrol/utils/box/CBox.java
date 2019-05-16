package tk.bolovsrol.utils.box;

import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.function.ThrowingBiConsumer;
import tk.bolovsrol.utils.function.ThrowingConsumer;
import tk.bolovsrol.utils.function.ThrowingFunction;
import tk.bolovsrol.utils.function.ThrowingPredicate;
import tk.bolovsrol.utils.function.ThrowingSupplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Коробка с коллекцией, экспериментальная версия.
 */
public class CBox<V> {

    private static final CBox EMPTY = new CBox(null);

    @SuppressWarnings("unchecked")
    public static <V> CBox<V> empty() {
        return (CBox<V>) EMPTY;
    }

    /**
     * Создаёт коробку с элементами переданной коллекции.
     *
     * @param c
     * @param <V>
     * @return коробка с переданной коллекцией
     */
    public static <V> CBox<V> with(Collection<V> c) {
        return c == null || c.isEmpty() ? empty() : new CBox<>(c instanceof Set ? new LinkedHashSet<>(c) : new ArrayList<>(c));
    }

    /**
     * Оборачивает переданную коллекцию.
     *
     * @param c
     * @param <V>
     * @return коробка с переданной коллекцией
     */
    public static <V> CBox<V> wrap(Collection<V> c) {
        return c == null || c.isEmpty() ? empty() : new CBox<>(c);
    }

    /**
     * Создаёт коробку с значениями переданной карты.
     *
     * @param m
     * @param <V>
     * @return коробка с переданной коллекцией
     */
    public static <V> CBox<V> withValues(Map<?, V> m) {
        return m == null || m.isEmpty() ? empty() : new CBox<>(new ArrayList<>(m.values()));
    }

    /**
     * Оборачивает значения переданной карты.
     *
     * @param m
     * @param <V>
     * @return коробка с переданной коллекцией
     */
    public static <V> CBox<V> wrapValues(Map<?, V> m) {
        return m == null || m.isEmpty() ? empty() : new CBox<>(m.values());
    }

    /**
     * Создаёт коробку с ключами переданной карты.
     *
     * @param m
     * @param <V>
     * @return коробка с переданной коллекцией
     */
    public static <V> CBox<V> withKeys(Map<V, ?> m) {
        return m == null || m.isEmpty() ? empty() : new CBox<>(new LinkedHashSet<>(m.keySet()));
    }

    /**
     * Оборачивает ключи переданной карты.
     *
     * @param m
     * @param <V>
     * @return коробка с переданной коллекцией
     */
    public static <V> CBox<V> wrapKeys(Map<V, ?> m) {
        return m == null || m.isEmpty() ? empty() : new CBox<>(m.keySet());
    }


    /**
     * Создаёт коробку, в которую кладёт переданные элементы.
     * <p>
     * Если переданный массив нул, то создаёт пустую коробку.
     *
     * @param items
     * @param <V>
     * @return
     */
    @SafeVarargs
    public static <V> CBox<V> with(V... items) {
        return items == null || items.length == 0 ? empty() : new CBox<>(Arrays.asList(items));
    }

    /**
     * Создаёт коробку, в которую кладёт переданный элемент.
     * <p>
     * Если переданный элемент нул, то создаёт коробку с нул-элементом.
     *
     * @param item
     * @param <V>
     * @return
     */
    public static <V> CBox<V> of(V item) {
        return new CBox<>(Collections.singleton(item));
    }

    private final Collection<V> c;

    protected CBox(Collection<V> c) {
        this.c = c;
    }

    /**
     * Возвращает коробку, в которой нет нул-элементов.
     *
     * @return
     */
    public CBox<V> nonNull() {
        if (c == null || !c.contains(null)) {
            return this;
        }
        Collection<V> nnc = c instanceof Set ? new LinkedHashSet<>(c.size()) : new ArrayList<>(c.size());
        for (V v : c) {
            if (v != null) {
                nnc.add(v);
            }
        }
        return c.size() == nnc.size() ? this : CBox.wrap(nnc);
    }

    /**
     * Возвращает коробку, в которой только одобренные фильтром элементы.
     *
     * @param filter
     * @param <T>
     * @return
     * @throws T
     */
    public <T extends Throwable> CBox<V> select(ThrowingPredicate<V, T> filter) throws T {
        if (c == null) {
            return this;
        }
        ArrayList<V> al = new ArrayList<>(c.size());
        for (V v : c) {
            if (filter.test(v)) {
                al.add(v);
            }
        }
        return al.size() == c.size() ? this : CBox.wrap(al);
    }

    /**
     * Возвращает коробку, в которой только не одобренные фильтром элементы.
     *
     * @param filter
     * @param <T>
     * @return
     * @throws T
     */
    public <T extends Throwable> CBox<V> remove(ThrowingPredicate<V, T> filter) throws T {
        if (c == null) {
            return this;
        }
        ArrayList<V> al = new ArrayList<>(c.size());
        for (V v : c) {
            if (!filter.test(v)) {
                al.add(v);
            }
        }
        return al.size() == c.size() ? this : CBox.wrap(al);
    }

    /**
     * Возвращает коробку, из которой убраны переданные элементы.
     * <p>
     * Если передан нул, возвращает исходную коробку.
     *
     * @param toRemove элементы, которые надо удалить, или нул
     * @param <T>
     * @return
     * @throws T
     */
    public <T extends Throwable> CBox<V> removeAll(Collection<V> toRemove) throws T {
        if (c == null || toRemove == null || toRemove.isEmpty()) {
            return this;
        }
        Set<V> set = new LinkedHashSet<>(c);
        return set.removeAll(toRemove) ? CBox.wrap(set) : this;
    }

    /**
     * Возвращает коробку, в которой только вещи, для которых переданная функция вернула указанное значение.
     *
     * @param filter
     * @param <T>
     * @return
     * @throws T
     */
    public <S, T extends Throwable> CBox<V> selectBy(ThrowingFunction<V, S, T> filter, S specimen) throws T {
        if (c == null) {
            return this;
        }
        ArrayList<V> al = new ArrayList<>(c.size());
        for (V v : c) {
            if (Objects.equals(filter.apply(v), specimen)) {
                al.add(v);
            }
        }
        return al.size() == c.size() ? this : CBox.wrap(al);
    }

    /**
     * Возвращает коробку, в которой только вещи, для которых переданная функция вернула не указанное значение.
     *
     * @param filter
     * @param <T>
     * @return
     * @throws T
     */
    public <S, T extends Throwable> CBox<V> removeBy(ThrowingFunction<V, S, T> filter, S specimen) throws T {
        if (c == null) {
            return this;
        }
        ArrayList<V> al = new ArrayList<>(c.size());
        for (V v : c) {
            if (!Objects.equals(filter.apply(v), specimen)) {
                al.add(v);
            }
        }
        return al.size() == c.size() ? this : CBox.wrap(al);
    }

    /**
     * Возвращает коробку с коллекцией без повторяющихся значений. Если в коробке не {@link Set}, перекладывает содержимое в {@link LinkedHashSet}.
     *
     * @return коробка без повторяющихся значений.
     */
    public CBox<V> distinct() {
        return c instanceof Set ? this : new CBox<>(new LinkedHashSet<>(c));
    }

    /**
     * Возвращает коробку с отсортированными в натуральном порядке элементами.
     * Если элементы не могут в компарабле, выкинет {@link ClassCastException}, ну всё как обычно.
     *
     * @return
     */
    public CBox<V> sort() {
        if (c == null || c instanceof SortedSet && ((SortedSet) c).comparator() == null) {
            return this;
        }
        ArrayList<V> result = new ArrayList<V>(c);
        result.sort(null);
        return CBox.wrap(result);
    }

    /**
     * Возвращает коробку с отсортированными элементами.
     *
     * @param comparator
     * @return
     */
    public CBox<V> sort(Comparator<? super V> comparator) {
        if (c == null) {
            return this;
        }
        ArrayList<V> result = new ArrayList<>(c);
        result.sort(comparator);
        return CBox.wrap(result);
    }

    /**
     * Собирает содержимое коробки в объекте, который вернёт targetSupplier.
     *
     * @param targetSupplier
     * @param targetValueCollator
     * @param <X>
     * @param <TX>
     * @return
     * @throws TX
     */
    public <X, TX extends Throwable> X collect(Supplier<X> targetSupplier, ThrowingBiConsumer<X, V, TX> targetValueCollator) throws TX {
        if (c == null) {
            return null;
        }
        X target = targetSupplier.get();
        for (V v : c) {
            targetValueCollator.accept(target, v);
        }
        return target;
    }

    public <X, Z, T extends Throwable> Map<X, Z> collectToMap(ThrowingBiConsumer<Map<X, Z>, V, T> consumer) throws T {
        return collect(HashMap::new, consumer);
    }

    public <Z, X, TZ extends Throwable, TX extends Throwable> MBox<Z, X> toMap(Supplier<Map<Z, X>> targetSupplier, ThrowingFunction<V, Z, TZ> keyMapper, ThrowingFunction<V, X, TX> valueMapper) throws TZ, TX {
        if (c == null) {
            return MBox.empty();
        }
        Map<Z, X> zx = targetSupplier.get();
        for (V v : c) {
            zx.put(keyMapper.apply(v), valueMapper.apply(v));
        }
        return new MBox<>(zx);
    }

    public <Z, X, TZ extends Throwable, TX extends Throwable> MBox<Z, X> toMap(ThrowingFunction<V, Z, TZ> keyMapper, ThrowingFunction<V, X, TX> valueMapper) throws TZ, TX {
        return toMap(HashMap::new, keyMapper, valueMapper);
    }

    public <X, TX extends Throwable> MBox<V, X> toMap(ThrowingFunction<V, X, TX> valueMapper) throws TX {
        if (c == null) {
            return MBox.empty();
        }
        Map<V, X> zx = new HashMap<>();
        for (V v : c) {
            zx.put(v, valueMapper.apply(v));
        }
        return new MBox<>(zx);
    }

    public <X, TX extends Throwable> CBox<X> toCollection(ThrowingFunction<V, X, TX> valueMapper) throws TX {
        if (c == null) {
            return CBox.empty();
        }
        List<X> target = new ArrayList<>(c.size());
        for (V v : c) {
            target.add(valueMapper.apply(v));
        }
        return CBox.wrap(target);
    }


    public <Z, X, TZ extends Throwable, TX extends Throwable> Map<Z, X> collectToMap(Supplier<Map<Z, X>> targetSupplier, ThrowingFunction<V, Z, TZ> keyMapper, ThrowingFunction<V, X, TX> valueMapper) throws TZ, TX {
        if (c == null) {
            return null;
        }
        Map<Z, X> zx = targetSupplier.get();
        for (V v : c) {
            zx.put(keyMapper.apply(v), valueMapper.apply(v));
        }
        return zx;
    }

    public <Z, X, TZ extends Throwable, TX extends Throwable> Map<Z, X> collectToMap(ThrowingFunction<V, Z, TZ> keyMapper, ThrowingFunction<V, X, TX> valueMapper) throws TZ, TX {
        return collectToMap(HashMap::new, keyMapper, valueMapper);
    }

    public <X, TX extends Throwable> Map<V, X> collectToMap(ThrowingFunction<V, X, TX> valueMapper) throws TX {
        return collectToMap(HashMap::new, v -> v, valueMapper);
    }

    public <Z, T extends Throwable> CBox<Z> map(ThrowingFunction<V, Z, T> mapper) throws T {
        if (c == null) {
            return empty();
        } else {
            List<Z> result = new ArrayList<>(c.size());
            for (V v : c) {
                result.add(mapper.apply(v));
            }
            return CBox.wrap(result);
        }
    }

    public <Z, T extends Throwable> List<Z> mapAndGet(ThrowingFunction<V, Z, T> mapper) throws T {
        if (c == null) {
            return null;
        } else {
            List<Z> result = new ArrayList<>(c.size());
            for (V v : c) {
                result.add(mapper.apply(v));
            }
            return result;
        }
    }

    public <Z, TZ extends Throwable, TE extends Throwable> CBox<Z> mapGuarded(ThrowingFunction<? super V, ? extends Z, TZ> mapper, ThrowingBiConsumer<? super Throwable, ? super V, TE> exceptionConsumer) throws TE {
        if (c == null) {
            return empty();
        } else {
            List<Z> result = new ArrayList<>(c.size());
            for (V v : c) {
                try {
                    result.add(mapper.apply(v));
                } catch (Throwable tz) {
                    exceptionConsumer.accept(tz, v);
                }
            }
            return CBox.wrap(result);
        }
    }

    public <Z, TZ extends Throwable, TE extends Throwable> CBox<Z> mapOrRethow(ThrowingFunction<? super V, ? extends Z, TZ> mapper, BiFunction<? super Throwable, ? super V, TE> exceptionGenerator) throws TE {
        if (c == null) {
            return empty();
        } else {
            List<Z> result = new ArrayList<>(c.size());
            for (V v : c) {
                try {
                    result.add(mapper.apply(v));
                } catch (Throwable tz) {
                    throw exceptionGenerator.apply(tz, v);
                }
            }
            return CBox.wrap(result);
        }
    }

    public Collection<V> get() {
        return c;
    }

    public Set<V> toSet() {
        return toSet(HashSet::new);
    }

    public Set<V> toOrderedSet() {
        return toSet(LinkedHashSet::new);
    }

    public Set<V> toSet(Function<Collection<V>, Set<V>> setMapper) {
        return c == null ? null : c instanceof Set ? (Set<V>) c : setMapper.apply(c);
    }

    public SortedSet<V> toSortedSet() {
        return toSortedSet(null);
    }

    public SortedSet<V> toSortedSet(Comparator<? super V> comparator) {
        if (c == null) {
            return null;
        }
        if (c instanceof SortedSet && Objects.equals(((SortedSet) c).comparator(), comparator)) {
            return (SortedSet<V>) c;
        }
        TreeSet<V> set = new TreeSet<>(comparator);
        set.addAll(c);
        return set;
    }

    public List<V> toList() {
        return toList(ArrayList::new);
    }

    public List<V> toList(Function<Collection<V>, List<V>> listMapper) {
        return c == null ? null : c instanceof List ? (List<V>) c : listMapper.apply(c);
    }

    public <T extends Throwable> Collection<V> getOr(ThrowingSupplier<Collection<V>, T> defaultSupplier) throws T {
        return c == null ? defaultSupplier.get() : c;
    }

    public <T extends Throwable> Collection<V> getOrDie(Supplier<T> exceptionSupplier) throws T {
        if (c == null) {
            throw exceptionSupplier.get();
        }
        return c;
    }

    public <T extends Throwable> CBox<V> orDie(Supplier<T> exceptionSupplier) throws T {
        if (c == null) {
            throw exceptionSupplier.get();
        }
        return this;
    }

    public <T extends Throwable> CBox<V> forEach(ThrowingConsumer<V, T> consumer) throws T {
        if (c != null) {
            for (V v : c) {
                consumer.accept(v);
            }
        }
        return this;
    }

    /**
     * Передаёт консьюмеру каждый элемент коробки.
     * <p>
     * Если консьюмер выкидывает исключение ожидаемого типа, метод передаёт это исключение мапперу и выкидывает исключение, которое возвращает маппер.
     * Если консьюмер выкидывает исключение неожиданного типа, то метод выкидывает либо это исключение, если оно анчекед, либо оборачивает его в {@link RuntimeException}, если оно чекед.
     *
     * @param consumer
     * @param exceptionMapper
     * @param <T>
     * @param <X>
     * @return this
     * @throws X
     */
    @SuppressWarnings("unchecked")
    public <T extends Throwable, X extends Throwable> CBox<V> forEach(ThrowingConsumer<V, T> consumer, BiFunction<V, T, X> exceptionMapper) throws X {
        if (c != null) {
            for (V v : c) {
                try {
                    consumer.accept(v);
                } catch (Throwable t) {
                    T castedT;
                    try {
                        castedT = (T) t;
                    } catch (ClassCastException x) {
                        if (t instanceof RuntimeException) {
                            throw (RuntimeException) t;
                        } else {
                            throw new RuntimeException(t);
                        }
                    }
                    throw exceptionMapper.apply(v, castedT);
                }
            }
        }
        return this;
    }

    public <T extends Throwable> CBox<V> peek(ThrowingConsumer<Collection<V>, T> consumer) throws T {
        if (c != null) {
            consumer.accept(c);
        }
        return this;
    }

    public int count() {
        return c == null ? 0 : c.size();
    }

    public CBox<V> orWrap(Collection<V> defaultValue) {
        return c != null || defaultValue == null ? this : CBox.wrap(defaultValue);
    }

    public <T extends Throwable> CBox<V> orWrap(ThrowingSupplier<? extends Collection<V>, T> defaultSupplier) throws T {
        if (c != null) {
            return this;
        }
        Collection<V> newValue = defaultSupplier.get();
        return newValue == null ? this : CBox.wrap(newValue);
    }

    public <T extends Throwable> CBox<V> or(V[] items) throws T {
        return c != null || items == null ? this : CBox.wrap(Arrays.asList(items));
    }

    public <T extends Throwable> CBox<V> or(V item) throws T {
        return c != null || item == null ? this : CBox.wrap(Collections.singletonList(item));
    }

    public String print(String delimiter) {
        return c == null ? null : StringUtils.enlistCollection(c, delimiter);
    }

    public String printDelimited(String delimiter, String lastDelimiter) {
        return c == null ? null : StringUtils.enlistCollection(c, delimiter, lastDelimiter);
    }

    public <K, C, CB extends Collection<C>, MKCB extends Map<K, CB>, VKT extends Exception, VCT extends Exception> MKCB collectToMultimap(Supplier<MKCB> targetSupplier, ThrowingFunction<V, K, VKT> keyMapper, Function<K, CB> valueCollectionGenrator, ThrowingFunction<V, C, VCT> valueMapper) throws VKT, VCT {
        if (c == null) {
            return null;
        }
        MKCB target = targetSupplier.get();
        for (V v : c) {
            target.computeIfAbsent(keyMapper.apply(v), valueCollectionGenrator).add(valueMapper.apply(v));
        }
        return target;
    }

}
