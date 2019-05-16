package tk.bolovsrol.utils.properties.sources;

import java.util.Map;

public interface ReadOnlySource {
    /**
     * Выясняет полное значение сокращённого ключа для диагностики пользователю.
     * <p>
     * Если соурс представляет собой ветку более обширного соурса, то пользователь должен увидеть ключ,
     * состоящий из общего имени ветки + локального имени ключа в ветке.
     * У остальных соурсов метод возвращает аргумент в неизменном виде.
     *
     * @param localBranchKey
     * @return имя бранча (если есть) + localBranchKey
     */
    default String expand(String localBranchKey) {
        return localBranchKey;
    }

    /**
     * Выясняет значение проперти по её ключу.
     *
     * @param key ключ.
     * @return значение проперти либо null, если проперти нету.
     */
    String get(String key) throws SourceUnavailableException;

    /**
     * Проверяет наличие проперти.
     *
     * @param key название ключа
     * @return true, если такому ключу назначено значение.
     */
    default boolean has(String key) throws SourceUnavailableException {
        return get(key) != null;
    }

    /**
     * Вываливает значения всех пропертей в данный момент времени.
     * <p>
     * Полученные проперти никак не связаны с исходными, их можно изменять.
     *
     * @return карта ключ--значение.
     */
    Map<String, String> dump() throws SourceUnavailableException;

    /**
     * Выясняет уникальное описание вхождения, понятное для человека.
     * Отдаётся в диагностике {@link tk.bolovsrol.utils.properties.InvalidPropertyValueFormatException},
     * чтобы облегчить поиск кривого значения в исходных данных.
     * <p>
     * Если такого описания предоставить нельзя, возвращает нул.
     *
     * @param key ключ.
     * @return описание вхождения или нул.
     */
    default String getIdentity(String key) throws SourceUnavailableException {
        return null;
    }
}
