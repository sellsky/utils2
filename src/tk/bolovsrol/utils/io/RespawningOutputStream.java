package tk.bolovsrol.utils.io;

import tk.bolovsrol.utils.StringDumpBuilder;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Поток, который позволяет прозрачно для пользователей потока
 * закрывать (менять) нижележащий поток-делегат.
 * <p/>
 * Так, вызов {@link #close()} этого объекта закроет текущий поток-делегат,
 * но при следующем вызовe методов записи будет открыт новый поток.
 */
public class RespawningOutputStream extends OutputStream {

    private final RespawnOutputSteamProvider provider;

    protected OutputStream stream;

    public RespawningOutputStream(RespawnOutputSteamProvider provider) {
        this.provider = provider;
    }

    /**
     * Если в момент вызова открытого потока нет,
     * то открывается новый поток методом {@link RespawnOutputSteamProvider#newStream()}.
     * <p/>
     * Если открытый поток уже есть, ничего не делает.
     *
     * @throws IOException
     */
    public void open() throws IOException {
        if (stream == null) {
            stream = provider.newStream();
        }
    }

    /**
     * Вызывает {@link OutputStream#write(int)} открытого потока-делегата.
     * <p/>
     * Если в момент вызова открытого потока нет,
     * то открывается новый поток методом {@link RespawnOutputSteamProvider#newStream()}.
     * <p/>
     * После возврата из этого метода поток находится в открытом состоянии.
     *
     * @throws IOException
     */
    @Override public void write(int b) throws IOException {
        open();
        stream.write(b);
    }

    /**
     * Вызывает {@link OutputStream#write(byte[])} открытого потока-делегата.
     * <p/>
     * Если в момент вызова открытого потока нет,
     * то открывается новый поток методом {@link RespawnOutputSteamProvider#newStream()}.
     * <p/>
     * После возврата из этого метода поток находится в открытом состоянии.
     *
     * @throws IOException
     */
    @Override public void write(byte[] b) throws IOException {
        open();
        stream.write(b);
    }

    /**
     * Вызывает {@link OutputStream#write(byte[], int, int)} открытого потока-делегата.
     * <p/>
     * Если в момент вызова открытого потока нет,
     * то открывается новый поток методом {@link RespawnOutputSteamProvider#newStream()}.
     * <p/>
     * После возврата из этого метода поток находится в открытом состоянии.
     *
     * @throws IOException
     */
    @Override public void write(byte[] b, int off, int len) throws IOException {
        open();
        stream.write(b, off, len);
    }

    /**
     * Вызывает {@link OutputStream#flush()} открытого потока-делегата.
     * <p/>
     * Если в момент вызова открытого потока нет, ничего не делает.
     *
     * @throws IOException
     */
    @Override public void flush() throws IOException {
        if (stream != null) {
            stream.flush();
        }
    }

    /**
     * Вызывает {@link OutputStream#close()} открытого потока-делегата
     * и считает, что поток закрыт.
     * <p/>
     * Если в момент вызова открытого потока нет, ничего не делает.
     *
     * @throws IOException
     */
    @Override public void close() throws IOException {
        if (stream != null) {
            stream.close();
            stream = null;
        }
    }

    @Override public String toString() {
        return new StringDumpBuilder()
                .append("provider", provider)
                .append("stream", stream)
                .toString();
    }
}
