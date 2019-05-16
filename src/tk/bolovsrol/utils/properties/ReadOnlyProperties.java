package tk.bolovsrol.utils.properties;

import tk.bolovsrol.utils.BooleanUtils;
import tk.bolovsrol.utils.PatternCompileException;
import tk.bolovsrol.utils.RegexUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.filters.BranchReadOnlySource;
import tk.bolovsrol.utils.properties.sources.FileReadOnlySource;
import tk.bolovsrol.utils.properties.sources.MapReadOnlySource;
import tk.bolovsrol.utils.properties.sources.ReadOnlySource;
import tk.bolovsrol.utils.properties.sources.SourceUnavailableException;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TimeUtils;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Простые проперти, доступные только для чтения.
 * <p/>
 * Реально этот класс занимается преобразованием в нужный формат значений, полученных
 * из {@link ReadOnlySource}, который занимается вопросом их физического выковыривания.
 */
public class ReadOnlyProperties implements ReadOnlySource {

	/** Источник данных. */
	protected final ReadOnlySource ros;

    public ReadOnlyProperties(ReadOnlySource pr) {
        this.ros = pr;
    }

    public ReadOnlyProperties(Map<String, String> dump) {
        this(new MapReadOnlySource(dump));
    }

    public ReadOnlyProperties(String filename) throws UnexpectedBehaviourException {
        this(new FileReadOnlySource(filename));
    }

    public ReadOnlySource getReadOnlySource() {
        return ros;
    }

    // ------ доступ к массивам значений

    /**
     * Возвращает карту значений текущих пропертей.
     * Карта не привязана к пропертям, изменения в ней не затонут проперти et vice versa.
     *
     * @return карта значений
     */
    @Override
    public Map<String, String> dump() {
        return ros.dump();
    }

    /**
     * Выбирает проперти, название которых начинается с указанного префикса,
     * и возвращает их отдельным пропертёвым объектом.
     * <p/>
     * Если указанный префикс не заканчивается точкой «.»,
     * то точка будет добавлена насильно. Такие дела.
     * <p/>
     * Ключи возвращаемых пропертей также будут без префикса.
     *
     * @param prefix интересующий префикс
     * @return проперти, название которых начинается с указанного префикса
     * @see #expand(String)
     */
    public ReadOnlyProperties getBranch(String prefix) {
        return new ReadOnlyProperties(new BranchReadOnlySource(prefix.endsWith(".") ? prefix : prefix + '.', ros));
    }

    /**
     * Возвращает полное название ключа, как оно задано в конфиге,
     * невзирая на бранчи.
     * <p/>
     * Этим методом следует оборачивать название ключа,
     * который нужно показать пользователю.
     * <p/>
     * Например:<br/>
     * <code>
     * if (!cfg.has(key)) {<br/>
     * &nbsp;&nbsp;throw new Exception("Key " + cfg.expand(key) + " not specified")<br/>
     * }
     * </code>
     *
     * @param localBranchKey ключ
     * @return полное название ключа, как он указан в конфиге
     * @see #getBranch(String)
     */
    @Override
    public String expand(String localBranchKey) {
        return ros.expand(localBranchKey);
    }

    /**
     * Проверяет наличие указанного ключа в пропертях.
     *
     * @param key
     * @return true, если есть, иначе false
     */
    @Override
    public boolean has(String key) {
        return ros.has(key);
    }

	@Override
	public String getIdentity(String key) throws SourceUnavailableException {
		return ros.getIdentity(key);
	}

	private <E> E assertNotNull(String key, E value) throws PropertyNotFoundException {
		if (value == null) {
			throw new PropertyNotFoundException(expand(key));
		}
		return value;
	}

	// ------ строки
	@Override
	public String get(String key) {
		return get(key, (String) null);
	}

    public String getOrDie(String key) throws PropertyNotFoundException {
		return assertNotNull(key, get(key));
	}

    public String get(String key, String defaultValue) {
        String value = ros.get(key);
        return value == null ? defaultValue : value;
    }

