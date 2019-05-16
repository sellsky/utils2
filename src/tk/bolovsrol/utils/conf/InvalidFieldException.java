package tk.bolovsrol.utils.conf;

public class InvalidFieldException extends InvalidConfigurationException {
    private final String fieldName;

    public InvalidFieldException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public InvalidFieldException(String message, Throwable cause, String fieldName) {
        super(message, cause);
        this.fieldName = fieldName;
    }

    public InvalidFieldException(Throwable cause, String fieldName) {
        super(cause);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
