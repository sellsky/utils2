package tk.bolovsrol.utils.function;

@FunctionalInterface public interface ThrowingBiPredicate<K, V, E extends Throwable> {

    boolean test(K k, V v) throws E;

}
