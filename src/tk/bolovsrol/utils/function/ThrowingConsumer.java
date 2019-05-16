package tk.bolovsrol.utils.function;

import java.util.Objects;

/**
 * Как {@link java.util.function.Consumer}, но ещё может выкидывать произвольное исключение.
 *
 * А ещё имплементит {@link ThrowingFunction}, возвращая всегда нул.
 *
 * @param <T>
 * @param <E>
 */
@FunctionalInterface public interface ThrowingConsumer<T, E extends Throwable> extends ThrowingFunction<T, Void, E> {

    void accept(T value) throws E;

    default ThrowingConsumer<T, E> andThen(ThrowingConsumer<? super T, E> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }

    @Override default Void apply(T t) throws E {
        accept(t);
        return null;
    }

}
