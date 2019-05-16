package tk.bolovsrol.utils.socket.client;

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
import javax.net.ssl.SSLSocket;
import java.io.IOException;

public class SslSocketFactory implements SocketFactory {

    public static class ClientSslContextConf extends SslContextGenerator.SslContextConf {
        @Param(key = "client.useServerMode", desc = "клиент будет играть роль ssl-сервера")
        public boolean clientUseServerMode = false;
    }

    private static final SslSocketFactory DEFAULT;

    static {
        try {
            DEFAULT = forConf(Log.getInstance(), Cfg.getInstance().getBranch("ssl."));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Возвращает статичный ссл-провайдер, созданный на основании ветки <code>ssl.</code> конфига по умолчанию.
     *
     * @return статичный ссл-провайдер
     * @see Cfg#getInstance()
     * @see #forConf(LogDome, ReadOnlyProperties)
     */
    public static SslSocketFactory getDefault() {
        return DEFAULT;
    }

    private SSLContext context;
    private boolean clientUseServerMode;

    public SslSocketFactory(SSLContext context, boolean clientUseServerMode) {
        this.context = context;
        this.clientUseServerMode = clientUseServerMode;
    }

    @Override
    public SSLSocket newSocket() throws IOException {
        SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket();
        socket.setUseClientMode(!clientUseServerMode);
        return socket;
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
     * @see #getDefault()
     * @see SslContextGenerator#newSslContext(LogDome, ReadOnlyProperties)
     */
    public static SslSocketFactory forConf(LogDome log, ReadOnlyProperties cfg) throws CustomSslContextException, InvalidConfigurationException {
        ClientSslContextConf conf = new ClientSslContextConf();
        conf.load(log, cfg);
        return forConf(log, conf);
    }

    public static SslSocketFactory forConf(LogDome log, ClientSslContextConf conf) throws CustomSslContextException {
        return new SslSocketFactory(SslContextGenerator.newSslContext(log, conf), conf.clientUseServerMode);
    }

    /**
     * Создаёт фабрику, использующую сертификаты указанного хранилища, используя приятные умолчания для прочих настроек.
     *
     * @param log
     * @param jksFileName
     * @param storePassword
     * @return
     * @throws CustomSslContextException
     */
    public static SslSocketFactory forJks(LogDome log, String jksFileName, String storePassword) throws CustomSslContextException {
        return new SslSocketFactory(SslContextGenerator.forJks(log, jksFileName, storePassword), false);
    }

    /**
     * Фабрика сокетов, которые доверяют содержащемуся в файле сертификату, и без приватных ключей.
     *
     * @param certFileName
     * @return
     * @throws CustomSslContextException
     */
    public static SslSocketFactory forCert(String certFileName) throws CustomSslContextException {
        return new SslSocketFactory(SslContextGenerator.forCert(certFileName), false);
    }

    /**
     * Фабрика доверчивых ссл-сокетов, которые верят любому сертификату.
     *
     * @return
     * @throws CustomSslContextException
     */
    public static SslSocketFactory gullible() throws CustomSslContextException {
        return new SslSocketFactory(SslContextGenerator.gullible(), false);
    }

    @Override public String getCaption() {
        return "ssl/" + context.getProtocol();
    }

    @Override
    public String toString() {
        // не очень-то понятно, что тут показывать
        return getCaption();
    }
}
