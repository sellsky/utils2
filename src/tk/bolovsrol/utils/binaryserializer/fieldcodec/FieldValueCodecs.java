package tk.bolovsrol.utils.binaryserializer.fieldcodec;

import tk.bolovsrol.utils.binaryserializer.FieldValueCodec;
import tk.bolovsrol.utils.reflectiondump.ReflectionDump;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TwofacedTime;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Кодировщики поддерживаемых типов полей.
 * <p/>
 * Врапперы примитивов также декодируют поля-примитивы.
 */
public class FieldValueCodecs {

    private static final Map<Class<?>, FieldValueCodec<?>> CODECS = new HashMap<Class<?>, FieldValueCodec<?>>();

    private FieldValueCodecs() {
    }

    static {
        register(new Class<?>[]{Boolean.class, boolean.class}, new BooleanFieldValueCodec());
        register(new Class<?>[]{Date.class}, new DateFieldValueCodec());
        register(new Class<?>[]{Duration.class}, new DurationFieldValueCodec());
        register(new Class<?>[]{Enum.class}, new EnumFieldValueCodec());
        register(new Class<?>[]{Integer.class, int.class}, new IntegerFieldValueCodec());
        register(new Class<?>[]{int[].class}, new IntArrayFieldValueCodec());
        register(new Class<?>[]{Integer[].class}, new IntegerArrayFieldValueCodec());
        register(new Class<?>[]{Long.class, long.class}, new LongFieldValueCodec());
        register(new Class<?>[]{String.class}, new StringFieldValueCodec());
        register(new Class<?>[]{String[].class}, new StringArrayFieldValueCodec());
        register(new Class<?>[]{TwofacedTime.class}, new TwofacedTimeFieldValueCodec());
    }

    private static void register(Class<?>[] classes, FieldValueCodec<?> codec) {
        for (Class<?> cl : classes) {
            CODECS.put(cl, codec);
        }
    }

    public static <C> FieldValueCodec<C> getCodec(Class<C> clas) {
        //noinspection unchecked
        return (FieldValueCodec<C>) CODECS.get(clas);
    }

    public static class Test implements Serializable {
        private static final long serialVersionUID = 413529841419709819L;
        String[] zuka;
        int[] lololo;

        public Test() {
        }

        @Override public String toString() {
            return ReflectionDump.getFor(this);
        }

    }
}
