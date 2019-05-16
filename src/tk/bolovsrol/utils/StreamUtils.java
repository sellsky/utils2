package tk.bolovsrol.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/** Всякие утилиты для потоков. */
public final class StreamUtils {

    private StreamUtils() {
    }

    /**
     * Читает поток до тех пор, когда поток перестанет читаться,
     * и считанное возвращает в виде массива.
     *
     * @param is поток для чтения
     * @return считанный массив как минимум нулевой длины
     * @see #readWhileAvailable(InputStream, Integer)
     */
    public static byte[] readWhileAvailable(InputStream is) throws IOException {
        int length = 0;
        int available = is.available();
        byte[] buf = new byte[Math.max(2048, available)]; // Если поток даст нам считать всё сразу, обойдёмся единственным массивом
        while (true) {
            int availableSpace = buf.length - length;
            int read = is.read(buf, length, availableSpace);
            if (read == -1) {
                break;
            }
            length += read;
            if (availableSpace == 0) {
                // читать было некуда? Увеличим буфер вдвое.
                int newLength = Math.max(buf.length << 1, is.available());
                // тупенькая проверка на размеры
                long freeMemory = Runtime.getRuntime().freeMemory();
                if (freeMemory <= (long) newLength) {
                    throw new IOException("Stream too long, already read " + length + " byte(s), free memory limit " + freeMemory + " byte(s)");
                }
                buf = Arrays.copyOf(buf, newLength);
            } else if (read == 0) {
                break;
            }
        }
        return length == buf.length ? buf : Arrays.copyOf(buf, length);
    }

    /**
     * Читает поток до тех пор, когда поток перестанет читаться,
     * но не более указанной длины.
     * <p/>
     * Если длину не указывать, то будет читать сколько влезет.
     *
     * @param is        поток для чтения
     * @param maxLength максимальная длина для чтения
     * @return считанный массив как минимум нулевой длины
     * @throws IOException
     * @see #readWhileAvailable(InputStream)
     */
    public static byte[] readWhileAvailable(InputStream is, Integer maxLength) throws IOException {
        if (maxLength == null) {
            // максимальная длина неизвестна -- читаем скока влезет.
            return readWhileAvailable(is);
        }
        byte[] buf = new byte[maxLength];
        int length = readWhileAvailable(is, buf, 0, buf.length);
        return length == buf.length ? buf : Arrays.copyOf(buf, length);
    }

    /**
     * Читает поток в переданный буфер с начала и до конца
     * до тех пор, когда поток перестанет читаться,
     * но не более размера буфера.
     *
     * @param is  поток для чтения
     * @param buf буфер для чтения
     * @return длина считанного
     * @throws IOException
     * @see #readWhileAvailable(InputStream)
     */
    public static int readWhileAvailable(InputStream is, byte[] buf) throws IOException {
        return readWhileAvailable(is, buf, 0, buf.length);
    }

    /**
     * Читает поток в переданный буфер до тех пор, когда поток перестанет читаться,
     * но не более указанной длины.
     *
     * @param is  поток для чтения
     * @param buf буфер для чтения
     * @param buf буфер для чтения
     * @return длина считанного
     * @throws IOException
     * @see #readWhileAvailable(InputStream)
     */
    public static int readWhileAvailable(InputStream is, byte[] buf, int pos, int len) throws IOException {
        if (pos + len > buf.length) {
            throw new IllegalArgumentException("Buffer too small");
        }
        int availableSpace = len;
        while (true) {
            int read = is.read(buf, pos, availableSpace);
            if (read == -1) {
                break;
            }
            pos += read;
            availableSpace -= read;
            if (availableSpace == 0 || read == 0) {
                break;
            }
        }
        return len - availableSpace;
    }

    /**
     * Записывает содержимое входящего потока в исходящий.
     *
     * @param is
     * @param os
     */
    public static void copyUntilEof(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[Math.min(Math.max(is.available(), 2048), 65536)];
        while (true) {
            int read = is.read(buf);
            if (read < 0) {
                return;
            }
            os.write(buf, 0, read);
        }
    }
}
