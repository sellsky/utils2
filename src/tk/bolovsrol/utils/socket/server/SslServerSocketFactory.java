package tk.bolovsrol.utils.socket.server;

import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.conf.InvalidConfigurationException;
import tk.bolovsrol.utils.conf.Param;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;
import tk.bolovsrol.utils.socket.CustomSslContextException;
import tk.bolovsrol.utils.socket.SslContextGenerator;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.ServerSocket;

public class SslServerSocketFactory implements ServerSocketFactory {

    public static class ServerSslContextConf extends SslContextGenerator.SslContextConf {
        @Param(key = "server.useClientMode", desc = "сервер будет играть роль ssl-клиента")
        public boolean serverUseClientMode = false;

        @Param(key = "server.clientAuthMode", desc = "режим ожидания клиентской авторизации сервером")
        public ServerClientAuthMode serverClientAuthMode = ServerClientAuthMode.NONE;
    }

    private static final SslServerSocketFactory STATIC;

    static {
        try {
            STATIC = newFactory(Log.getInstance(), Cfg.getInstance().getBranch("ssl."));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Возвращает статичный ссл-провайдер, созданный на основании конфига по умолчанию.
     *
     * @return статичный ссл-провайдер
     */
    public static SslServerSocketFactory getStatic() {
        return STATIC;
    }

    private SSLContext context;
    private boolean serverUseClientMode;
    private ServerClientAuthMode serverClientAuthMode;

    public SslServerSocketFactory(SSLContext context, ServerClientAuthMode serverClientAuthMode) {
        this(context, false, serverClientAuthMode);
    }

    public SslServerSocketFactory(SSLContext context, boolean serverUseClientMode, ServerClientAuthMode serverClientAuthMode) {
        this.context = context;
        this.serverUseClientMode = serverUseClientMode;
        this.serverClientAuthMode = serverClientAuthMode;
    }

    public enum ServerClientAuthMode {
        NONE {
            @Override public void setupClientAuth(SSLServerSocket sslServerSocket) {
                sslServerSocket.setWantClientAuth(false);
                sslServerSocket.setNeedClientAuth(false);
            }
        },
        WANT {
            @Override public void setupClientAuth(SSLServerSocket sslServerSocket) {
                sslServerSocket.setWantClientAuth(true);
            }
        },
        NEED {
            @Override public void setupClientAuth(SSLServerSocket sslServerSocket) {
                sslServerSocket.setNeedClientAuth(true);
            }
        };

        public abstract void setupClientAuth(SSLServerSocket sslServerSocket);
    }

    @Override public ServerSocket newServerSocket() throws IOException {
        SSLServerSocket serverSocket = (SSLServerSocket) context.getServerSocketFactory().createServerSocket();
        serverSocket.setUseClientMode(serverUseClientMode);
        serverClientAuthMode.setupClientAuth(serverSocket);
        return serverSocket;
    }

    /**
     * Возвращает новый ссл-сокет-провайдер.
     * В передаваемых пропертях имеют смысл такие параметры серверного сокета:
     * <ul>
     * <li>ssl.server.useClientMode: создавать серверный сокет в клиентском режиме;
     * <li>ssl.server.clientAuthMode: режим авторизации клиента: NONE (по умолчанию), WANT (запрашиваем) или NEED (запрашиваем, и если нет, то сессия не устанавливается).
     * </ul>
     *
     * @param log
     * @param cfg
     * @return новый ссл-сокет-провайдер
     * @throws UnexpectedBehaviourException
     * @see #getStatic()
     * @see SslContextGenerator#newSslContext(LogDome, ReadOnlyProperties)
     */
    public static SslServerSocketFactory newFactory(LogDome log, ReadOnlyProperties cfg) throws InvalidConfigurationException, CustomSslContextException {
        ServerSslContextConf conf = new ServerSslContextConf();
        conf.load(log, cfg);
        return newFactory(log, conf);
    }

    public static SslServerSocketFactory newFactory(LogDome log, ServerSslContextConf conf) throws CustomSslContextException {
        return new SslServerSocketFactory(
              SslContextGenerator.newSslContext(log, conf),
              conf.serverUseClientMode,
              conf.serverClientAuthMode
        );
    }


    @Override
    public String getCaption() {
        return "secure-" + context.getProtocol();
    }

    @Override
    public String toString() {
        // не очень-то понятно, что тут показывать
        return getCaption();
    }

    public boolean isServerUseClientMode() {
        return serverUseClientMode;
    }

    public void setServerUseClientMode(boolean serverUseClientMode) {
        this.serverUseClientMode = serverUseClientMode;
    }

    public ServerClientAuthMode getServerClientAuthMode() {
        return serverClientAuthMode;
    }

    public void setServerClientAuthMode(ServerClientAuthMode serverClientAuthMode) {
        this.serverClientAuthMode = serverClientAuthMode;
    }
}