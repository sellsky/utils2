package tk.bolovsrol.utils.caseinsensitive;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class CaseInsensitiveMap<V> implements Map<String, V> {
    private final Map<CaselessKey, V> m;

    protected CaseInsensitiveMap(Map<CaselessKey, V> m) {
        this.m = m;
    }

    public int size() {
        return m.size();
    }

    public boolean isEmpty() {
        return m.isEmpty();
    }

    public boolean containsKey(Object key) {
        return key instanceof String && m.containsKey(new CaselessKey((String) key));
    }

    public boolean containsValue(Object value) {
        return m.containsValue(value);
    }

    public V get(Object key) {
        return key instanceof String ? m.get(new CaselessKey((String) key)) : null;
    }

    public V put(String key, V value) {
        return m.put(new CaselessKey(key), value);
    }

    public V remove(Object key) {
        return key instanceof String ? m.remove(new CaselessKey((String) key)) : null;
    }

    public void putAll(Map<? extends String, ? extends V> map) {
        for (Entry<? extends String, ? extends V> entry : map.entrySet()) {
            m.put(new CaselessKey(entry.getKey()), entry.getValue());
        }
    }

    public void clear() {
        m.clear();
    }

    public Set<String> keySet() {
        Set<String> result = new LinkedHashSet<String>(m.size());
        for (CaselessKey caselessKey : m.keySet()) {
            result.add(caselessKey.getOriginalKey());
        }
        return result;
    }

    public Collection<V> values() {
        return m.values();
    }

    public Set<Entry<String, V>> entrySet() {
        Set<Entry<String, V>> result = new LinkedHashSet<Entry<String, V>>(m.size());
        for (Entry<CaselessKey, V> entry : m.entrySet()) {
            result.add(new AbstractMap.SimpleEntry<String, V>(entry.getKey().getOriginalKey(), entry.getValue()));
        }
        return result;
    }
}
