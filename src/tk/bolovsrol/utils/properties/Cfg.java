package tk.bolovsrol.utils.properties;

import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.sources.EmptyReadOnlySource;
import tk.bolovsrol.utils.properties.sources.FileReadOnlySource;
import tk.bolovsrol.utils.properties.sources.ReadOnlySource;
import tk.bolovsrol.utils.properties.sources.SourceUnavailableException;
import tk.bolovsrol.utils.time.Duration;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Статический конфиг, чтобы параметр везде не таскать.
 * <p/>
 * Делегирует все вызовы пропертям, которые в него надо воткнуть.
 *
 * @see Cfg#init(ReadOnlyProperties)
 */
public final class Cfg {

    private static ReadOnlyProperties delegate = EmptyReadOnlySource.EMPTY_PROPERTIES;

    private Cfg() {
    }

    public static void init(String filename) throws UnexpectedBehaviourException {
        init(new FileReadOnlySource(filename));
    }

    public static void init(ReadOnlySource source) {
        init(new ReadOnlyProperties(source));
    }

    public static void init(ReadOnlyProperties properties) {
        delegate = properties;
    }

    public static ReadOnlyProperties getInstance() {
        return delegate;
    }

    // delegation

	public static ReadOnlySource getReadOnlySource() {return delegate.getReadOnlySource();}

	public static String get(String key) {return delegate.get(key);}

	public static Map<String, String> dump() {return delegate.dump();}

	public static BigDecimal getBigDecimal(String key) throws InvalidPropertyValueFormatException {return delegate.getBigDecimal(key);}

	public static String coalesce(String key, String defaultValue, ReadOnlyProperties rop1, ReadOnlyProperties rop2) {return ReadOnlyProperties.coalesce(key, defaultValue, rop1, rop2);}

	public static Boolean getBoolean(String key, Supplier<Boolean> defaultValueSupplier) {return delegate.getBoolean(key, defaultValueSupplier);}

	public static BigDecimal getBigDecimal(String key, Supplier<BigDecimal> defaultValueSupplier) throws InvalidPropertyValueFormatException {return delegate.getBigDecimal(key, defaultValueSupplier);}

	public static Long getLong(String key, Supplier<Long> defaultValueSupplier) throws InvalidPropertyValueFormatException {return delegate.getLong(key, defaultValueSupplier);}