	public String get(String key, Supplier<String> defaultValueSupplier) {
		String value = ros.get(key);
		return value == null ? defaultValueSupplier.get() : value;
	}

	// ------ Integer
	public Integer getInteger(String key, Supplier<Integer> defaultValueSupplier) throws InvalidPropertyValueFormatException {
		String source = get(key);
		if (source == null) { return defaultValueSupplier.get(); }
		try {
			return Integer.valueOf(source);
		} catch (NumberFormatException e) {
			throw InvalidPropertyValueFormatException.getNotANumber(expand(key), source, getIdentity(key), e);
		}
	}

	public Integer getInteger(String key, Integer defaultValue) throws InvalidPropertyValueFormatException {
		String source = get(key);
		if (source == null) { return defaultValue; }
		try {
			return Integer.valueOf(source);
		} catch (NumberFormatException e) {
			throw InvalidPropertyValueFormatException.getNotANumber(expand(key), source, getIdentity(key), e);
		}
	}

	public Integer getInteger(String key, Integer defaultValue, LogDome log) {
		try {
			return getInteger(key, defaultValue);
		} catch (InvalidPropertyValueFormatException e) {
			if (log != null) { log.warning(e.getMessage() + " Using default " + Spell.get(defaultValue) + '.'); }
			return defaultValue;
		}
	}

	public Integer getInteger(String key) throws InvalidPropertyValueFormatException {
		return getInteger(key, (Integer) null);
	}

