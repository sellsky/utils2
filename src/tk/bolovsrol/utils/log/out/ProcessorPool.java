package tk.bolovsrol.utils.log.out;

import tk.bolovsrol.utils.log.LogLevel;
import tk.bolovsrol.utils.log.LogWriterPool;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/** Сборник обработчиков параметров для парсера {@link OutParser}. */
final class ProcessorPool {

	private static final Map<String, WordProcessor> PROCESSORS = new TreeMap<>();

    private ProcessorPool() {
    }

    static {
        register(LogLevel.getLevelNames(), (key, data, out) -> out.level = LogLevel.valueOf(key.toUpperCase()));
        register(LogWriterPool.getProviderNames(), (key, data, out) -> out.writer = LogWriterPool.retrieve(key, data));
    }

	private static void register(String keyword, WordProcessor processor) {
		PROCESSORS.put(keyword.toLowerCase(), processor);
	}

    private static void register(Collection<String> keywords, WordProcessor processor) {
        for (String keyword : keywords) {
            PROCESSORS.put(keyword.toLowerCase(), processor);
        }
    }

    public static WordProcessor getProcessor(String keyword) {
        return PROCESSORS.get(keyword.toLowerCase());
    }

}
