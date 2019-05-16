package tk.bolovsrol.utils.spawnmap;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

public class TreeSpawnMap<K, V> extends TreeMap<K, V> implements NavigableSpawnMap<K, V> {

    private final Function<K, V> spawner;

    public TreeSpawnMap(Function<K, V> spawner) {
        this.spawner = spawner;
    }

    public TreeSpawnMap(Comparator<? super K> comparator, Function<K, V> spawner) {
        super(comparator);
        this.spawner = spawner;
    }

    public TreeSpawnMap(Map<? extends K, ? extends V> m, Function<K, V> spawner) {
        super(m);
        this.spawner = spawner;
    }

    public TreeSpawnMap(SortedMap<K, ? extends V> m, Function<K, V> spawner) {
        super(m);
        this.spawner = spawner;
    }

    @Override public Function<K, V> getSpawner() {
        return spawner;
    }
}
