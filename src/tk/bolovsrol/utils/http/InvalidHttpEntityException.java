package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

/** Ошибка валидации. */
public class InvalidHttpEntityException extends UnexpectedBehaviourException {
    public InvalidHttpEntityException(String message) {
        super(message);
    }

    public InvalidHttpEntityException(Throwable cause) {
        super(cause);
    }

    public InvalidHttpEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
