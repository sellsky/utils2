package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

public class HttpEntityParsingException extends UnexpectedBehaviourException {

    public HttpEntityParsingException(String message) {
        super(message);
    }

    public HttpEntityParsingException(Throwable cause) {
        super(cause);
    }

    public HttpEntityParsingException(String message, Throwable cause) {
        super(message, cause);
    }

}
