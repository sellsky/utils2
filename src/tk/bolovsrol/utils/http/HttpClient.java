package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.ArrayUtils;
import tk.bolovsrol.utils.Composition;
import tk.bolovsrol.utils.SchemeDefaults;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.function.ThrowingConsumer;
import tk.bolovsrol.utils.Ticker;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.UriParsingException;
import tk.bolovsrol.utils.box.Box;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.socket.RespawningSocket;
import tk.bolovsrol.utils.socket.client.SocketFactory;
import tk.bolovsrol.utils.syncro.Locked;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TimeUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Более-менее универсальный хттп-клиент.
 * <p>
 * Един в двух интерфейсах: без заданного урла (только с хостом) {@link HostHttpClient} и с урлом (и хостом) {@link UrlHttpClient}.
 * Интерфейсы обладают эквивалетнтным набором методов, но первый требует указание урла в каждом методе, а второй, наоборот,
 * всегда использует установленный урл. Все методы реализует один и тот же класс (этот), они разнесены просто для удобства,
 * чтобы не вызвать случайно неверный запрос. Возможна лёгкая миграция между интерфейсами методами {@link #setUrl(Uri)} и {@link #dropUrl()},
 * объект остаётся тем же.
 * <p>
 * Клиент может в кип-элайв. Чтобы воспользоваться им,
 * надо у инстанции, которую вернут фабрики <code>#forHost(...)</code> или <code>#forUrl(...)</code>,
 * вызвать метод {@link #keepAlive()} или {@link #keepAlive(Duration)} (с не-нуллом), затем некоторое время
 * использовать клиент, а по завершению работы явно закрыть клиент методом {@link #close()}.
 * Если кип-элайв выключен (по умолчанию), явно закрывать клиент не требуется.
 * В режиме кип-элайв клиент потокобезопасен, он сам упорядочивает запросы.
 * <p>
 * Клиент может прозрачно следовать редиректам, режим включается методом {@link #followRedirection()}. По умолчанию выключен.
 * <p>
 * Клиент дампит в лог исходящие запросы и входящие ответы.
 * <p>
 * Названия методов вполне очевидны; тиражировать однообразные жавадоки займёт весь вечер — думаю, вполне обойдёмся этой ремаркой.
 */
public class HttpClient implements AutoCloseable, HostHttpClient, UrlHttpClient {

    public static final Duration DEFAULT_KEEP_ALIVE_TIMEOUT = new Duration(5 * TimeUtils.MS_IN_MINUTE);
    public static final Duration DEFAULT_IO_TIMEOUT = new Duration(TimeUtils.MS_IN_MINUTE);
    public static final Integer DEFAULT_CUT_BODY_TO_STRING_AT = Cfg.getInteger("log.http.cutBodyAt", null, Log.getInstance());

    private final LogDome log;
    private final Map<String, SocketFactory> schemeSocketFactoriesOrNull;
    private final RespawningSocket rs;

    // режим работы клиента
    private Uri defaultUrl;

    // управление соединением
    private Integer cutBodyToStringAt = DEFAULT_CUT_BODY_TO_STRING_AT;
    private boolean followRedirection = false;
    private Duration keepAliveTimeout = null;
    private ReentrantLock sync;
    private Duration ioTimeout = DEFAULT_IO_TIMEOUT;

    protected HttpClient(LogDome log, Map<String, SocketFactory> schemeSocketFactoriesOrNull, String schemeOrNull, String hostname, Integer portOrNull) {
        this.log = log;
        this.schemeSocketFactoriesOrNull = schemeSocketFactoriesOrNull;
        String scheme = Box.with(schemeOrNull).or(HttpConst.HTTP_SCHEME).get();
        this.rs = new RespawningSocket(
            log,
            Box.with(schemeSocketFactoriesOrNull).map(f -> f.get(scheme))
                .or(() -> SchemeDefaults.SOCKET_FACTORIES.get(scheme))
                .orDie(() -> new IllegalArgumentException("No socket factory defined for scheme " + Spell.get(scheme)))
                .get(),
            hostname,
            Box.with(portOrNull).or(() -> SchemeDefaults.PORTS.get(scheme)).orDie(() -> new IllegalArgumentException("No default port defined for scheme " + Spell.get(scheme))).get(),
            (int) ioTimeout.getMillis()
        );
    }

    @Override public HttpClient keepAlive(Duration keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
        // Пара условий, чтобы не подменять уже использующийся sync при изменении таймаута
        if (keepAliveTimeout == null) sync = null;
        else if (sync == null) sync = new ReentrantLock(true);
        return this;
    }

    @Override public HttpClient keepAlive() {
        return keepAlive(DEFAULT_KEEP_ALIVE_TIMEOUT);
    }

    @Override public Duration getKeepAlive() {
        return keepAliveTimeout;
    }

    @Override public HttpClient withIoTimeout(Duration ioTimeout) {
        this.ioTimeout = ioTimeout;
        this.rs.setIoTimeout((int) ioTimeout.getMillis());
        return this;
    }

    @Override public UrlHttpClient setUrl(Uri url) {
        this.defaultUrl = url;
        return this;
    }

    @Override public HostHttpClient dropUrl() {
        this.defaultUrl = null;
        return this;
    }

    @Override public HttpClient followRedirection(boolean followRedirection) {
        this.followRedirection = followRedirection;
        return this;
    }

    @Override public HttpClient followRedirection() {
        return followRedirection(true);
    }

    @Override public boolean isFollowRedirection() {
        return followRedirection;
    }

    @Override public HttpClient cutBodyToStringAt(Integer cutBodyToStringAt) {
        this.cutBodyToStringAt = cutBodyToStringAt;
        return this;
    }

    @Override public Integer getCutBodyToStringAt() {
        return cutBodyToStringAt;
    }

    @Override public void close() {
        rs.close();
    }

    private <E extends Exception> HttpResponse request(Method method, Uri url, ThrowingConsumer<HttpRequest, E> setupOrNull, Set<Composition> visited) throws InterruptedException, HttpEntityParsingException, IOException, E {
        HttpRequest hreq = new HttpRequest(HttpVersion.HTTP_1_1, method);
        hreq.setCutBodyToStringAt(cutBodyToStringAt);
        hreq.setUrl(url);
        if (keepAliveTimeout != null) { hreq.setConnectionKeepAlive(keepAliveTimeout.getMillis()); }
        if (setupOrNull != null) { setupOrNull.accept(hreq); }

        HttpResponse hresp;
        try (Locked ignored = new Locked(sync)) {
            log.hint("Sending HTTP Request " + Spell.get(hreq));
            Ticker ticker = new Ticker();
            try {
                hresp = rs.use(s -> {
                    hreq.writeToStream(s.getOutputStream());
                    return HttpResponse.parse(s.getInputStream(), ioTimeout.getMillis());
                });
            } catch (InterruptedException | RuntimeException | HttpEntityParsingException | IOException e) {
                log.hint("Request failed " + ticker);
                throw e;
            } catch (Exception e) {
                log.hint("Request failed " + ticker + " with unexpected cause " + Spell.get(e));
                throw new RuntimeException(e);
            } finally {
                if (keepAliveTimeout == null) {
                    rs.close();
                }
            }
            hresp.setCutBodyToStringAt(cutBodyToStringAt);
            log.hint("Received HTTP Response " + ticker + ": " + Spell.get(hresp));
        }
        return hresp.getStatus().isRedirection() && followRedirection ? followIfPossible(method, url, setupOrNull, hresp, visited) : hresp;
    }

    private <E extends Exception> HttpResponse followIfPossible(Method method, Uri currentUrl, ThrowingConsumer<HttpRequest, E> setupOrNull, HttpResponse hresp, Set<Composition> visited) throws InterruptedException, HttpEntityParsingException, IOException, E {
        HttpStatus hs = hresp.getStatus();
        String location = hresp.headers.get("Location");
        try {
            if (location != null) {
                String setCookie = hresp.headers().get("Set-Cookie");
                if ((HttpStatus._301_MOVED_PERMANENTLY.equals(hs) && (method == Method.GET || method == Method.HEAD)) ||
                    (HttpStatus._307_TEMPORARY_REDIRECT.equals(hs) || HttpStatus._308_PERMANENT_REDIRECT.equals(hs))) {
                    return follow(location, setCookie, method, currentUrl, setupOrNull, visited);
                } else if ((HttpStatus._302_FOUND.equals(hs) || HttpStatus._303_SEE_OTHER.equals(hs))) {
                    // фишки: код 302 и 303 сбрасывает методы POST и PUT в GET.
                    return follow(location, setCookie, (method == Method.POST || method == Method.PUT) ? Method.GET : method, currentUrl, setupOrNull, visited);
                }
            }
        } catch (UriParsingException e) {
            throw new HttpEntityParsingException("Error parsing Location header for following: " + Spell.get(location));
        }
        return hresp;
    }

    private <E extends Exception> HttpResponse follow(String location, String setCookie, Method method, Uri currentUrl, ThrowingConsumer<HttpRequest, E> setupOrNull, Set<Composition> visited) throws InterruptedException, HttpEntityParsingException, IOException, E, UriParsingException {
        ThrowingConsumer<HttpRequest, E> setup;
        Composition reference;
        { // Прицепим любые куки, какие найдём. Заодно создадим рефренс для проверки, что с такими куками нас ещё не отправляли на данный урл
            String[] cookies = StringUtils.parseDelimited(setCookie, HttpEntity.HEADER_VALUE_DELIMITER);
            if (cookies == null) {
                setup = setupOrNull;
                reference = new Composition(location);
            } else {
                StringDumpBuilder sdb = new StringDumpBuilder("; ");
                ArrayUtils.forEach(cookies, (i, cookie) -> sdb.append(cookie.contains(";") ? cookie.substring(0, cookie.indexOf(';')) : cookie));
                String cookie = sdb.toString();
                ThrowingConsumer<HttpRequest, E> setCookieSetup = hreq -> hreq.headers().merge("Cookie", cookie, (a, b) -> a + "; " + b);
                setup = setupOrNull == null ? setCookieSetup : hreq -> {
                    setupOrNull.accept(hreq);
                    setCookieSetup.accept(hreq);
                };
                reference = new Composition(location, cookie);
            }
        }

        if (visited == null) {
            visited = new HashSet<>();
            visited.add(reference);
        } else if (!visited.add(reference)) {
            throw new HttpEntityParsingException("Redirection loop detected for Location " + Spell.get(location));
        }

        Uri targetUrl = Uri.parseUri(location);
        log.hint("Following " + Spell.get(targetUrl));
        if (targetUrl.getHostname() == null) {
            // форвард на новый путь, используем текущее подключение.
            targetUrl.setScheme(currentUrl.getScheme());
            targetUrl.setUsername(currentUrl.getUsername());
            targetUrl.setPassword(currentUrl.getPassword());
            targetUrl.setHostname(currentUrl.getHostname());
            targetUrl.setPort(currentUrl.getPort());
            return request(method, targetUrl, setup, visited);
        } else {
            // форвард на новый урл
            HttpClient hc = new HttpClient(log, schemeSocketFactoriesOrNull, targetUrl.getScheme(), targetUrl.getHostname(), targetUrl.getPort());
            hc.cutBodyToStringAt = this.cutBodyToStringAt;
            hc.followRedirection = true;
            hc.keepAliveTimeout = null;
            hc.ioTimeout = this.ioTimeout;
            return hc.request(method, targetUrl, setup, visited);
        }
    }

    @Override public <E extends Exception> HttpResponse request(Method method, Uri url, ThrowingConsumer<HttpRequest, E> setupOrNull) throws InterruptedException, HttpEntityParsingException, IOException, E {
        return request(method, url, setupOrNull, null);
    }

    @Override public <E extends Exception> HttpResponse request(Method method, ThrowingConsumer<HttpRequest, E> setup) throws InterruptedException, HttpEntityParsingException, IOException, E {
        return request(method, defaultUrl, setup, null);
    }

    // === statics ===
    // --- urlless factories
    public static HostHttpClient forHost(LogDome log, String hostname) {
        return new HttpClient(log, null, null, hostname, null);
    }

    public static HostHttpClient forHost(LogDome log, String hostname, Integer portOrNull) {
        return new HttpClient(log, null, null, hostname, portOrNull);
    }

    public static HostHttpClient forHost(LogDome log, String scheme, String hostname, Integer portOrNull) {
        return new HttpClient(log, null, scheme, hostname, portOrNull);
    }

    public static HostHttpClient forHost(LogDome log, Uri url) {
        return new HttpClient(log, null, url.getScheme(), url.getHostname(), url.getPort());
    }

    public static HostHttpClient forHost(LogDome log, SocketFactory httpsSocketFactoryOrNull, String hostname) {
        return new HttpClient(log, httpsSocketFactoryOrNull == null ? null : Collections.singletonMap(HttpConst.HTTPS_SCHEME, httpsSocketFactoryOrNull), null, hostname, null);
    }

    public static HostHttpClient forHost(LogDome log, SocketFactory httpsSocketFactoryOrNull, String hostname, Integer portOrNull) {
        return new HttpClient(log, httpsSocketFactoryOrNull == null ? null : Collections.singletonMap(HttpConst.HTTPS_SCHEME, httpsSocketFactoryOrNull), null, hostname, portOrNull);
    }

    public static HostHttpClient forHost(LogDome log, SocketFactory httpsSocketFactoryOrNull, String scheme, String hostname, Integer portOrNull) {
        return new HttpClient(log, httpsSocketFactoryOrNull == null ? null : Collections.singletonMap(HttpConst.HTTPS_SCHEME, httpsSocketFactoryOrNull), scheme, hostname, portOrNull);
    }

    public static HostHttpClient forHost(LogDome log, SocketFactory httpsSocketFactoryOrNull, Uri url) {
        return new HttpClient(log, httpsSocketFactoryOrNull == null ? null : Collections.singletonMap(HttpConst.HTTPS_SCHEME, httpsSocketFactoryOrNull), url.getScheme(), url.getHostname(), url.getPort());
    }

    public static HostHttpClient forHost(LogDome log, Map<String, SocketFactory> schemeSocketFactoriesOrNull, String hostname) {
        return new HttpClient(log, schemeSocketFactoriesOrNull, null, hostname, null);
    }

    public static HostHttpClient forHost(LogDome log, Map<String, SocketFactory> schemeSocketFactoriesOrNull, String hostname, Integer portOrNull) {
        return new HttpClient(log, schemeSocketFactoriesOrNull, null, hostname, portOrNull);
    }

    public static HostHttpClient forHost(LogDome log, Map<String, SocketFactory> schemeSocketFactoriesOrNull, String scheme, String hostname, Integer portOrNull) {
        return new HttpClient(log, schemeSocketFactoriesOrNull, scheme, hostname, portOrNull);
    }

    public static HostHttpClient forHost(LogDome log, Map<String, SocketFactory> schemeSocketFactoriesOrNull, Uri url) {
        return new HttpClient(log, schemeSocketFactoriesOrNull, url.getScheme(), url.getHostname(), url.getPort());
    }

    // --- url factories
    public static UrlHttpClient forUrl(LogDome log, Uri uri) {
        return new HttpClient(log, null, uri.getScheme(), uri.getHostname(), uri.getPort()).setUrl(uri);
    }

    public static UrlHttpClient forUrl(LogDome log, SocketFactory httpsSocketFactoryOrNull, Uri uri) {
        return new HttpClient(log, httpsSocketFactoryOrNull == null ? null : Collections.singletonMap(HttpConst.HTTPS_SCHEME, httpsSocketFactoryOrNull), uri.getScheme(), uri.getHostname(), uri.getPort()).setUrl(uri);
    }

    public static UrlHttpClient forUrl(LogDome log, Map<String, SocketFactory> schemeSocketFactoriesOrNull, Uri uri) {
        return new HttpClient(log, schemeSocketFactoriesOrNull, uri.getScheme(), uri.getHostname(), uri.getPort()).setUrl(uri);
    }

}
