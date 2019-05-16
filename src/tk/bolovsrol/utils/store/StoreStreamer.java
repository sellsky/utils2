package tk.bolovsrol.utils.store;

import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.conf.AutoConfiguration;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.io.LineOutputStream;
import tk.bolovsrol.utils.log.LogDome;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/** Устройство для записи и чтения из хранилища. */
public interface StoreStreamer<C extends AutoConfiguration> {

    /**
     * Инициализирует стример.
     *
     * @param log лог
     * @param conf конфигурация
     * @throws UnexpectedBehaviourException
     * @throws IOException
     */
    void init(LogDome log, C conf) throws UnexpectedBehaviourException, IOException;

    /**
     * Подготоавливает и отдаёт {@link Writer}, в который будет осуществлена
     * запись состояния объекта и который затем будет закрыт.
     *
     * @return Writer
     */
    LineOutputStream newStoreOutputStream(String id) throws IOException;

    /**
     * Подготавливает и отдаёт {@link Reader}, из которого будет осуществлено
     * чтение состояния объекта и который затем будет закрыт.
     * <p/>
     * Если читать нечего, возвращает null.
     *
     * @return поток для чтения или null
     */
    LineInputStream newStoreInputStream(String id) throws IOException;
}