package tk.bolovsrol.utils.binaryserializer.fieldcodec;

import tk.bolovsrol.utils.binaryserializer.FieldValueCodec;
import tk.bolovsrol.utils.binaryserializer.PosContainer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public class DateFieldValueCodec implements FieldValueCodec<Date> {

    private static final LongFieldValueCodec LONG_CODEC = new LongFieldValueCodec();

    @Override public void encodeValue(OutputStream target, Date value) throws IOException {
        LONG_CODEC.encodeValue(target, value.getTime());
    }

    @Override public Date decodeValue(byte[] source, PosContainer posContainer, Class<? extends Date> notUsed) {
        return new Date(LONG_CODEC.decodeValue(source, posContainer, Long.class));
    }
}
