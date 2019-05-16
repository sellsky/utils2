package tk.bolovsrol.utils.function;

@FunctionalInterface public interface ThrowingRunnable<E extends Throwable> {

    void run() throws E;

}
