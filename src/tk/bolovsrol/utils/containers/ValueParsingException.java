package tk.bolovsrol.utils.containers;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

/** Не удалось распознать значение контейнера. */
public class ValueParsingException extends UnexpectedBehaviourException {
    public ValueParsingException(String message) {
        super(message);
    }

    public ValueParsingException(Throwable cause) {
        super(cause);
    }

    public ValueParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
