package tk.bolovsrol.utils.binaryserializer.fieldcodec;

import tk.bolovsrol.utils.binaryserializer.FieldValueCodec;
import tk.bolovsrol.utils.binaryserializer.PosContainer;

import java.io.IOException;
import java.io.OutputStream;

public class IntegerArrayFieldValueCodec implements FieldValueCodec<Integer[]> {

    private static final IntegerFieldValueCodec INT_CODEC = new IntegerFieldValueCodec();

    @Override public void encodeValue(OutputStream target, Integer[] value) throws IOException {
        INT_CODEC.encodeValue(target, value.length);
        for (Integer integer : value) {
            INT_CODEC.encodeValue(target, integer);
        }
    }

    @Override public Integer[] decodeValue(byte[] source, PosContainer posContainer, Class<? extends Integer[]> type) {
        int arrayLen = INT_CODEC.decodeValue(source, posContainer, Integer.class);
        Integer[] result = new Integer[arrayLen];
        for (int i = 0; i < arrayLen; i++) {
            result[i] = INT_CODEC.decodeValue(source, posContainer, Integer.class);
        }
        return result;
    }
}
