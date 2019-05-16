package tk.bolovsrol.utils.http;

/**
 * Обработчик успешной отправки Http-сущности.
 *
 * @see HttpEntityIOExceptionHandler
 */
public interface HttpEntityAfterWriteHandler {

	/**
	 * Обрабатывает успешную отправку Http-сущности.
	 */
	void handleAfterWrite();
}


