package tk.bolovsrol.utils.conf;

import tk.bolovsrol.utils.log.LogDome;

import java.util.Map;

/**
 * Что делать, если при загрузке в конфигурации остались неиспользованные ключи.
 */
public interface UnusedKeyAction {
    /**
     * Обработать неиспользованные при загрузке конфигурации ключи.
     *
     * @param log лог
     * @param conf загружаемая конфигурация
     * @param unusedKeys сет неиспользованных ключей
     * @throws InvalidConfigurationException
     */
    void processUnusedKeys(LogDome log, AutoConfiguration conf, Map<String, String> unusedKeys) throws UnusedKeysException;

    /** Игнорировать неиспользованные ключи. */
    UnusedKeyAction IGNORE = (log, conf, unusedKeys) -> {
    };

    /** Писать в лог варнинг о неиспользованных ключах. */
    UnusedKeyAction WARNING = (log, conf, unusedKeys) -> log.warning("Ignored unknown " + UnusedKeysException.enumerateUnusedKeys(unusedKeys.keySet()));

    /** Выбрасывать {@link InvalidConfigurationException}. */
    UnusedKeyAction EXCEPTION = (log, conf, unusedKeys) -> {
        throw UnusedKeysException.forUnusedKeys(unusedKeys.keySet());
    };
}
