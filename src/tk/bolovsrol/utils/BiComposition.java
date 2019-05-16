package tk.bolovsrol.utils;

import java.util.Map;

/**
 * Композиция двух объектов.
 * <p>
 * Удобно использовать в качестве составного ключика для {@link Map}.
 * <p>
 * Композиция будет {@link Comparable}, если оба элемента Comparable,
 * иначе при попытке сравнения вывалится {@link ClassCastException}.
 * Сравнение считает, что null-элементы меньше любых значений,
 * а также считает, что соответствующие null-элементы равны.
 */
public class BiComposition<A, B> implements Comparable<BiComposition> {

    protected A a;
    protected B b;

    public BiComposition(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public void setA(A a) {
        this.a = a;
    }

    public void setB(B b) {
        this.b = b;
    }

    @Override
    public int hashCode() {
        int result = 31;
        if (a != null) {
            result += a.hashCode();
        }
        result = (result << 5) - result;
        if (b != null) {
            result += b.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof BiComposition && equals((BiComposition) obj));
    }

    public boolean equals(BiComposition that) {
        if (this.a == that.a && this.b == that.b) {
            return true;
        }

        if (this.a == null) {
            if (that.a != null) {
                return false;
            }
        } else if (!this.a.equals(that.a)) {
            return false;
        }

        if (this.b == null) {
            return that.b == null;
        } else {
            return this.b.equals(that.b);
        }
    }

    @Override
    public String toString() {
        return "a=" + Spell.get(a) + " b=" + Spell.get(b);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public int compareTo(BiComposition that) {
        if (that == this) {
            return 0;
        }

        if (this.a == null) {
            if (that.a != null) {
                return -1;
            }
        } else if (that.a == null) {
            return 1;
        } else {
            int itemCompared = ((Comparable<Object>) this.a).compareTo(that.a);
            if (itemCompared != 0) {
                return itemCompared;
            }
        }

        if (this.b == null) {
            if (that.b != null) {
                return -1;
            } else {
                return 0;
            }
        } else if (that.b == null) {
            return 1;
        } else {
            return ((Comparable<Object>) this.b).compareTo(that.b);
        }
    }
}
