package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

public class AmbigousRequestProcessorMappingException extends UnexpectedBehaviourException {
    public AmbigousRequestProcessorMappingException(String message) {
        super(message);
    }
}
