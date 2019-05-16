package tk.bolovsrol.utils.properties;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

/** Простой процессор, обрабатывающий входящие проперти и отдающий исходящие проперти же. */
public interface PropertiesProcessor {
    /**
     * Обрабатывает переданные {@link ReadOnlyProperties проперти}, возвращая результат тоже в виде пропертей.
     *
     * @param data исходные данные
     * @return результат обработки
     * @throws UnexpectedBehaviourException системная ошибка.
     * @throws InterruptedException         ясно
     */
    ReadOnlyProperties processProperies(ReadOnlyProperties data) throws UnexpectedBehaviourException, InterruptedException;

}
