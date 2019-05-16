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
import java.util.function.Function;

/**
 * Здесь в одном месте собраны правила форматирования значений контейнеров для вывода их
 * в SQL-выражениях, используемых для отладочного SQL-лога. Они по возможности выглядят так,
 * чтобы эти выражения можно было скопировать в консоль и запустить.
 * Строковые значения обрамляются одинарными кавычечками, циферки постятся как есть.
 */
public final class ContainerToSqlLogString {
    public static final Function<BigDecimal, String> BIG_DECIMAL = BigDecimal::toPlainString;
    public static final Function<byte[], String> BYTE_ARRAY = StringUtils::getHexDump;
    public static final Function<int[], String> INT_ARRAY = a -> '\'' + NumberUtils.enlistIntValues(a, ",") + '\'';
    public static final Function<Date, String> DATE = date -> '\'' + SimpleDateFormats.DATE_SPACE_TIME_MS.get().format(date) + '\'';
    public static final Function<Duration, String> DURATION = duration -> '\'' + duration.toString() + '\'';
    public static final Function<Enum<?>, String> ENUM = e -> '\'' + e.toString() + '\'';
    public static final Function<EnumSet<?>, String> ENUM_SET = enums -> '\'' + StringUtils.enlistCollection(enums, ",") + '\'';
    public static final Function<Instant, String> INSTANT = instant -> '\'' + DateTimeFormatters.DATE_SPACE_TIME_MS.format(instant) + '\'';
    public static final Function<Json, String> JSON = (json) -> '\'' + json.toString() + '\'';
    public static final Function<Number, String> NUMBER = Object::toString;
    public static final Function<String, String> STRING = s -> '\'' + maskSpecialChars(s) + '\'';
    public static final Function<Time, String> TIME = time -> '\'' + SimpleDateFormats.TIME_MS.get().format(time) + '\'';
    private static final Function<TwofacedTime, String> TWOFACED_TIME = twofacedTime -> '\'' + twofacedTime.getHumanReadable() + '\'';

    private ContainerToSqlLogString() {
    }

    /**
     * Вписывает обратный слэш перед символом с кодом 0, одинарной кавычкой, бэкслешом и всякими спецсимволами, рвущими строку.
     * <p>
     * https://dev.mysql.com/doc/refman/5.0/en/string-literals.html#character-escape-sequences
     *
     * @param str исходная строка
     * @return плоская маскированная строка
     */
    private static String maskSpecialChars(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            switch (ch) {
            case '\'':
            case '\\':
            case '\n':
            case '\r':
            case '\t':
            case '\0':
            case '\b':
                return flattenHeavy(str, i, length);
            }
        }
        return str;
    }

    /**
     * Сюда попадают строки, в которых точно есть символы для замены.
     * Причём нам уже показывают на символ с наибольшим индексом.
     *
     * @param str
     * @return
     */
    private static String flattenHeavy(String str, int i, int length) {
        StringBuilder sb = new StringBuilder(str.length() * 2).append(str, 0, i);
        while (i < length) {
            char ch = str.charAt(i);
            switch (ch) {
            case '\'':
            case '\\':
                sb.append('\\').append(ch);
                break;
            case '\n':
                sb.append('\\').append('n');
                break;
            case '\r':
                sb.append('\\').append('r');
                break;
            case '\t':
                sb.append('\\').append('t');
                break;
            case '\0':
                sb.append('\\').append('0');
                break;
            case '\b':
                sb.append('\\').append('b');
                break;
            default:
                sb.append(ch);
            }
            i++;
        }
        return sb.toString();
    }

    /**
     * Общий форматирователь строк.
     * <p>
     * Отдаёт «null» или отформатированное переданным форматирователем значение.
     *
     * @param value значение
     * @param formatter форматирователь значения
     * @param <T> тип значения
     * @return отформатированное значение для человеческого взора
     */
    public static <T> String print(T value, Function<T, String> formatter) {
        return value == null ? "null" : formatter.apply(value);
    }

    public static String forBigDecimal(BigDecimal value) { return print(value, BIG_DECIMAL); }

    public static String forByteArray(byte[] value) { return print(value, BYTE_ARRAY); }

    public static String forIntArray(int[] value) { return print(value, INT_ARRAY); }

    public static String forDate(Date value) { return print(value, DATE); }

    public static String forDuration(Duration value) { return print(value, DURATION); }

    public static String forEnum(Enum<?> value) { return print(value, ENUM); }

    public static String forEnumSet(EnumSet<?> value) { return print(value, ENUM_SET); }

    public static String forInstant(Instant value) { return print(value, INSTANT);}

    public static String forJson(Json value) { return print(value, JSON);}

    public static String forNumber(Number value) { return print(value, NUMBER); }

    public static String forString(String value) { return print(value, STRING); }

    public static String forTime(Time value) { return print(value, TIME); }

    public static String forTwofacedTime(TwofacedTime value) { return print(value, TWOFACED_TIME); }
}
