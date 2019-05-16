package tk.bolovsrol.utils.textformatter.compiling.sections;

/** Просто строка текста.. */
public class ConstSection implements Section {

    public static final ConstSection EMPTY_CONST_SECTION = new ConstSection("");
    private final String value;

    public ConstSection(String value) {
        this.value = value;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public String evaluate(boolean strict) {
        return value;
    }

    @Override
    public void reset() {
        // nothing to do
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof ConstSection && this.value.equals(((ConstSection) that).value);
    }

    @Override public String toString() {
        return "const[" + value + ']';
    }
}
