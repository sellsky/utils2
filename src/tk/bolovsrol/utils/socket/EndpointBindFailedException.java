package tk.bolovsrol.utils.socket;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

/** Техническая ошибка байнда эндпоинта. */
public class EndpointBindFailedException extends UnexpectedBehaviourException {

    public EndpointBindFailedException(String message) {
        super(message);
    }

    public EndpointBindFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EndpointBindFailedException(Throwable cause) {
        super(cause);
    }

}
