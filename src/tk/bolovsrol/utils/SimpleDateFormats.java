package tk.bolovsrol.utils;

import java.text.SimpleDateFormat;

/**
 * Тут собраны более-менее стандартные паттерны {@link SimpleDateFormat},
 * рассованные по контейнерам {@link ThreadLocal<SimpleDateFormat>},
 * чтобы каждый раз не создавать новую инстанцию и не синхронизироваться около общей инстанции.
 * <p>
 * Формат имён соответствует паттерну и вполне очевиден.
 *
 * @see DateTimeFormatters
 */
public final class SimpleDateFormats {

    public static final ThreadLocal<SimpleDateFormat> DATE_SPACE_TIME = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    public static final ThreadLocal<SimpleDateFormat> DATE_SPACE_TIME_MS = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
    public static final ThreadLocal<SimpleDateFormat> DATE_COMMA_TIME_MS = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss.SSS"));
    public static final ThreadLocal<SimpleDateFormat> DATE_T_TIME_MS_TZ = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
    public static final ThreadLocal<SimpleDateFormat> DATE = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    public static final ThreadLocal<SimpleDateFormat> TIME = ThreadLocal.withInitial(() -> new SimpleDateFormat("HH:mm:ss"));
    public static final ThreadLocal<SimpleDateFormat> TIME_MS = ThreadLocal.withInitial(() -> new SimpleDateFormat("HH:mm:ss.SSS"));

    private SimpleDateFormats() {
    }
}
