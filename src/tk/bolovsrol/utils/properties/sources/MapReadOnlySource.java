package tk.bolovsrol.utils.properties.sources;

import tk.bolovsrol.utils.Spell;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapReadOnlySource implements ReadOnlySource {
    protected final Map<String, String> source;

    public MapReadOnlySource(Map<String, String> source) {
        this.source = source;
    }

    @Override public String expand(String localBranchKey) {
        return localBranchKey;
    }

    @Override public String get(String key) {
        return source.get(key);
    }

    @Override public boolean has(String key) {
        return source.containsKey(key);
    }

    @Override public Map<String,String> dump() {
        return new LinkedHashMap<>(source);
    }

	@Override public String toString() {
		return Spell.get(source);
    }

    @Override
    public String getIdentity(String key) throws SourceUnavailableException {
        return null;
    }

	public Map<String, String> getMap() {
		return source;
	}
}
