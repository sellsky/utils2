package tk.bolovsrol.utils.objectpool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Создаёт объект конструктором без аргументов.
 */
public class DefaultObjectProducer<O> implements ObjectProducer<O> {
    @Override public O produce(Class<? extends O> source) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Constructor<? extends O> constructor = source.getConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
