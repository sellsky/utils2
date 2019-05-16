package tk.bolovsrol.utils.properties.filters;

import tk.bolovsrol.utils.properties.sources.PlainSource;
import tk.bolovsrol.utils.properties.sources.SourceUnavailableException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Created by andrew.cherepivsky */
public class BranchPlainSource extends BranchReadOnlySource implements PlainSource {

    private final PlainSource parent;

    public BranchPlainSource(String prefix, PlainSource parent) {
        super(prefix, parent);
        this.parent = parent;
    }

    @Override public BranchPlainSource clear() throws SourceUnavailableException {
        parent.clear();
        return this;
    }

    @Override public BranchPlainSource drop(String key) throws SourceUnavailableException {
        parent.drop(key == null ? key : prefix + key);
        return this;
    }

    @Override public BranchPlainSource set(String key, String value) throws SourceUnavailableException {
        parent.set(key == null ? key : prefix + key, value);
        return this;
    }

    @Override public BranchPlainSource setAll(Map<String, String> matter) throws SourceUnavailableException {
        Map<String, String> lhm = new LinkedHashMap<>(matter.size());
        for (Map.Entry<String, String> entry : matter.entrySet()) {
            String key = entry.getKey();
            lhm.put(key == null ? key : prefix + key, entry.getValue());
        }
        parent.setAll(lhm);
        return this;
    }

}
