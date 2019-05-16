package tk.bolovsrol.utils.socket.client;

import java.io.IOException;
import java.net.Socket;

public class PlainSocketFactory implements SocketFactory {
    private static final PlainSocketFactory STATIC = new PlainSocketFactory();

    public static PlainSocketFactory getStatic() {
        return STATIC;
    }

    private PlainSocketFactory() {
    }

    @Override public Socket newSocket() throws IOException {
        return new Socket();
    }

    @Override public String getCaption() {
        return "plain";
    }

    @Override public String toString() {
        // не очень-то понятно, что тут показывать
        return getCaption();
    }

}
