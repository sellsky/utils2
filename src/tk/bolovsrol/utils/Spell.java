package tk.bolovsrol.utils;

import tk.bolovsrol.utils.time.TimeUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Статический класс для форматирования различных объектов
 * для приведения их к виду, доступном простому,
 * хоть и специально обученному, человечку.
 */
public final class Spell {


    private Spell() {
    }

    /**
     * Возвращает содержимое стринга  в квадратных скобках, а если это null, то null.
     * Для красоты.
     *
     * @param message
     * @return x
     */
    public static String get(String message) {
        return (message == null) ? "null" : '[' + StringUtils.flatten(message) + ']';
    }

    public static String get(StringBuffer message) {
        return get(message.toString());
    }

    @SuppressWarnings("rawtypes")
    public static String get(Enum item) {
        return (item == null) ? "null" : item.toString();
    }

    /**
     * Возвращает содержимое даты в квадратных скобках, а если это null, то null
     *
     * @param date
     * @return x
     */
    public static String get(Date date) {
        return get(date, SimpleDateFormats.DATE_SPACE_TIME_MS.get());
    }

    public static String get(Date date, ThreadLocal<? extends DateFormat> threadLocalDateFormat) {
        return get(date, threadLocalDateFormat.get());
    }

    public static String get(Date date, DateFormat dateFormat) {
        return (date == null) ? "null" : '[' + dateFormat.format(date) + ']';
    }

	private static DateTimeFormatter s_instantFormat = new DateTimeFormatterBuilder()
			.parseCaseInsensitive().append(DateTimeFormatter.ISO_LOCAL_DATE)
			.appendLiteral(' ').append(DateTimeFormatter.ISO_LOCAL_TIME)
			.toFormatter().withZone(ZoneId.systemDefault());

	public static String get(Instant instant) {
		return get(instant, s_instantFormat); }

	public static String get(Instant instant, DateTimeFormatter format) {
		return (instant == null) ? "null" : '[' + format.format(instant) + ']'; }

    public static String getDuration(long millis) {
        return getDuration(millis, TimeUtils.ForceFields.HOURS_MINUTES_SECONDS_MS);
    }

    public static String getDuration(long millis, TimeUtils.ForceFields forceFields) {
        return '[' + TimeUtils.formatDuration(millis, forceFields) + ']';
    }

    public static String get(SimpleDateFormat sdf) {
        return (sdf == null) ? null : '[' + sdf.toPattern() + ']';
    }

    public static String get(Map<?, ?> map) {
        if (map == null) {
            return "null";
        } else if (map.isEmpty()) {
            return "{empty}";
        } else {
            StringBuilder sb = new StringBuilder(64);
            sb.append('{').append(map.size()).append(": ");
            Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
            while (true) {
                Map.Entry<?, ?> entry = it.next();
                sb.append(get(entry.getKey())).append('~').append(get(entry.getValue()));
                if (!it.hasNext()) {
                    break;
                }
                sb.append(' ');
            }
            sb.append('}');
            return sb.toString();
        }
    }

    public static String get(Collection<?> collection) {
        if (collection == null) {
            return "null";
        } else if (collection.isEmpty()) {
            return "{empty}";
        } else {
            StringBuilder sb = new StringBuilder(64);
            sb.append('{').append(collection.size()).append(": ");
            Iterator<?> it = collection.iterator();
            while (true) {
                sb.append(get(it.next()));
                if (!it.hasNext()) {
                    break;
                }
                sb.append(' ');
            }
            sb.append('}');
            return sb.toString();
        }
    }

