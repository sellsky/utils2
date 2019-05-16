package tk.bolovsrol.utils.log;

import java.util.HashSet;
import java.util.Set;

/**
 * Содержит список имён служебных классов,
 * которые нужно игнорировать при поиске имени класса для строки лога.
 * <p/>
 * Классы сопоставляются по их именам.
 * <p/>
 * Если между приложением и логгером используется класс-посредник,
 * то предполагается, что в нём будет статический инициализатор,
 * вызывающий {@link #registerLoggerClass(String)} со своим именем.
 */
public final class UtilityLogClasses {

	private static final Set<String> LOGGER_CLASSES = new HashSet<>();

    private UtilityLogClasses() {
    }

    /**
     * Внешний лог-класс должен регистрировать себя здесь, чтобы в строках лога,
     * которые он транслирует, фигурировало имя класса, вызвавшего запись в лог, а не имя этого
     * класса- транслятора.
     * <p/>
     * Предполагается, что этот метод будут вызывать
     * из статического инициализатора логгирующего класса.
     *
     * @param logClass класс.
     */
    public static void registerLoggerClass(Class<?> logClass) {
        registerLoggerClass(logClass.getName());
    }

    /**
     * Внешний лог-класс должен регистрировать себя здесь, чтобы в строках лога,
     * которые он транслирует, фигурировало имя класса, вызвавшего запись в лог, а не имя этого
     * класса- транслятора.
     * <p/>
     * Предполагается, что этот метод будут вызывать
     * из статического инициализатора логгирующего класса.
     *
     * @param logClassName имя класса.
     */
    public static void registerLoggerClass(String logClassName) {
		synchronized (LOGGER_CLASSES) {
			LOGGER_CLASSES.add(logClassName);
		}
	}

    /**
     * Проверяем, является ли переданный класс одним из классов-логгеров.
     *
     * @param className имя интересующего класса
     * @return true, если является, иначе false
     */
    public static boolean isLoggerClass(String className) {
		return LOGGER_CLASSES.contains(className);
	}
}
