package tk.bolovsrol.utils;

import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.properties.InvalidPropertyValueFormatException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Тред-демон.
 * <p/>
 * Ждёт обращения по указанному порту, пишет текущий системный стектрейс
 * и закрывает соединение.
 * <p/>
 * Треды в стекрейсе упорядочены по признаку демона (не демоны выше), затем по имени.
 * <p/>
 * Синтаксис заголовока треда:<br/>
 * <code>"name" id: state (prio) group [alive] [daemon] [interrupted]</code><br/>
 * — суть очевидна.
 * <p/>
 * Рекомендуется инициализировать при запуске приложения
 * методом {@link #launch(Integer)} с указанием порта, который слушать.
 */
public class StackTraceDaemon extends Thread {

    /** Название параметра для конфига. */
    public static final String STACKTRACE_PORT = "stacktrace.port";

    private final ServerSocket ss;

    /**
     * Создаёт тред-демон на указанном порту.
     *
     * @param port
     * @see #launch()
     * @see #launch(Integer)
     */
    public StackTraceDaemon(int port) throws IOException {
        super("StackTraceDaemon-" + port);
        setDaemon(true);
        //noinspection SocketOpenedButNotSafelyClosed
        ss = new ServerSocket(port);
    }

    @Override public void run() {
        try {
            while (true) {
                try (Socket s = ss.accept()) {
                    s.getOutputStream().write(ThreadUtils.getFormattedThreadDump().getBytes());
                }
            }
        } catch (Throwable e) {
            Log.exception(e);
        }
    }

    /**
     * Если передан не нул, создаёт тред-демон на указанном порту, запускает его
     * и пишет об этом хинт в лог.
     * <p/>
     * Если передан нул, ничего не делает.
     *
     * @param port
     * @return port != null
     * @throws IOException не удалось прибиндиться на указанный порт
     * @see #launch()
     */
    public static boolean launch(Integer port) throws IOException {
        if (port != null) {
            new StackTraceDaemon(port).start();
            Log.hint("Listening on port " + port);
            return true;
        }
        return false;
    }

    /**
     * Читает в стандартном конфиге номер порта из параметра <code>stacktrace.port</code>.
     * <p/>
     * Если считан не нул, создаёт тред-демон на этом порту, запускает его
     * и пишет об этом хинт в лог.
     * <p/>
     * Если считан нул, ничего не делает.
     *
     * @return порт != null
     * @throws IOException                         не удалось прибиндиться на указанный порт
     * @throws InvalidPropertyValueFormatException в конфиге указано не число
     * @see #launch(Integer)
     */
    public static boolean launch() throws InvalidPropertyValueFormatException, IOException {
        return launch(Cfg.getInteger(STACKTRACE_PORT));
    }
}
