package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.UnexpectedBehaviourException;

/** Версия протокола HTTP. */
public enum HttpVersion {
    HTTP_1_0("HTTP/1.0", false),
    HTTP_1_1("HTTP/1.1", true);

    private final String raw;
    private final boolean persistentByDefault;

    HttpVersion(String raw, boolean persistentByDefault) {
        this.raw = raw;
        this.persistentByDefault = persistentByDefault;
    }

    public static HttpVersion parse(String raw) throws UnexpectedBehaviourException {
        for (HttpVersion httpVersion : values()) {
            if (httpVersion.raw.equals(raw)) {
                return httpVersion;
            }
        }
        throw new UnexpectedBehaviourException("Unknown HTTP version " + Spell.get(raw));
    }

    public boolean isPersistentByDefault() {
        return persistentByDefault;
    }

    @Override
    public String toString() {
        return raw;
    }
}
