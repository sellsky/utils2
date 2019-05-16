package tk.bolovsrol.utils.http.server;

import tk.bolovsrol.utils.*;
import tk.bolovsrol.utils.http.HttpConst;
import tk.bolovsrol.utils.socket.SocketEndpoint;
import tk.bolovsrol.utils.socket.server.PlainServerSocketFactory;
import tk.bolovsrol.utils.socket.server.ServerSocketFactory;
import tk.bolovsrol.utils.socket.server.SslServerSocketFactory;

import java.net.InetSocketAddress;
import java.util.regex.Pattern;

/**
 * Создаёт эндпоинт с одним из двух сокет-протоколов,
 * используя простой сокет-коннектор или ссльный.
 */
public class BiprotocolSocketEndpointBuilder {

    public BiprotocolSocketEndpointBuilder() {
    }

    private boolean ssl = false;
    private ServerSocketFactory plainServerSocketFactory;
    private ServerSocketFactory sslServerSocketFactory;

    private String bindHostname = null;
    private int bindPort;
    private InetSocketAddress bindInetSocketAddress = null;

    public SocketEndpoint newEndpoint() throws UnexpectedBehaviourException {
        return new SocketEndpoint(getServerSocketFactory(), getBindInetSocketAddress());
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
     * @param defaultPlainPort
     * @param defaultSslPort
     * @return билдер
     * @throws UnexpectedBehaviourException невозможно установить требуемую схему
     */
    protected void parseUrl(Uri url, Pattern plainPattern, int defaultPlainPort, Pattern sslPattern, int defaultSslPort) throws UnexpectedBehaviourException {
        if (RegexUtils.matches(plainPattern, url.getScheme())) {
            setSsl(false);
        } else if (RegexUtils.matches(sslPattern, url.getScheme())) {
            setSsl(true);
        } else {
            throw new UnexpectedBehaviourException("Unexpected URL scheme " + Spell.get(url.getScheme()));
        }
        setBindHostname("".equals(url.getHostname()) ? null : url.getHostname());
        setBindPort(url.getPortIntValue(ssl ? defaultSslPort : defaultPlainPort));
    }

    /**
     * Устанавливает режим сокетов, а также вызывает метод, создающий сокет соответствующего типа
     * {@link #newPlainServerSocketFactory()} или {@link #newSslServerSocketFactory()}.
     *
     * @param ssl
     * @return билдер
     * @throws UnexpectedBehaviourException не удалось создать фабрику сокетов
     */
    public BiprotocolSocketEndpointBuilder setSsl(boolean ssl) throws UnexpectedBehaviourException {
        this.ssl = ssl;
        return this;
    }

    /**
     * Возвращает новую фабрику серверных сокетов для дальнейшего использования в конечных точках.
     * <p/>
     * Рекомендуется переопределять этот метод для тонкой настройки.
     * В реализации по умолчанию возвращается {@link PlainServerSocketFactory#getStatic()}.
     * При создании билдера этот метод не вызывается.
     *
     * @return фабрика сокетов для дальнейшего использования билдером
     * @throws UnexpectedBehaviourException не удалось создать фабрику сокетов
     */
    protected PlainServerSocketFactory newPlainServerSocketFactory() throws UnexpectedBehaviourException {
        return PlainServerSocketFactory.getStatic();
    }

    /**
     * Возвращает новую фабрику защищённых серверных сокетов для дальнейшего использования в конечных точках.
     * <p/>
     * Рекомендуется переопределять этот метод для тонкой настройки.
     * В реализации по умолчанию возвращается {@link SslServerSocketFactory#getStatic()}.
     *
     * @return фабрика сокетов для дальнейшего использования билдером
     * @throws UnexpectedBehaviourException не удалось создать фабрику сокетов
     */
    protected SslServerSocketFactory newSslServerSocketFactory() throws UnexpectedBehaviourException {
        return SslServerSocketFactory.getStatic();
    }

    public boolean isSsl() {
        return ssl;
    }

    public ServerSocketFactory getServerSocketFactory() throws UnexpectedBehaviourException {
        if (ssl) {
            return sslServerSocketFactory == null ? newSslServerSocketFactory() : sslServerSocketFactory;
        } else {
            return plainServerSocketFactory == null ? newPlainServerSocketFactory() : plainServerSocketFactory;
        }
    }


    /**
     * Устанавливает имя хоста, к которому сокет будет прибинден.
     * <p/>
     * Можно установить null, это обозначает все локальные интерфейсы.
     *
     * @return билдер
     */
    public BiprotocolSocketEndpointBuilder setBindHostname(String bindHostname) {
        if (!StringUtils.equals(this.bindHostname, bindHostname)) {
            this.bindHostname = bindHostname;
            this.bindInetSocketAddress = null;
        }
        return this;
    }

    public BiprotocolSocketEndpointBuilder setBindPort(int bindPort) {
        if (this.bindPort != bindPort) {
            this.bindPort = bindPort;
            this.bindInetSocketAddress = null;
        }
        return this;
    }

    public BiprotocolSocketEndpointBuilder setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.bindInetSocketAddress = inetSocketAddress;
        this.bindHostname = inetSocketAddress.getHostName();
        this.bindPort = inetSocketAddress.getPort();
        return this;
    }

    public BiprotocolSocketEndpointBuilder setPlainServerSocketFactory(ServerSocketFactory plainServerSocketFactory) {
        this.plainServerSocketFactory = plainServerSocketFactory;
        return this;
    }

    public BiprotocolSocketEndpointBuilder setSslServerSocketFactory(ServerSocketFactory sslServerSocketFactory) {
        this.sslServerSocketFactory = sslServerSocketFactory;
        return this;
    }

    public String getBindHostname() {
        return bindHostname;
    }

    public int getBindPort() {
        return bindPort;
    }

    public InetSocketAddress getBindInetSocketAddress() {
        return this.bindInetSocketAddress == null ?
                (bindHostname == null ? new InetSocketAddress(bindPort) : new InetSocketAddress(bindHostname, bindPort))
                : bindInetSocketAddress;
    }

}