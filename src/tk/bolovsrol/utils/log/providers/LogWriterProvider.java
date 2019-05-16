package tk.bolovsrol.utils.log.providers;

import java.io.IOException;
import java.io.Writer;

/** Управляет потоком, в который происходит вывод. */
public interface LogWriterProvider {

    /**
     * Символическое название потока для человеческого наблюдения.
     *
     * @return название потока
     */
    String getCaption();

    /**
     * Возвращает писателя для вывода лога.
     *
     * @return поток
     */
    Writer getWriter() throws IOException;

    /**
     * Возвращает true, если прошлый {@link #getWriter()} вернул
     * нового писателя. В таком случае в поток будет
     * записан заголовок.
     *
     * @return true, если открыт новый поток, иначе false
     */
    boolean isNewWriter();

    /**
     * Закрывает писателя. Вызывается обычно при завершении работы джава-машины.
     * <p/>
     * После закрытия писателя может быть вызван {@link #getWriter()},
     * но после него непременно снова будет вызван {@link #close()}.
     */
    void close() throws IOException;
}
