package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.function.ThrowingConsumer;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.properties.sources.ReadOnlySource;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.xml.Element;

import java.io.IOException;

/**
 * Этот хттп-клиент содержит в себе урл запроса и позволяет выполнять только запросы без явного указания урла.
 *
 * @see HttpClient
 */
public interface UrlHttpClient extends AutoCloseable {

    /**
     * Устанавливает режим постянного соединения (чтобы в рамках одного соединения отправлять несколько хттп-запросов)
     * с указанным таймаутом соединения, который клиент запросит у сервера.
     * <p>
     * Если передать нул, то режим будет выключен, клиент, приняв ответ, сразу закроет соединение. По умолчанию кип-элайв выключен.
     *
     * @param keepAliveTimeout запрашиваемый у сервера таймаут соединения
     * @return this
     * @see #keepAlive()
     * @see #getKeepAlive()
     */
    UrlHttpClient keepAlive(Duration keepAliveTimeout);

    /**
     * Устанавливает режим постянного соединения (чтобы в рамках одного соединения отправлять несколько хттп-запросов)
     * с таймаутом соединения по умолчанию {@link HttpClient#DEFAULT_KEEP_ALIVE_TIMEOUT}, который клиент запросит у сервера.
     *
     * @return this
     * @see #keepAlive(Duration)
     * @see #getKeepAlive()
     */
    UrlHttpClient keepAlive();

    /**
     *
     * @return таймаут режима постоянного соединения или нул, если режим выключен.
     * @see #keepAlive(Duration)
     * @see #keepAlive()
     */
    Duration getKeepAlive();

    /**
     * Настраивает таймаут ввода-вывода. По умолчанию он {@link HttpClient#DEFAULT_IO_TIMEOUT}.
     *
     * @param ioTimeout
     * @return this
     */
    UrlHttpClient withIoTimeout(Duration ioTimeout);

    /**
     * Настраивает урл, с которым работает хттп-клиент.
     *
     * @param url новый урл
     * @return this
     */
    UrlHttpClient setUrl(Uri url);

    /**
     * Переключает хттп-клиент в «безурловый» режим .
     * Возвращает этот же объект в виде интерфейса {@link HostHttpClient}.
     *
     * @return (HostHttpClient) this
     */
    HostHttpClient dropUrl();

    /**
     * Если сервер вернёт редирект, следовать ли за ним прозрачно, если возмжоно.
     *
     * @return this
     */
    UrlHttpClient followRedirection(boolean followRedirection);

    /**
     * Если сервер вернёт редирект, по возможности прозрачно следовать за ним.
     *
     * @return this
     */
    UrlHttpClient followRedirection();

    /** @return следовать ли за редиректом, который вернул сервер. */
    boolean isFollowRedirection();

    /**
     * Сколько символов тела запроса и ответа показывать в дампе в логе. Если нул, то показывать всё.
     * Это значение прилипнет к обрабатываемому запросу или возвращаемому ответу.
     *
     * @param cutBodyToStringAt
     * @return
     */
    UrlHttpClient cutBodyToStringAt(Integer cutBodyToStringAt);

    /** @return сколько символов тела запроса и ответа показывать в дампе в логе; если нул, то показывать всё. */
    Integer getCutBodyToStringAt();

    /** Закрывает открытое соединение. Действие обратимое, требующие соединения методы инициируют установку соединения (снова). */
    @Override void close();

    <E extends Exception> HttpResponse request(Method method, ThrowingConsumer<HttpRequest, E> setup) throws InterruptedException, HttpEntityParsingException, IOException, E;

    // --- Bound request
    default HttpResponse request(Method method, byte[] raw, String contentType) throws IOException, InterruptedException, HttpEntityParsingException { return request(method, hreq -> hreq.setBody(raw, contentType)); }

    default HttpResponse request(Method method, String body) throws IOException, InterruptedException, HttpEntityParsingException { return request(method, hreq -> hreq.setBody(body)); }

    default HttpResponse request(Method method, String body, String contentType) throws IOException, InterruptedException, HttpEntityParsingException { return request(method, hreq -> hreq.setBody(body, contentType)); }

    default HttpResponse request(Method method, ReadOnlySource form) throws IOException, InterruptedException, HttpEntityParsingException { return request(method, hreq -> hreq.setBody(form)); }

    default HttpResponse request(Method method, Element element) throws IOException, InterruptedException, HttpEntityParsingException { return request(method, hreq -> hreq.setBody(element)); }

    default HttpResponse request(Method method, Json json) throws IOException, InterruptedException, HttpEntityParsingException { return request(method, hreq -> hreq.setBody(json)); }

    // --- bound GET
    default <E extends Exception> HttpResponse get(ThrowingConsumer<HttpRequest, E> setup) throws InterruptedException, HttpEntityParsingException, IOException, E { return request(Method.GET, setup); }

    default HttpResponse get(ReadOnlySource query) throws IOException, HttpEntityParsingException, InterruptedException { return get(hreq -> hreq.setQuery(query)); }

    default HttpResponse get() throws IOException, HttpEntityParsingException, InterruptedException { return get((ThrowingConsumer<HttpRequest, RuntimeException>) null); }

    // --- Bound POST
    default <E extends Exception> HttpResponse post(ThrowingConsumer<HttpRequest, E> setup) throws InterruptedException, HttpEntityParsingException, IOException, E { return request(Method.POST, setup); }

    default HttpResponse post(byte[] raw, String contentType) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.POST, raw, contentType); }

    default HttpResponse post(String body) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.POST, body); }

    default HttpResponse post(String body, String contentType) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.POST, body, contentType); }

    default HttpResponse post(ReadOnlySource form) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.POST, form); }

    default HttpResponse post(Element el) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.POST, el); }

    default HttpResponse post(Json json) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.POST, json); }

    // --- Bound PUT
    default <E extends Exception> HttpResponse put(ThrowingConsumer<HttpRequest, E> setup) throws InterruptedException, HttpEntityParsingException, IOException, E { return request(Method.PUT, setup); }

    default HttpResponse put(byte[] raw, String contentType) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.PUT, raw, contentType); }

    default HttpResponse put(String body) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.PUT, body); }

    default HttpResponse put(String body, String contentType) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.PUT, body, contentType); }

    default HttpResponse put(ReadOnlySource form) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.PUT, form); }

    default HttpResponse put(Element el) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.PUT, el); }

    default HttpResponse put(Json json) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.PUT, json); }

}
