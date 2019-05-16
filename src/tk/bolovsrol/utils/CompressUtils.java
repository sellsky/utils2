package tk.bolovsrol.utils;

import tk.bolovsrol.utils.function.ThrowingFunction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Сжимает и разжимает массив байтиков стандартными алгоритамаи deflate и gzip.
 */
@SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"}) public final class CompressUtils {

    private CompressUtils() {}

    /**
     * Сжимает массив байтов и возвращает результат в формате gzip.
     *
     * @param uncompressed несжатое
     * @return сжатое
     */
    public static byte[] gzip(byte[] uncompressed) {
        return deflate(uncompressed, baos -> new GZIPOutputStream(baos, true));
    }

    /**
     * Сжимает массив байтов и возвращает результат в формате deflate.
     *
     * @param uncompressed несжатое
     * @return сжатое
     */
    public static byte[] deflate(byte[] uncompressed) {
        return deflate(uncompressed, baos -> new DeflaterOutputStream(baos, true));
    }

    private static byte[] deflate(byte[] uncompressed, ThrowingFunction<ByteArrayOutputStream, DeflaterOutputStream, IOException> streamProvider) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(uncompressed.length);
            DeflaterOutputStream dos = streamProvider.apply(baos);
            dos.write(uncompressed);
            dos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("This shouldn't happen", e);
        }
    }

    /**
     * Распаковывает массив байтов в формате gzip и возвращает результат.
     *
     * @param compressed сжатое
     * @return несжатое
     */
    public static byte[] ungzip(byte[] compressed) {
        return inflate(compressed, GZIPInputStream::new);
    }

    /**
     * Распаковывает массив байтов в формате deflate и возвращает результат.
     *
     * @param compressed сжатое
     * @return несжатое
     */
    public static byte[] inflate(byte[] compressed) {
        return inflate(compressed, InflaterInputStream::new);
    }

    private static byte[] inflate(byte[] compressed, ThrowingFunction<ByteArrayInputStream, InflaterInputStream, IOException> streamProvider) {
        try {
            InflaterInputStream iis = streamProvider.apply(new ByteArrayInputStream(compressed));
            int bufSize = compressed.length;
            ArrayList<byte[]> buffers = new ArrayList<>(10);
            while (true) {
                byte[] buf = new byte[bufSize];
                int pos = 0;
                while (pos < bufSize) {
                    int read = iis.read(buf, pos, bufSize - pos);
                    if (read < 0) { // EOS
                        byte[] result = new byte[buffers.size() * bufSize + pos];
                        int u = 0;
                        for (byte[] buffer : buffers) {
                            System.arraycopy(buffer, 0, result, u, bufSize);
                            u += bufSize;
                        }
                        if (pos > 0) { System.arraycopy(buf, 0, result, u, pos); }
                        return result;
                    }
                    pos += read;
                }
                buffers.add(buf);
            }
        } catch (IOException e) {
            throw new RuntimeException("This shouldn't happen", e);
        }
    }

}
