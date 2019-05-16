package tk.bolovsrol.utils.localcache;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

public class ObjectNotFoundException extends UnexpectedBehaviourException {
    public ObjectNotFoundException(String message) {
        super(message);
    }

    public ObjectNotFoundException(Throwable cause) {
        super(cause);
    }

    public ObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
