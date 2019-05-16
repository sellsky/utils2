package tk.bolovsrol.utils.http.server;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.http.AmbigousRequestProcessorMappingException;
import tk.bolovsrol.utils.http.HttpRequestMethodPathMapper;
import tk.bolovsrol.utils.http.HttpRequestProcessor;
import tk.bolovsrol.utils.http.Method;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.socket.EndpointAlreadyBoundException;
import tk.bolovsrol.utils.socket.EndpointBindFailedException;
import tk.bolovsrol.utils.socket.SocketProcessor;
import tk.bolovsrol.utils.socket.SocketServer;

/**
 * Простенький хттп-сервер. Позволяет клиентам биндиться на определённые адреса
 * по определённым путям, при этом он сам разруливает ситуации, когда несколько запросов висят
 * на одном порту и т. п.
 * <p/>
 * Пути с финальным слешем и без него считает эквивалентными.
 * <p/>
 * Синглтон.
 * <p/>
 * Ограничения: поддерживает протоколы HTTP и HTTP-через-SSL в рамках порта;
 * то есть, на один порт можно навесить либо только HTTP-, либо только HTTPS-процессоры.
 * <p/>
 * Настройки логов берутся стандартные + из ветки «httpServer.» стандартного конфига.
 */
public class HttpServer {

    private static final HttpServer INSTANCE = new HttpServer();

    private HttpServer() {
    }

    public static HttpServer server() {
        return INSTANCE;
    }

    private static class HttpServerSocketProcessor extends HttpSocketProcessor<HttpRequestMethodPathMapper> {
        private HttpServerSocketProcessor(LogDome log) {
            super(log, new HttpRequestMethodPathMapper(log));
        }
    }

    private final SocketServer socketServer = SocketServer.socketServer();
    private final LogDome log = LogDome.coalesce(Cfg.getBranch("httpServer."), socketServer.getLog());

    /**
     * Регистрирует процессор по указанному адресу.
     * <p/>
     * Протокол должен быть http или https.
     * <p/>
     * Имя хоста может быть пустое, это будет воспринято так же,
     * как «0.0.0.0» (все локальные адреса).
     * <p/>
     * Номер порта можно не указывать, тогда будет использован
     * соответствующий протоколу порт по умолчанию.
     * <p/>
     * Путь также может быть не указан, тогда будет использоваться корень.
     * <p/>
     * Остальные поля значения не имеют.
     *
     * @param bindUrl
     * @param httpRequestProcessor
     * @throws IllegalArgumentException
     * @see #registerProcessor(HttpEndpoint, HttpRequestProcessor)
     */
    public void registerProcessor(Uri bindUrl, Method method, HttpRequestProcessor httpRequestProcessor) throws UnexpectedBehaviourException {
        registerProcessor(new HttpEndpointBuilder().parseUrl(bindUrl).setMethod(method).newEndpoint(), httpRequestProcessor);
    }

    /**
     * Регистрирует процессор по указанному адресу.
     * <p/>
     * bindHostname может быть null, это значит то же, что «0.0.0.0» (все локальные адреса).
     *
     * @param bindHostname
     * @param bindPort
     * @param path
     * @param ssl
     * @param httpRequestProcessor
     * @see #registerProcessor(HttpEndpoint, HttpRequestProcessor)
     */
    public void registerProcessor(String bindHostname, int bindPort, Method method, String path, boolean ssl, HttpRequestProcessor httpRequestProcessor) throws UnexpectedBehaviourException {
        registerProcessor(new HttpEndpointBuilder().setBindHostname(bindHostname).setBindPort(bindPort).setMethod(method).setPath(path).setSsl(ssl).newEndpoint(), httpRequestProcessor);
    }

    /**
     * Регистрирует процессор на указанной точке.
     *
     * @param endpoint
     * @param httpRequestProcessor
     * @throws EndpointAlreadyBoundException
     * @see #registerProcessor(Uri, Method, HttpRequestProcessor)
     * @see #registerProcessor(String, int, Method, String, boolean, HttpRequestProcessor)
     */
    public synchronized void registerProcessor(HttpEndpoint endpoint, HttpRequestProcessor httpRequestProcessor) throws EndpointAlreadyBoundException, AmbigousRequestProcessorMappingException, EndpointBindFailedException {
        SocketProcessor socketProcessor = socketServer.get(endpoint);
        if (socketProcessor == null) {
            socketProcessor = new HttpServerSocketProcessor(log);
            socketServer.register(endpoint, socketProcessor);
        } else if (!(socketProcessor instanceof HttpServerSocketProcessor)) {
            throw new EndpointAlreadyBoundException("Alien processor is already bound to " + Spell.get(endpoint.getBindSocketAddress()));
        }
        ((HttpServerSocketProcessor) socketProcessor).getHttpRequestProcessor().addMapping(
                endpoint.getMethod(),
                endpoint.getPath(),
                endpoint.getAuthorization(),
                httpRequestProcessor
        );
        log.trace("Registered processor " + Spell.get(httpRequestProcessor) + " at " + Spell.get(endpoint));
    }

    /**
     * Разрегистрирует процессор, зарегистрированный по указанному адресу.
     *
     * @param uri
     * @return разрегистрированный процессор или null, если ничего зарегистрировано не было
     * @see #unregisterProcessor(HttpEndpoint, HttpRequestProcessor
     */
    public boolean unregisterProcessor(Uri uri, Method method, HttpRequestProcessor httpRequestProcessor) throws UnexpectedBehaviourException {
        return unregisterProcessor(new HttpEndpointBuilder().parseUrl(uri).setMethod(method).newEndpoint(), httpRequestProcessor);
    }

    /**
     * Разрегистрирует процессор, зарегистрированный по указанному адресу.
     *
     * @param bindHostname
     * @param bindPort
     * @param path
     * @return разрегистрированный процессор или null, если ничего зарегистрировано не было
     * @see #unregisterProcessor(HttpEndpoint, HttpRequestProcessor
     */
    public boolean unregisterProcessor(String bindHostname, int bindPort, Method method, String path, HttpRequestProcessor httpRequestProcessor) throws UnexpectedBehaviourException {
        return unregisterProcessor(new HttpEndpointBuilder().setBindHostname(bindHostname).setBindPort(bindPort).setMethod(method).setPath(path).newEndpoint(), httpRequestProcessor);
    }

    /**
     * Разрегистрирует процессор, зарегистрированный на указанной точке.
     *
     * @param endpoint
     * @return разрегистрированный процессор или null, если ничего зарегистрировано не было
     * @see #registerProcessor(Uri, Method, HttpRequestProcessor)
     * @see #registerProcessor(String, int, Method, String, boolean, HttpRequestProcessor)
     */
    public synchronized boolean unregisterProcessor(HttpEndpoint endpoint, HttpRequestProcessor httpRequestProcessor) {
        SocketProcessor socketProcessor = socketServer.get(endpoint);
        if (!(socketProcessor instanceof HttpServerSocketProcessor)) {
            return false;
        }
        HttpRequestMethodPathMapper mapper = ((HttpServerSocketProcessor) socketProcessor).getHttpRequestProcessor();
        if (mapper.removeMapping(endpoint.getMethod(), endpoint.getPath(), httpRequestProcessor)) {
            if (mapper.isEmpty()) {
                socketServer.unregister(endpoint);
            }

            log.trace("Unregistered processor " + Spell.get(httpRequestProcessor) + " at " + Spell.get(endpoint));
            return true;
        } else {
            return false;
        }
    }

}