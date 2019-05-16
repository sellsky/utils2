package tk.bolovsrol.utils.spawnmap;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

public class ConcurrentSkipListSpawnMap<K, V> extends ConcurrentSkipListMap<K, V> implements ConcurrentNavigableSpawnMap<K, V> {

    private final Function<K, V> spawner;

    public ConcurrentSkipListSpawnMap(Function<K, V> spawner) {
        super();
        this.spawner = spawner;
    }

    public ConcurrentSkipListSpawnMap(Comparator<? super K> comparator, Function<K, V> spawner) {
        super(comparator);
        this.spawner = spawner;
    }

    public ConcurrentSkipListSpawnMap(Map<? extends K, ? extends V> m, Function<K, V> spawner) {
        super(m);
        this.spawner = spawner;
    }

    public ConcurrentSkipListSpawnMap(SortedMap<K, ? extends V> m, Function<K, V> spawner) {
        super(m);
        this.spawner = spawner;
    }

    @Override public Function<K, V> getSpawner() {
        return spawner;
    }
}


