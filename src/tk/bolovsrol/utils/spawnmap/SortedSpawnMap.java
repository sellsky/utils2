package tk.bolovsrol.utils.spawnmap;

import java.util.Map;
import java.util.SortedMap;
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
public interface SortedSpawnMap<K, V> extends SpawnMap<K, V>, SortedMap<K, V> {
}
