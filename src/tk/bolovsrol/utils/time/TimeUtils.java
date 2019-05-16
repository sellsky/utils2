package tk.bolovsrol.utils.time;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.UnexpectedBehaviourException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalQuery;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Время -- деньги!
 */
public final class TimeUtils {

    private TimeUtils() {
    }

    public static final long MS_IN_SECOND = 1000L;
    public static final long MS_IN_MINUTE = MS_IN_SECOND * 60L;
    public static final long MS_IN_HOUR = MS_IN_MINUTE * 60L;
    public static final long MS_IN_DAY = MS_IN_HOUR * 24L;

    public static final int SECONDS_IN_MINUTE = 60;
    public static final int SECONDS_IN_HOUR = SECONDS_IN_MINUTE * 60;
    public static final int SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;

    public static final int MINUTES_IN_HOUR = 60;
    public static final int MINUTES_IN_DAY = MINUTES_IN_HOUR * 24;

    /**
     * Вычисляет 00:00:00 первого дня месяца, отстоящего относительно месяца указанной даты на shift месяцев.
     * <p>
     * Так, shift = 0 даст начало текущего месяца, 1 — следующего, -1 — предыдущего и т.д.
     *
     * @param date
     * @return первый день месяца, 00:00:00.
     */
    public static Date getStartOfMonth(Date date, int shift) {
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.MONTH, shift);
        return c.getTime();
    }


    /**
     * Получает 00:00:00 последнего дня месяца, отстоящего относительно месяца указанной даты на shift месяцев.
     * <p>
     * Так, shift = 0 даст конец текущего месяца, 1 — следующего, -1 — предыдущего и т.д.
     *
     * @param date
     * @return последний день месяца, 00:00:00.
     */
    public static Date getEndOfMonth(Date date, int shift) {
        Calendar c = new GregorianCalendar();
        c.setTime(date);

        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.MONTH, shift);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        return c.getTime();
    }


    /**
     * Получает начало текущего дня, когда время 00:00:00.000.
     *
     * @param date
     * @return начало дня, 00:00:00.
     * @see #getStartOfDay(Date, int)
     */
    public static Date getStartOfDay(Date date) {
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        resetHours(c);
        return c.getTime();
    }

    /**
     * Получает начало дня, отстоящего относительно указанной даты на shift суток.
     *
     * @param date
     * @return начало дня, 00:00:00.
     * @see #getStartOfDay(Date)
     */
    public static Date getStartOfDay(Date date, int shift) {
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        resetHours(c);
        c.add(Calendar.DATE, shift);
        return c.getTime();
    }

    public static void resetHours(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        resetMinutes(c);
    }

    public static Date getStartOfHour(Date date) {
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        resetMinutes(c);
        return c.getTime();
    }

    public static void resetMinutes(Calendar c) {
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    public static Date resetFromDay(Calendar c) {
        return new Date(
                (long) ((((c.get(Calendar.HOUR_OF_DAY) * 60 +
                        c.get(Calendar.MINUTE)) * 60) +
                        c.get(Calendar.SECOND)) * 1000 + c.get(Calendar.MILLISECOND)));
    }

    /**
     * Возвращает количество миллисекунд, прошедших с начала указанной даты.
     * <p/>
     * Это число равно или больше 0 и меньше {@link #MS_IN_DAY}.
     *
     * @param date
     * @return миллисекунды.
     */
    public static long getMillisSinceDayStart(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return (long) (((cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) * 60 + cal.get(Calendar.SECOND)) * 1000 + cal.get(Calendar.MILLISECOND));
    }

    /**
     * Возвращает наименьшую дату после указанного момента и соответствующую одному из переданных интервалов,
     * отсчитываемых с полуночи. Время локальное.
     * <p/>
     * Интервалы рассматриваются как совокупность миллисекунд, секунд, минут и часов.
     * Это даёт возможность пользоваться указанными временами и в дни перевода часов.
     * Количество суток игнорируется.
     *
     * @param after
     * @param timesOfDay
     * @return ближайшая дата
     * @throws InterruptedException
     */
    public static Date getClosestDate(Date after, Duration... timesOfDay) throws InterruptedException {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(TimeUtils.getStartOfDay(after));
        Date wakeDate = null;
        while (true) {
            for (Duration duration : timesOfDay) {
                cal.set(Calendar.MILLISECOND, duration.getMillisPart());
                cal.set(Calendar.SECOND, duration.getSecondsPart());
                cal.set(Calendar.MINUTE, duration.getMinutesPart());
                cal.set(Calendar.HOUR_OF_DAY, duration.getHoursPart());
                Date probablyWakeDate = cal.getTime();
                if (probablyWakeDate.after(after) && (wakeDate == null || wakeDate.after(probablyWakeDate))) {
                    wakeDate = probablyWakeDate;
                }
            }
            if (wakeDate != null) {
                return wakeDate;
            }
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.add(Calendar.DATE, 1);
        }

    }

    /**
     * Ошибка парсинга продложительности из строки.
     *
     * @see TimeUtils#parseDuration(String)
     */
    public static class DurationParsingException extends UnexpectedBehaviourException {
        public DurationParsingException(String s) {
            super(s);
        }

        public DurationParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Распознаёт в строке в формате <code>[[[[d].][hh]:][mm]:][ss][.[z[z[z]]]]</code>
     * продолжительность в миллисекундах.
     * <p/>
     * Миллисекунды должны быть заданы не более чем тремя цифрами,
     * остальные значения допускают куда больше свободы. Можно указать время типа 65:00,
     * и это будет распознано как 1:05:00.
     *
     * @param durationString
     * @return продолжительность в миллисекундах
     * @throws DurationParsingException ошибка при парсинге
     * @see #formatDuration(long, ForceFields)
     */
    public static long parseDuration(String durationString) throws DurationParsingException {
        long d = 0L;
        int secondsStart = durationString.lastIndexOf(':') + 1;
        int secondsEnd = durationString.indexOf('.', secondsStart);
        if (secondsEnd < 0) {
            secondsEnd = durationString.length();
        } else {
            // ms
            int msStart = secondsEnd + 1;
            int msLen = durationString.length() - msStart;
            try {
                switch (msLen) {
                    case 0:
                        break;
                    case 1:
                        d = (long) Integer.parseInt(durationString.substring(msStart) + "00");
                        break;
                    case 2:
                        d = (long) Integer.parseInt(durationString.substring(msStart) + '0');
                        break;
                    case 3:
                        d = (long) Integer.parseInt(durationString.substring(msStart));
                        break;
                    default:
                        throw new DurationParsingException("Milliseconds part too precise (up to 3 digits allowed) " + Spell.get(durationString.substring(msStart)));
                }
            } catch (NumberFormatException e) {
                throw new DurationParsingException("Error parsing milliseconds value", e);
            }
        }
        if (secondsStart != secondsEnd) {
            try {
                d += (long) Integer.parseInt(durationString.substring(secondsStart, secondsEnd)) * MS_IN_SECOND;
            } catch (NumberFormatException e) {
                throw new DurationParsingException("Error parsing seconds value", e);
            }
        }
        if (secondsStart == 0) {
            return d;
        }
        int minutesEnd = secondsStart - 1;
        int minutesStart = durationString.lastIndexOf(':', minutesEnd - 1) + 1;
        if (minutesStart != minutesEnd) {
            try {
                d += (long) Integer.parseInt(durationString.substring(minutesStart, minutesEnd)) * MS_IN_MINUTE;
            } catch (NumberFormatException e) {
                throw new DurationParsingException("Error parsing minutes value", e);
            }
        }
        if (minutesStart == 0) {
            return d;
        }
        int hoursEnd = minutesStart - 1;
        int hoursStart = durationString.lastIndexOf('.', hoursEnd - 1) + 1;
        if (hoursStart != hoursEnd) {
            try {
                d += (long) Integer.parseInt(durationString.substring(hoursStart, hoursEnd)) * MS_IN_HOUR;
            } catch (NumberFormatException e) {
                throw new DurationParsingException("Error parsing hours value", e);
            }
        }
        if (hoursStart <= 1) {
            return d;
        }
        int daysEnd = hoursStart - 1;
        try {
            return d + (long) Integer.parseInt(durationString.substring(0, daysEnd)) * MS_IN_DAY;
        } catch (NumberFormatException e) {
            throw new DurationParsingException("Error parsing days value", e);
        }
    }

    /**
     * Описывает, какие поля должен обазательно отобразить
     * метод {@link TimeUtils#formatDuration(long, ForceFields)}
     */
    public enum ForceFields {
        DAYS_HOURS_MINUTES_SECONDS_MS(true, true, true, true, true),
        DAYS_HOURS_MINUTES_SECONDS(true, true, true, true, false),
        HOURS_MINUTES_SECONDS_MS(false, true, true, true, true),
        HOURS_MINUTES_SECONDS(false, true, true, true, false),
        MINUTES_SECONDS_MS(false, false, true, true, true),
        MINUTES_SECONDS(false, false, true, true, false),
        SECONDS_MS(false, false, false, true, true),
        SECONDS(false, false, false, true, false),
        MS(false, false, false, false, true),
        NOTHING(false, false, false, false, false);

        final boolean days;
        final boolean hours;
        final boolean minutes;
        final boolean seconds;
        final boolean ms;

        private static final TreeMap<String, ForceFields> BY_SHORTCUT = new TreeMap<String, ForceFields>();

        static {
            BY_SHORTCUT.put("dhmsz", DAYS_HOURS_MINUTES_SECONDS_MS);
            BY_SHORTCUT.put("dhms", DAYS_HOURS_MINUTES_SECONDS);
            BY_SHORTCUT.put("hmsz", HOURS_MINUTES_SECONDS_MS);
            BY_SHORTCUT.put("hms", HOURS_MINUTES_SECONDS);
            BY_SHORTCUT.put("msz", MINUTES_SECONDS_MS);
            BY_SHORTCUT.put("ms", MINUTES_SECONDS);
            BY_SHORTCUT.put("sz", SECONDS_MS);
            BY_SHORTCUT.put("s", SECONDS);
            BY_SHORTCUT.put("z", MS);
        }

        ForceFields(boolean days, boolean hours, boolean minutes, boolean seconds, boolean ms) {
            this.days = days;
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
            this.ms = ms;
        }

        /**
         * Используется для сопоставления короткого ключа,
         * обозначающего один из режимов вывода. Предполагается задание
         * такого ключа пользователем, например, в конфигурационном файле.
         * <p/>
         * Общий формат вывода «[[[[д.]чч:]мм:]сс][.µµµ]», квадратные скобки обозначают
         * необязательные поля. Так, если значения всех полей в квадратных скобках 00,
         * такие поля не будут отображаться.
         * <p/>
         * Параметром forceFields можно указать, какие поля нужно отображать
         * даже если их значение ноль:
         * <dl>
         * <dt><code>dhmsz</code></dt><dd>«д.чч:мм:сс.µµµ»;</dd>
         * <dt><code>dhms</code></dt><dd>«д.чч:мм:сс»;</dd>
         * <dt><code>hmsz</code></dt><dd>«чч:мм:сс.µµµ»;</dd>
         * <dt><code>hms</code></dt><dd>«чч:мм:сс»;</dd>
         * <dt><code>msz</code></dt><dd>«мм:сс.µµµ»;</dd>
         * <dt><code>ms</code></dt><dd>«мм:сс»;</dd>
         * <dt><code>sz</code></dt><dd>«сс.µµµ»;</dd>
         * <dt><code>s</code></dt><dd>«сс»;</dd>
         * <dt><code>z</code></dt><dd>«.µµµ».</dd>
         * </dl>
         * <p/>
         * Если указана любая иная последовательность, обязательных полей нет.
         *
         * @param shortcut
         * @return
         */
        public static ForceFields pickByShortcut(String shortcut) {
            ForceFields value = BY_SHORTCUT.get(shortcut);
            return value == null ? NOTHING : value;
        }

    }

    /**
     * Формирует из переданной продолжительности время в формате <code>[[[[d.]hh:]mm:]ss][.zzz]</code>.
     * <p/>
     * В маске нужно указать, какие поля следует обязательно отображать.
     * Если значение поля 0, значение зависимых полей тоже 0 и обязательного отображения не требуется,
     * поле в результате не появится.
     *
     * @param duration    продолжительность
     * @param forceFields указатель полей, которые нужно обязательно отобразить
     * @return результат в виде текстовой строки
     * @see ForceFields
     * @see #parseDuration(String)
     */
    public static String formatDuration(long duration, ForceFields forceFields) {
        StringBuilder sb = new StringBuilder(16);
        long millis = duration % MS_IN_SECOND;
        long seconds = duration % MS_IN_MINUTE / MS_IN_SECOND;
        long minutes = duration % MS_IN_HOUR / MS_IN_MINUTE;
        long hours = duration % MS_IN_DAY / MS_IN_HOUR;
        long days = duration / MS_IN_DAY;
        boolean force = false;
        if (forceFields.days || days != 0L) {
            sb.append(days).append('.');
            force = true;
        }
        if (force || forceFields.hours || hours != 0L) {
            if (hours < 10L) {
                sb.append('0');
            }
            sb.append(hours).append(':');
            force = true;
        }
        if (force || forceFields.minutes || minutes != 0L) {
            if (minutes < 10L) {
                sb.append('0');
            }
            sb.append(minutes).append(':');
            force = true;
        }
        if (force || forceFields.seconds || seconds != 0L) {
            if (seconds < 10L) {
                sb.append('0');
            }
            sb.append(seconds);
        }
        if (forceFields.ms || millis != 0L) {
            sb.append('.');
            if (millis < 100L) {
                sb.append('0');
                if (millis < 10L) {
                    sb.append('0');
                }
            }
            sb.append(millis);
        }
        return sb.toString();
    }

    /**
     * Возвращает время, происходящее в другой временной зоне.
     *
     * @param sourceDate исходное время
     * @param sourceZone исходная зона
     * @param destZone   интересующая зона
     * @return интересующее время
     */
    public static Date changeTimeZone(Date sourceDate, TimeZone sourceZone, TimeZone destZone) {
        if (sourceDate == null) {
            return null;
        }
        if (sourceZone.equals(destZone)) {
            return sourceDate;
        }
        int diff = destZone.getOffset(sourceDate.getTime()) - sourceZone.getOffset(sourceDate.getTime());
        if (diff == 0) {
            return sourceDate;
        }
        return new Date(sourceDate.getTime() + (long) diff);
    }

    /**
     * Возвращает наименьшую из переданных дат.
     * Даты-нулы игнорируются.
     * <p/>
     * Если передан нул или все даты нулы, то вернётся тоже нул.
     *
     * @param dates
     * @return наименьшая дата
     */
    public static Date min(Date... dates) {
        if (dates == null) {
            return null;
        }
        Date result = null;
        for (Date date : dates) {
            if (result == null || (date != null && date.before(result))) {
                result = date;
            }
        }
        return result;
    }

    /**
     * Возвращает наибольшую из переданных дат.
     * Даты-нулы игнорируются.
     * <p/>
     * Если передан нул или все даты нулы, то вернётся тоже нул.
     *
     * @param dates
     * @return наибольшая дата
     */
    public static Date max(Date... dates) {
        if (dates == null) {
            return null;
        }
        Date result = null;
        for (Date date : dates) {
            if (result == null || (date != null && date.after(result))) {
                result = date;
            }
        }
        return result;
    }

    /**
     * Пытается распознать строку, в которой записана дата в формате ИСО-8601.
     * Если переданный параметр нул, вернёт нул.
     *
     * @param probablyIso
     * @return соответствующая дата
     */
    public static Date parseIsoDate(String probablyIso) {
        return parseIso(probablyIso, temporal -> new Date(temporal.getLong(ChronoField.INSTANT_SECONDS) * 1000L + temporal.getLong(ChronoField.MILLI_OF_SECOND)));
    }

    /**
     * Пытается распознать строку, в которой записана дата в формате ИСО-8601.
     * Если переданный параметр нул, вернёт нул.
     *
     * @param probablyIso
     * @return соответствующий инстант
     */
    public static Instant parseIsoInstant(String probablyIso) {
        return parseIso(probablyIso, Instant::from);
    }

    /**
     * Пытается распознать строку, в которой записана дата в формате ИСО-8601.
     * Если переданный параметр нул, вернёт нул.
     *
     * @param probablyIso
     * @param temporalQuery
     * @return соответствующий результат
     */
    public static <T> T parseIso(String probablyIso, TemporalQuery<T> temporalQuery) {
        if (probablyIso == null) {
            return null;
        }
        DateTimeFormatter formatter;

        int len = probablyIso.length();
        if (len > 0 && probablyIso.charAt(len - 1) == 'Z') {
            formatter = DateTimeFormatter.ISO_INSTANT;

        } else {
            formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

            // ...±0100 → ...±01:00
            if (len > 5) {
                char len5char = probablyIso.charAt(len - 5);
                if ((len5char == '+' || len5char == '-') &&
                        Character.isDigit(probablyIso.charAt(len - 1)) &&
                        Character.isDigit(probablyIso.charAt(len - 2)) &&
                        Character.isDigit(probablyIso.charAt(len - 3)) &&
                        Character.isDigit(probablyIso.charAt(len - 4))) {
                    probablyIso = probablyIso.substring(0, len - 2) + ":" + probablyIso.substring(len - 2);
                }
            }

            // ...±01 → ...±01:00
            if (len > 3) {
                char len3char = probablyIso.charAt(len - 3);
                if ((len3char == '+' || len3char == '-') &&
                        Character.isDigit(probablyIso.charAt(len - 2)) &&
                        Character.isDigit(probablyIso.charAt(len - 1))) {
                    probablyIso += ":00";
                }
            }
        }

        return formatter.parse(probablyIso, temporalQuery);
    }

    /**
     * Печатает дату-время с таймзоной Z (по Гринвичу) в формате ИСО-8601.
     * Если переданный параметр нул, вернёт нул.
     *
     * @param date
     * @return
     */
    public static String printIso(Date date) {
        if (date == null) {
            return null;
        }
        return DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
    }

    /**
     * Печатает дату-время с таймзоной Z (по Гринвичу) в формате ИСО-8601.
     * Если переданный параметр нул, вернёт нул.
     *
     * @param instant
     * @return
     */
    public static String printIso(Instant instant) {
        if (instant == null) {
            return null;
        }
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}