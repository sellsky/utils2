package tk.bolovsrol.utils.store;

/** При попытке выдать состояние объекта произошла необратимая ошибка. */
public class StoreException extends Exception {
    public StoreException() {
    }

    public StoreException(String message) {
        super(message);
    }

    public StoreException(Throwable cause) {
        super(cause);
    }

    public StoreException(String message, Throwable cause) {
        super(message, cause);
    }
}