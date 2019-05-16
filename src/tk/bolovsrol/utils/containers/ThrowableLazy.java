package tk.bolovsrol.utils.containers;

import tk.bolovsrol.utils.function.ThrowingSupplier;

/**
 * Простейший контейнер с ленивой инициализацией. Выполняет инициализацию при первом {@link #get()}.
 *
 * @param <O>
 */
public class ThrowableLazy<O, E extends Exception> {

    private O value;
    private final ThrowingSupplier<O, E> supplier;

    public ThrowableLazy(ThrowingSupplier<O, E> supplier) {
        this.supplier = supplier;
    }

    public O get() throws E {
        if (value == null) { value = supplier.get(); }
        return value;
    }

    public boolean isInitialized() {
        return value != null;
    }

}
