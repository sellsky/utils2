package tk.bolovsrol.utils.containers;

import tk.bolovsrol.utils.Json;

import java.util.Objects;

/**
 * Контейнер, содержащий значение, а также знающий, что его значение изменилось.
 * <p>
 * Контейнер может содержать значение или быть пустым,
 * а также фиксирует изменения своего содержимого.
 */
public interface ValueContainer<V> {

    /** @return возвращает хранимое значение */
    V getValue();

    /**
     * @param value новое значение
     */
    void setValue(V value);

    /**
     * Устанавливает контейнеру значение, если контейнер содержит нул. Если контейнер содержит не нул, ничего не делает.
     *
     * @param value новое значение
     */
    default void setValueIfNull(V value) {
        if (isValueNull()) { setValue(value); }
    }

    /** Очищает значение контейнера, его значение становится null. */
    default void dropValue() {
        setValue(null);
    }

    /** @return true, если контейнер содержит значение, false, если в нём нул. */
    default boolean isValueNull() {
        return getValue() == null;
    }

    /**
     * @return сохранённое значение (например, методом {@link #valueCommitted()}).
     */
    V getCommittedValue();

    /**
     * Фиксирует, что значение поля было сохранено
     * (копирует текущее значение в коммиттед-значение).
     */
    void valueCommitted();

    /** Возвращает полю закоммиченное значение. */
    void rollbackValue();

    /**
     * Было ли изменено поле. Вызывается для проверки, следует ли сохранять объект в базе данных.
     *
     * @return true, если поле изменилось, false иначе
     */
    default boolean isValueChanged() {
        return Objects.equals(getCommittedValue(), getValue());
    }

    /**
     * Отдаёт содержимое контейнера в виде строки.
     * <p>
     * Строку, которую вернёт этот метод, можно скормить методу {@link #parseValue(String)}.
     *
     * @return строка, представляющая текстовое отображение поля, либо null
     */
    String valueToString();

    /**
     * Парсит текстовое представление значения контейнера, если это возможно.
     * Если передать нул, поле будет сброшено в нул.
     * <p>
     * Результат метода {@link #valueToString()} контейнера аналогичного типа будет распознан без ошибок.
     *
     * @param value
     * @throws ValueParsingException распознать значение не удалось
     */
    void parseValue(String value) throws ValueParsingException;

    /**
     * Передаёт значение контейнера в виде строки для вывода в пользовательский лог.
     * [committedValue→][value]
     *
     * @return содержимое поля для дебага.
     */
    String valueToLogString();

    /**
     * Копирует значение поля из переданного поля. В отличие от {@link #setValue(Object)},
     * копируется как value, так и committedValue.
     * <p>
     * Исходное поле должно быть совместимого класса.
     *
     * @param source поле со значением-образцом.
     * @throws ClassCastException попытка скопировать поле несовместимого класса
     * @throws ObjectCopyException ошибка при копировании
     */
    void copyValueFrom(ValueContainer<V> source) throws ClassCastException, ObjectCopyException;

    /** @return базовый класс, который хранится в контейнере */
    Class<V> getComponentType();

    /**
     * Записывает значение контейнера в переданный джсон-объект.
     *
     * @param json
     */
    void putValue(Json json);

    /** @return джсон со значением контейнера. */
    default Json toJson() {
        Json result = new Json();
        putValue(result);
        return result;
    }

    /**
     * Считывает значение из переданного джсон-объекта.
     *
     * @param json
     */
    void parseValue(Json json) throws ValueParsingException;
}
