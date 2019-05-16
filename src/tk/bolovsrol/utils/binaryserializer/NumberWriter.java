package tk.bolovsrol.utils.binaryserializer;

import java.io.IOException;
import java.io.OutputStream;

public interface NumberWriter {
    void writeTo(OutputStream os, int value) throws IOException;

    NumberWriter BYTE_NUMBER_WRITER = OutputStream::write;

    NumberWriter SHORT_NUMBER_WRITER = (os, value) -> {
        os.write(value >> 8);
        os.write(value);
    };

}
