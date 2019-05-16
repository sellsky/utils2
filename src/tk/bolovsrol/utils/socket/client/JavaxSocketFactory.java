package tk.bolovsrol.utils.socket.client;

import java.io.IOException;
import java.net.Socket;

/** Прокся-адаптер для джавной {@link javax.net.SocketFactory}. */
public class JavaxSocketFactory implements SocketFactory {

    private final javax.net.SocketFactory javaSocketFactory;

    public JavaxSocketFactory(javax.net.SocketFactory javaSocketFactory) {
        this.javaSocketFactory = javaSocketFactory;
    }

    @Override public Socket newSocket() throws IOException {
        return javaSocketFactory.createSocket();
    }

    @Override public String getCaption() {
        return javaSocketFactory.toString();
    }

    @Override
    public String toString() {
        return javaSocketFactory.toString();
    }

}
