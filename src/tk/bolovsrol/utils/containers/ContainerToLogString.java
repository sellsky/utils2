package tk.bolovsrol.utils.containers;

import tk.bolovsrol.utils.DateTimeFormatters;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.NumberUtils;
import tk.bolovsrol.utils.SimpleDateFormats;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TwofacedTime;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Function;

/**
 * Здесь в одном месте собраны правила форматирования значений контейнеров для вывода их в лог.
 * <p>
 * В классе описаны правила преобразования не-нул-значений в строку, понятную человеку
 * и по возможности приятную человеческому взору.
 */
public final class ContainerToLogString {

    // числа и енумыы — значения как есть
    private static final Function<BigDecimal, String> BIG_DECIMAL = BigDecimal::toPlainString;
    private static final Function<Number, String> NUMBER = Object::toString;
    private static final Function<Enum<?>, String> ENUM = Enum::toString;
    private static final Function<EnumSet<?>, String> ENUM_SET = enums -> StringUtils.enlistCollection(enums, ",");
    // двоичные данные — в фигурных скобках обозначение
    private static final Function<byte[], String> BYTE_ARRAY = a -> "{binary data " + a.length + " byte(s)}";
	private static final Function<int[], String> INT_ARRAY = a -> "{int array " + NumberUtils.enlistIntValues(a, ",") + '}';
    // время — форматированное, в фигурных скобках
    private static final Function<Date, String> DATE = date -> '{' + SimpleDateFormats.DATE_SPACE_TIME_MS.get().format(date) + '}';
    private static final Function<Duration, String> DURATION = (duration) -> '{' + duration.toString() + '}';
	private static final Function<Instant, String> INSTANT = instnant -> '{' + DateTimeFormatters.DATE_SPACE_TIME_MS.format(instnant) + '}';
    private static final Function<Json, String> JSON = (json) -> '<' + json.toString() + '>';
    private static final Function<Time, String> TIME = time -> '{' + SimpleDateFormats.TIME_MS.get().format(time) + '}';
	private static final Function<TwofacedTime, String> TWOFACED_TIME = twofacedTime -> '{' + twofacedTime.getHumanReadable() + '}';
    // строки — в квадратных скобках, служебные символы замаскированы, но скобки внутри строк — нет, нафиг надо.
    private static final Function<String, String> STRING = s -> '[' + StringUtils.flatten(s) + ']';

    private ContainerToLogString() {
    }

    /**
     * Общий форматирователь строк.
     *
     * @param committedValue исходное значение
     * @param value новое значение
     * @param formatter форматирователь значения
     * @param <T> тип значения
     * @return отформатированное значение для человеческого взора
     */
    private static <T> String print(T committedValue, T value, Function<T, String> formatter) {
        if (Objects.equals(committedValue, value)) {
            return value == null ? "null" : formatter.apply(value);
        } else if (committedValue == null) {
            return '→' + formatter.apply(value);
        } else {
            return formatter.apply(committedValue) + '→' + (value == null ? "null" : formatter.apply(value));
        }
    }

    public static String forBigDecimal(BigDecimal committedValue, BigDecimal value) { return print(committedValue, value, BIG_DECIMAL); }

    public static String forByteArray(byte[] committedValue, byte[] value) { return print(committedValue, value, BYTE_ARRAY); }

    public static String forIntArray(int[] committedValue, int[] value) { return print(committedValue, value, INT_ARRAY); }

    public static String forDate(Date committedValue, Date value) { return print(committedValue, value, DATE); }

    public static String forDuration(Duration committedValue, Duration value) { return print(committedValue, value, DURATION); }

    public static String forEnum(Enum<?> committedValue, Enum<?> value) { return print(committedValue, value, ENUM); }

    public static String forEnumSet(EnumSet<?> committedValue, EnumSet<?> value) { return print(committedValue, value, ENUM_SET); }

	public static String forInstant(Instant committedValue, Instant value) { return print(committedValue, value, INSTANT); }

    public static String forJson(Json committedValue, Json value) { return print(committedValue, value, JSON); }

    public static String forNumber(Number committedValue, Number value) { return print(committedValue, value, NUMBER); }

    public static String forString(String committedValue, String value) { return print(committedValue, value, STRING); }

    public static String forTime(Time committedValue, Time value) { return print(committedValue, value, TIME); }

    public static String forTwofacedTime(TwofacedTime committedValue, TwofacedTime value) { return print(committedValue, value, TWOFACED_TIME); }
}
