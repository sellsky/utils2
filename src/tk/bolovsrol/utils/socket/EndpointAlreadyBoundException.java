package tk.bolovsrol.utils.socket;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

/**
 * Попытка назначить обработчик конечной точке,
 * обработчик которой уже назначен ранее.
 */
public class EndpointAlreadyBoundException extends UnexpectedBehaviourException {

    public EndpointAlreadyBoundException(String message) {
        super(message);
    }

    public EndpointAlreadyBoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EndpointAlreadyBoundException(Throwable cause) {
        super(cause);
    }

}
