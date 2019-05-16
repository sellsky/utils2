package tk.bolovsrol.utils.caseinsensitive;

import java.util.Map;
import java.util.TreeMap;

public class CaseInsensitiveTreeMap<V> extends CaseInsensitiveMap<V> {

    public CaseInsensitiveTreeMap() {
        super(new TreeMap<CaselessKey, V>());
    }

    public CaseInsensitiveTreeMap(Map<String, V> source) {
        super(new TreeMap<CaselessKey, V>());
        putAll(source);
    }
}