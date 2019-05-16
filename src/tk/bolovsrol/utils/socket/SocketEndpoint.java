package tk.bolovsrol.utils.socket;

import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.socket.server.ServerSocketFactory;

import java.net.InetSocketAddress;

public class SocketEndpoint {

    private final ServerSocketFactory serverSocketFactory;
    private final InetSocketAddress bindSocketAddress;

    public SocketEndpoint(ServerSocketFactory serverSocketFactory, InetSocketAddress bindSocketAddress) {
        this.serverSocketFactory = serverSocketFactory;
        this.bindSocketAddress = bindSocketAddress;
    }

    public ServerSocketFactory getSocketFactory() {
        return serverSocketFactory;
    }

    public InetSocketAddress getBindSocketAddress() {
        return bindSocketAddress;
    }

    @Override public String toString() {
        return new StringDumpBuilder()
                .append("serverSocketFactory", serverSocketFactory)
                .append("socketAddress", bindSocketAddress)
                .toString();
    }
}
