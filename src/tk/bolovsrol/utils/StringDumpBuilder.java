package tk.bolovsrol.utils;

/**
 * Строковый буфер для создания дампов.
 * Например, для дампа переменных в {@link Object#toString()}.
 * <p/>
 * Формат дампа следующий:<br>
 * <code>[&lt;<em>caption1</em>&gt;&lt;connector&gt;&lt;<em>value1</em>&gt;[&lt;delimiter&gt;&lt;<em>caption2</em>&gt;&lt;connector&gt;&lt;<em>value2</em>&gt;...]]<br/></code>
 * где &lt;<em>captionN</em>&gt; и &lt;<em>valueN</em>&gt; &mdash; название и значение очередной пары полезной информации,<br/>
 * &lt;connector&gt; &mdash; разделитель caption и соответствующего value, а<br/>
 * &lt;delimiter&gt; &mdash; разделитель отдельных пар caption&value.
 */
public class StringDumpBuilder {

    private final StringBuilder sb = new StringBuilder(256);
    private String delimiter = " ";
    private String connector = "=";

    public StringDumpBuilder() {
    }

    public StringDumpBuilder(String delimiter) {
        this.delimiter = delimiter;
    }

    public StringDumpBuilder(String delimiter, String connector) {
        this.delimiter = delimiter;
        this.connector = connector;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getConnector() {
        return connector;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    public boolean isEmpty() {
        return sb.length() == 0;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    //-- ну, тут дальше пополнятели
    private void put(String caption, String spell) {
        delimit();
        sb.append(caption).append(connector).append(spell);
    }

    public StringDumpBuilder append(String asIs) {
        if (asIs != null) {
            asIs = asIs.trim();
            if (!asIs.isEmpty()) {
                delimit();
                sb.append(asIs);
            }
        }
        return this;
    }

    private void delimit() {
        if (sb.length() > 0) {
            sb.append(delimiter);
        }
    }

    public StringDumpBuilder append(String caption, Object value) {
        if (value != null) {
            put(caption, Spell.get(value));
        }
        return this;
    }

    public StringDumpBuilder append(String caption, byte[] value, int from, int to) {
        if (value != null) {
            put(caption, Spell.get(value, from, to));
        }
        return this;
    }

    public StringDumpBuilder append(String caption, byte[] value, int maxlen) {
        if (value != null) {
            put(caption, Spell.getTruncated(value, maxlen));
        }
        return this;
    }

    public StringDumpBuilder append(String caption, byte[] value, int from, int to, int maxlen) {
        if (value != null) {
            put(caption, Spell.get(value, from, to, maxlen));
        }
        return this;
    }

    public StringDumpBuilder append(String caption, String value) {
        if (value != null) {
            put(caption, (value == null) ? "null" : '[' + value + ']');
        }
        return this;
    }

    public StringDumpBuilder append(String caption, char value) {
        put(caption, Character.toString(value));
        return this;
    }

    public StringDumpBuilder append(String caption, int value) {
        put(caption, Integer.toString(value));
        return this;
    }

    public StringDumpBuilder append(String caption, long value) {
        put(caption, Long.toString(value));
        return this;
    }

    public StringDumpBuilder append(String caption, float value) {
        put(caption, Float.toString(value));
        return this;
    }

    public StringDumpBuilder append(String caption, double value) {
        put(caption, Double.toString(value));
        return this;
    }

    public StringDumpBuilder append(String caption, boolean value) {
        put(caption, Boolean.toString(value));
        return this;
    }

    public StringDumpBuilder appendNonEmpty(String caption, Object[] array) {
        if (array != null && array.length > 0) {
            put(caption, Spell.get(array));
        }
        return this;
    }

    public StringDumpBuilder appendNonEmpty(String caption, byte[] array) {
        if (array != null && array.length > 0) {
            put(caption, Spell.get(array));
        }
        return this;
    }

    public StringDumpBuilder appendNonEmpty(String caption, char[] array) {
        if (array != null && array.length > 0) {
            put(caption, Spell.get(array));
        }
        return this;
    }

    public StringDumpBuilder appendNonEmpty(String caption, int[] array) {
        if (array != null && array.length > 0) {
            put(caption, Spell.get(array));
        }
        return this;
    }

    public StringDumpBuilder appendNonEmpty(String caption, long[] array) {
        if (array != null && array.length > 0) {
            put(caption, Spell.get(array));
        }
        return this;
    }

    public void clear() {
        sb.delete(0, sb.length());
    }
}
