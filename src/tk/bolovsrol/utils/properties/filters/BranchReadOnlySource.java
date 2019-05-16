package tk.bolovsrol.utils.properties.filters;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.properties.sources.ReadOnlySource;
import tk.bolovsrol.utils.properties.sources.SourceUnavailableException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Позволяет обращаться к ветке пропертей:
 * т.е. к пропертям, все назыания которых начинаются
 * с заданного префикса.
 */
public class BranchReadOnlySource implements ReadOnlySource {

    protected final String prefix;
    private final ReadOnlySource parent;
    private Map<String, String> dump;

    public BranchReadOnlySource(String prefix, ReadOnlySource parent) {
        this.prefix = prefix;
        this.parent = parent;
    }

    @Override public String expand(String localBranchKey) {
        return parent.expand(prefix + localBranchKey);
    }

    @Override public String get(String key) throws SourceUnavailableException {
        return parent.get(key == null ? null : prefix + key);
    }

    @Override public boolean has(String key) throws SourceUnavailableException {
        return parent.has(key == null ? null : prefix + key);
    }

    @Override public Map<String, String> dump() throws SourceUnavailableException {
        if (this.dump == null) {
            Map<String, String> source = parent.dump();
            Map<String, String> dump = new LinkedHashMap<>(source.size());
            for (Map.Entry<String, String> entry : source.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(prefix)) {
                    dump.put(key.substring(prefix.length()), entry.getValue());
                }
            }
            this.dump = dump;
        }
        return this.dump;
    }

    @Override
    public String toString() {
        return Spell.get(dump());
    }

    @Override
    public String getIdentity(String key) throws SourceUnavailableException {
        return null;
    }

}
