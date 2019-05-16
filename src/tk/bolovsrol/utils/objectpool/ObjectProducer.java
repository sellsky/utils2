package tk.bolovsrol.utils.objectpool;

import java.lang.reflect.InvocationTargetException;

/** Умеет создавать объект класса правильным образом.. */
public interface ObjectProducer<O> {

    /**
     * Создаёт объект класса.
     *
     * @param source интересующий класс
     * @return объект
     * @throws InstantiationException    ошибка создания
     * @throws IllegalAccessException    ошибка создания
     * @throws NoSuchMethodException     ошибка создания
     * @throws InvocationTargetException ошибка создания
     */
    O produce(Class<? extends O> source) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException;
}
