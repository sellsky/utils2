package tk.bolovsrol.utils.xml.soap;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

public class SoapException extends UnexpectedBehaviourException {
    public SoapException(String message) {
        super(message);
    }

    public SoapException(Throwable cause) {
        super(cause);
    }

    public SoapException(String message, Throwable cause) {
        super(message, cause);
    }
}
