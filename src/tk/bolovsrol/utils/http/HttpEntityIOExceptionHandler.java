package tk.bolovsrol.utils.http;

import java.io.IOException;

/**
 * Обработчик ошибки ввода-вывода, возникшей при записи сущности в исходящий поток.
 *
 * @see HttpEntityAfterWriteHandler
 */
public interface HttpEntityIOExceptionHandler {
    /**
     * Обрабатывает исключение ввода-вывода.
     * <p/>
     * Если этот метод вернёт true, то исключение считается полностью обработанным внутри хендлера,
     * в ином случае оно будет обработано далее обычным образом (то есть, выведено в лог).
     *
     * @param e исключение.
     * @return true: прекратить обработку исключения; false: обрабатывать исключение дальше обычным образом
     */
    boolean handleIOException(IOException e);

}
