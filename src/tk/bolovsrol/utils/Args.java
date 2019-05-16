package tk.bolovsrol.utils;

import java.util.Iterator;

/**
 * Императивный читатель параметров командной строки.
 * <p>
 * Сам же является итерируемым объектом.
 */
public class Args implements Iterable<Args.Entry>, Iterator<Args.Entry> {

    public enum Type {
        SHORTKEY(true), LONGKEY(true), VALUE(false);

        private final boolean key;

        Type(boolean key) {
            this.key = key;
        }

        public boolean isKey() {
            return key;
        }
    }

    public class Entry {
        public final Type type;
        public final String content;

        public Entry(Type type, String content) {
            this.type = type;
            this.content = content;
        }

        public boolean isValue() {
            return type == Type.VALUE;
        }

        public boolean isKey() {
            return type.isKey();
        }

        public boolean isShortKey() {
            return type == Type.SHORTKEY;
        }

        public boolean isLongKey() {
            return type == Type.LONGKEY;
        }

        public Type getType() {
            return type;
        }

        public String get() {
            return content;
        }

        public String getKeyOrDie() {
            if (type.isKey()) {
                return content;
            }
            throw new IllegalArgumentException("Key expected, but value " + content + " occured");
        }

        public String getValueOrDie() {
            if (!type.isKey()) {
                return content;
            }
            throw new IllegalArgumentException("Value expected, but key " + content + " occured");
        }

        public String nextValueOrDie() {
            Entry next = next();
            if (next == null) {
                throw new IllegalArgumentException("Value expected, but end of arguments reached");
            }
            return next.getValueOrDie();
        }

        public Args args() {
            return Args.this;
        }

        public boolean hasNext() {
            return args().hasNext();
        }

        public boolean isNextKey() {
            return args().isNextKey();
        }

        public boolean isNextValue() {
            return args().isNextValue();
        }

        public Entry next() {
            return args().next();
        }

        @Override
        public String toString() {
            return type.name() + ' ' + content;
        }
    }

    private final String[] args;
    private int ptr = -1;
    private int shortPtr;
    private boolean keysOver = false;

    // hasNext() получает следующее значение и накапливает его тут
    private Entry next = null;
    // --ключ=значение накапливает значение тут
    private Entry tailValue = null;

    public Args(String[] args) {
        this.args = args;
    }

    public Entry next() {
        if (next != null) {
            Entry result = next;
            next = null;
            return result;
        }

        if (tailValue != null) {
            Entry result = tailValue;
            tailValue = null;
            return result;
        }

        if (shortPtr > 0) {
            shortPtr++;
            String arg = args[ptr];
            if (shortPtr == arg.length() - 1) {
                Entry result = new Entry(Type.SHORTKEY, "-" + arg.substring(shortPtr));
                shortPtr = 0;
                return result;
            } else {
                return new Entry(Type.SHORTKEY, "-" + arg.substring(shortPtr, shortPtr + 1));
            }
        }

        while (true) {
            ptr++;
            if (ptr == args.length) {
                return null;
            }

            String arg = args[ptr];
            if (keysOver) {
                return new Entry(Type.VALUE, arg);
            }

            // Длинный ключ или конец ключей
            if (arg.startsWith("--")) {
                // --
                if (arg.length() == 2) {
                    keysOver = true;
                    continue;
                }
                int eqPos = arg.indexOf('=');
                if (eqPos < 0) {
                    // --key
                    return new Entry(Type.LONGKEY, arg);
                } else {
                    // --key=value
                    tailValue = new Entry(Type.VALUE, arg.substring(eqPos + 1));
                    return new Entry(Type.LONGKEY, arg.substring(0, eqPos));
                }
            }

            // короткие параметры или одиночный минус в качестве значения
            if (arg.startsWith("-")) {
                switch (arg.length()) {
                    case 1: // -
                        return new Entry(Type.VALUE, arg);
                    case 2: // -a
                        return new Entry(Type.SHORTKEY, arg);
                    default:
                        shortPtr = 1;
                        return new Entry(Type.SHORTKEY, args[ptr].substring(0, 2));
                }
            }

            // это не ключ, а значение
            return new Entry(Type.VALUE, arg);
        }
    }

    public boolean hasNext() {
        return tailValue != null || next != null || (next = next()) != null;
    }

    public boolean isNextKey(){
        return hasNext() && (tailValue == null && next.isKey());
    }

    public boolean isNextValue(){
        return hasNext() && (tailValue != null || next.isValue());
    }

    @Override
    public Iterator<Entry> iterator() {
        return this;
    }

}
