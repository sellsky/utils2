package tk.bolovsrol.utils.properties;

import tk.bolovsrol.utils.properties.sources.MapPlainSource;
import tk.bolovsrol.utils.properties.sources.PlainSource;
import tk.bolovsrol.utils.properties.sources.ReadOnlySource;
import tk.bolovsrol.utils.time.Duration;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Небольшой враппер над пропертями для пущей халявности.
 * <p>
 * Реально этот класс занимается преобразованием передаваемых значений и трансляцией их
 * в передаваемый {@link PlainSource}, который уже занимается вопросом их физического размещения.
 */
public class PlainProperties extends ReadOnlyProperties implements PlainSource {

    protected final PlainSource ps;

    public PlainProperties(PlainSource ps) {
        super(ps);
        this.ps = ps;
    }

    public PlainProperties() {
        this(new MapPlainSource());
    }

    public PlainProperties(Map<String, String> pr) {
        this(new MapPlainSource(pr));
    }

    public PlainSource getPlainSource() {
        return ps;
    }

    public PlainProperties join(String prefix, ReadOnlySource toJoin) {
        toJoin.dump().forEach((key, value) -> set(prefix + key, value));
        return this;
    }

    public PlainProperties join(ReadOnlySource toJoin) {
        ps.setAll(toJoin.dump());
        return this;
    }

    @Override public PlainProperties clear() {
        ps.clear();
        return this;
    }

    @Override public PlainProperties drop(String key) {
        ps.drop(key);
        return this;
    }

    // set -----
    @Override public PlainProperties set(String key, String value) {
        if (value == null) {
            ps.drop(key);
        } else {
            ps.set(key, value);
        }
        return this;
    }

    public PlainProperties set(String key, int value) {
        ps.set(key, String.valueOf(value));
        return this;
    }

    public PlainProperties set(String key, long value) {
        ps.set(key, String.valueOf(value));
        return this;
    }

    public PlainProperties set(String key, float value) {
        ps.set(key, String.valueOf(value));
        return this;
    }

    public PlainProperties set(String key, BigDecimal value) {
        if (value == null) {
            ps.drop(key);
        } else {
            ps.set(key, value.toPlainString());
        }
        return this;
    }

    public PlainProperties set(String key, Number value) {
        if (value == null) {
            ps.drop(key);
        } else {
            //noinspection ObjectToString
            ps.set(key, value.toString());
        }
        return this;
    }

    public PlainProperties set(String key, boolean value) {
        ps.set(key, value ? "true" : "false");
        return this;
    }

    public PlainProperties set(String key, Date value, DateFormat format) {
        if (value == null) {
            ps.drop(key);
        } else {
            ps.set(key, format.format(value));
        }
        return this;
    }

    public PlainProperties set(String key, Duration value) {
        if (value == null) {
            ps.drop(key);
        } else {
            ps.set(key, value.getString());
        }
        return this;
    }

    public PlainProperties setAll(ReadOnlySource source) {
        ps.setAll(source.dump());
        return this;
    }

    @Override public PlainProperties setAll(Map<String, String> dump) {
        ps.setAll(dump);
        return this;
    }

    // merge -----
    public PlainProperties set(String key, long value, BiFunction<String, String, String> mergeFunction) {
        ps.merge(key, String.valueOf(value), mergeFunction);
        return this;
    }

    public PlainProperties set(String key, float value, BiFunction<String, String, String> mergeFunction) {
        ps.merge(key, String.valueOf(value), mergeFunction);
        return this;
    }

    public PlainProperties set(String key, BigDecimal value, BiFunction<String, String, String> mergeFunction) {
        if (value == null) {
            ps.drop(key);
        } else {
            ps.merge(key, value.toPlainString(), mergeFunction);
        }
        return this;
    }

    public PlainProperties set(String key, Number value, BiFunction<String, String, String> mergeFunction) {
        if (value == null) {
            ps.drop(key);
        } else {
            //noinspection ObjectToString
            ps.merge(key, value.toString(), mergeFunction);
        }
        return this;
    }

    public PlainProperties set(String key, boolean value, BiFunction<String, String, String> mergeFunction) {
        ps.merge(key, value ? "true" : "false", mergeFunction);
        return this;
    }

    public PlainProperties set(String key, Date value, DateFormat format, BiFunction<String, String, String> mergeFunction) {
        if (value == null) {
            ps.drop(key);
        } else {
            ps.merge(key, format.format(value), mergeFunction);
        }
        return this;
    }

    public PlainProperties set(String key, Duration value, BiFunction<String, String, String> mergeFunction) {
        if (value == null) {
            ps.drop(key);
        } else {
            ps.merge(key, value.getString(), mergeFunction);
        }
        return this;
    }

    public PlainProperties mergeAll(ReadOnlySource source, BiFunction<String, String, String> mergeFunction) {
        ps.mergeAll(source.dump(), mergeFunction);
        return this;
    }

}
