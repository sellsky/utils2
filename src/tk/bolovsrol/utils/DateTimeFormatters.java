package tk.bolovsrol.utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Тут собраны более-менее стандартные паттерны {@link java.time.format.DateTimeFormatter} с системной дефолтной зоной,
 * чтобы каждый раз не создавать новую инстанцию.
 * <p>
 * Формат имён соответствует паттерну и вполне очевиден.
 *
 * @see SimpleDateFormats
 */
public final class DateTimeFormatters {

	public static final DateTimeFormatter DATE_SPACE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter DATE_SPACE_TIME_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter DATE_SPACE_TIME_NANOS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter DATE_COMMA_TIME_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd,HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter DATE_T_TIME_MS_TZ = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter DATE_T_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
	public static final DateTimeFormatter TIME_MS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

	private DateTimeFormatters() {
	}
}
