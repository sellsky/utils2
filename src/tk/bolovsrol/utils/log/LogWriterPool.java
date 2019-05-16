package tk.bolovsrol.utils.log;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.providers.GzipFileProviderPool;
import tk.bolovsrol.utils.log.providers.LogWriterProvider;
import tk.bolovsrol.utils.log.providers.LogWriterProviderPool;
import tk.bolovsrol.utils.log.providers.SmtpWriterProviderPool;
import tk.bolovsrol.utils.log.providers.StandardWriterProviderPool;
import tk.bolovsrol.utils.log.providers.StreamProviderException;
import tk.bolovsrol.utils.log.providers.TextFileProviderPool;
import tk.bolovsrol.utils.mail.smtp.SmtpConst;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Пул писателей в лог. Для каждого лога в пуле создаётся собственный писатель.
 * <p/>
 * Здесь всё захардкодено лол.
 */
public final class LogWriterPool {

    private static final Map<String, LogWriterProviderPool> PROVIDER_FACTORIES = new TreeMap<>();
    private static final Set<String> PROVIDER_NAMES = Collections.unmodifiableSet(PROVIDER_FACTORIES.keySet());

    static {
        PROVIDER_FACTORIES.put("stream", new StandardWriterProviderPool());
        TextFileProviderPool textFileProviderPool = new TextFileProviderPool();
        PROVIDER_FACTORIES.put("file", textFileProviderPool);
        PROVIDER_FACTORIES.put("text", textFileProviderPool);
        PROVIDER_FACTORIES.put("gzip", new GzipFileProviderPool());
        PROVIDER_FACTORIES.put(SmtpConst.SCHEME_SMTP, new SmtpWriterProviderPool(SmtpConst.SCHEME_SMTP, SmtpConst.DEFAULT_SMTP_PORT));
        PROVIDER_FACTORIES.put(SmtpConst.SCHEME_SMTPS, new SmtpWriterProviderPool(SmtpConst.SCHEME_SMTPS, SmtpConst.DEFAULT_SMTPS_PORT));
    }

    private static final Map<LogWriterProvider, LogWriter> WRITERS = new HashMap<>();

    private LogWriterPool() {
    }

    public static Set<String> getProviderNames() {
        return PROVIDER_NAMES;
    }

    public static synchronized LogWriter retrieve(String type, String data) throws StreamProviderException {
        LogWriterProviderPool providerPool = PROVIDER_FACTORIES.get(type);
        if (providerPool == null) {
            throw new StreamProviderException("Unexpected Log Provider Type " + Spell.get(type) + ", assuming [stream]");
        }

        LogWriterProvider provider = providerPool.retrieve(data);

        LogWriter writer = WRITERS.get(provider);
        if (writer == null) {
            writer = new ThreadedLogWriter(provider);
            WRITERS.put(provider, writer);
        }
        return writer;
    }
}
