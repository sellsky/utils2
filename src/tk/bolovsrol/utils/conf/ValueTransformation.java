package tk.bolovsrol.utils.conf;

import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TimeUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

/**
 * Костыль для определения форматирования классов,
 * которые сами дают не подходящий toString() и перегрузить которые нельзя.
 */
public enum ValueTransformation {
    /** Ничего не преобразует. */
    NONE {
        @Override public String transformConf(String value) {
            return value;
        }

        @Override public String transformPrintable(Object value) {
            return value == null ? null : value.toString();
        }
    },

    /**
     * Вместо значков перевода строки и табуляции ставит соответствующие символы.
     *
     * @see StringUtils#deflatten(String)
     */
    DEFLATTEN {
        @Override public String transformConf(String value) {
            return StringUtils.deflatten(value);
        }

        @Override public String transformPrintable(Object value) {
            return StringUtils.flatten(value.toString());
        }
    },

    /**
     * Входящей трансформации нет, исходящая — преобразуем строковое представление в нижний регистр.
     */
    TOLOWER {
        @Override public String transformConf(String value) {
            return value;
        }

        @Override public String transformPrintable(Object value) {
            return value.toString().toLowerCase();
        }
    },

    /**
     * Входящей трансформации нет, исходящая — преобразуем строковое представление в верхний регистр.
     */
    TOUPPER {
        @Override public String transformConf(String value) {
            return value;
        }

        @Override public String transformPrintable(Object value) {
            return value.toString().toUpperCase();
        }
    },


    /**
     * Для печати возвращает паттерн класса {@link java.text.SimpleDateFormat}.
     * <p>
     * При использовании с другими классами поведение не определено.
     * @deprecated не нужно, автоконф делает это сам
     */
    @Deprecated
    SIMPLE_DATE_FORMAT {
        @Override public String transformConf(String value) {
            return value;
        }

        @Override public String transformPrintable(Object value) {
            return ((SimpleDateFormat) value).toPattern();
        }
    },

    /**
     * Преобразует строку вида "hh:mm:ss.xxx" в миллисекунды.
     *
     * @deprecated следует использовать поле типа {@link Duration}.
     */
    @Deprecated
    DURATION {
        @Override public String transformConf(String value) throws ValueTransformationFailedException {
            try {
                return value == null ? null : String.valueOf(TimeUtils.parseDuration(value));
            } catch (TimeUtils.DurationParsingException e) {
                throw new ValueTransformationFailedException(e);
            }
        }

        @Override public String transformPrintable(Object value) {
            return value instanceof Long ?
                  TimeUtils.formatDuration((Long) value, TimeUtils.ForceFields.HOURS_MINUTES_SECONDS_MS)
                  : value.toString();
        }
    },

    /**
     * Для печати возвращает {@link java.math.BigDecimal#toPlainString()}.
     *
     * @deprecated не нужно, автоконф делает это сам
     */
    @Deprecated
    BIG_DECIMAL {
        @Override public String transformConf(String value) {
            return value;
        }

        @Override public String transformPrintable(Object value) {
            return value instanceof BigDecimal ? ((BigDecimal) value).toPlainString() : value.toString();
        }
    };

    public abstract String transformConf(String value) throws ValueTransformationFailedException;

    public abstract String transformPrintable(Object value);


}
