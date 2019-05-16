package tk.bolovsrol.utils.binaryserializer.fieldcodec;

import tk.bolovsrol.utils.binaryserializer.FieldValueCodec;
import tk.bolovsrol.utils.binaryserializer.PosContainer;

import java.io.IOException;
import java.io.OutputStream;

public class BooleanFieldValueCodec implements FieldValueCodec<Boolean> {
    @Override public void encodeValue(OutputStream target, Boolean value) throws IOException {
        if (value.booleanValue()) {
            target.write(0xff);
        } else {
            target.write(0);
        }
    }

    @Override public Boolean decodeValue(byte[] source, PosContainer posContainer, Class<? extends Boolean> notUsed) {
        Boolean result = source[posContainer.getPos()] == 0 ? Boolean.FALSE : Boolean.TRUE;
        posContainer.incPos();
        return result;
    }
}
