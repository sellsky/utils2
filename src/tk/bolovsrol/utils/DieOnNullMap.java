package tk.bolovsrol.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class DieOnNullMap<K, V> implements Map<K, V> {

    private final Map<K, V> delegate;
    private final Function<Object, String> npeMessageProvider;

    public DieOnNullMap(Map<K, V> delegate) {
        this(delegate, key -> "No value found in the map for key " + Spell.get(key));
    }

    public DieOnNullMap(Map<K, V> delegate, Function<Object, String> npeMessageProvider) {
        this.delegate = delegate;
        this.npeMessageProvider = npeMessageProvider;
    }

    public int size() {
        return delegate.size();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    public V get(Object key) {
        V value = delegate.get(key);
        if (value == null) {
            throw new NullPointerException(npeMessageProvider.apply(key));
        }
        return value;
    }

    public V put(K key, V value) {
        return delegate.put(key, value);
    }

    public V remove(Object key) {
        V value = delegate.remove(key);
        if (value == null) {
            throw new NullPointerException(npeMessageProvider.apply(key));
        }
        return value;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        delegate.putAll(m);
    }

    public void clear() {
        delegate.clear();
    }

    public Set<K> keySet() {
        return delegate.keySet();
    }

    public Collection<V> values() {
        return delegate.values();
    }

    public Set<Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }

    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
