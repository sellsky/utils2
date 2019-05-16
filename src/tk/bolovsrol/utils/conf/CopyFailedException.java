package tk.bolovsrol.utils.conf;

public class CopyFailedException extends RuntimeException {
    public CopyFailedException(String message) {
        super(message);
    }

    public CopyFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CopyFailedException(Throwable cause) {
        super(cause);
    }
}
