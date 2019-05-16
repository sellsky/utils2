package tk.bolovsrol.utils.spawnmap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class LinkedHashSpawnMap<K, V> extends LinkedHashMap<K, V> implements SpawnMap<K, V> {

    private final Function<K, V> spawner;

    public LinkedHashSpawnMap(int initialCapacity, float loadFactor, Function<K, V> spawner) {
        super(initialCapacity, loadFactor);
        this.spawner = spawner;
    }

    public LinkedHashSpawnMap(int initialCapacity, Function<K, V> spawner) {
        super(initialCapacity);
        this.spawner = spawner;
    }

    public LinkedHashSpawnMap(Function<K, V> spawner) {
        this.spawner = spawner;
    }

    public LinkedHashSpawnMap(Map<? extends K, ? extends V> m, Function<K, V> spawner) {
        super(m);
        this.spawner = spawner;
    }

    public LinkedHashSpawnMap(int initialCapacity, float loadFactor, boolean accessOrder, Function<K, V> spawner) {
        super(initialCapacity, loadFactor, accessOrder);
        this.spawner = spawner;
    }

    @Override public Function<K, V> getSpawner() {
        return spawner;
    }
}
