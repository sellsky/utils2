package tk.bolovsrol.utils.log;

/** Управляет записью логгируемых данных в лог. */
public interface LogWriter {

    /**
     * Записывает переданные данные в лог.
     * <p/>
     * Метод может возвратить управление и до фактической записи.
     *
     * @param ald данные для записи.
     */
    void write(LogData ald);

}
