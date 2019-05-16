package tk.bolovsrol.utils.http.server;

import tk.bolovsrol.utils.PatternCompileException;
import tk.bolovsrol.utils.RegexUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.http.HttpAuthorization;
import tk.bolovsrol.utils.http.HttpConst;
import tk.bolovsrol.utils.http.Method;
import tk.bolovsrol.utils.socket.server.PlainServerSocketFactory;
import tk.bolovsrol.utils.socket.server.ServerSocketFactory;

import java.net.InetSocketAddress;
import java.util.regex.Pattern;

/**
 * Построитель {@link HttpEndpoint}:ов.
 * <p/>
 * Сеттеры можно выстраивать в цепочку.
 * <p/>
 * Исходная конфигурация:
 * <ul><li>ssl = false, serverSocketFactory = {@link PlainServerSocketFactory#getStatic()};
 * <li>hostname = null;
 * <li>port = {@link HttpConst#DEFAULT_PORT};
 * <li>method = {@link Method#GET};
 * <li>path = "/".</ul>
 */
public class HttpEndpointBuilder extends BiprotocolSocketEndpointBuilder {

    public static final Pattern HTTP_PATTERN;
    public static final Pattern HTTPS_PATTERN;

    static {
        try {
            HTTP_PATTERN = RegexUtils.compilePattern("^http$");
            HTTPS_PATTERN = RegexUtils.compilePattern("^https$");
        } catch (PatternCompileException e) {
            throw new RuntimeException(e); // не будет
        }
    }

    public HttpEndpointBuilder() {
    }

    private Method method = Method.GET;
    private String path = "/";
    private HttpAuthorization authorization = null;

    /**
     * Создаёт новый {@link HttpEndpoint} на основании сделанных ранее настроек.
     *
     * @return новый HttpEndpoint
     */
    @Override
    public HttpEndpoint newEndpoint() throws UnexpectedBehaviourException {
        return new HttpEndpoint(getServerSocketFactory(), getBindInetSocketAddress(), method, path, authorization);
    }

    /**
     * Разбирает поля переданного URL:
     * <ul><li>проверяет, что схема {@link Uri#getScheme()} возвращает <code>http</code> или <code>https</code> и вызывает {@link #setSsl(boolean)} соответственно с false или true;
     * <li>если {@link Uri#getHostname()} пуст, устанавливается {@link #setBindHostname(String)} со значением null, иначе имя хоста переносится как есть;
     * <li>{@link Uri#getPort()} если не null, переносится как есть, иначе используется  порт по умолчанию в зависимости от использования ssl ({@link HttpConst#DEFAULT_PORT} или {@link HttpConst#DEFAULT_SSL_PORT};
     * <li>{@link Uri#getPath()} если не null переносится как есть, иначе устанавливается в <code>/</code>.</ul>
     * <p/>
     * Остальные поля переданного url:а роли не играют.
     *
     * @param url
     * @return билдер
     * @throws UnexpectedBehaviourException невозможно установить требуемую схему
     */
    public HttpEndpointBuilder parseUrl(Uri url) throws UnexpectedBehaviourException {
        super.parseUrl(url, HTTP_PATTERN, HttpConst.DEFAULT_PORT, HTTPS_PATTERN, HttpConst.DEFAULT_SSL_PORT);
        setPath(url.getPath());
        return this;
    }

    /**
     * Устанавливает режим сокетов, а также вызывает метод, создающий сокет соответствующего типа
     * {@link #newPlainServerSocketFactory()} или {@link #newSslServerSocketFactory()}.
     *
     * @param ssl
     * @return билдер
     * @throws UnexpectedBehaviourException не удалось создать фабрику сокетов
     */
    @Override
    public HttpEndpointBuilder setSsl(boolean ssl) throws UnexpectedBehaviourException {
        super.setSsl(ssl);
        return this;
    }

    /**
     * Устанавливает имя хоста, к которому сокет будет прибинден.
     * <p/>
     * Можно установить null, это обозначает все локальные интерфейсы.
     *
     * @return билдер
     */
    @Override
    public HttpEndpointBuilder setBindHostname(String hostname) {
        super.setBindHostname(hostname);
        return this;
    }

    @Override
    public HttpEndpointBuilder setBindPort(int port) {
        super.setBindPort(port);
        return this;
    }

    @Override
    public HttpEndpointBuilder setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        super.setInetSocketAddress(inetSocketAddress);
        return this;
    }

    @Override
    public HttpEndpointBuilder setPlainServerSocketFactory(ServerSocketFactory plainServerSocketFactory) {
        super.setPlainServerSocketFactory(plainServerSocketFactory);
        return this;
    }

    @Override
    public HttpEndpointBuilder setSslServerSocketFactory(ServerSocketFactory sslServerSocketFactory) {
        super.setSslServerSocketFactory(sslServerSocketFactory);
        return this;
    }

    public HttpEndpointBuilder setAuthorization(HttpAuthorization authorization) {
        this.authorization = authorization;
        return this;
    }

    public HttpEndpointBuilder removeAuthorization() {
        this.authorization = null;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public HttpEndpointBuilder setMethod(Method method) {
        this.method = method;
        return this;
    }

    public String getPath() {
        return path;
    }

    public HttpEndpointBuilder setPath(String path) {
        if (path == null || path.isEmpty()) {
            this.path = "/";
        } else if (path.startsWith("/")) {
            this.path = path;
        } else {
            this.path = '/' + path;
        }
        return this;
    }

    public HttpEndpointBuilder appendPath(String path) {
        if (path != null && !path.isEmpty()) {
            if (this.path.endsWith("/")) {
                this.path += path;
            } else {
                this.path = this.path + '/' + path;
            }
        }
        return this;
    }
}
