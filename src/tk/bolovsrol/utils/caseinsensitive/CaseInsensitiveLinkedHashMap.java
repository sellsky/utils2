package tk.bolovsrol.utils.caseinsensitive;

import java.util.LinkedHashMap;
import java.util.Map;

public class CaseInsensitiveLinkedHashMap<V> extends CaseInsensitiveMap<V> {

    public CaseInsensitiveLinkedHashMap() {
        super(new LinkedHashMap<CaselessKey, V>());
    }

    public CaseInsensitiveLinkedHashMap(int initialCapacity) {
        super(new LinkedHashMap<CaselessKey, V>(initialCapacity));
    }

    public CaseInsensitiveLinkedHashMap(Map<String, V> source) {
        super(new LinkedHashMap<CaselessKey, V>(source.size()));
        putAll(source);
    }


}
