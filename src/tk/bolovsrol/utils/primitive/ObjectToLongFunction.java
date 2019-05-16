package tk.bolovsrol.utils.primitive;

@FunctionalInterface public interface ObjectToLongFunction<V> {
    long apply(V v);
}
