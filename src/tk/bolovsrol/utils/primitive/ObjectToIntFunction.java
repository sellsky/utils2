package tk.bolovsrol.utils.primitive;

@FunctionalInterface public interface ObjectToIntFunction<V> {
    int apply(V v);
}
