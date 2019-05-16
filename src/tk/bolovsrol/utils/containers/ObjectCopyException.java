package tk.bolovsrol.utils.containers;

/** Ошибка копирования поля */
public class ObjectCopyException extends RuntimeException {
    public ObjectCopyException(String message) {
        super(message);
    }

    public ObjectCopyException(Throwable cause) {
        super(cause);
    }

    public ObjectCopyException(String message, Throwable cause) {
        super(message, cause);
    }
}
