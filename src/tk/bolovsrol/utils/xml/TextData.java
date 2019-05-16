package tk.bolovsrol.utils.xml;

import tk.bolovsrol.utils.Spell;

/**
 * Элемент типа простых символьных данных.
 * Без имени, без атрибутов и без детей.
 * <p/>
 * Зато в конструкторе надо указывать егойное значение.
 * <p/>
 * В принципе, можно и к нему приделать много детей, но они нигде видны не будут.
 */
public class TextData extends Element {
    private final String value;
    private final Type type;

    public enum Type {
        TEXT,
        CDATA
    }

    protected TextData(String value, Type type) {
        super(null);
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public Element copy() {
        return new TextData(value, type);
    }

    @Override
    public String toString() {
        return type.toString() + ' ' + Spell.get(value);
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof TextData && this.equals((TextData) that);
    }

    @Override
    public boolean equals(Element that) {
        return that instanceof TextData && this.equals((TextData) that);
    }

    @SuppressWarnings({"StringEquality"})
    public boolean equals(TextData that) {
        return this == that || this.value == that.value || this.value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
}