package tk.bolovsrol.utils.properties.sources;

import java.util.Map;

public class ProxyReadOnlySource implements ReadOnlySource {
    private ReadOnlySource delegate;

    public ProxyReadOnlySource() {
    }

    public ProxyReadOnlySource(ReadOnlySource delegate) {
        this.delegate = delegate;
    }

    public ReadOnlySource getDelegate() {
        return delegate;
    }

    public void setDelegate(ReadOnlySource delegate) {
        this.delegate = delegate;
    }

    @Override public String expand(String localBranchKey) {
        return delegate.expand(localBranchKey);
    }

    @Override public String get(String key) throws SourceUnavailableException {
        return delegate.get(key);
    }

    @Override public boolean has(String key) throws SourceUnavailableException {
        return delegate.has(key);
    }

    @Override public Map<String, String> dump() throws SourceUnavailableException {
        return delegate.dump();
    }

    @Override public String getIdentity(String key) throws SourceUnavailableException {
        return delegate.getIdentity(key);
    }

    @Override public String toString() {
		return delegate.toString();
	}
}
