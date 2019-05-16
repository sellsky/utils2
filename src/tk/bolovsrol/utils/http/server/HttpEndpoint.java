package tk.bolovsrol.utils.http.server;

import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.http.HttpAuthorization;
import tk.bolovsrol.utils.http.Method;
import tk.bolovsrol.utils.socket.SocketEndpoint;
import tk.bolovsrol.utils.socket.server.ServerSocketFactory;

import java.net.InetSocketAddress;

/**
 * Конечная точка, на которую следует монтировать http-процессор.
 * <p/>
 * Помимо наследованных сетевых параметров, содержит признаки метода
 * и пути запроса.
 *
 * @see HttpEndpointBuilder
 */
public class HttpEndpoint extends SocketEndpoint {
    private final Method method;
    private final String path;
    private final HttpAuthorization authorization;

    public HttpEndpoint(ServerSocketFactory socketFactory, InetSocketAddress socketAddress, Method method, String path, HttpAuthorization authorization) {
        super(socketFactory, socketAddress);
        this.method = method;
        this.path = path;
        this.authorization = authorization;
    }

    public HttpEndpoint(ServerSocketFactory socketFactory, InetSocketAddress socketAddress, Method method, String path) {
        this(socketFactory, socketAddress, method, path, null);
    }

    public Method getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public HttpAuthorization getAuthorization() {
        return authorization;
    }

    @Override
    public String toString() {
        return new StringDumpBuilder()
                .append(super.toString())
                .append("method", method)
                .append("path", path)
                .append("authorization", authorization)
                .toString();
    }

}
