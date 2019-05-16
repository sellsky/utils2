package tk.bolovsrol.utils.textformatter.compiling;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

public class TextFormatterException extends UnexpectedBehaviourException {
    public TextFormatterException(String message) {
        super(message);
    }

    public TextFormatterException(Throwable cause) {
        super(cause);
    }

    public TextFormatterException(String message, Throwable cause) {
        super(message, cause);
    }
}
