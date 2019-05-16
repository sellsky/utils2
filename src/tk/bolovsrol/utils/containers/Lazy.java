package tk.bolovsrol.utils.containers;

import java.util.function.Supplier;

/**
 * Простейший контейнер с ленивой инициализацией. Выполняет инициализацию при первом {@link #get()}.
 *
 * @param <O>
 */
public class Lazy<O> {

    private O value;
    private final Supplier<O> supplier;

    public Lazy(Supplier<O> supplier) {
        this.supplier = supplier;
    }

    public O get() {
        if (value == null) {
            value = supplier.get();
        }
        return value;
    }

    /**
     * @return инициализированное значение либо нул, если значение не было инициализировано
     */
    public O peek() {
        return value;
    }

    public boolean isInitialized() {
        return value != null;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public void reset() {
        this.value = null;
    }

}
