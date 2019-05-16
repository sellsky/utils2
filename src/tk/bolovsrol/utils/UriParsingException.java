package tk.bolovsrol.utils;

/** Ошибка разбора строки параметров. */
public class UriParsingException extends UnexpectedBehaviourException {
    public UriParsingException(String message) {
        super(message);
    }

    public UriParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public UriParsingException(Throwable cause) {
        super(cause);
    }
}
