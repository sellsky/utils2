package tk.bolovsrol.utils.socket;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.conf.InvalidConfigurationException;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Здесь можно зарегистрировать некий сокет-процессор,
 * навесив его на опредлеёный SocketEndpoint.
 * <p/>
 * А также разрегистрировать его обратно.
 */
public class SocketServer {

    private static final SocketServer INSTANCE = newStaticSocketServer();

    public static SocketServer socketServer() {
        return INSTANCE;
    }

    private static SocketServer newStaticSocketServer() {
        ReadOnlyProperties cfg = Cfg.getBranch("socketServer.");
        LogDome log = LogDome.coalesce(cfg, Log.getInstance());
        ProcessorSocketListener.Conf pslConf = new ProcessorSocketListener.Conf();
        try {
            pslConf.load(log, cfg);
        } catch (InvalidConfigurationException e) {
            throw new IllegalArgumentException("Error initializing Socket Server", e);
        }
        return new SocketServer(log, pslConf);
    }

    private final LogDome log;
    private final ProcessorSocketListener.Conf pslConf;
    private final Map<InetSocketAddress, ProcessorSocketListener> bound = new HashMap<InetSocketAddress, ProcessorSocketListener>();

    private SocketServer(LogDome log, ProcessorSocketListener.Conf pslConf) {
        this.log = log;
        this.pslConf = pslConf;
    }

    public synchronized void register(SocketEndpoint socketEndpoint, SocketProcessor socketProcessor) throws EndpointAlreadyBoundException, EndpointBindFailedException {
        checkAddress(socketEndpoint);
        startNewListener(socketEndpoint, socketProcessor);
    }

    private void checkAddress(SocketEndpoint socketEndpoint) throws EndpointAlreadyBoundException {
        ProcessorSocketListener alreadyBound = bound.get(socketEndpoint.getBindSocketAddress());
        if (alreadyBound != null) {
            throw new EndpointAlreadyBoundException("Endpoint " + socketEndpoint.getBindSocketAddress() + " already bound");
        }
    }

    private void startNewListener(SocketEndpoint socketEndpoint, SocketProcessor socketProcessor) throws EndpointBindFailedException {
        ProcessorSocketListener socketListener = null;
        try {
            socketListener = new ProcessorSocketListener(
                    log,
                    socketEndpoint,
                    socketProcessor,
                    pslConf
            );
            socketListener.setDaemon(true);
            socketListener.start();
        } catch (Exception e) {
            throw new EndpointBindFailedException("Error binding endpoint " + Spell.get(socketEndpoint), e);
        }
        bound.put(socketEndpoint.getBindSocketAddress(), socketListener);
    }

    public synchronized SocketProcessor get(SocketEndpoint socketEndpoint) {
        ProcessorSocketListener listener = bound.get(socketEndpoint.getBindSocketAddress());
        return listener == null ? null : listener.getSocketProcessor();
    }

    public synchronized SocketProcessor unregister(SocketEndpoint socketEndpoint) {
        final ProcessorSocketListener listener = bound.remove(socketEndpoint.getBindSocketAddress());
        if (listener == null) {
            return null;
        }
        new Thread(listener.getName() + "-killer") {
            @Override public void run() {
                try {
                    listener.shutdown();
                } catch (Exception e) {
                    log.exception("Error shutting down listener " + Spell.get(listener) + ", exception follows");
                    log.exception(e);
                }
            }
        }.start();
        return listener.getSocketProcessor();
    }

    public LogDome getLog() {
        return log;
    }
}