	public static Integer getIntegerOrDie(String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {return delegate.getIntegerOrDie(key);}

	public static Long getULong(String key) throws InvalidPropertyValueFormatException {return delegate.getULong(key);}

	public static boolean equals(ReadOnlyProperties that) {return delegate.equals(that);}

	public static String getIdentity(String key) throws SourceUnavailableException {return delegate.getIdentity(key);}

	public static Integer getInteger(String key, Supplier<Integer> defaultValueSupplier) throws InvalidPropertyValueFormatException {return delegate.getInteger(key, defaultValueSupplier);}

	public static Long getULong(String key, Supplier<Long> defaultValueSupplier) throws InvalidPropertyValueFormatException {return delegate.getULong(key, defaultValueSupplier);}

	public static Pattern getPattern(String key) throws InvalidPropertyValueFormatException {return delegate.getPattern(key);}

	public static <E extends Enum<E>> E getEnum(Class<E> cl, String key, Supplier<E> defaultValueSupplier) throws InvalidPropertyValueFormatException {return delegate.getEnum(cl, key, defaultValueSupplier);}

	public static BigDecimal getBigDecimal(String key, BigDecimal defaultValue, LogDome log) {return delegate.getBigDecimal(key, defaultValue, log);}

	public static String expand(String localBranchKey) {return delegate.expand(localBranchKey);}

	public static String getOrDie(String key) throws PropertyNotFoundException {return delegate.getOrDie(key);}

	public static Pattern getPatternOrDie(String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {return delegate.getPatternOrDie(key);}

	public static Integer getInteger(String key, Integer defaultValue, LogDome log) {return delegate.getInteger(key, defaultValue, log);}

	public static String get(String key, String defaultValue) {return delegate.get(key, defaultValue);}

	public static Pattern getPattern(String key, Pattern defaultValue) throws InvalidPropertyValueFormatException {return delegate.getPattern(key, defaultValue);}

	public static <E extends Enum<E>> E getEnum(Class<E> cl, String key, E defaultValue) throws InvalidPropertyValueFormatException {return delegate.getEnum(cl, key, defaultValue);}

	public static Date getDate(String key, DateFormat format, Supplier<Date> defaultValueSupplier) throws InvalidPropertyValueFormatException {return delegate.getDate(key, format, defaultValueSupplier);}

	public static Long getULongOrDie(String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {return delegate.getULongOrDie(key);}

	public static Long getLong(String key) throws InvalidPropertyValueFormatException {return delegate.getLong(key);}

	public static Duration getDuration(String key, Supplier<Duration> defaultValueSupplier) throws InvalidPropertyValueFormatException {return delegate.getDuration(key, defaultValueSupplier);}

	public static Date getDate(String key, DateFormat format, Date defaultValue) throws InvalidPropertyValueFormatException {return delegate.getDate(key, format, defaultValue);}

	public static <E extends Enum<E>> E getEnum(Class<E> cl, String key) throws InvalidPropertyValueFormatException {return delegate.getEnum(cl, key);}

	public static BigDecimal getBigDecimal(String key, BigDecimal defaultValue) throws InvalidPropertyValueFormatException {return delegate.getBigDecimal(key, defaultValue);}

	public static boolean has(String key) {return delegate.has(key);}

	public static Date getDate(String key, DateFormat format, Date defaultValue, LogDome log) {return delegate.getDate(key, format, defaultValue, log);}

	public static Long getLong(String key, Long defaultValue) throws InvalidPropertyValueFormatException {return delegate.getLong(key, defaultValue);}

	public static Date getDate(String key, DateFormat format) throws InvalidPropertyValueFormatException {return delegate.getDate(key, format);}

	public static Pattern getPattern(String key, Supplier<Pattern> defaultValueSupplier) throws InvalidPropertyValueFormatException {return delegate.getPattern(key, defaultValueSupplier);}

	public static Long getULong(String key, Long defaultValue, LogDome log) {return delegate.getULong(key, defaultValue, log);}

	public static BigDecimal getBigDecimalOrDie(String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {return delegate.getBigDecimalOrDie(key);}

	public static ReadOnlyProperties getBranch(String prefix) {return delegate.getBranch(prefix);}

	public static Date getDateOrDie(String key, DateFormat format) throws PropertyNotFoundException, InvalidPropertyValueFormatException {return delegate.getDateOrDie(key, format);}

	public static <E extends Enum<E>> E getEnumOrDie(Class<E> cl, String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {return delegate.getEnumOrDie(cl, key);}

	public static String coalesce(String key, String defaultValue, ReadOnlyProperties rop1, ReadOnlyProperties rop2, ReadOnlyProperties rop3) {return ReadOnlyProperties.coalesce(key, defaultValue, rop1, rop2, rop3);}

	public static Boolean getBoolean(String key) {return delegate.getBoolean(key);}

	public static <E extends Enum<E>> E getEnum(String key, E defaultValue, LogDome log) {return delegate.getEnum(key, defaultValue, log);}

	public static Long getLong(String key, Long defaultValue, LogDome log) {return delegate.getLong(key, defaultValue, log);}

	public static Long getLongOrDie(String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {return delegate.getLongOrDie(key);}

	public static Boolean getBooleanOrDie(String key) throws PropertyNotFoundException {return delegate.getBooleanOrDie(key);}

	public static <E extends Enum<E>> E getEnum(String key, E defaultValue) throws InvalidPropertyValueFormatException {return delegate.getEnum(key, defaultValue);}

	public static Long getULong(String key, Long defaultValue) throws InvalidPropertyValueFormatException {return delegate.getULong(key, defaultValue);}

	public static Boolean getBoolean(String key, Boolean defaultValue) {return delegate.getBoolean(key, defaultValue);}

	@Deprecated public static <E extends Enum<E>> E[] getEnumDelimited(Class<E> cl, String key) throws InvalidPropertyValueFormatException {return delegate.getEnumDelimited(cl, key);}

	public static String get(String key, Supplier<String> defaultValueSupplier) {return delegate.get(key, defaultValueSupplier);}

	@Deprecated public static Long[] getDelimitedLongs(String key) throws InvalidPropertyValueFormatException {return delegate.getDelimitedLongs(key);}

	public static Duration getDuration(String key) throws InvalidPropertyValueFormatException {return delegate.getDuration(key);}

	public static Duration getDurationOrDie(String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {return delegate.getDurationOrDie(key);}

	public static Integer getInteger(String key, Integer defaultValue) throws InvalidPropertyValueFormatException {return delegate.getInteger(key, defaultValue);}

	public static Duration getDuration(String key, Duration defaultValue) throws InvalidPropertyValueFormatException {return delegate.getDuration(key, defaultValue);}

	public static Integer getInteger(String key) throws InvalidPropertyValueFormatException {return delegate.getInteger(key);}

	public static String coalesce(String key, String defaultValue, ReadOnlyProperties... rops) {return ReadOnlyProperties.coalesce(key, defaultValue, rops);}

	public static Duration getDuration(String key, Duration defaultValue, LogDome log) {return delegate.getDuration(key, defaultValue, log);}


}
