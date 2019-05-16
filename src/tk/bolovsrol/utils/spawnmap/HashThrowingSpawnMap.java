package tk.bolovsrol.utils.spawnmap;

import tk.bolovsrol.utils.function.ThrowingFunction;

import java.util.HashMap;
import java.util.Map;

public class HashThrowingSpawnMap<K, V, E extends Exception> extends HashMap<K, V> implements ThrowingSpawnMap<K, V, E> {

    private final ThrowingFunction<K, V, E> spawner;

    public HashThrowingSpawnMap(int initialCapacity, float loadFactor, ThrowingFunction<K, V, E> spawner) {
        super(initialCapacity, loadFactor);
        this.spawner = spawner;
    }

    public HashThrowingSpawnMap(int initialCapacity, ThrowingFunction<K, V, E> spawner) {
        super(initialCapacity);
        this.spawner = spawner;
    }

    public HashThrowingSpawnMap(ThrowingFunction<K, V, E> spawner) {
        this.spawner = spawner;
    }

    public HashThrowingSpawnMap(Map<? extends K, ? extends V> m, ThrowingFunction<K, V, E> spawner) {
        super(m);
        this.spawner = spawner;
    }

    @Override public ThrowingFunction<K, V, E> getSpawner() {
        return spawner;
    }
}
