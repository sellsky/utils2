package tk.bolovsrol.utils.properties.sources;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapPlainSource extends MapReadOnlySource implements PlainSource {

    public MapPlainSource() {
        this(new LinkedHashMap<>());
    }

    public MapPlainSource(Map<String, String> source) {
        super(source);
    }

    @Override public MapPlainSource clear() throws SourceUnavailableException {
        source.clear();
        return this;
    }

    @Override public MapPlainSource drop(String key) {
        source.remove(key);
        return this;
    }

    @Override public MapPlainSource set(String key, String value) {
        source.put(key, value);
        return this;
    }

    @Override public MapPlainSource setAll(Map<String, String> matter) {
        source.putAll(matter);
        return this;
    }
}
