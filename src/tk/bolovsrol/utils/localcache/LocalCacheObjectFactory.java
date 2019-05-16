package tk.bolovsrol.utils.localcache;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Фабрика, создающая объекты по их ид, используемая локальным кэшом {@link LocalCache}.
 *
 * @param <I> класс ключа
 * @param <O> класс объекта
 */
public interface LocalCacheObjectFactory<I, O> {

    /**
     * Создаёт объект по его ид. Передаваемый ид никогда не нул.
     * <p/>
     * Может вернуть нул, если для указанного ид нет соответствующего объекта.
     *
     * @param id ключ объекта
     * @return объект или нул
     * @throws ObjectCreationFailedException ошибка создания объекта
     * @see #newObjects(java.util.Collection)
     */
    O newObject(I id) throws ObjectCreationFailedException;

    /**
     * Опциональный метод для создания пачки новых объектов по их ид.
     * Передаваемая коллекция ид никогда не нул и никогда не пуста.
     * <p/>
     * Фабрика может вернуть нул, что значит, что данный метод не поддерживается,
     * и следует использовать {@link #newObject(Object)} для каждого конкретного id.
     * Если для какого-то ключа нет соответствующего объекта, то такой ключ
     * в карту-результат не попадает.
     *
     * @param ids коллекция ключей объектов
     * @return карта созданных объектов
     * @throws ObjectCreationFailedException
     * @see #newObject(Object)
     */
    default Map<I, O> newObjects(Collection<I> ids) throws ObjectCreationFailedException {
        Map<I, O> result = new LinkedHashMap<>(ids.size());
        for (I id : ids) {
            result.put(id, newObject(id));
        }
        return result;
    }

}
