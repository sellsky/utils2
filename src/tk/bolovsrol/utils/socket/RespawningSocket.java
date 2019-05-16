package tk.bolovsrol.utils.socket;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.function.ThrowingConsumer;
import tk.bolovsrol.utils.function.ThrowingFunction;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.socket.client.SocketFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Феникс-сокет, восстанавливающийся по мере протухания.
 * <p>
 * В случае IOException при работе с сокетом, если сокет уже использовался повторно - можно попробовать переподключиться.
 * Или, ещё проще, передать сокету дельту для выполнения, чтобы он сам переподключался.
 */
public class RespawningSocket implements Closeable {
    protected final LogDome log;
    private final SocketFactory sf;
    private final String hostname;
    private final int port;
    protected int ioTimeout;
    protected Socket socket;
    protected boolean reused;

    /** @return true - если #getSocket() отдал один и тот же сокет дважды (или более) */
    public boolean isReused() {
        return reused;
    }

    public int getIoTimeout() {
        return ioTimeout;
    }

    public void setIoTimeout(int ioTimeout) {
        this.ioTimeout = ioTimeout;
    }

    public RespawningSocket(LogDome log, SocketFactory socketFactory, String hostname, int port, int ioTimeout) {
        this.log = log;
        this.sf = socketFactory;
        this.hostname = hostname;
        this.port = port;
        this.ioTimeout = ioTimeout;
    }

    @SuppressWarnings("SocketOpenedButNotSafelyClosed")
    protected void createNewAndConnect() throws IOException {
        socket = sf.newSocket();
        socket.setKeepAlive(true);
        socket.setSoTimeout(ioTimeout);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
        log.trace("Estabilishing connection to " + Spell.get(inetSocketAddress));
        socket.connect(inetSocketAddress);
        log.info("Estabilished connection " + Spell.get(socket));
    }

    /**
     * Отдаёт подключённый настроенный сокет.
     * <p>
     * По мере необходимости устанавливает соединение.
     * Сокет можно закрыть методом {@link #close()} или {@link Socket#close()}, это приведёт к переустановке соединения
     * при последующем вызове данного метода.
     * <p>
     * Можно пользоваться как-то так:
     * <pre>
     * RespawningSocket rs = ...;
     *
     * void work() {
     *     while (true) {
     *         try {
     *             Socket s = rs.getSocket();
     *             useSocket(s);
     *             break;
     *         } catch (IOException e) {
     *             if (!rs.isReused()) {
     *                 throw e;
     *             }
     *             rs.close();
     *         }
     *     }
     * }
     *
     * void close() {
     *     rs.close();
     * }
     * </pre>
     * <p>
     * А лучше использовать {@link #use(ThrowingConsumer)} или {@link #use(ThrowingFunction)},
     * которые всю обёртку реализуют сами.
     *
     * @return
     * @throws IOException
     * @see #use(ThrowingFunction)
     * @see #use(ThrowingConsumer)
     */
    public Socket getSocket() throws IOException {
        if (socket == null || socket.isClosed()) {
            reused = false;
            createNewAndConnect();
        } else if (socket.isInputShutdown() || socket.isOutputShutdown()) {
            reused = false;
            log.info("Shutting down the dead socket " + Spell.get(socket));
            close();
            createNewAndConnect();
        } else {
            reused = true;
            log.info("Using existing connection " + Spell.get(socket));
        }
        return socket;
    }

    /**
     * Передаёт подключённый настроенный сокет пользователю <code>socketFunction</code>,
     * принимая меры к восстановлению протухшего сокета.
     * <p>
     * Пользователь будет вызван второй раз с новым сокетом, если первая попытка использования сокета выкинет IOException.
     * <p>
     * По мере необходимости устанавливает соединение.
     * Сокет можно закрыть методом {@link #close()} или {@link Socket#close()}, это приведёт к переустановке соединения
     * при последующем вызове данного метода.
     * <p>
     * Можно пользоваться как-то так:
     * <pre>
     * RespawningSocket rs = ...;
     *
     * void work() {
     *     try {
     *         processResult(rs.use(socket -> this::useSocket));
     *     } catch (Exception e) {
     *         processException(e);
     *     }
     * }
     *
     * void close() {
     *     rs.close();
     * }
     * </pre>
     *
     * @param socketFunction
     * @param <R>
     * @param <E>
     * @return
     * @throws IOException
     * @throws E
     * @see #getSocket()
     * @see #use(ThrowingConsumer)
     */
    public <R, E extends Exception> R use(ThrowingFunction<Socket, R, E> socketFunction) throws IOException, E {
        while (true) {
            try {
                return socketFunction.apply(getSocket());
            } catch (IOException e) {
                if (!isReused()) {
                    throw e;
                }
                close();
            }
        }
    }

    /**
     * Передаёт подключённый настроенный сокет пользователю <code>socketConsumer</code>,
     * принимая меры к восстановлению протухшего сокета.
     * <p>
     * Пользователь будет вызван второй раз с новым сокетом, если первая попытка использования сокета выкинет IOException.
     * <p>
     * По мере необходимости устанавливает соединение.
     * Сокет можно закрыть методом {@link #close()} или {@link Socket#close()}, это приведёт к переустановке соединения
     * при последующем вызове данного метода.
     * <p>
     * Можно пользоваться как-то так:
     * <pre>
     * RespawningSocket rs = ...;
     *
     * void work() {
     *     try {
     *         rs.use(socket -> this::useSocket);
     *     } catch (Exception e) {
     *         processException(e);
     *     }
     * }
     *
     * void close() {
     *     rs.close();
     * }
     * </pre>
     *
     * @param socketConsumer
     * @param <E>
     * @throws IOException
     * @throws E
     * @see #getSocket()
     * @see #use(ThrowingFunction)
     */
    public <E extends Exception> void use(ThrowingConsumer<Socket, E> socketConsumer) throws IOException, E {
        while (true) {
            try {
                socketConsumer.accept(getSocket());
                return;
            } catch (IOException e) {
                if (!isReused()) {
                    throw e;
                }
                close();
            }
        }
    }

    /** Закрывает открытый сокет. Действие обратимое, на то сокет и «респавнинг» — следующий вызов {@link #getSocket()} откроет новое соединение. */
    @Override public void close() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            log.warning(e);
        }
    }
}
