package tk.bolovsrol.utils.store.expirabledictionary;

import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.io.LineOutputStream;
import tk.bolovsrol.utils.store.RestoreException;
import tk.bolovsrol.utils.store.StoreException;
import tk.bolovsrol.utils.store.Storeable;
import tk.bolovsrol.utils.stringserializer.StringDeserializer;
import tk.bolovsrol.utils.stringserializer.StringSerializable;
import tk.bolovsrol.utils.stringserializer.StringSerializer;
import tk.bolovsrol.utils.threads.IterationThread;

import java.util.HashMap;
import java.util.Map;

/**
 * Стор словаря, {@link ExpirableStorableDictionaryValue} которого могут истекать и они регулярно подчищаются.
 */
public class ExpirableStorableDictionary<K, V extends ExpirableStorableDictionaryValue> extends IterationThread implements Storeable {
    private Map<K, V> dictionary = new HashMap<>();
    private final long lifeTime;

    public ExpirableStorableDictionary(long lifeTimeMillis) {
        super("ExpirableStoreCleaner");
        lifeTime = lifeTimeMillis;
    }

    public V get(K key) {
        synchronized (dictionary) {
            V value = dictionary.get(key);
            if (isNotExpired(value)) {
                return value;
            } else {
                return null;
            }
        }
    }

    public V remove(K key) {
        synchronized (dictionary) {
            V value = dictionary.remove(key);
            if (value != null && isNotExpired(value)) {
                return value;
            } else {
                return null;
            }
        }
    }

    public void put(K key, V value) {
        synchronized (dictionary) {
            dictionary.put(key, value);
        }
    }

    @Override
    protected void work() throws Exception {
        //подчищает и формирует время сна до следующей подчистки, сделующая очистка должна бует сделана не раньше чем через дата самого старого непротухшего сзначения + срок жизна
        Long now = System.currentTimeMillis();
        Long nextCleanDate = null;
        if (!dictionary.isEmpty()) {
            nextCleanDate = forceClean(now) + lifeTime;
        }
        if (nextCleanDate == null || nextCleanDate <= now) {
            setIterationSleep(lifeTime / 10);
        } else {
            setIterationSleep(nextCleanDate - now);
        }

    }

    private Long forceClean(Long now) {
        Long minTime = now;
        synchronized (dictionary) {
            for (Map.Entry<K, V> entry : dictionary.entrySet()) {
                if (!isNotExpired(entry.getValue())) {
                    dictionary.remove(entry.getKey());
                } else if (minTime > entry.getValue().getTime()) {
                    minTime = entry.getValue().getTime();
                }
            }
        }
        return minTime;
    }

    @Override
    public void store(LineOutputStream los) throws Exception {
        try {
            synchronized (dictionary) {
                for (Map.Entry<K, V> entry : dictionary.entrySet()) {
                    if (isNotExpired(entry.getValue())) {
                        los.writeln(StringSerializer.serialize(new KeyValueContainer(entry.getKey(), entry.getValue())));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public void restore(LineInputStream lis) throws Exception {
        try {
            synchronized (dictionary) {
                while (true) {
                    String item = lis.readLine();
                    if (item == null) {
                        break;
                    }

                    KeyValueContainer container = StringDeserializer.deserialize(item);
                    if (!dictionary.containsKey(container.getKey())) {
                        if (isNotExpired(container.getValue())) {
                            dictionary.put(container.getKey(), container.getValue());
                        }
                    }
                }
            }
        } catch (UnexpectedBehaviourException e) {
            throw new RestoreException(e);
        }
    }

    private class KeyValueContainer implements StringSerializable {
        private K key;
        private V value;

        KeyValueContainer(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return new StringDumpBuilder()
                  .append("key", key)
                  .append("value", value)
                  .toString();
        }
    }

    private boolean isNotExpired(ExpirableStorableDictionaryValue value) {
        return value.getTime() >= System.currentTimeMillis() - lifeTime;
    }
}
