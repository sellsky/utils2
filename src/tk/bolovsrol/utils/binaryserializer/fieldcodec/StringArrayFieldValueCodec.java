package tk.bolovsrol.utils.binaryserializer.fieldcodec;

import tk.bolovsrol.utils.binaryserializer.FieldValueCodec;
import tk.bolovsrol.utils.binaryserializer.PosContainer;

import java.io.IOException;
import java.io.OutputStream;

public class StringArrayFieldValueCodec implements FieldValueCodec<String[]> {

    private static final IntegerFieldValueCodec INT_CODEC = new IntegerFieldValueCodec();
    private static final StringFieldValueCodec STRING_CODEC = new StringFieldValueCodec();

    @Override public void encodeValue(OutputStream target, String[] value) throws IOException {
        INT_CODEC.encodeValue(target, value.length);
        for (String string : value) {
            STRING_CODEC.encodeValue(target, string);
        }
    }

    @Override public String[] decodeValue(byte[] source, PosContainer posContainer, Class<? extends String[]> type) {
        int arrayLen = INT_CODEC.decodeValue(source, posContainer, Integer.class);
        String[] result = new String[arrayLen];
        for (int i = 0; i < arrayLen; i++) {
            result[i] = STRING_CODEC.decodeValue(source, posContainer, String.class);
        }
        return result;
    }


//    @Override public void encodeValue(OutputStream target, Duration value) throws IOException {
//
//
//        LONG_CODEC.encodeValue(target, value.getMillis());
//    }
//
//    @Override public Duration decodeValue(byte[] source, PosContainer posContainer, Class<? extends Duration> notUsed) {
//        return new Duration(LONG_CODEC.decodeValue(source, posContainer, Long.class));
//    }
}
