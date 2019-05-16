package tk.bolovsrol.utils.time;

import java.util.concurrent.TimeUnit;

/** Продолжительность времени. */
public class Duration implements Comparable<Duration> {

    public static Duration ZERO = new Duration(0L);

    private final long millis;
	private String string;

    /**
     * Создаёт продожительность в миллисекундах.
     *
     * @param millis требуемая продолжительность
     */
    public Duration(long millis) {
        this.millis = millis;
		this.string = null;
	}

    /**
     * Распознаёт продолжительность в формате <code>[[[[d].][hh]:][mm]:][ss][.[z[z[z]]]]</code>.
     *
     * @param string требуемая продолжительность
     * @throws TimeUtils.DurationParsingException
     * @see TimeUtils#parseDuration(String)
     */
    public Duration(String string) throws TimeUtils.DurationParsingException {
        this(TimeUtils.parseDuration(string));
    }

    public long getMillis() {
        return millis;
    }

	public long getNanos() {
		return TimeUnit.MILLISECONDS.toNanos(millis);
	}

	public long getNanos(int nanosPart) {
		return getNanos() + nanosPart;
	}

    public int getMillisPart() {
        return (int) (millis % TimeUtils.MS_IN_SECOND);
    }

    public int getSecondsPart() {
        return (int) ((millis % TimeUtils.MS_IN_MINUTE) / TimeUtils.MS_IN_SECOND);
    }

    public int getMinutesPart() {
        return (int) ((millis % TimeUtils.MS_IN_HOUR) / TimeUtils.MS_IN_MINUTE);
    }

    public int getHoursPart() {
        return (int) ((millis % TimeUtils.MS_IN_DAY) / TimeUtils.MS_IN_HOUR);
    }

    public int getDaysPart() {
        return (int) (millis / TimeUtils.MS_IN_DAY);
    }

    public String getString() {
		if (string == null) {
            string = spellMillis(millis);
        }
        return string;
	}

    public static String spellMillis(long millis) {
        return TimeUtils.formatDuration(millis, TimeUtils.ForceFields.HOURS_MINUTES_SECONDS_MS);
    }


    @Override public String toString() {
		return getString();
	}

    @Override public int hashCode() {
        return (int) (millis ^ (millis >>> 32));
    }

    @Override public boolean equals(Object that) {
        return that instanceof Duration && ((Duration) that).millis == this.millis;
    }

    @Override public int compareTo(Duration that) {
        return this.millis < that.millis ? -1 : this.millis == that.millis ? 0 : 1;
    }

    /**
     * Возвращает Duration, соответствующий содержимому строки;
     * если строка null, то возвращает тоже null.
     *
     * @param durationOrNull
     * @return Duration или null
     * @throws TimeUtils.DurationParsingException
     */
    public static Duration parse(String durationOrNull) throws TimeUtils.DurationParsingException {
        return durationOrNull == null ? null : new Duration(durationOrNull);
    }

    /**
     * Возвращает массив Duration, соответствующий содержимому массива строк;
     * если массив null, то вернётся тоже null;
     * если строка null, то соответствующий Duration тоже будет null.
     *
     * @param durations
     * @return массив Duration или null
     * @throws TimeUtils.DurationParsingException
     */
    public static Duration[] parse(String[] durations) throws TimeUtils.DurationParsingException {
        if (durations == null) {
            return null;
        }
        Duration[] result = new Duration[durations.length];
        for (int i = 0, durationsLength = durations.length; i < durationsLength; i++) {
            result[i] = parse(durations[i]);
        }
        return result;
    }
}
