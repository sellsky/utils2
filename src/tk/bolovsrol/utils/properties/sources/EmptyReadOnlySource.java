package tk.bolovsrol.utils.properties.sources;

import tk.bolovsrol.utils.properties.ReadOnlyProperties;

import java.util.Collections;
import java.util.Map;

/**
 * Пустые проперти без значений ваще.
 * <p/>
 * Делать более одного объекта нет смысла.
 */
public class EmptyReadOnlySource implements ReadOnlySource {

    public static final EmptyReadOnlySource EMPTY_SOURCE = new EmptyReadOnlySource();
    public static final ReadOnlyProperties EMPTY_PROPERTIES = new ReadOnlyProperties(EMPTY_SOURCE);

    protected EmptyReadOnlySource() {
    }

    @Override public String expand(String localBranchKey) {
        return localBranchKey;
    }

    @Override public String get(String key) {
        return null;
    }

    @Override public boolean has(String key) {
        return false;
    }

    @Override public Map<String,String> dump() throws SourceUnavailableException {
        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        return "(empty)";
    }

    @Override
    public String getIdentity(String key) throws SourceUnavailableException {
        return null;
    }
}
