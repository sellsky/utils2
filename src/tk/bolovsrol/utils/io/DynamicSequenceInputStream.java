package tk.bolovsrol.utils.io;

import java.io.InputStream;
import java.io.IOException;

/**
 * Поток динамически меняет источник по мере иссякания текущего источника.
 * <p/>
 * Можно настроить поток как автоматически закрывать, так и не закрывать
 * иссякшие потоки. Вызов метода {@link #close()} транслируется текущему
 * актуальному источнику.
 * <p/>
 * Метод {@link #read(byte[], int, int)} не смешивает данные двух потоков.
 */
public class DynamicSequenceInputStream extends InputStream {

    public interface InputStreamProvider {
        InputStream nextStream() throws IOException;
    }

    private final InputStreamProvider inputStreamProvider;
    private boolean autoCloseRanOutStream;

    private InputStream source;

    public DynamicSequenceInputStream(InputStreamProvider inputStreamProvider) {
        this.inputStreamProvider = inputStreamProvider;
    }

    public DynamicSequenceInputStream(InputStreamProvider inputStreamProvider, boolean autoCloseRanOutStream) {
        this.inputStreamProvider = inputStreamProvider;
        this.autoCloseRanOutStream = autoCloseRanOutStream;
    }

    private boolean checkSource() throws IOException {
        return source != null || nextSource();
    }

    private boolean nextSource() throws IOException {
        if (autoCloseRanOutStream && source != null) {
            source.close();
        }
        return (source = inputStreamProvider.nextStream()) != null;
    }

    @Override
    public int read() throws IOException {
        if (!checkSource()) {
            return -1;
        }
        while (true) {
            int read = source.read();
            if (read != -1) {
                return read;
            }
            if (!nextSource()) {
                return -1;
            }
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (!checkSource()) {
            return -1;
        }
        while (true) {
            int read = source.read(b, off, len);
            if (read != -1) {
                return read;
            }
            if (!nextSource()) {
                return -1;
            }
        }
    }

    @Override
    public long skip(long n) throws IOException {return source.skip(n);}

    @Override
    public int available() throws IOException {return source.available();}

    @Override
    public void close() throws IOException {source.close();}

    public boolean isAutoCloseRanOutStream() {
        return autoCloseRanOutStream;
    }

    public void setAutoCloseRanOutStream(boolean autoCloseRanOutStream) {
        this.autoCloseRanOutStream = autoCloseRanOutStream;
    }
}