    public static String get(Object[] objects) {
        if (objects == null) {
            return "null";
        }
        if (objects.length == 0) {
            return "{empty}";
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('{').append(objects.length).append(": ").append(get(objects[0]));
        for (int i = 1; i < objects.length; i++) {
            sb.append(' ');
            sb.append(get(objects[i]));
        }
        sb.append('}');
        return sb.toString();
    }

    public static String get(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        return get(bytes, 0, bytes.length);
    }

    public static String getTruncated(byte[] bytes, int maxlen) {
        if (bytes == null) {
            return "null";
        }
        return get(bytes, 0, bytes.length, maxlen);
    }

    public static String get(byte[] bytes, int from, int len) {
        return get(bytes, from, len, len);
    }

    /**
     * Печатает дамп указанного диапазона байтов, но не более чем truncateAt.
     * <p>
     * Печатает две цифры, разделитель, следующие две цифры и так
     * до полного удовлетворения. Разделитель — три пробела или, каждый четвёртый, — точка.
     * <p>
     * Если len < truncateAt, то будут напечатаны только truncateAt байтов,
     * завершаемые символом «✂» (ножницы).
     *
     * @param bytes
     * @param from
     * @param len
     * @param truncateAt
     * @return
     */
    public static String get(byte[] bytes, int from, int len, int truncateAt) {
        if (bytes == null) {
            return "null";
        }
        if (bytes.length == 0) {
            return "{empty}";
        }
        boolean truncate = truncateAt < len;
        StringBuilder sb = new StringBuilder(64);
        sb.append('{').append(len).append(": [");
        int to;
        if (truncate) {
            sb.append(StringUtils.getAsciiPrintable(bytes, from, truncateAt, '.'));
            sb.append("✂] ");
            to = from + truncateAt;
        } else {
            sb.append(StringUtils.getAsciiPrintable(bytes, from, len, '.'));
            sb.append("] ");
            to = from + len;
        }
        StringUtils.appendDelimitedHexDump(bytes, from, to, sb);
        if (truncate) {
            sb.append(" ✂}");
        } else {
            sb.append('}');
        }

        return sb.toString();
    }

    public static String get(int[] ints) {
        if (ints == null) {
            return "null";
        }
        if (ints.length == 0) {
            return "{empty}";
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('{').append(ints.length).append(": ");
        sb.append(ints[0]).append(' ');
        for (int i = 1; i < ints.length; i++) {
            sb.append(' ');
            sb.append(ints[i]);
        }
        sb.append('}');
        return sb.toString();
    }

    public static String get(char[] chars) {
        if (chars == null) {
            return "null";
        }
        if (chars.length == 0) {
            return "{empty}";
        }
        return "{" + chars.length + ": " + get(new String(chars)) + '}';
    }

    public static String get(long[] longs) {
        if (longs == null) {
            return "null";
        }
        if (longs.length == 0) {
            return "{empty}";
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('{').append(longs.length).append(": ");
        sb.append(longs[0]);
        for (int i = 1; i < longs.length; i++) {
            sb.append(' ');
            sb.append(longs[i]);
        }
        sb.append('}');
        return sb.toString();
    }

    public static String get(float[] floats) {
        if (floats == null) {
            return "null";
        }
        if (floats.length == 0) {
            return "{empty}";
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('{').append(floats.length).append(": ");
        sb.append(floats[0]);
        for (int i = 1; i < floats.length; i++) {
            sb.append(' ');
            sb.append(floats[i]);
        }
        sb.append('}');
        return sb.toString();
    }

    public static String get(Boolean val) {
        return val == null ? "null" : val.toString();
    }

    public static String get(BigDecimal val) {
        return val == null ? "null" : val.toPlainString();
    }

    @SuppressWarnings({"ObjectToString"})
    public static String get(Number val) {
        return val == null ? "null" : val.toString();
    }

    public static String get(Throwable throwable) {
        StringBuilder sb = new StringBuilder(64);
        while (true) {
            sb.append(throwable.toString());
            throwable = throwable.getCause();
            if (throwable == null) {
                return sb.toString();
            }
            sb.append(" -> ");
        }
    }

    public static String get(Object object) {
        if (object == null) {
            return "null";
        } else {
            if (object instanceof int[]) {
                return get((int[]) object);
            } else if (object instanceof long[]) {
                return get((long[]) object);
            } else if (object instanceof char[]) {
                return get((char[]) object);
            } else if (object instanceof byte[]) {
                return get((byte[]) object);
            } else if (object instanceof float[]) {
                return get((float[]) object);
            } else if (object instanceof Boolean) {
                return get((Boolean) object);
            } else if (object instanceof BigDecimal) {
                return get((BigDecimal) object);
            } else if (object instanceof Number) {
                return get((Number) object);
            } else if (object instanceof Object[]) {
                return get((Object[]) object);
            } else if (object instanceof String) {
                return get((String) object);
            } else if (object instanceof StringBuffer) {
                return get((StringBuffer) object);
            } else if (object instanceof Date) {
                return get((Date) object);
			} else if (object instanceof Instant) {
				return get((Instant) object);
            } else if (object instanceof SimpleDateFormat) {
                return get((SimpleDateFormat) object);
            } else if (object instanceof Enum) {
                return get((Enum<?>) object);
            } else if (object instanceof Collection) {
                return get((Collection<?>) object);
            } else if (object instanceof Map) {
                return get((Map<?, ?>) object);
            } else {
                return '{' + object.toString() + '}';
            }
        }
    }

    public static String getHexLong(long number) {
        String num = Long.toHexString(number);
        switch (num.length()) {
        case 1:
            return "0x000000000000000" + num;
        case 2:
            return "0x00000000000000" + num;
        case 3:
            return "0x0000000000000" + num;
        case 4:
            return "0x000000000000" + num;
        case 5:
            return "0x00000000000" + num;
        case 6:
            return "0x0000000000" + num;
        case 7:
            return "0x000000000" + num;
        case 8:
            return "0x00000000" + num;
        case 9:
            return "0x0000000" + num;
        case 10:
            return "0x000000" + num;
        case 11:
            return "0x00000" + num;
        case 12:
            return "0x0000" + num;
        case 13:
            return "0x000" + num;
        case 14:
            return "0x00" + num;
        case 15:
            return "0x0" + num;
        case 16:
            return "0x" + num;
        default:
            return "0x" + num.substring(num.length() - 16);
        }
    }

    public static String getHexInt(int number) {
        String num = Integer.toHexString(number);
        switch (num.length()) {
        case 1:
            return "0x0000000" + num;
        case 2:
            return "0x000000" + num;
        case 3:
            return "0x00000" + num;
        case 4:
            return "0x0000" + num;
        case 5:
            return "0x000" + num;
        case 6:
            return "0x00" + num;
        case 7:
            return "0x0" + num;
        default:
            return "0x" + num;
        }
    }

    public static String getHexWord(int number) {
        String num = Integer.toHexString(number);
        switch (num.length()) {
        case 1:
            return "0x000" + num;
        case 2:
            return "0x00" + num;
        case 3:
            return "0x0" + num;
        case 4:
            return "0x" + num;
        default:
            return "0x" + num.substring(num.length() - 4);
        }
    }

    public static String getHexByte(int number) {
        String num = Integer.toHexString(number);
        switch (num.length()) {
        case 1:
            return "0x0" + num;
        case 2:
            return "0x" + num;
        default:
            return "0x" + num.substring(num.length() - 2);
        }
    }

    public static String getHuman(long number) {
        if (number < 1024L) {
            return String.valueOf(number);
        }
        if (number < 1048576L) {
            return BigDecimal.valueOf(number).divide(new BigDecimal(1024L), 2, RoundingMode.HALF_UP).toPlainString() + 'k';
        }
        if (number < 1073741824L) {
            return BigDecimal.valueOf(number).divide(new BigDecimal(1048576L), 2, RoundingMode.HALF_UP).toPlainString() + 'M';
        }
        if (number < 1099511627776L) {
            return BigDecimal.valueOf(number).divide(new BigDecimal(1073741824L), 2, RoundingMode.HALF_UP).toPlainString() + 'G';
        }
//        if(number<1024*1024 * 1024 * 1024 * 1024){
        return BigDecimal.valueOf(number).divide(new BigDecimal(1099511627776L), 2, RoundingMode.HALF_UP).toPlainString() + 'T';
//        }

    }
}
