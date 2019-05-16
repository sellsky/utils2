package tk.bolovsrol.utils.box;

import tk.bolovsrol.utils.function.ThrowingBiFunction;
import tk.bolovsrol.utils.function.ThrowingConsumer;
import tk.bolovsrol.utils.function.ThrowingFunction;
import tk.bolovsrol.utils.function.ThrowingPredicate;
import tk.bolovsrol.utils.function.ThrowingSupplier;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Контейнер наподобие {@link java.util.Optional}, но не пытающийся выкинуть NPE при любом удобном случае,
 * а наоборот, вполне живущий с пустым содержимым.
 * <p>
 * Можно выполнять цепочки действий над содержимым, если оно есть, и не бояться NPE, если содержимого нет.
 *
 * @param <V> тип содержимого
 */
public class Box<V> {

    private static final Box EMPTY = new Box(null);

    /**
     * Возвращает пустую коробку нужного типа.
     *
     * @param <V> тип пустоты
     * @return пустая коробка
     */
    @SuppressWarnings("unchecked") public static <V> Box<V> empty() {
        return (Box<V>) EMPTY;
    }

    /**
     * Создаёт коробку с указанным содержимым.
     *
     * @param value содержимое коробки (может быть и нулом)
     * @param <V> тип содержимого
     * @return коробка
     */
    public static <V> Box<V> with(V value) {
        return value == null ? empty() : new Box<>(value);
    }

    private final V v;

    protected Box(V v) {
        this.v = v;
    }

    /** @return true, если пусто, false, если что-то есть */
    public boolean isEmpty() {
        return v == null;
    }

    /** @return false, если пусто, true, если что-то есть */
    public boolean has() {
        return v != null;
    }

    /** @return содержимое коробки (или нул) */
    public V get() {
        return v;
    }

    /**
     * @param defaultValue значение по умолчанию
     * @return содержимое коробки или, если она пуста, значение по умолчанию
     */
    public V getOr(V defaultValue) {
        return v == null ? defaultValue : v;
    }

    /**
     * @param defaultValueSupplier генератор значения по умолчанию
     * @return содержимое коробки или, если она пуста, значение по умолчанию
     */
    public <T extends Throwable> V getOr(ThrowingSupplier<V, T> defaultValueSupplier) throws T {
        return v == null ? defaultValueSupplier.get() : v;
    }

    /**
     * Возвращает содержимое коробки, если она не пуста. Иначе выкидывает {@link NullPointerException}.
     *
     * @return содержимое коробки
     * @throws NullPointerException если коробка пуста
     */
    public V getOrDie() throws NullPointerException {
        return getOrDie(NullPointerException::new);
    }

    /**
     * Возвращает содержимое коробки, если она не пуста. Иначе выкидывает исключение, которое создаст суплаер.
     *
     * @param exceptionSupplier суплаер
     * @param <T> класс исключения
     * @return содержимое коробки
     * @throws T если коробка пуста
     */
    public <T extends Throwable> V getOrDie(Supplier<T> exceptionSupplier) throws T {
        if (v == null) {
            throw exceptionSupplier.get();
        }
        return v;
    }

    /**
     * Если коробка пуста, выкидывает {@link NullPointerException}. Иначе ничего не делает.
     *
     * @return this
     * @throws NullPointerException
     */
    public Box<V> orDie() throws NullPointerException {
        return orDie(NullPointerException::new);
    }

    /**
     * Если коробка пуста, выкидывает исключение, которое создаст суплаер. Иначе ничего не делает.
     *
     * @param exceptionSupplier суплаер
     * @param <T> класс исключения
     * @return this
     * @throws T если коробка пуста
     */
    public <T extends Throwable> Box<V> orDie(Supplier<T> exceptionSupplier) throws T {
        if (v == null) {
            throw exceptionSupplier.get();
        }
        return this;
    }

