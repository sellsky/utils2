package tk.bolovsrol.utils.time;

import tk.bolovsrol.utils.SimpleDateFormats;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;

import java.text.ParseException;
import java.util.Date;

/** Двуликое поле времени: может обозначать как дату, так и продолжительность. */
public class TwofacedTime {

    private final boolean relative;
    private final long millis;
    //    private static final String YYYY_MM_DD_T_HH_MM_SS_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private String humanReadable;

    public TwofacedTime(boolean relative, long millis) {
        this.relative = relative;
        this.millis = millis;
    }

    public boolean isRelative() {
        return relative;
    }

    /**
     * Возвращает абсолютную дату. Если в объекте хранится абсолютная дата, то её и возвращает,
     * а если продолжительность, то прибавляет её к переданной базовой дате.
     *
     * @param base базовая дата для разрешения относительного времени (продложительности)
     * @return абсолютное время
     */
    public Date toAbsolute(Date base) {
        return new Date(relative ? base.getTime() + millis : millis);
    }

    public long getMillis() {
        return millis;
    }

    //r7.12:13:45.200
    //a2010-06-16T11:24:18.400+0400
    public String getHumanReadable() {
        if (humanReadable == null) {
            if (relative) {
                humanReadable = 'r' + TimeUtils.formatDuration(millis, TimeUtils.ForceFields.SECONDS);
            } else {
                humanReadable = 'a' + SimpleDateFormats.DATE_T_TIME_MS_TZ.get().format(new Date(millis));
            }
        }
        return humanReadable;
    }

    public static TwofacedTime parseHumanReadable(String src) throws IllegalArgumentException {
        if (src == null) {
            return null;
        }
        if (src.length() < 2) {
            throw new IllegalArgumentException("Source too short");
        }
        switch (src.charAt(0)) {
        case 'a':
        case 'A':
            try {
                return new TwofacedTime(false, SimpleDateFormats.DATE_T_TIME_MS_TZ.get().parse(src.substring(1)).getTime());
            } catch (ParseException e) {
                throw new IllegalArgumentException("Unparseable absolute date value " + Spell.get(src.substring(1)), e);
            }

        case 'r':
        case 'R':
            try {
                return new TwofacedTime(true, TimeUtils.parseDuration(src.substring(1)));
            } catch (TimeUtils.DurationParsingException e) {
                throw new IllegalArgumentException("Unparseable relative duration value " + Spell.get(src.substring(1)), e);
            }

        default:
            throw new IllegalArgumentException("Unexpected relativity prefix \'" + src.charAt(0) + '\'');
        }
    }

    @Override public String toString() {
        StringDumpBuilder sb = new StringDumpBuilder()
              .append("relative", relative)
              .append("millis", millis);
//        if (relative) {
//            sb.append("(duration)", TimeUtils.formatDuration(millis, TimeUtils.ForceFields.SECONDS));
//        } else {
//            sb.append("(local)", new Date(millis));
//        }
        return sb.toString();
    }

}
