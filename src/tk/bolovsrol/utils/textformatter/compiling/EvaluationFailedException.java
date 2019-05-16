package tk.bolovsrol.utils.textformatter.compiling;

public class EvaluationFailedException extends Exception {
    public EvaluationFailedException(String message) {
        super(message);
    }

    public EvaluationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvaluationFailedException(Throwable cause) {
        super(cause);
    }
}
