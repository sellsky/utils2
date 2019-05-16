package tk.bolovsrol.utils.containers;

/** Контейнер содержит число. */
public interface NumberContainer<V extends Number> extends ValueContainer<V> {

    /**
     * Возвращает информацию о знаке содержащегося в контейнере числа,
     * -1, 0 или 1. Знак соответствует.
     *
     * @return
     */
    int signum();
}
