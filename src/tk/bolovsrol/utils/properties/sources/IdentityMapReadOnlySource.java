package tk.bolovsrol.utils.properties.sources;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.properties.PropertyIdentityValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Сурс для свойст с координатой
 * IdentityMapReadOnlySource
 */
public class IdentityMapReadOnlySource implements ReadOnlySource {
    protected final Map<String, PropertyIdentityValue> source;

    public IdentityMapReadOnlySource(Map<String, PropertyIdentityValue> source) {
        this.source = source;
    }

    @Override
    public String expand(String localBranchKey) {
        return localBranchKey;
    }

    @Override
    public String get(String key) {
        PropertyIdentityValue propertyIdentityValue = source.get(key);
        return propertyIdentityValue == null ? null : propertyIdentityValue.getValue();
    }

    @Override
    public boolean has(String key) {
        return source.containsKey(key);
    }

    @Override
    public Map<String, String> dump() {
        Map<String, String> dump = new LinkedHashMap<>();
        for(Map.Entry<String, PropertyIdentityValue> stringPropertyIdentityValueEntry : source.entrySet()) {
            dump.put(stringPropertyIdentityValueEntry.getKey(), stringPropertyIdentityValueEntry.getValue().getValue());
        }
        return dump;
    }

    public String toString() {
        return Spell.get(source);
    }

    @Override
    public String getIdentity(String key) throws SourceUnavailableException {
        PropertyIdentityValue propertyIdentityValue = source.get(key);
        return propertyIdentityValue == null ? null : propertyIdentityValue.getIdentity();
    }

}
