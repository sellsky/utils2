package tk.bolovsrol.utils.binaryserializer;

public class EncodeException extends Exception {
    public EncodeException(String message) {
        super(message);
    }

    public EncodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncodeException(Throwable cause) {
        super(cause);
    }
}
