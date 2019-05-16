package tk.bolovsrol.utils.binaryserializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Кодировщик и декодировщих объектов определённого типа..
 *
 * @param <E> тип обслуживаемых объектов
 */
public interface FieldValueCodec<E> {
    /**
     * Кодирует значение и записывает его в поток.
     *
     * @param target
     * @param value
     * @throws IOException
     */
    void encodeValue(OutputStream target, E value) throws IOException;

    /**
     * Декодирует значение, обновляет posContainer и возвращает распознанное значение.
     *
     * @param source
     * @param posContainer
     * @param type
     * @return
     */
    E decodeValue(byte[] source, PosContainer posContainer, Class<? extends E> type);
}
