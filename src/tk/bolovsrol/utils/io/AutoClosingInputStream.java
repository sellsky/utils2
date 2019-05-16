package tk.bolovsrol.utils.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Поток-фильтр. Автоматически закрывает нижележащий поток
 * (вызывает метод {@link #close()} потока)
 * по достижении EOF.
 * <p/>
 * Так как поток может быть закрыт в любой момент,
 * механизм mark()/reset() не поддерживается.
 */
public class AutoClosingInputStream extends InputStream {
    private final InputStream is;
    private final InputStreamClosedListener listener;

    public interface InputStreamClosedListener {
        void inputStreamClosed(InputStream inputStream);
    }

    public AutoClosingInputStream(InputStream is) {
        this.is = is;
        this.listener = null;
    }

    public AutoClosingInputStream(InputStream is, InputStreamClosedListener inputStreamClosedListener) {
        this.is = is;
        this.listener = inputStreamClosedListener;
    }

    @Override
    public int read() throws IOException {
        int val = is.read();
        if (val < 0) {
            close();
        }
        return val;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = is.read(b);
        if (read < 0) {
            close();
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = is.read(b, off, len);
        if (read < 0) {
            close();
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {return is.skip(n);}

    @Override
    public int available() throws IOException {return is.available();}

    @Override
    public void close() throws IOException {
        is.close();
        if (listener != null) {
            listener.inputStreamClosed(is);
        }
    }

}
