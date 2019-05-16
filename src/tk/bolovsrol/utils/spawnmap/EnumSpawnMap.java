package tk.bolovsrol.utils.spawnmap;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class EnumSpawnMap<K extends Enum<K>, V> extends EnumMap<K, V> implements SpawnMap<K, V> {

    private final Function<K, V> spawner;

    public EnumSpawnMap(Class<K> keyType, Function<K, V> spawner) {
        super(keyType);
        this.spawner = spawner;
    }

    public EnumSpawnMap(EnumMap<K, ? extends V> m, Function<K, V> spawner) {
        super(m);
        this.spawner = spawner;
    }

    public EnumSpawnMap(Map<K, ? extends V> m, Function<K, V> spawner) {
        super(m);
        this.spawner = spawner;
    }

    @Override public Function<K, V> getSpawner() {
        return spawner;
    }
}
