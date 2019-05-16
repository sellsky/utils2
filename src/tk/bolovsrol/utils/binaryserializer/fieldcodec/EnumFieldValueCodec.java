package tk.bolovsrol.utils.binaryserializer.fieldcodec;

import tk.bolovsrol.utils.binaryserializer.FieldValueCodec;
import tk.bolovsrol.utils.binaryserializer.NumberReader;
import tk.bolovsrol.utils.binaryserializer.NumberWriter;
import tk.bolovsrol.utils.binaryserializer.PosContainer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Сохраняет {@link Enum#ordinal()} енума в одном байте, если в классе не более 256 енумов,
 * и в двух байтах, если больше.
 */
public class EnumFieldValueCodec implements FieldValueCodec<Enum<?>> {
    @Override
    public void encodeValue(OutputStream target, Enum<?> value) throws IOException {
        (value.getClass().getEnumConstants().length > 256 ?
                NumberWriter.SHORT_NUMBER_WRITER :
                NumberWriter.BYTE_NUMBER_WRITER).writeTo(target, value.ordinal());
    }

    @Override
    public Enum<?> decodeValue(byte[] source, PosContainer posContainer, Class<? extends Enum<?>> type) {
        Enum<?>[] enumConstants = type.getEnumConstants();
        return enumConstants[(enumConstants.length > 256 ?
                NumberReader.SHORT_NUMBER_READER :
                NumberReader.BYTE_NUMBER_READER).readFrom(source, posContainer)];
    }
}
