package tk.bolovsrol.utils.conf;

public class ValueTransformationFailedException extends Exception {
    public ValueTransformationFailedException(String message) {
        super(message);
    }

    public ValueTransformationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueTransformationFailedException(Throwable cause) {
        super(cause);
    }
}
