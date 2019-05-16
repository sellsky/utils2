package tk.bolovsrol.utils.function;

/**
 * Как {@link java.util.function.Consumer}, но ещё может выкидывать произвольное исключение.
 *
 * @param <A>
 * @param <B>
 * @param <T>
 */
@FunctionalInterface public interface ThrowingTriConsumer<A, B, C, T extends Throwable> {

    void accept(A a, B b, C c) throws T;
}
