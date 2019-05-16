package tk.bolovsrol.utils;

public class PatternCompileException extends Exception {
    public PatternCompileException(String message) {
        super(message);
    }

    public PatternCompileException(Throwable cause) {
        super(cause);
    }

    public PatternCompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
