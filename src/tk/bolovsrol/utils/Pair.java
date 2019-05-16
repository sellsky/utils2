package tk.bolovsrol.utils;

/**
 * Пара значений.
 * <p>
 * Если вдруг методу надо вернуть два значения, то можно вернуть этот объект.
 *
 * @param <A>
 * @param <B>
 */
public class Pair<A, B> {
    public A a;
    public B b;

    public Pair(A a, B b) {
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

    @Override public String toString() {
        return new StringDumpBuilder()
            .append("a", a)
            .append("b", b)
            .toString();
    }
}
