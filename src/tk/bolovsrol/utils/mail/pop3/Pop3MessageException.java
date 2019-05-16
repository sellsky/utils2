package tk.bolovsrol.utils.mail.pop3;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

/** Ошибка при обработке сообщения. */
public class Pop3MessageException extends UnexpectedBehaviourException {
    public Pop3MessageException(String message) {
        super(message);
    }

    public Pop3MessageException(Throwable cause) {
        super(cause);
    }

    public Pop3MessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
