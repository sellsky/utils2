package tk.bolovsrol.utils.log;

import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.properties.Cfg;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Содержит вычитанный из конфига список названий классов,
 * для которых надо всегда показывать полный стектрейс.
 * <p/>
 * Можно указывать через запятую как полные имена классов,
 * так и простые (без пекеджа).
 * <p/>
 * Логгер пользуется этим при выяснении, нужно ли показывать полный стектрейс.
 */
final class StackTracedThrowables {
	/**
	 * Список названий классов-исключений, наследников {@link Throwable}, для которых нужно всегда печатать стектрейс.
	 * Можно указывать как полное ({@link Class#getName()}), так и сокращённое ({@link Class#getSimpleName()}) названия.
	 */
	private static final Set<String> STACKTRACE_CLASS_NAMES = new HashSet<>();

    private StackTracedThrowables() {
    }

    static {
        String classList = Cfg.get("log.stacktrace.classes");
        if (classList != null) {
            STACKTRACE_CLASS_NAMES.addAll(Arrays.asList(StringUtils.parseDelimited(classList)));
        }
        // мы всегда заинтересованы в стектрейсах RuntimeException и наследников
        STACKTRACE_CLASS_NAMES.add(RuntimeException.class.getName());
    }

    /**
     * @param throwable проверяемое исключение
     * @return true, если класс исключения или родителя упомянули в списке для стектрейса, иначе false
     */
    public static boolean isStackTraceFor(Throwable throwable) {
        return isStackTraceFor(throwable.getClass());
    }

    /**
     * @param cl проверяемый класс
     * @return true, если класс или родителя упомянули в списке для стектрейса, иначе false
     */
    public static boolean isStackTraceFor(Class<? extends Throwable> cl) {
        while (true) {
            boolean enrolled = STACKTRACE_CLASS_NAMES.contains(cl.getName()) || STACKTRACE_CLASS_NAMES.contains(cl.getSimpleName());
            if (enrolled) {
                return true;
            }
            Class<?> parent = cl.getSuperclass();
            if (Throwable.class.isAssignableFrom(parent)) {
                //noinspection unchecked
                cl = (Class<? extends Throwable>) parent;
            } else {
                return false;
            }
        }
    }

}
