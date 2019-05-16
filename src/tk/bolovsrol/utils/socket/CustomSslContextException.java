package tk.bolovsrol.utils.socket;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

public class CustomSslContextException extends UnexpectedBehaviourException {
    public CustomSslContextException(String message) {
        super(message);
    }

    public CustomSslContextException(Throwable cause) {
        super(cause);
    }

    public CustomSslContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
