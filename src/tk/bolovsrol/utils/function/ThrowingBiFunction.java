package tk.bolovsrol.utils.function;

@FunctionalInterface public interface ThrowingBiFunction<K, V, R, E extends Throwable> {

    R apply(K k, V v) throws E;

}
