package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.function.ThrowingConsumer;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.properties.sources.ReadOnlySource;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.xml.Element;

import java.io.IOException;

/**
 * Этот хттп-клиент не содержит в себе урла запроса, вследствие чего позволяет выполнять запросы с явным указанием урла.
 *
 * @see HttpClient
 */
public interface HostHttpClient extends AutoCloseable {

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
    HostHttpClient keepAlive(Duration keepAliveTimeout);

    /**
     * Устанавливает режим постянного соединения (чтобы в рамках одного соединения отправлять несколько хттп-запросов)
     * с таймаутом соединения по умолчанию {@link HttpClient#DEFAULT_KEEP_ALIVE_TIMEOUT}, который клиент запросит у сервера.
     *
     * @return this
     * @see #keepAlive(Duration)
     * @see #getKeepAlive()
     */
    HostHttpClient keepAlive();

    /**
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
    HostHttpClient withIoTimeout(Duration ioTimeout);

    /**
     * Переключает хттп-клиент в режим заданного урла&
     * Возвращает этот же объект в виде интерфейса {@link UrlHttpClient}.
     *
     * @param url урл, с которым будет работать хттп-клиент
     * @return (UrlHttpClient) this
     */
    UrlHttpClient setUrl(Uri url);

    /**
     * Если сервер вернёт редирект, следовать ли за ним прозрачно, если возмжоно.
     *
     * @return this
     */
    HostHttpClient followRedirection(boolean followRedirection);

    /**
     * Если сервер вернёт редирект, по возможности прозрачно следовать за ним.
     *
     * @return this
     */
    HostHttpClient followRedirection();

    /** @return следовать ли за редиректом, который вернул сервер. */
    boolean isFollowRedirection();

    /**
     * Сколько символов тела запроса и ответа показывать в дампе в логе. Если нул, то показывать всё.
     * Это значение прилипнет к обрабатываемому запросу или возвращаемому ответу.
     *
     * @param cutBodyToStringAt
     * @return
     */
    HostHttpClient cutBodyToStringAt(Integer cutBodyToStringAt);

    /** @return сколько символов тела запроса и ответа показывать в дампе в логе; если нул, то показывать всё. */
    Integer getCutBodyToStringAt();

    /** Закрывает открытое соединение. Действие обратимое, требующие соединения методы инициируют установку соединения (снова). */
    @Override void close();

    // --- Main
    <E extends Exception> HttpResponse request(Method method, Uri url, ThrowingConsumer<HttpRequest, E> setupOrNull) throws InterruptedException, HttpEntityParsingException, IOException, E;

    // === сахар ===
    // --- Unbound request
    default HttpResponse request(Method method, Uri url, byte[] raw, String contentType) throws IOException, InterruptedException, HttpEntityParsingException { return request(method, url, hreq -> hreq.setBody(raw, contentType)); }

    default HttpResponse request(Method method, Uri url, String body) throws IOException, InterruptedException, HttpEntityParsingException { return request(method, url, hreq -> hreq.setBody(body)); }

    default HttpResponse request(Method method, Uri url, String body, String contentType) throws IOException, InterruptedException, HttpEntityParsingException { return request(method, url, hreq -> hreq.setBody(body, contentType)); }

    default HttpResponse request(Method method, Uri url, ReadOnlySource form) throws IOException, InterruptedException, HttpEntityParsingException { return request(method, url, hreq -> hreq.setBody(form)); }

    default HttpResponse request(Method method, Uri url, Element element) throws IOException, InterruptedException, HttpEntityParsingException { return request(method, url, hreq -> hreq.setBody(element)); }

    default HttpResponse request(Method method, Uri url, Json json) throws IOException, InterruptedException, HttpEntityParsingException { return request(method, url, hreq -> hreq.setBody(json)); }

    // --- unbound GET
    default <E extends Exception> HttpResponse get(Uri url, ThrowingConsumer<HttpRequest, E> setup) throws InterruptedException, HttpEntityParsingException, IOException, E { return request(Method.GET, url, setup); }

    default HttpResponse get(Uri url, ReadOnlySource query) throws IOException, HttpEntityParsingException, InterruptedException { return get(url, hreq -> hreq.setQuery(query)); }

    default HttpResponse get(Uri url) throws IOException, HttpEntityParsingException, InterruptedException { return get(url, (ThrowingConsumer<HttpRequest, RuntimeException>) null); }

    // --- Unound POST
    default <E extends Exception> HttpResponse post(Uri url, ThrowingConsumer<HttpRequest, E> setup) throws InterruptedException, HttpEntityParsingException, IOException, E { return request(Method.POST, url, setup); }

    default HttpResponse post(Uri url, byte[] raw, String contentType) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.POST, url, raw, contentType); }

    default HttpResponse post(Uri url, String body) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.POST, url, body); }

    default HttpResponse post(Uri url, String body, String contentType) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.POST, url, body, contentType); }

    default HttpResponse post(Uri url, ReadOnlySource form) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.POST, url, form); }

    default HttpResponse post(Uri url, Element el) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.POST, url, el); }

    default HttpResponse post(Uri url, Json json) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.POST, url, json); }

    // --- Unound PUT
    default <E extends Exception> HttpResponse put(Uri url, ThrowingConsumer<HttpRequest, E> setup) throws InterruptedException, HttpEntityParsingException, IOException, E { return request(Method.PUT, url, setup); }

    default HttpResponse put(Uri url, byte[] raw, String contentType) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.PUT, url, raw, contentType); }

    default HttpResponse put(Uri url, String body) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.PUT, url, body); }

    default HttpResponse put(Uri url, String body, String contentType) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.PUT, url, body, contentType); }

    default HttpResponse put(Uri url, ReadOnlySource form) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.PUT, url, form); }

    default HttpResponse put(Uri url, Element el) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.PUT, url, el); }

    default HttpResponse put(Uri url, Json json) throws IOException, InterruptedException, HttpEntityParsingException { return request(Method.PUT, url, json); }

}
