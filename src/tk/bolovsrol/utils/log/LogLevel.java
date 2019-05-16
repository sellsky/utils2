package tk.bolovsrol.utils.log;

import tk.bolovsrol.utils.properties.Cfg;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Допустимые уровни детализации. */
public enum LogLevel {
    CONCEAL(false),
    TRACE(false),
    INFO(false),
    HINT(false),
    WARNING(false),
    EXCEPTION(true),
    NONE(false);

    public static final LogLevel DEFAULT = HINT;

    public static final Set<String> LEVEL_NAMES;

    static {
		Set<String> levelNames = new LinkedHashSet<>();
		for (LogLevel logLevel : LogLevel.values()) {
            levelNames.add(logLevel.name());
        }
        LEVEL_NAMES = Collections.unmodifiableSet(levelNames);
    }

    private final boolean fullStackTrace;

    LogLevel(boolean fullStackTraceDef) {
		this.fullStackTrace = Cfg.getBoolean("log.stacktrace." + name(), fullStackTraceDef);
	}

    public boolean isFullStackTrace() {
        return fullStackTrace;
    }

    public static Set<String> getLevelNames() {
        return LEVEL_NAMES;
    }
}
