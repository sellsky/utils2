package tk.bolovsrol.utils.caseinsensitive;

class CaselessKey implements Comparable<CaselessKey> {
    private final String originalKey;
    private String lowercaseKey;

    public CaselessKey(String originalKey) {
        this.originalKey = originalKey;
        this.lowercaseKey = null;
    }

    public String getOriginalKey() {
        return originalKey;
    }

    public String getLowercaseKey() {
        if (lowercaseKey == null) {
            lowercaseKey = originalKey.toLowerCase();
        }
        return lowercaseKey;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CaselessKey && ((CaselessKey) o).getLowercaseKey().equals(getLowercaseKey());
    }

    @Override
    public int hashCode() {
        return getLowercaseKey().hashCode();
    }

    public int compareTo(CaselessKey o) {
        return getLowercaseKey().compareTo(o.getLowercaseKey());
    }

    @Override
    public String toString() {
        return originalKey;
    }
}
