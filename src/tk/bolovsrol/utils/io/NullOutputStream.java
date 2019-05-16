package tk.bolovsrol.utils.io;

import java.io.IOException;
import java.io.OutputStream;

/** Игнорирует всё записанное. */
public class NullOutputStream extends OutputStream {

    private static final NullOutputStream INSTANCE = new NullOutputStream();

    public static NullOutputStream getInstance() {
        return INSTANCE;
    }

    private NullOutputStream() {
    }

    @Override
    public void write(int b) throws IOException {
    }

    @Override public void write(byte[] b, int off, int len) throws IOException {
    }

    @Override public void write(byte[] b) throws IOException {
    }

    @Override public void flush() throws IOException {
    }

    @Override public void close() throws IOException {
    }
}
