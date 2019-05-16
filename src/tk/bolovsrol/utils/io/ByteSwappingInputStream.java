package tk.bolovsrol.utils.io;

import java.io.IOException;
import java.io.InputStream;

/** Меняет местами каждый нечётный байт и последующий чётный байт. */
public class ByteSwappingInputStream extends InputStream {
    private final InputStream source;

    private byte[] internalBuf = new byte[1024];

    private int nextByte = -1;
    private int nextNextByte = -1;

    public ByteSwappingInputStream(InputStream source) {
        this.source = source;
    }

    @Override
    public int read() throws IOException {
        if (nextByte >= 0) {
            int ret = nextByte;
            nextByte = -1;
            return ret;
        }
        if (nextNextByte >= 0) {
            nextByte = nextNextByte;
            nextNextByte = -1;
        } else {
            nextByte = source.read();
            if (nextByte < 0) {
                return -1;
            }
        }
        return source.read();
    }

    private int appendSingleByte(byte[] buf, int off) throws IOException {
        int val = read();
        if (val < 0) {
            return -1;
        }
        buf[off] = (byte) val;
        return 1;

    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        if (nextByte >= 0 || nextNextByte >= 0 || len == 1) {
            return appendSingleByte(buf, off);
        } else {
            return readEvenBytes(buf, off, len & 0xfffffffe);
        }
    }

    private int readEvenBytes(byte[] buf, int off, int len) throws IOException {
        if (internalBuf.length < len) {
            internalBuf = new byte[len];
        }
        int read = source.read(internalBuf, 0, len);
        if (read <= 0) {
            return read;
        }
        if ((read & 1) == 1) {
            nextNextByte = internalBuf[read - 1] & 0xff;
            read--;
        }
        int from = 0;
        int to = off;
        while (from < read) {
            buf[to++] = internalBuf[from + 1];
            buf[to++] = internalBuf[from];
            from += 2;
        }
        return read;
    }

    @Override
    public int available() throws IOException {return source.available();}

    @Override
    public void close() throws IOException {source.close();}

}