	public Integer getIntegerOrDie(String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {
		return assertNotNull(key, getInteger(key));
	}


	// ------ Longs
	public Long getLong(String key, Supplier<Long> defaultValueSupplier) throws InvalidPropertyValueFormatException {
		String source = get(key);
		if (source == null) { return defaultValueSupplier.get(); }
		try {
			return Long.valueOf(source);
		} catch (NumberFormatException e) {
			throw InvalidPropertyValueFormatException.getNotANumber(expand(key), source, getIdentity(key), e);
		}
	}

	public Long getLong(String key, Long defaultValue) throws InvalidPropertyValueFormatException {
		String source = get(key);
		if (source == null) { return defaultValue; }
		try {
			return Long.valueOf(source);
		} catch (NumberFormatException e) {
			throw InvalidPropertyValueFormatException.getNotANumber(expand(key), source, getIdentity(key), e);
		}
	}

	public Long getLong(String key, Long defaultValue, LogDome log) {
		try {
			return getLong(key, defaultValue);
		} catch (InvalidPropertyValueFormatException e) {
			if (log != null) {
				log.warning(e.getMessage() + " Using default " + Spell.get(defaultValue) + '.');
			}
			return defaultValue;
		}
	}

	public Long getLong(String key) throws InvalidPropertyValueFormatException {
		return getLong(key, (Long) null);
	}

	public Long getLongOrDie(String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {
		return assertNotNull(key, getLong(key));
	}

	// Unsigned longs
	public Long getULong(String key, Supplier<Long> defaultValueSupplier) throws InvalidPropertyValueFormatException {
		String source = get(key);
		if (source == null) { return defaultValueSupplier.get(); }
		try { return Long.parseUnsignedLong(source); } catch (NumberFormatException e) {
			throw InvalidPropertyValueFormatException.getNotANumber(expand(key), source, getIdentity(key), e);
		}
	}

	public Long getULong(String key, Long defaultValue) throws InvalidPropertyValueFormatException {
		String source = get(key);
		if (source == null) { return defaultValue; }
		try { return Long.parseUnsignedLong(source); } catch (NumberFormatException e) {
			throw InvalidPropertyValueFormatException.getNotANumber(expand(key), source, getIdentity(key), e);
		}
	}

	public Long getULong(String key, Long defaultValue, LogDome log) {
		try { return getULong(key, defaultValue); } catch (InvalidPropertyValueFormatException e) {
			if (log != null) { log.warning(e.getMessage() + " Using default " + Spell.get(defaultValue) + '.'); }
			return defaultValue;
		}
	}

	public Long getULong(String key) throws InvalidPropertyValueFormatException {
		return getULong(key, (Long) null);
	}

	public Long getULongOrDie(String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {
		return assertNotNull(key, getULong(key));
	}

	@Deprecated
	public Long[] getDelimitedLongs(String key) throws InvalidPropertyValueFormatException {
		String[] delimited = StringUtils.parseDelimited(get(key));
		if (delimited == null) {
			return null;
		}
		Long[] values = new Long[delimited.length];
		for (int i = 0; i < values.length; i++) {
			try {
				values[i] = Long.valueOf(delimited[i]);
			} catch (NumberFormatException e) {
				throw InvalidPropertyValueFormatException.getNotANumber(expand(key), delimited[i], getIdentity(key), e);
			}
		}
		return values;
	}

	// ----- BigDecimal
	public BigDecimal getBigDecimal(String key, Supplier<BigDecimal> defaultValueSupplier) throws InvalidPropertyValueFormatException {
		String source = get(key);
		if (source == null) { return defaultValueSupplier.get(); }
		try {
			return new BigDecimal(source);
		} catch (NumberFormatException e) {
			throw InvalidPropertyValueFormatException.getNotANumber(expand(key), source, getIdentity(key), e);
		}
	}

	public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) throws InvalidPropertyValueFormatException {
		String source = get(key);
		if (source == null) { return defaultValue; }
		try {
			return new BigDecimal(source);
		} catch (NumberFormatException e) {
			throw InvalidPropertyValueFormatException.getNotANumber(expand(key), source, getIdentity(key), e);
		}
	}

	public BigDecimal getBigDecimal(String key, BigDecimal defaultValue, LogDome log) {
		try {
			return getBigDecimal(key, defaultValue);
		} catch (InvalidPropertyValueFormatException e) {
			if (log != null) {
				log.warning(e.getMessage() + " Using default " + Spell.get(defaultValue) + '.');
			}
			return defaultValue;
		}
	}

	public BigDecimal getBigDecimal(String key) throws InvalidPropertyValueFormatException {
		return getBigDecimal(key, (BigDecimal) null);
	}

    public BigDecimal getBigDecimalOrDie(String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {
		return assertNotNull(key, getBigDecimal(key));
	}


	// ------ Booleans
	public Boolean getBoolean(String key, Supplier<Boolean> defaultValueSupplier) {
		return BooleanUtils.parse(ros.get(key), defaultValueSupplier.get());
	}

	public Boolean getBoolean(String key, Boolean defaultValue) {
		return BooleanUtils.parse(ros.get(key), defaultValue);
	}

	public Boolean getBoolean(String key) {
		return getBoolean(key, (Boolean) null);
	}

    public Boolean getBooleanOrDie(String key) throws PropertyNotFoundException {
		return assertNotNull(key, getBoolean(key));
	}


	// ------ Dates
	public Date getDate(String key, DateFormat format, Supplier<Date> defaultValueSupplier) throws InvalidPropertyValueFormatException {
		String source = ros.get(key);
		if (source == null) { return defaultValueSupplier.get(); }
		try {
			return format.parse(source);
		} catch (ParseException e) {
			throw InvalidPropertyValueFormatException.getNotADate(expand(key), source, getIdentity(key), format, e);
		}
	}

	public Date getDate(String key, DateFormat format, Date defaultValue) throws InvalidPropertyValueFormatException {
		String source = ros.get(key);
		if (source == null) { return defaultValue; }
		try {
			return format.parse(source);
		} catch (ParseException e) {
			throw InvalidPropertyValueFormatException.getNotADate(expand(key), source, getIdentity(key), format, e);
		}
	}

	public Date getDate(String key, DateFormat format, Date defaultValue, LogDome log) {
		try {
			return getDate(key, format, defaultValue);
		} catch (InvalidPropertyValueFormatException e) {
			if (log != null) {
				log.warning(e.getMessage() + " Using default " + defaultValue + '.');
			}
			return defaultValue;
		}
	}

	public Date getDate(String key, DateFormat format) throws InvalidPropertyValueFormatException {
		return getDate(key, format, (Date) null);
	}

    public Date getDateOrDie(String key, DateFormat format) throws PropertyNotFoundException, InvalidPropertyValueFormatException {
		return assertNotNull(key, getDate(key, format));
	}


	// ------ Durations
	public Duration getDuration(String key, Supplier<Duration> defaultValueSupplier) throws InvalidPropertyValueFormatException {
		String source = ros.get(key);
		if (source == null) { return defaultValueSupplier.get(); }
		try {
			return new Duration(source);
		} catch (TimeUtils.DurationParsingException e) {
			throw InvalidPropertyValueFormatException.getNotADuration(expand(key), source, getIdentity(key), e);
		}
	}

	public Duration getDuration(String key, Duration defaultValue) throws InvalidPropertyValueFormatException {
		String source = ros.get(key);
		if (source == null) { return defaultValue; }
		try {
			return new Duration(source);
		} catch (TimeUtils.DurationParsingException e) {
			throw InvalidPropertyValueFormatException.getNotADuration(expand(key), source, getIdentity(key), e);
		}
	}

	public Duration getDuration(String key, Duration defaultValue, LogDome log) {
		try {
			return getDuration(key, defaultValue);
		} catch (InvalidPropertyValueFormatException e) {
			if (log != null) {
				log.warning(e.getMessage() + " Using default " + defaultValue + '.');
			}
			return defaultValue;
		}
	}

	public Duration getDuration(String key) throws InvalidPropertyValueFormatException {
		return getDuration(key, (Duration) null);
	}

    public Duration getDurationOrDie(String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {
		return assertNotNull(key, getDuration(key));
	}


	// ------ нумерации
	public <E extends Enum<E>> E getEnum(Class<E> cl, String key, Supplier<E> defaultValueSupplier) throws InvalidPropertyValueFormatException {
		String value = get(key);
		if (value == null) { return defaultValueSupplier.get(); }
		try {
			return Enum.valueOf(cl, value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw InvalidPropertyValueFormatException.getNotAnEnumValue(expand(key), value, getIdentity(key), cl, e);
		}
	}

	public <E extends Enum<E>> E getEnum(Class<E> cl, String key, E defaultValue) throws InvalidPropertyValueFormatException {
		String value = get(key);
		if (value == null) { return defaultValue; }
		try {
			return Enum.valueOf(cl, value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw InvalidPropertyValueFormatException.getNotAnEnumValue(expand(key), value, getIdentity(key), cl, e);
		}
	}

	public <E extends Enum<E>> E getEnum(String key, E defaultValue) throws InvalidPropertyValueFormatException {
		return getEnum(defaultValue.getDeclaringClass(), key, defaultValue);
	}

	public <E extends Enum<E>> E getEnum(String key, E defaultValue, LogDome log) {
		try {
			return getEnum(key, defaultValue);
		} catch (InvalidPropertyValueFormatException e) {
			if (log != null) {
				log.warning(e.getMessage() + " Using default " + Spell.get(defaultValue) + '.');
			}
			return defaultValue;
		}
	}

	public <E extends Enum<E>> E getEnum(Class<E> cl, String key) throws InvalidPropertyValueFormatException {
		return getEnum(cl, key, (E) null);
	}

    public <E extends Enum<E>> E getEnumOrDie(Class<E> cl, String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {
		return assertNotNull(key, getEnum(cl, key));
	}

	@SuppressWarnings({"unchecked"})
	@Deprecated
	public <E extends Enum<E>> E[] getEnumDelimited(Class<E> cl, String key) throws InvalidPropertyValueFormatException {
		String[] delimited = StringUtils.parseDelimited(get(key));
		if (delimited == null) {
			return null;
		}
		E[] result = (E[]) Array.newInstance(cl, delimited.length);
		for (int i = 0; i < delimited.length; i++) {
			try {
				result[i] = Enum.valueOf(cl, delimited[i]);
			} catch (IllegalArgumentException e) {
				throw InvalidPropertyValueFormatException.getNotAnEnumValue(expand(key), delimited[i], getIdentity(key), cl, e);
			}
		}
		return result;
	}

	//------ регулярные выражения
	public Pattern getPattern(String key, Supplier<Pattern> defaultValueSupplier) throws InvalidPropertyValueFormatException {
		String source = get(key);
		if (source == null) { return defaultValueSupplier.get(); }
		try {
			return RegexUtils.compilePattern(source);
		} catch (PatternCompileException e) {
			throw InvalidPropertyValueFormatException.getInvalidPattern(key, source, getIdentity(key), e);
		}
	}

	public Pattern getPattern(String key, Pattern defaultValue) throws InvalidPropertyValueFormatException {
		String source = get(key);
		if (source == null) { return defaultValue; }
		try {
			return RegexUtils.compilePattern(source);
		} catch (PatternCompileException e) {
			throw InvalidPropertyValueFormatException.getInvalidPattern(key, source, getIdentity(key), e);
		}
	}

	public Pattern getPattern(String key) throws InvalidPropertyValueFormatException {
		return getPattern(key, (Pattern) null);
	}

    public Pattern getPatternOrDie(String key) throws PropertyNotFoundException, InvalidPropertyValueFormatException {
		return assertNotNull(key, getPattern(key));
	}

	//------ статические методы для выбора ключа из набора пропертей без организации специального соурса
	/**
	 * Ищет в переданных пропертях значение по указанному ключу, возвращает первый не-нул-результат; если во всех пропертях значения нет, возвращает defaultValue.
	 *
	 * @param key искомый ключ
	 * @param defaultValue значение по умолчанию
	 * @param rops
	 * @return
	 * @see tk.bolovsrol.utils.properties.sources.DescendantReadOnlySource
	 */
	public static String coalesce(String key, String defaultValue, ReadOnlyProperties... rops) {
		String result;
		for (ReadOnlyProperties rop : rops) {
			result = rop.get(key);
			if (result != null) { return result; }
		}
		return defaultValue;
	}

	/**
	 * Ищет в переданных пропертях значение по указанному ключу, возвращает первый не-нул-результат; если во всех пропертях значения нет, возвращает defaultValue.
	 *
	 * @param key искомый ключ
	 * @param defaultValue значение по умолчанию
	 * @param rop1
	 * @param rop2
	 * @return
	 */
	public static String coalesce(String key, String defaultValue, ReadOnlyProperties rop1, ReadOnlyProperties rop2) {
		String result = rop1.get(key);
		if (result != null) { return result; }
		return rop2.get(key, defaultValue);
	}

	/**
	 * Ищет в переданных пропертях значение по указанному ключу, возвращает первый не-нул-результат; если во всех пропертях значения нет, возвращает defaultValue.
	 *
	 * @param key искомый ключ
	 * @param defaultValue значение по умолчанию
	 * @param rop1
	 * @param rop2
	 * @param rop3
	 * @return
	 */
	public static String coalesce(String key, String defaultValue, ReadOnlyProperties rop1, ReadOnlyProperties rop2, ReadOnlyProperties rop3) {
		String result = rop1.get(key);
		if (result != null) { return result; }
		result = rop2.get(key);
		if (result != null) { return result; }
		return rop3.get(key, defaultValue);
	}

    // ------ служебные методы
    @Override
    public boolean equals(Object that) {
        return this == that || (that instanceof ReadOnlyProperties && this.equals((ReadOnlyProperties) that));
    }

    public boolean equals(ReadOnlyProperties that) {
		return this == that || this.ros == that.ros || this.ros.equals(that.ros);
	}

    @Override
    public int hashCode() {
		return this.ros.hashCode();
	}

    @Override
    public String toString() {
        return ros.toString();
    }

}


