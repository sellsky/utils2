package tk.bolovsrol.utils.reflectiondump;

import tk.bolovsrol.utils.Spell;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс-хелпер для сборки строки с содержимым полей объекта через рефлекшн.
 * <p/>
 * Например, можно использовать в toString():
 * <pre>   @Override public String toString() {
 *     return ReflectionDump.getFor(this);
 * }</pre>
 */
public class ReflectionDump {

    private static final Map<Class<?>, List<Field>> CLASS_FIELDS = new ConcurrentHashMap<>();

    private static final int FORBIDDEN_MODIFIERS = Modifier.STATIC;

    private ReflectionDump() {
    }

    /**
     * Собирает строку с содержимым полей объекта через рефлекшн.
     * Строка начинается названием класса, после перечисляются поля в виде «название=значение»,
     * начиная с полей предков.
     * <p/>
     * Включаются все не статические поля без аннотации {@link ExcludeFromReflectionDump}.
     *
     * @param o объект для форматирования
     * @return дамп
     */
    public static String getFor(Object o) {
        Class<?> cl = o.getClass();
        List<Field> fields = retrieveFieldList(cl);
        StringBuilder sb = new StringBuilder(fields.size() << 5);
        sb.append(cl.getSimpleName());
        try {
            for (Field field : fields) {
                sb.append(' ');
                sb.append(field.getName());
                sb.append('=');
                sb.append(Spell.get(field.get(o)));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot generate reflected ReflectionDump for " + o.getClass(), e);
        }
        return sb.toString();
    }

    private static List<Field> retrieveFieldList(Class<?> cl) {
        List<Field> fields = CLASS_FIELDS.get(cl);
        if (fields == null) {
            ArrayList<Field> tmp = new ArrayList<>(256);
            Class<?> superclass = cl.getSuperclass();
            if (superclass != null && !superclass.getName().startsWith("java.")) {
                tmp.addAll(retrieveFieldList(superclass));
            }
            for (Field field : cl.getDeclaredFields()) {
                field.setAccessible(true);
                if ((field.getModifiers() & FORBIDDEN_MODIFIERS) == 0
                      && field.getAnnotation(ExcludeFromReflectionDump.class) == null) {
                    tmp.add(field);
                }
            }
            tmp.trimToSize();
            fields = tmp;
            CLASS_FIELDS.put(cl, fields);
        }
        return fields;
    }
}