    /**
     * Если коробка не пуста, выполняет действие над её содержимым и кладёт его в новую коробку, которую возвращает.
     * Если коробка пуста, то возвращает пустую коробку нового типа.
     *
     * @param mapper преобразователь содержимого
     * @param <Z> новый тип содержимого
     * @param <T> исключение
     * @return коробку с содержимым нового типа
     * @throws T если маппер захотел
     */
    public <Z, T extends Throwable> Box<Z> map(ThrowingFunction<? super V, Z, T> mapper) throws T {
        return v == null ? empty() : new Box<>(mapper.apply(v));
    }

    /**
     * Если коробка не пуста, выполняет действие над её содержимым и кладёт его в новую коробку, которую возвращает.
     * Если коробка пуста, то возвращает пустую коробку нового типа.
     * <p>
     * Если маппер выкинул исключение обещанного типа, то возвращает результат обработки этого эксепшна процессором.
     *
     * @param mapper преобразователь содержимого
     * @param <Z> новый тип содержимого
     * @param <T> исключение
     * @return коробку с содержимым нового типа
     */
    public <Z, T extends Throwable> Box<Z> mapOrCatch(ThrowingFunction<? super V, Z, T> mapper, Function<T, Z> exceptionProcessor) {
        try {
            return Box.with(mapper.apply(v));
        } catch (Throwable t) {
            try {
                //noinspection unchecked
                return Box.with(exceptionProcessor.apply((T) t));
            } catch (ClassCastException x) {
                //noinspection ConstantConditions
                throw (RuntimeException) t;
            }
        }
    }

    /**
     * Если коробка не пуста, выполняет действие над её содержимым и возвращает результат.
     * Если коробка пуста, то возвращает нул.
     *
     * @param mapper преобразователь содержимого
     * @param <Z> тип отввета
     * @param <T> исключение
     * @return значение нового типа
     * @throws T если маппер захотел
     */
    public <Z, T extends Throwable> Z mapAndGet(ThrowingFunction<V, Z, T> mapper) throws T {
        return v == null ? null : mapper.apply(v);
    }

    /**
     * Если коробка не пуста, и фильтр вернул true, то возвращает себя.
     * Иначе возвращает пустую коробку.
     *
     * @param filterPredicate проверятель
     * @param <T> исключение
     * @return this или пустая коробка
     * @throws T если проверятель захотел
     */
    public <T extends Throwable> Box<V> filter(ThrowingPredicate<V, T> filterPredicate) throws T {
        return v != null && filterPredicate.test(v) ? this : empty();
    }

    /**
     * Если коробка не пуста, возвращает себя, иначе возвращает коробку с результатом работы суплаера.
     *
     * @param coalesceSupplier наполнятель коробки
     * @param <T> исключение
     * @return наполненная коробка
     * @throws T если наполнятель захотел
     */
    public <T extends Throwable> Box<V> or(ThrowingSupplier<V, T> coalesceSupplier) throws T {
        return v == null ? new Box<>(coalesceSupplier.get()) : this;
    }

    /**
     * Если коробка не пуста, возвращает себя, иначе возвращает коробку с переданным объектом.
     *
     * @param coalesceValue
     * @return наполненная коробка
     */
    public Box<V> or(V coalesceValue) {
        return v == null ? new Box<>(coalesceValue) : this;
    }

    /**
     * Если передан нул, возвращает себя.
     * Если коробка пуста, возвращает коробку с новым значением.
     * Иначе возвращает коробку с результатом работы сливателя, которому передаёт первым аргументом значение коробки, вторым — переданное новое значение.
     *
     * @param newValue кандидат в содержимое коробки
     * @param merger сливатель значений
     * @param <T> исключение сливателя
     * @return коробка со слитым значением
     * @throws T если сливатель захотел
     */
    public <T extends Throwable> Box<V> merge(V newValue, ThrowingBiFunction<V, V, V, T> merger) throws T {
        if (newValue == null) { return this; }
        if (this.v == null) { return new Box<>(newValue); }
        return new Box<>(merger.apply(this.v, newValue));
    }

