package tk.bolovsrol.utils.function;

@FunctionalInterface public interface ThrowingPredicate<V, E extends Throwable> {

    boolean test(V v) throws E;

}
