package tk.bolovsrol.utils.properties.sources;

import tk.bolovsrol.utils.CollectionUtils;
import tk.bolovsrol.utils.box.Box;

import java.util.Map;
import java.util.function.BiFunction;

public interface PlainSource extends ReadOnlySource {

    /**
     * Очищает проперти.
     *
     * @return this
     * @throws SourceUnavailableException
     */
    PlainSource clear() throws SourceUnavailableException;

    /**
     * Удаляет проперть по ключу.
     *
     * @param key
     * @return this
     */
    PlainSource drop(String key) throws SourceUnavailableException;

    /**
     * Записывает проперть по ключу.
     *
     * @param key
     * @param value
     * @return this
     */
    PlainSource set(String key, String value) throws SourceUnavailableException;

    /**
     * Записывает проперть по ключу.
     * Если у проперти уже есть значение, записывает результат переданной функции.
     *
     * @param key
     * @param value
     * @return this
     */
    default PlainSource merge(String key, String value, BiFunction<String, String, String> mergeFunction) throws SourceUnavailableException {
        set(key, Box.with(get(key)).merge(value, mergeFunction::apply).get());
        return this;
    }

    /**
     * Добавляет все переданные проперти.
     *
     * @param matter
     * @return this
     */
    PlainSource setAll(Map<String, String> matter) throws SourceUnavailableException;

    /**
     * Добавляет все переданные проперти.
     * Если у какой-либо проперти уже есть значение, записывает результат переданной функции.
     *
     * @param matter
     * @param mergeFunction
     * @return this
     */
    default PlainSource mergeAll(Map<String, String> matter, BiFunction<String, String, String> mergeFunction) throws SourceUnavailableException {
        CollectionUtils.forEach(matter, (key, value) -> merge(key, value, mergeFunction));
        return this;
    }

}
