package tk.bolovsrol.utils.objectpool;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

/**
 * Это исключение выбрасывает {@link ObjectPool} при возникновении
 * {@link InstantiationException}, {@link IllegalAccessException} или {@link ClassNotFoundException},
 * просто чтобы более компактно организовать обработку этой неприятности.
 */
public class ObjectProducingException extends UnexpectedBehaviourException {
    /**
     * Создаёт исключение из базового класса
     *
     * @param cause Причина ошибки
     */
    public ObjectProducingException(Throwable cause) {
        super(cause);
    }
}
