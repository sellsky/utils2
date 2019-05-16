package tk.bolovsrol.utils.binaryserializer.fieldcodec;

import tk.bolovsrol.utils.binaryserializer.FieldValueCodec;
import tk.bolovsrol.utils.binaryserializer.PosContainer;
import tk.bolovsrol.utils.time.Duration;

import java.io.IOException;
import java.io.OutputStream;

public class DurationFieldValueCodec implements FieldValueCodec<Duration> {

    private static final LongFieldValueCodec LONG_CODEC = new LongFieldValueCodec();

    @Override public void encodeValue(OutputStream target, Duration value) throws IOException {
        LONG_CODEC.encodeValue(target, value.getMillis());
    }

    @Override public Duration decodeValue(byte[] source, PosContainer posContainer, Class<? extends Duration> notUsed) {
        return new Duration(LONG_CODEC.decodeValue(source, posContainer, Long.class));
    }
}
