package tk.bolovsrol.utils.binaryserializer;

import tk.bolovsrol.utils.binaryserializer.fieldcodec.FieldValueCodecs;
import tk.bolovsrol.utils.binaryserializer.fieldcodec.StringFieldValueCodec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Кодирует объекты в последовательность байтов, из которой {@link StatefulDecoder декодировщик}
 * может восстановить исходные объекты.
 * <p/>
 * Кодировщик ориентирован на компактную сериализацию последовательности контейнеров одного класса.
 * Требования к кодируемым классам:<br/>
 * - объект должен быть прямым наследником {@link Object} и имплементить {@link Serializable};<br/>
 * - объект должен обладать конструктором без параметров и<br/>
 * - типы полей должны входить в определённый, достаточно небольшой список.
 * <p/>
 * Состоит последовательных записей:<br/>
 * - рефренс класса (1 байт);<br/>
 * - (возможное описание класса);<br/>
 * - описание данных;<br/>
 * - терминатор класса (один нулевой байт, если в классе меньше 255 полей, или 2 нулевых байта, если больше).
 * <p/>
 * Рефренс класса представляет собой комбинацию: установленный старший бит означает, что за рефренсом следует описание класса.
 * Остальные биты являются индексом описания, который увеличивается с 0.
 * <p/>
 * Рефренсы служат для кеширования описаний классов, чтобы не приходилось прилагать описание класса к каждому объекту
 * и проверять это описание при восстановлении объекта. Кодировщик и декодировщик запоминают рефренсы.
 * При необходимости кэш рефренсов можно очистить вручную методом {@link #resetClassReferences()},
 * также можно заставить кодировщик автоматически очищать кэш после каждого вызова {@link #getEncodedAndReset()}
 * методом {@link #setAutoResetClassReferences(boolean)}.
 * <p/>
 * Описание класса:<br/>
 * - название (рефренс-строка);<br/>
 * - serialVersionUID (8 байтов).
 * <p/>
 * Формат рефренс-строки описан в {@link StringFieldValueCodec кодировщике строк}.
 * <p/>
 * Описание данных:<br/>
 * - номер поля, на 1 больше номера поля в рефлекшне (один байт, если в классе меньше 255 полей, или 2 байта, если больше);<br/>
 * - данные поля (длина зависит от типа поля).
 */
public class StatefulEncoder {

    // кодировщики для записи данных
    private static final FieldValueCodec<String> STRING_CODEC = FieldValueCodecs.getCodec(String.class);
    private static final FieldValueCodec<Long> LONG_CODEC = FieldValueCodecs.getCodec(Long.class);

    // модификаторы полей, которые нам не нужны
    private static final int FORBIDDEN_MODIFIERS = Modifier.FINAL | Modifier.STATIC | Modifier.TRANSIENT;

    /** Буфер для записи. */
    private final ByteArrayOutputStream b = new ByteArrayOutputStream(8192);

    /** Список рефренсов классов. */
    private final LinkedHashMap<Class<?>, Integer> classReferences = new LinkedHashMap<>();

    /** Сбрасывать ли список известных классов между выемками. */
    private boolean autoResetClassReferences = false;

    public StatefulEncoder() {
    }

    /** @param autoResetClassReferences режим автоматической очистки кеша рефренсов после каждого кодирования */
    public StatefulEncoder(boolean autoResetClassReferences) {
        this.autoResetClassReferences = autoResetClassReferences;
    }

    /** Очищает кэш рефренсов. */
    public void resetClassReferences() {
        classReferences.clear();
    }

    /**
     * Добавляет объект в массив сериализованных данных.
     *
     * @param entity
     * @throws EncodeException
     */
    public void append(Serializable entity) throws EncodeException {
        writeClassIndex(entity);
        writeClassData(entity);
    }

    private void writeClassData(Serializable entity) throws EncodeException {
        Field[] declaredFields = entity.getClass().getDeclaredFields();
        int declaredFieldsLength = declaredFields.length;
        NumberWriter indexWriter = declaredFieldsLength > 255 ?
                NumberWriter.SHORT_NUMBER_WRITER :
                NumberWriter.BYTE_NUMBER_WRITER;
        try {
            for (int i = 0; i < declaredFieldsLength; i++) {
                Field field = declaredFields[i];
                if ((field.getModifiers() & FORBIDDEN_MODIFIERS) != 0) {
                    continue;
                }
                field.setAccessible(true);
                Object value;
                try {
                    value = field.get(entity);
                } catch (IllegalAccessException e) {
                    throw new EncodeException("Cannot read field " + field + " value", e);
                }
                if (value != null) {
                    indexWriter.writeTo(b, i + 1);
                    @SuppressWarnings("rawtypes") FieldValueCodec fvc = FieldValueCodecs.getCodec(field.getType().isEnum() ? (Class) Enum.class : field.getType());
                    if (fvc == null) {
                        throw new IllegalArgumentException("Don't know how to encode class " + field.getType() + " (field " + field + ')');
                    }
                    //noinspection unchecked
                    fvc.encodeValue(b, value);
                }
            }
            indexWriter.writeTo(b, 0);
        } catch (IOException wontHappen) {
            // won't happen
        }
    }

    private void writeClassIndex(Serializable entity) throws EncodeException {
        Integer classIndex = classReferences.get(entity.getClass());
        if (classIndex != null) {
            // Объект известного класса. Запишем его сущность.
            b.write(classIndex & 0x7f);
        } else {
            // Объект нового класса. Проверим его и запишем в словарик.
            if (entity.getClass().getSuperclass() != Object.class) {
                throw new EncodeException("Object for serialization should be primary Object subclass");
            }
            try {
                entity.getClass().getConstructor();
            } catch (NoSuchMethodException e) {
                throw new EncodeException("Object for serialization should have public no-argument constructor", e);
            }

            // выясним его индекс и запомним его.
            classIndex = classReferences.size();
            if ((classIndex & 0x80) != 0) {
                // список классов переполнился - удалим наименее используемый рефренс и займём его индекс
                Iterator<Map.Entry<Class<?>, Integer>> it = classReferences.entrySet().iterator();
                classIndex = it.next().getValue();
                it.remove();
            }
            classReferences.put(entity.getClass(), classIndex);

            // записываем описание класса
            try {

                long uid = 0L;
                try {
                    Field serialVersionUIDField = entity.getClass().getDeclaredField("serialVersionUID");
                    serialVersionUIDField.setAccessible(true);
                    uid = (Long) serialVersionUIDField.get(null);
                } catch (NoSuchFieldException ignore) {
                } catch (Exception e) {
                    throw new EncodeException("Cannot retrieve serialVersionUID value from class " + entity.getClass(), e);
                }

                b.write(classIndex.intValue() | 0x80);
                STRING_CODEC.encodeValue(b, entity.getClass().getName());
                LONG_CODEC.encodeValue(b, uid);

            } catch (IOException wontHappen) {
                // won't happen
            }
        }
    }

    /**
     * Возвращает текущие результаты кодирования и сбрасывает кодировщик.
     *
     * @return закодированный массив
     */
    public byte[] getEncodedAndReset() {
        byte[] result = b.toByteArray();
        b.reset();
        if (autoResetClassReferences) {
            classReferences.clear();
        }
        return result;
    }

    public boolean isAutoResetClassReferences() {
        return autoResetClassReferences;
    }

    /** @param autoResetClassReferences режим автоматической очистки кеша рефренсов после каждого кодирования */
    public void setAutoResetClassReferences(boolean autoResetClassReferences) {
        this.autoResetClassReferences = autoResetClassReferences;
    }
}
