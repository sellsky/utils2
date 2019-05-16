package tk.bolovsrol.utils.binaryserializer.fieldcodec;

import tk.bolovsrol.utils.binaryserializer.FieldValueCodec;
import tk.bolovsrol.utils.binaryserializer.PosContainer;

import java.io.IOException;
import java.io.OutputStream;

public class IntArrayFieldValueCodec implements FieldValueCodec<int[]> {

    private static final IntegerFieldValueCodec INT_CODEC = new IntegerFieldValueCodec();

    @Override public void encodeValue(OutputStream target, int[] value) throws IOException {
        INT_CODEC.encodeValue(target, value.length);
        for (int integer : value) {
            INT_CODEC.encodeValue(target, integer);
        }
    }

    @Override public int[] decodeValue(byte[] source, PosContainer posContainer, Class<? extends int[]> type) {
        int arrayLen = INT_CODEC.decodeValue(source, posContainer, Integer.class);
        int[] result = new int[arrayLen];
        for (int i = 0; i < arrayLen; i++) {
            result[i] = INT_CODEC.decodeValue(source, posContainer, Integer.class);
        }
        return result;
    }
}