    /**
     * Если передан нул, возвращает содержимое коробки.
     * Если коробка пуста, возвращает новое значение.
     * Иначе возвращает результат работы сливателя, которому передаёт первым аргументом значение коробки, вторым — переданное новое значение.
     *
     * @param newValue новое значение
     * @param merger сливатель значений
     * @param <T> исключение сливателя
     * @return слитое значение
     * @throws T если сливатель захотел
     */
    public <T extends Throwable> V mergeAndGet(V newValue, ThrowingBiFunction<V, V, V, T> merger) throws T {
        if (newValue == null) { return this.v; }
        if (this.v == null) { return newValue; }
        return merger.apply(this.v, newValue);
    }

    /**
     * Если коробка не пуста, то передаёт содержимое консьюмеру.
     * Иначе ничего не делает.
     *
     * @param consumer
     * @return this
     */
    public <T extends Throwable> Box<V> peek(ThrowingConsumer<V, T> consumer) throws T {
        if (v != null) {
            consumer.accept(v);
        }
        return this;
    }

//    /** @return значение в виде коробки с коллекцией из одного элемента или без него, если эта коробка пуста. */
//    public CBox<V> asCBox() {
//        return v == null ? CBox.empty() : new CBox<>(Collections.singleton(v));
//    }

    /**
     * Если коробка не пуста, маппит её содержимое о переданный маппер и возвращает коробку с коллекцией. Если коробка пуста, возвращает коробку без коллекции.
     *
     * @param mapper преобразователь значения в коллекцию
     * @param <Z> тип элемента коллекции
     * @param <T> исключение преобразователя
     * @return коробка с коллекцией или без неё
     * @throws T если маппер захотел
     */
    public <Z, T extends Throwable> CBox<Z> toCollection(ThrowingFunction<V, Collection<Z>, T> mapper) throws T {
        return v == null ? CBox.empty() : CBox.with(mapper.apply(v));
    }

    public <Z, T extends Throwable> CBox<Z> toArray(ThrowingFunction<V, Z[], T> mapper) throws T {
        return v == null ? CBox.empty() : CBox.with(mapper.apply(v));
    }

    /**
     * Если коробка не пуста, маппит её содержимое о переданные мапперы и возвращает коробку с картой с единственной парой. Если коробка пуста, возвращает коробку без карты.
     *
     * @param keyMapper преобразователь значения в ключ
     * @param valueMapper преобразователь значения в значение
     * @param <Z> тип ключа карты
     * @param <Z> тип значения карты
     * @param <TZ> исключение преобразователя ключа
     * @param <TX> исключение преобразователя значения
     * @return коробка с коллекцией или без неё
     * @throws TZ если маппер ключа захотел
     * @throws TX если маппер значения захотел
     */
    public <Z, X, TZ extends Throwable, TX extends Throwable> MBox<Z, X> toMap(ThrowingFunction<V, Z, TZ> keyMapper, ThrowingFunction<V, X, TX> valueMapper) throws TZ, TX {
        return v == null ? MBox.empty() : MBox.with(Collections.singletonMap(keyMapper.apply(v), valueMapper.apply(v)));
    }

    /**
     * Если коробка не пуста, маппит её содержимое о переданный маппер и возвращает коробку с картой. Если коробка пуста, возвращает коробку без карты.
     *
     * @param mapper преобразователь значения в карту
     * @param <Z> тип ключа карты
     * @param <X> тип значчения карты
     * @param <T> исключение преобразователя
     * @return коробку с картой или без неё
     * @throws T если преобразователь захотел
     */
    public <Z, X, T extends Throwable> MBox<Z, X> toMap(ThrowingFunction<V, Map<Z, X>, T> mapper) throws T {
        return v == null ? MBox.empty() : MBox.with(mapper.apply(v));
    }

}
