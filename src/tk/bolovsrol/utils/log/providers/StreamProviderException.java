package tk.bolovsrol.utils.log.providers;

public class StreamProviderException extends Exception {
    public StreamProviderException(String message) {
        super(message);
    }

    public StreamProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public StreamProviderException(Throwable cause) {
        super(cause);
    }
}
