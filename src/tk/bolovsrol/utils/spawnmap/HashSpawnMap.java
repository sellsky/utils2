package tk.bolovsrol.utils.spawnmap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class HashSpawnMap<K, V> extends HashMap<K, V> implements SpawnMap<K, V> {

    private final Function<K, V> spawner;

    public HashSpawnMap(int initialCapacity, float loadFactor, Function<K, V> spawner) {
        super(initialCapacity, loadFactor);
        this.spawner = spawner;
    }

    public HashSpawnMap(int initialCapacity, Function<K, V> spawner) {
        super(initialCapacity);
        this.spawner = spawner;
    }

    public HashSpawnMap(Function<K, V> spawner) {
        this.spawner = spawner;
    }

    public HashSpawnMap(Map<? extends K, ? extends V> m, Function<K, V> spawner) {
        super(m);
        this.spawner = spawner;
    }

    @Override public Function<K, V> getSpawner() {
        return spawner;
    }
}
