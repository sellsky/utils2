package tk.bolovsrol.utils.socket.server;

import java.io.IOException;
import java.net.ServerSocket;

public class PlainServerSocketFactory implements ServerSocketFactory {
    private static final PlainServerSocketFactory STATIC = new PlainServerSocketFactory();

    public static PlainServerSocketFactory getStatic() {
        return STATIC;
    }

    private PlainServerSocketFactory() {}

    public ServerSocket newServerSocket() throws IOException {
        return new ServerSocket();
    }

    public String getCaption() {
        return "plain";
    }

    @Override public String toString() {
        // не очень-то понятно, что тут показывать
        return getCaption();
    }

}