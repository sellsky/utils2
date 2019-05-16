package tk.bolovsrol.utils.properties.sources;

public class SourceUnavailableException extends RuntimeException {
    public SourceUnavailableException(Throwable cause) {
        super(cause);
    }

    public SourceUnavailableException(String message) {
        super(message);
    }

    public SourceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
