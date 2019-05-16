package tk.bolovsrol.utils.function;

/**
 * Как {@link java.util.function.Consumer}, но ещё может выкидывать произвольное исключение.
 *
 * @param <A>
 * @param <B>
 * @param <E>
 */
@FunctionalInterface public interface ThrowingBiConsumer<A, B, E extends Throwable> {

    void accept(A a, B b) throws E;
}
