package tk.bolovsrol.utils.log;

/**
 * Лёгкий карманный лог.
 * <p/>
 * Статический класс, содержит инстанцию {@link LogDome}, которой всё и делегирует.
 */
public final class Log {

    private Log() {
    }

    private static LogDome instance = null;

	private static final class DefaultLogDomeContainer {
		private static final LogDome DEFAULT = new LogDome(LogDome.DEF_OUT);

        private DefaultLogDomeContainer() {
        }
    }

    static {
        UtilityLogClasses.registerLoggerClass(Log.class);
    }

    /**
     * Возвращает актуальную инстанцию логгера.
     * <p/>
     * После создания класса это логгер с параметрами по умолчанию.
     *
     * @return логгер
     */
    public static LogDome getInstance() {
        return instance != null ? instance : DefaultLogDomeContainer.DEFAULT;
    }

    /**
     * Устанавливает новый логгер.
     *
     * @param log новый логгер.
     */
    public static void setInstance(LogDome log) {
        instance = log;
    }

    // ----- Делегаты
    public static void trace(Object message) {
        if (instance != null) {
            instance.trace(message);
        } else {
            DefaultLogDomeContainer.DEFAULT.trace(message);
        }
    }

    public static void trace(Object... messages) {
        if (instance != null) {
            instance.trace(messages);
        } else {
            DefaultLogDomeContainer.DEFAULT.trace(messages);
        }
    }

    public static void info(Object message) {
        if (instance != null) {
            instance.info(message);
        } else {
            DefaultLogDomeContainer.DEFAULT.info(message);
        }
    }

    public static void info(Object... messages) {
        if (instance != null) {
            instance.info(messages);
        } else {
            DefaultLogDomeContainer.DEFAULT.info(messages);
        }
    }

    public static void hint(Object message) {
        if (instance != null) {
            instance.hint(message);
        } else {
            DefaultLogDomeContainer.DEFAULT.hint(message);
        }
    }

    public static void hint(Object... messages) {
        if (instance != null) {
            instance.hint(messages);
        } else {
            DefaultLogDomeContainer.DEFAULT.hint(messages);
        }
    }

    public static void warning(Object message) {
        if (instance != null) {
            instance.warning(message);
        } else {
            DefaultLogDomeContainer.DEFAULT.warning(message);
        }
    }

    public static void warning(Object... messages) {
        if (instance != null) {
            instance.warning(messages);
        } else {
            DefaultLogDomeContainer.DEFAULT.warning(messages);
        }
    }

    public static void exception(Object message) {
        if (instance != null) {
            instance.exception(message);
        } else {
            DefaultLogDomeContainer.DEFAULT.exception(message);
        }
    }

    public static void exception(Object... messages) {
        if (instance != null) {
            instance.exception(messages);
        } else {
            DefaultLogDomeContainer.DEFAULT.exception(messages);
        }
    }

    public static void write(LogLevel level, Object message) {
        if (instance != null) {
            instance.write(level, message);
        } else {
            DefaultLogDomeContainer.DEFAULT.write(level, message);
        }
    }

    public static void write(LogLevel level, Object... messages) {
        if (instance != null) {
            instance.write(level, messages);
        } else {
            DefaultLogDomeContainer.DEFAULT.write(level, messages);
        }
    }

    public static void write(LogLevel level, boolean fullStacktrace, Object message) {
        if (instance != null) {
            instance.write(level, fullStacktrace, message);
        } else {
            DefaultLogDomeContainer.DEFAULT.write(level, fullStacktrace, message);
        }
    }

    public static void write(LogLevel level, boolean fullStacktrace, Object... messages) {
        if (instance != null) {
            instance.write(level, fullStacktrace, messages);
        } else {
            DefaultLogDomeContainer.DEFAULT.write(level, fullStacktrace, messages);
        }
    }

    public static boolean isAllowed(LogLevel level) {
        return instance != null ? instance.isAllowed(level) : DefaultLogDomeContainer.DEFAULT.isAllowed(level);
    }
}
