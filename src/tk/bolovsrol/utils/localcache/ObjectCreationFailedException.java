package tk.bolovsrol.utils.localcache;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

public class ObjectCreationFailedException extends UnexpectedBehaviourException {
    public ObjectCreationFailedException(String message) {
        super(message);
    }

    public ObjectCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectCreationFailedException(Throwable cause) {
        super(cause);
    }
}
