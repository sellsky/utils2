package tk.bolovsrol.utils.log.out;

import tk.bolovsrol.utils.log.providers.StreamProviderException;

/** Обработчик ключевого слова. */
interface WordProcessor {

    /**
     * Обрабатывает слово, изменяя соответствующим образом конфигурацию.
     *
     * @param key
     * @param data
     * @param out
     */
    void process(String key, String data, Out out) throws StreamProviderException;

}
