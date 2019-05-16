package tk.bolovsrol.utils.conf;

public class MandatoryFieldMissingException extends InvalidFieldException {
    public MandatoryFieldMissingException(String message, String fieldName) {
        super(message, fieldName);
    }

    public MandatoryFieldMissingException(String message, Throwable cause, String fieldName) {
        super(message, cause, fieldName);
    }

    public MandatoryFieldMissingException(Throwable cause, String fieldName) {
        super(cause, fieldName);
    }
}
