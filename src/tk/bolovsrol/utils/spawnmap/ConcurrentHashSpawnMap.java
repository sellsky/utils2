package tk.bolovsrol.utils.spawnmap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ConcurrentHashSpawnMap<K, V> extends ConcurrentHashMap<K, V> implements ConcurrentSpawnMap<K, V> {

    private final Function<K, V> spawner;

    public ConcurrentHashSpawnMap(Function<K, V> spawner) {
        this.spawner = spawner;
    }

    public ConcurrentHashSpawnMap(int initialCapacity, Function<K, V> spawner) {
        super(initialCapacity);
        this.spawner = spawner;
    }

    public ConcurrentHashSpawnMap(Map<? extends K, ? extends V> m, Function<K, V> spawner) {
        super(m);
        this.spawner = spawner;
    }

    public ConcurrentHashSpawnMap(int initialCapacity, float loadFactor, Function<K, V> spawner) {
        super(initialCapacity, loadFactor);
        this.spawner = spawner;
    }

    public ConcurrentHashSpawnMap(int initialCapacity, float loadFactor, int concurrencyLevel, Function<K, V> spawner) {
        super(initialCapacity, loadFactor, concurrencyLevel);
        this.spawner = spawner;
    }

    @Override public Function<K, V> getSpawner() {
        return spawner;
    }
}


