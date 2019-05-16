package tk.bolovsrol.utils.http.server;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.Ticker;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.http.HttpConst;
import tk.bolovsrol.utils.http.HttpRequest;
import tk.bolovsrol.utils.http.HttpRequestProcessor;
import tk.bolovsrol.utils.http.HttpResponse;
import tk.bolovsrol.utils.http.HttpStatus;
import tk.bolovsrol.utils.http.HttpVersion;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.socket.SocketProcessor;

import java.io.IOException;
import java.net.Socket;

/**
 * Преобразует входящий из сокета поток информации в серию Http-запросов.
 * <p/>
 * Поддерживает keep-alive.
 */
class HttpSocketProcessor<P extends HttpRequestProcessor> implements SocketProcessor {
    private final LogDome log;
    private final P httpRequestProcessor;
    private long ioTimeout = HttpConst.DEFAULT_IO_TIMEOUT;

    public HttpSocketProcessor(LogDome log, P httpRequestProcessor) {
        this.log = log;
        this.httpRequestProcessor = httpRequestProcessor;
    }

    public P getHttpRequestProcessor() {
        return httpRequestProcessor;
    }

    @Override
    public boolean process(Socket socket) {
        /*
         * Мы должны прочитать HTTP-запрос.
         * Получить от процессора ответ и отправить его
         * Всё.
         */
        try {
            processInternal(socket);
        } catch (IOException e) {
            // ошибки связи -- обычное дело.
            HttpResponse hresp = HttpResponse.generate(HttpVersion.HTTP_1_0, HttpStatus._400_BAD_REQUEST, e.getMessage());
            try {
                hresp.writeToStream(socket.getOutputStream());
                log.hint("Incoming connection broken, sent BAD_REQUEST response. ", e);
            } catch (Exception ignored) {
                // нет так нет
                log.hint("Incoming connection broken. ", e);
            }
        } catch (UnexpectedBehaviourException e) {
            log.warning(e);
        } catch (Throwable e) {
            log.exception(e);
        }
        return true;
    }

    /**
     * Обрабатывает запрос. Если в запросе указано Keep-Alive
     *
     * @param socket
     * @throws IOException
     * @throws UnexpectedBehaviourException
     * @throws InterruptedException
     */
    private void processInternal(Socket socket) throws IOException, UnexpectedBehaviourException, InterruptedException {
        boolean keepAlive;
        long ioTimeout = this.ioTimeout;
        do {
            HttpRequest request;
            try {
                request = HttpRequest.parse(socket.getInputStream(), ioTimeout);
            } catch (Exception e) {
                // вместо запроса нам прислали лабуду или вообще ничего не прислали
                // чтобы не отмечать в логе исключение, вернёмся, будто ничего и не приняли.
                log.info(e.getMessage());
                return;
            }

            Ticker t = new Ticker();
            log.hint("Serving request" + Spell.get(request));
            InterruptedException interruptedException;
            HttpResponse response;
            try {
                response = httpRequestProcessor.process(socket, request);
                interruptedException = null;
            } catch (InterruptedException e) {
                response = HttpResponse.generate(request, HttpStatus._503_SERVICE_UNAVAILABLE);
                interruptedException = e;
                log.trace("Thread Interrupted while processing HTTP Request. Sending graceful error as response. " + t);
            }

            // если клиент хочет постоянное соединение, будем ждать следующего запросца
            if (demandsPersistentConnection(request)) {
                String keepAliveString = request.headers().get("Keep-Alive");
                if (keepAliveString != null) {
                    int fromPos = keepAliveString.indexOf("timeout=");
                    if (fromPos >= 0) {
                        fromPos += "timeout=".length();
                        int toPos = keepAliveString.indexOf(';', fromPos);
                        if (toPos < 0) {
                            toPos = keepAliveString.length();
                        }
                        ioTimeout = Math.min(Integer.parseInt(keepAliveString.substring(fromPos, toPos)) * 1000L, ioTimeout);
                    }
                }
                response.setConnectionKeepAlive(ioTimeout);
                keepAlive = true;
            } else {
                response.setConnectionClose();
                keepAlive = false;
            }

//            // укажем, что нет тела, если его нет
//            if (!response.hasBody()) {
//                response.getKludges().set("Content-Length", 0);
//            }

//                // укажем дату.
//                if (!response.getKludges().has("Date")) {
//                    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
//                    df.setTimeZone(TimeZone.getTimeZone("GMT"));
//                    response.getKludges().set("Date", df.format(new Date()));
//                }

            log.hint("Sending response " + t + ' ' + Spell.get(response));
            response.writeToStream(socket.getOutputStream(), ioTimeout);

            if (interruptedException != null) {
                throw interruptedException;
            }

        } while (keepAlive);
    }

    private static boolean demandsPersistentConnection(HttpRequest httpRequest) {
        if (httpRequest.getHttpVersion().isPersistentByDefault()) {
			return !StringUtils.equalsIgnoreCase(httpRequest.headers().get("Connection"), "close");
		} else {
			return StringUtils.equalsIgnoreCase(httpRequest.headers().get("Connection"), "keep-alive");
		}
	}

    public long getIoTimeout() {
        return ioTimeout;
    }

    public void setIoTimeout(long ioTimeout) {
        this.ioTimeout = ioTimeout;
    }
}
