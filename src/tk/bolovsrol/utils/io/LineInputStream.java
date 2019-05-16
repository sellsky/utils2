package tk.bolovsrol.utils.io;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/** Стрим, который умеет читать строки. */
public class LineInputStream extends InputStream {

    private Charset charset;
    private final InputStream src;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    /**
     * Создаёт поток с указанием кодировки, в которой будут раскодировать строки.
     *
     * @param src
     * @param charset
     */
    public LineInputStream(InputStream src, Charset charset) {
        this.src = src;
        this.charset = charset;
    }

    /**
     * Создаёт поток с указанием кодировки, в которой будут раскодировать строки.
     *
     * @param src
     * @param charsetName
     */
    public LineInputStream(InputStream src, String charsetName) {
        this(src, Charset.forName(charsetName));
    }

    /**
     * Создаёт поток с кодировкой UTF-8.
     *
     * @param src
     */
    public LineInputStream(InputStream src) {
        this(src, "UTF-8");
    }

    /**
     * Возвращает актуальную кодировку, используемую для раскодирования строк.
     *
     * @return название кодировки
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Устанавливает новую кодировку, которой будут раскодироваться
     * последующие вычитываемые из потока строки.
     *
     * @param charsetName название кодировки
     */
    public void setCharset(String charsetName) {
        setCharset(Charset.forName(charsetName));
    }

    /**
     * Устанавливает новую кодировку, которой будут раскодироваться
     * последующие вычитываемые из потока строки.
     *
     * @param charset кодировка
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * @return очередную строку либо null, если читать больше нечего.
     * @throws IOException
     */
    public String readLine() throws IOException {
        byte[] bytes = readUntilLineSeparator();
        try {
            return bytes == null ? null : StringUtils.decodeOrDie(bytes, charset);
        } catch (CharacterCodingException e) {
            throw new IOException("Error decoding read bytes " + Spell.get(bytes) + " into " + charset + " string", e);
        }
    }

    /**
     * Создаёт строку из переданных байтов. Если , если считанные
     *
     * @param bytes
     * @return
     * @throws CharacterCodingException
     */
    /**
     * @return байты до перевода строки либо null, если читать больше нечего.
     * @throws IOException
     */
    public byte[] readUntilLineSeparator() throws IOException {
        baos.reset();
        while (true) {
            int val = src.read();
            switch (val) {
                case -1:
                    return baos.size() == 0 ? null : baos.toByteArray();

                case 0xd:
                    break;

                case 0xa:
                    return baos.toByteArray();

                default:
                    baos.write(val);
                    break;
            }
        }
    }

    @Override
    public int read() throws IOException {
        return src.read();
    }

    @Override
    public void close() throws IOException {
        src.close();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return src.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return src.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return src.skip(n);
    }

    @Override
    public int available() throws IOException {
        return src.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        src.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        src.reset();
    }

    @Override
    public boolean markSupported() {
        return src.markSupported();
    }
}
