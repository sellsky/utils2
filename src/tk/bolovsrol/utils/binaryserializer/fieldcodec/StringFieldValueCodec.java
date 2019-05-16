package tk.bolovsrol.utils.binaryserializer.fieldcodec;

import tk.bolovsrol.utils.binaryserializer.FieldValueCodec;
import tk.bolovsrol.utils.binaryserializer.PosContainer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Кодирует строку:<br/>
 * - рефренс длины (1..5 байтов);<br/>
 * - строка в UTF-8 указанной длины.
 */
public class StringFieldValueCodec implements FieldValueCodec<String> {
    public static final Charset CHARSET = Charset.forName("UTF-8");
    private final IntegerFieldValueCodec integerFieldValueCodec = new IntegerFieldValueCodec();

    @Override public void encodeValue(OutputStream target, String value) throws IOException {
        byte[] bytes = value.getBytes(CHARSET);
        integerFieldValueCodec.encodeValue(target, bytes.length);
        target.write(bytes);
    }

    @Override public String decodeValue(byte[] source, PosContainer posContainer, Class<? extends String> type) {
        int len = integerFieldValueCodec.decodeValue(source, posContainer, Integer.class);
        return new String(source, posContainer.getPosAndInc(len), len, CHARSET);
    }
}
