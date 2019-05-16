package tk.bolovsrol.utils.spawnmap;

import tk.bolovsrol.utils.function.ThrowingFunction;

import java.util.Map;
import java.util.function.Function;

/**
 * Обёртка для карты, автоматически создающая отсутствующие ключи
 * при вызове {@link #getOrSpawn(Object)} с несуществующим ключом.
 * <p>
 * Все остальные вызовы делегируются без вмешательств.
 *
 * @param <K>
 * @param <V>
 * @see Map#computeIfAbsent(Object, Function)
 */
public interface ThrowingSpawnMap<K, V, E extends Exception> extends Map<K, V> {

    /**
     * @return функция, создающая новое значение для указанного ключа.
     * @see #getOrSpawn(Object)
     */
    ThrowingFunction<K, V, E> getSpawner();

    /**
     * То же, что и {@link #computeIfAbsent(Object, Function)} с генератором, который возвращает {@link #getSpawner()}.
     *
     * @param key искомый ключ
     * @return новое значение
     * @throws ClassCastException
     * @see #computeIfAbsent(Object, Function)
     */
    default V getOrSpawn(K key) throws E {
        V result;
        if ((result = this.get(key)) == null && (result = getSpawner().apply(key)) != null) {
            this.put(key, result);
        }
        return result;
    }
}
