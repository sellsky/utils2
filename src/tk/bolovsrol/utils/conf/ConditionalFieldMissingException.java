package tk.bolovsrol.utils.conf;

public class ConditionalFieldMissingException extends InvalidFieldException {
    public ConditionalFieldMissingException(String message, String fieldName) {
        super(message, fieldName);
    }

    public ConditionalFieldMissingException(String message, Throwable cause, String fieldName) {
        super(message, cause, fieldName);
    }

    public ConditionalFieldMissingException(Throwable cause, String fieldName) {
        super(cause, fieldName);
    }
}
