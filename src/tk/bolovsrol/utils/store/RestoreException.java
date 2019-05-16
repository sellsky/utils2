package tk.bolovsrol.utils.store;

/** При попытке восстановления состояния произошла необратимая ошибка. */
public class RestoreException extends Exception {
    public RestoreException() {
    }

    public RestoreException(String message) {
        super(message);
    }

    public RestoreException(Throwable cause) {
        super(cause);
    }

    public RestoreException(String message, Throwable cause) {
        super(message, cause);
    }
}