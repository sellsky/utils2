package tk.bolovsrol.utils.properties;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

public class PropertyException extends UnexpectedBehaviourException {
    private final String propertyName;

    PropertyException(String propertyName, String message) {
        super(message);
        this.propertyName = propertyName;
    }

    public PropertyException(String propertyName, String message, Throwable cause) {
        super(message, cause);
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}