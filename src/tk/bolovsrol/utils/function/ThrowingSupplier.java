package tk.bolovsrol.utils.function;

@FunctionalInterface public interface ThrowingSupplier<T, E extends Throwable> {

    T get() throws E;

}
