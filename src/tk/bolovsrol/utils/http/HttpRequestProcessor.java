package tk.bolovsrol.utils.http;

import java.net.Socket;

@FunctionalInterface public interface HttpRequestProcessor {

	/**
	 * Обрабатывает запрос, прочитанный из указанного сокета.
	 *
	 * @param socket
	 * @param httpRequest
	 * @return
	 * @throws InterruptedException
	 */
	HttpResponse process(Socket socket, HttpRequest httpRequest) throws InterruptedException;

}
