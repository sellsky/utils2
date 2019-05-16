package tk.bolovsrol.utils.binaryserializer.fieldcodec;

import tk.bolovsrol.utils.binaryserializer.FieldValueCodec;
import tk.bolovsrol.utils.binaryserializer.PosContainer;
import tk.bolovsrol.utils.time.TwofacedTime;

import java.io.IOException;
import java.io.OutputStream;

public class TwofacedTimeFieldValueCodec implements FieldValueCodec<TwofacedTime> {

    private final BooleanFieldValueCodec one = new BooleanFieldValueCodec();
    private final LongFieldValueCodec two = new LongFieldValueCodec();

    @Override public void encodeValue(OutputStream target, TwofacedTime value) throws IOException {
        one.encodeValue(target, value.isRelative());
        two.encodeValue(target, value.getMillis());
    }

    @Override
    public TwofacedTime decodeValue(byte[] source, PosContainer posContainer, Class<? extends TwofacedTime> type) {
        return new TwofacedTime(
                one.decodeValue(source, posContainer, Boolean.class),
                two.decodeValue(source, posContainer, Long.class)
        );
    }
}
