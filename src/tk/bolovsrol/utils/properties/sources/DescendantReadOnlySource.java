package tk.bolovsrol.utils.properties.sources;

import tk.bolovsrol.utils.StringDumpBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Источник-воронка. Автоматический сливатель нескольких источников.
 * <p/>
 * Если в первом источнике нет запрошенного ключа, то запрос адресуется
 * второму источнику и т.д.
 * <p/>
 * Аналогично, дамп возвращает содержимое всех источников.
 * Чем первее источник, тем выше приоритет его значения.
 */
public class DescendantReadOnlySource implements ReadOnlySource {

    private final ReadOnlySource[] sources;

    /** Чем раньше соурс в списке, тем выше его приоритет. */
    public DescendantReadOnlySource(ReadOnlySource... sources) {
        this.sources = sources;
    }

    @Override
    public String expand(String localBranchKey) {
        return localBranchKey;
    }

    @Override
    public String get(String key) {
        for (ReadOnlySource source : sources) {
            if (source.has(key)) {
                return source.get(key);
            }
        }
        return null;
    }

    @Override
    public boolean has(String key) {
        for (ReadOnlySource source : sources) {
            if (source.has(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String,String> dump() {
        Map<String, String> result = new HashMap<>();
        for (int i = sources.length - 1; i >= 0; i--) {
            result.putAll(sources[i].dump());
        }
        return result;
    }

    @Override
    public String getIdentity(String key) throws SourceUnavailableException {
        for (ReadOnlySource source : sources) {
            if (source.has(key)) {
                return source.getIdentity(key);
            }
        }
        return null;
    }

    @Override public String toString() {
        StringDumpBuilder sdb = new StringDumpBuilder();
        for (Map.Entry<String, String> entry : dump().entrySet()) {
            sdb.append(entry.getKey(), entry.getValue());
        }
        return sdb.toString();
    }
}
