package tk.bolovsrol.utils.log.providers;

/**
 * Генератор и хранитель провайдеров потока {@link LogWriterProvider}.
 * <p/>
 * Устроен таким образом, что запоминает созданные провайдеры
 * и возвращает походящие, не создавая их заново.
 */
public interface LogWriterProviderPool {

    /**
     * Создаёт новый или находит уже созданный провайдер
     * для указанных данных.
     * <p/>
     * Если данные некорректны, может вернуть нул.
     *
     * @param data данные, специфичные для провайдера, может быть нулом
     * @return подходящий провайдер.
     */
    LogWriterProvider retrieve(String data) throws StreamProviderException;

}
