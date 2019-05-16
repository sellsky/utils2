package tk.bolovsrol.utils;

public class LineParsingException extends Exception {
    public LineParsingException(String message) {
        super(message);
    }

    public LineParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public LineParsingException(Throwable cause) {
        super(cause);
    }
}
