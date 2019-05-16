package tk.bolovsrol.utils.localcache;

import java.util.Collection;

/**
 * Хранилище информации для локал-кэша.
 * <p>
 * Всё очевидно.
 */
public interface LocalCacheStorage<I extends Comparable<? super I>, O> {
    /**
     * Добавляет объект в кэш под указанным ид.
     * <p>
     * Добавление ни к чему кэш не обязывает, разумеется.
     *
     * @param id
     * @param object
     */
    void put(I id, O object);

    /**
     * Удаляет больше не нужный элемент из кэша.
     *
     * @param id
     */
    void remove(I id);

    /**
     * Удаляет больше не нужные элементы из кэша.
     *
     * @param ids
     */
    void removeAll(Collection<I> ids);

    /**
     * Возвращает объект, хранящийся в кэше под указанным идентификатором. Или нул, если сейчас ничего не хранится.
     *
     * @param id
     * @return соответствующий идентификатору объект или нул
     */
    O get(I id);

}
