package tk.bolovsrol.utils.spawnmap;

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
public interface SpawnMap<K, V> extends Map<K, V> {

    /**
     * @return функция, создающая новое значение для указанного ключа.
     * @see #getOrSpawn(Object)
     */
    Function<K, V> getSpawner();

    /**
     * То же, что и {@link #computeIfAbsent(Object, Function)} с генератором, который возвращает {@link #getSpawner()}.
     *
     * @param key искомый ключ
     * @return новое значение
     * @throws ClassCastException
     * @see #computeIfAbsent(Object, Function)
     */
    default V getOrSpawn(K key) {
        return computeIfAbsent(key, getSpawner());
    }
}
