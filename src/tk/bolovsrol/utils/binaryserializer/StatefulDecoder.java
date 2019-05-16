package tk.bolovsrol.utils.binaryserializer;

import tk.bolovsrol.utils.binaryserializer.fieldcodec.FieldValueCodecs;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Восстанавливает объекты, сериализованные при помощи {@link StatefulEncoder}. */
public class StatefulDecoder {

    // используем эти читатели в декодировании
    public static final FieldValueCodec<String> STRING_CODEC = FieldValueCodecs.getCodec(String.class);
    private static final FieldValueCodec<Long> LONG_CODEC = FieldValueCodecs.getCodec(Long.class);

    /** Контейнер с указателем на текущий байт в читаемом буфере. */
    private final PosContainer posContainer = new PlainPosContainer();

    /** Известные классы. */
    private final Map<Integer, Class<? extends Serializable>> classReferences = new TreeMap<>();

    /** Данные для декодирования. */
    private byte[] encoded;

    private boolean autoResetClassReferences = false;

    /** Очищает список запомненных известных классов. */
    public void resetClassReferences() {
        classReferences.clear();
    }

    public StatefulDecoder() {
    }

    public StatefulDecoder(boolean autoResetClassReferences) {
        this.autoResetClassReferences = autoResetClassReferences;
    }

    /**
     * Декодирует объекты, возвращая их список.
     *
     * @param encoded закодированные данные
     * @return список декодированных объектов
     * @throws DecodeException декодирование не удалось
     */
    public List<Serializable> decode(byte[] encoded) throws DecodeException {
        this.encoded = encoded;
        if (autoResetClassReferences) {
            classReferences.clear();
        }
        List<Serializable> result = new ArrayList<>();
        posContainer.setPos(0);
        int encodedLength = encoded.length;
        while (posContainer.getPos() < encodedLength) {
            int signature = encoded[posContainer.getPosAndInc()];
            Serializable entity = createEntity(retrieveEntityClass(signature));
            loadFields(entity);
            result.add(entity);
        }
        return result;
    }

    /**
     * Прописывает значения полям объекта.
     *
     * @param entity восстанавливаемый объект.
     * @throws DecodeException
     */
    private void loadFields(Serializable entity) throws DecodeException {
        Field[] declaredFields = entity.getClass().getDeclaredFields();
        NumberReader indexReader = declaredFields.length > 255 ?
                NumberReader.SHORT_NUMBER_READER :
                NumberReader.BYTE_NUMBER_READER;
        try {
            while (true) {
                int fieldIndex = indexReader.readFrom(encoded, posContainer) - 1;
                if (fieldIndex < 0) {
                    return;
                }
                Field f = declaredFields[fieldIndex];
                f.setAccessible(true);
                @SuppressWarnings("rawtypes") // иначе компиляция не проходит из-за каких-то багов в джава-конпилере
                      FieldValueCodec codec = FieldValueCodecs.getCodec(f.getType().isEnum() ? (Class) Enum.class : f.getType());
                //noinspection unchecked
                f.set(entity, codec.decodeValue(encoded, posContainer, f.getType()));
            }
        } catch (Exception e) {
            throw new DecodeException("Error reading field values for entity " + entity.getClass(), e);
        }
    }

    private static Serializable createEntity(Class<? extends Serializable> cl) throws DecodeException {
        Serializable entity;
        try {
            entity = cl.getConstructor().newInstance();
        } catch (Exception e) {
            throw new DecodeException("Cannot instantinate object " + cl, e);
        }

        return entity;
    }

    /**
     * Выясняет соответствующий сигнатуре класс объекта.
     *
     * @param signature
     * @return объект для восстановления
     * @throws DecodeException
     */
    private Class<? extends Serializable> retrieveEntityClass(int signature) throws DecodeException {
        int classIndex = signature & 0x7f;
        boolean knownSignature = (signature & 0x80) == 0;

        Class<? extends Serializable> cl;
        if (knownSignature) {
            // восстанвливаем известный класс
            cl = classReferences.get(classIndex);
            if (cl == null) {
                throw new DecodeException("Unexpected signature " + signature);
            }
        } else {
            // восстанавливаем новый класс. Читаем описание и проверяем считанное
            String className;
            try {
                className = STRING_CODEC.decodeValue(encoded, posContainer, String.class);
            } catch (Exception e) {
                throw new DecodeException("Cannot read class name after signature " + signature, e);
            }
            try {
                //noinspection unchecked
                cl = (Class<Serializable>) Class.forName(className);
            } catch (Exception e) {
                throw new DecodeException("Cannot find class by name " + className, e);
            }

            long expectedUid = LONG_CODEC.decodeValue(encoded, posContainer, Long.class);
            long actualUid = 0L;
            try {
                Field serialVersionUIDField = cl.getDeclaredField("serialVersionUID");
                serialVersionUIDField.setAccessible(true);
                actualUid = (Long) serialVersionUIDField.get(null);
            } catch (NoSuchFieldException ignore) {
            } catch (Exception e) {
                throw new DecodeException("Cannot retrieve serialVersionUID value from class " + cl, e);
            }
            if (expectedUid != actualUid) {
                throw new DecodeException("For Class " + cl + " expected serialVersionUID=" + expectedUid
                        + ", but provided serialVersionUID=" + actualUid);
            }

            classReferences.put(classIndex, cl);
        }
        return cl;
    }

    public boolean isAutoResetClassReferences() {
        return autoResetClassReferences;
    }

    public void setAutoResetClassReferences(boolean autoResetClassReferences) {
        this.autoResetClassReferences = autoResetClassReferences;
    }
}
