package tk.bolovsrol.utils.spawnmap;

import java.util.Map;
import java.util.NavigableMap;
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
public interface NavigableSpawnMap<K, V> extends SortedSpawnMap<K, V>, NavigableMap<K, V> {

}
