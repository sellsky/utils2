package tk.bolovsrol.utils.log.providers;

import tk.bolovsrol.utils.Spell;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Пул провайдеров, принимающик в качестве параметра файл.
 * <p/>
 * Для сопоставления имена файлов приводятся к канонической форме, всё чотко.
 */
abstract class AbstractFileProviderPool implements LogWriterProviderPool {

    private final Map<String, LogWriterProvider> providers = new TreeMap<>();

    @Override public LogWriterProvider retrieve(String data) throws StreamProviderException {
        File file = new File(data);
        try {
            LogWriterProvider provider = providers.get(file.getCanonicalPath());
            if (provider == null) {
                provider = newStreamProvider(file);
                providers.put(data, provider);
            }
            return provider;
        } catch (IOException e) {
            throw new StreamProviderException("Cannot resolve file " + Spell.get(data), e);
        }
    }

    protected abstract LogWriterProvider newStreamProvider(File file);

}
