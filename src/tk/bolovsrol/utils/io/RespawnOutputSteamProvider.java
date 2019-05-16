package tk.bolovsrol.utils.io;

import java.io.IOException;
import java.io.OutputStream;

/** Генератор потоков-делегатов для вывода. */
public interface RespawnOutputSteamProvider {

    /**
     * Возвращает новый поток-делегат.
     * <p/>
     * Вызывается при необходимости записи,
     * и только если предыдущий полученный поток уже закрыт.
     *
     * @return новый поток для записи
     */
    OutputStream newStream() throws IOException;

}
