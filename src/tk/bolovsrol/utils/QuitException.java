package tk.bolovsrol.utils;

/**
 * Исключение значит, что работа завершается штатным образом.
 * Это исключение надо тихонько проглотить и завершить работу.
 */
public class QuitException extends Exception {
    private static final String DEFAULT_MESSAGE = "Quit.";

    private static final QuitException DEFAULT = new QuitException(DEFAULT_MESSAGE);

    public static QuitException getDefault() {
        return DEFAULT;
    }

    public QuitException(String message) {
        super(message);
    }

    public QuitException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
    }

    public QuitException(String message, Throwable cause) {
        super(message, cause);
    }
}
