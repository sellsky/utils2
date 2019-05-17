package tk.bolovsrol.utils.socket;

import tk.bolovsrol.utils.QuitException;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.socket.server.PlainServerSocketFactory;
import tk.bolovsrol.utils.socket.server.ServerSocketFactory;
import tk.bolovsrol.utils.threads.HaltableThread;
import tk.bolovsrol.utils.threads.ShutdownException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Слушатель принимает входящие соединения
 * и вызывает реализуемый в детях метод,
 * который разбирается с гостем.
 * <p/>
 * Не рекомендуется пользоваться этим классом непосредственно, лучше использовать
 * {@link ProcessorSocketListener} или {@link SocketServer}.
 */
public abstract class AbstractSocketListener extends HaltableThread {

	private static final long ERROR_SLEEP = Cfg.getLong("io.error.sleep", 10000L, Log.getInstance());
	private static final boolean REUSE_ADDRESS = Cfg.getBoolean("socketListener.reuseAddress", true);
	private static final Integer RECEIVE_BUFFER_SIZE = Cfg.getInteger("socketListener.receiveBufferSize", null, Log.getInstance());

    protected final LogDome log;

    protected final InetSocketAddress bindAddress;
    protected final ServerSocketFactory socketFactory;

    private ServerSocket serverSocket;

    // ServerSocket.accept() не прерывается Thread.interrupt()`ом, поэтому нужен флажок
    private boolean terminating;

    /**
     * Создаёт слушателя.
     *
     * @param name название треда, в котором будет крутиться слушатель.
     * @param log лог, в который гадить
     * @param hostname хост, к которому биндиться, либо null
     * @param port порт, к которому биндиться
     * @deprecated следует пользоваться SocketListener:ом либо SocketServer:ом.
     */
    @Deprecated
    protected AbstractSocketListener(String name, LogDome log, String hostname, int port) {
        this(name, log, hostname, port, PlainServerSocketFactory.getStatic());
    }

    /**
     * Создаёт слушателя.
     *
     * @param name название треда, в котором будет крутиться слушатель.
     * @param log лог, в который гадить
     * @param hostname хост, к которому биндиться, либо null
     * @param port порт, к которому биндиться
     */
    protected AbstractSocketListener(String name, LogDome log, String hostname, int port, ServerSocketFactory socketFactory) {
        this(name, log, hostname == null ? new InetSocketAddress(port) : new InetSocketAddress(hostname, port), socketFactory);
    }

    /**
     * Создаёт слушателя.
     *
     * @param name
     * @param log
     * @param bindAddress
     * @param socketFactory
     */
    protected AbstractSocketListener(String name, LogDome log, InetSocketAddress bindAddress, ServerSocketFactory socketFactory) {
        super(name);
        this.log = log;
        this.bindAddress = bindAddress;
        this.socketFactory = socketFactory;
    }

    /**
     * Принимает установленное входящее соединение.
     * <p/>
     * Для пущей асинхронности метод должен запускать отдельный тред и
     * производить дальшейшие работы в нём.
     *
     * todo вынести эти методы в отдельный интерфейс
     *
     * @param socket
     */
    protected abstract void accept(Socket socket);

    /**
     * Создаёт серверный сокет.
     * <p/>
     * По умолчанию создаётся обычный сокет.
     * Можно перегрузить для, например, создания SSL-ного сокета.
     *
     * @return незабинденный сокет
     * @throws IOException
     */
    protected ServerSocket createServerSocket() throws IOException {
        return socketFactory.newServerSocket();
    }

    @Override public synchronized void start() {
        log.trace("Listener starting...");
        open();
        super.start();
    }

    /**
     * Основной цикл.
     * <p/>
     * Открывает соединение, работает, закрывает соединение.
     */
    @Override
    public void run() {
        log.trace("Listener is started up and running!");
        try {
            while (!isInterrupted()) {
                try {
                    try {
                        work();
                    } finally {
                        close();
                        log.trace("Listener closed.");
                    }
                } catch (IOException e) {
                    log.warning(e);
                    sleep(ERROR_SLEEP);
                }
            }
        } catch (QuitException e) {
            log.trace(e.getMessage());
        } catch (InterruptedException e) {
            // Прервана спячка после ошибки.
            log.warning(e);
        }
    }

    /**
     * Открывает слушательное соединение.
     *
     * @throws InterruptedException
     */
    protected void open() {
        if (serverSocket != null) {
            throw new IllegalStateException("Listener is already open.");
        }

        try {
            serverSocket = createServerSocket();
            if (RECEIVE_BUFFER_SIZE != null) {
                log.trace("Setting receive buffer size to " + Spell.get(RECEIVE_BUFFER_SIZE) + " byte(s) (default was " + Spell.get(serverSocket.getReceiveBufferSize()) + ')');
                serverSocket.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);
            }
            serverSocket.setReuseAddress(REUSE_ADDRESS);
            serverSocket.bind(bindAddress);
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't bind Server Socket to address " + Spell.get(bindAddress), e);
        }
    }

    /**
     * Закрывает слушательное соединение в конце работы.
     * <p/>
     * Если осталось чего закрывать.
     */
    private void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.exception(e);
            }
            serverSocket = null;
        }
    }

    /**
     * Принимает соединения и вызывает их обработчики.
     *
     * @throws IOException
     * @throws QuitException
     */
    @SuppressWarnings({"SocketOpenedButNotSafelyClosed"})
    private void work() throws IOException, QuitException {
        while (!isInterrupted()) {
            try {
                accept(serverSocket.accept());
            } catch (IOException e) {
                if (terminating) {
                    throw new QuitException(e.getMessage());
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Затыкает слушателя.
     *
     * @throws InterruptedException
     * @throws ShutdownException
     */
    @Override
    public void shutdown() throws InterruptedException, ShutdownException {
        terminating = true;
        close();
        super.shutdown();
    }

    public ServerSocketFactory getSocketFactory() {
        return socketFactory;
    }
}
