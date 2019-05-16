package tk.bolovsrol.utils.conf;

public class InvalidFieldValueException extends InvalidFieldException {
    private final String value;

    public InvalidFieldValueException(String message, String fieldName, String value) {
        super(message, fieldName);
        this.value = value;
    }

    public InvalidFieldValueException(String message, Throwable cause, String fieldName, String value) {
        super(message, cause, fieldName);
        this.value = value;
    }

    public InvalidFieldValueException(Throwable cause, String fieldName, String value) {
        super(cause, fieldName);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
