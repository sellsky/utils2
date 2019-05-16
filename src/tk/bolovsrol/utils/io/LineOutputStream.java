package tk.bolovsrol.utils.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/** Поток, который умеет записывать строки. */
public class LineOutputStream extends OutputStream {

    private final OutputStream dst;
    private Charset charset;
    private LineSeparator ls;

    public static class LineSeparator {
        public static final LineSeparator CR = new LineSeparator(new byte[]{(byte) 0x0d});
        public static final LineSeparator CRLF = new LineSeparator(new byte[]{(byte) 0x0d, (byte) 0x0a});
        public static final LineSeparator LF = new LineSeparator(new byte[]{(byte) 0x0a});

        private final byte[] sep;

        public LineSeparator(byte[] sep) {
            this.sep = sep;
        }

        void writeTo(OutputStream os) throws IOException {
            os.write(sep, 0, sep.length);
        }
    }

    public LineOutputStream(OutputStream dst, Charset charset, LineSeparator ls) {
        this.dst = dst;
        this.charset = charset;
        this.ls = ls;
    }

    public LineOutputStream(OutputStream dst, String charsetName, LineSeparator ls) {
        this(dst, Charset.forName(charsetName), ls);
    }

    public LineOutputStream(OutputStream dst, Charset charset) {
        this(dst, charset, LineSeparator.CRLF);
    }

    public LineOutputStream(OutputStream dst, String charsetName) {
        this(dst, Charset.forName(charsetName));
    }

    public LineOutputStream(OutputStream dst, LineSeparator ls) {
        this(dst, Charset.forName("UTF-8"), ls);
    }

    public LineOutputStream(OutputStream dst) {
        this(dst, Charset.forName("UTF-8"), LineSeparator.CRLF);
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Записывает в поток ввода-вывода строку.
     *
     * @throws IOException
     */
    public void write(String s) throws IOException {
        dst.write(s.getBytes(charset));
    }

    /**
     * Записывает в поток ввода-вывода текущую последовательность перевода строки.
     *
     * @throws IOException
     */
    public void writeln() throws IOException {
        ls.writeTo(dst);
    }

    /**
     * Записывает в поток ввода-вывода строку, а за ней последовательность перевода строки.
     *
     * @param s
     * @throws IOException
     */
    public void writeln(String s) throws IOException {
        write(s);
        writeln();
    }

    /**
     * Устанавливает новую кодировку, которой будут кодироваться
     * последующие записываемые в поток строки.
     *
     * @param charsetName название кодировки
     */
    public void setCharset(String charsetName) {
        setCharset(Charset.forName(charsetName));
    }

    public LineSeparator getLs() {
        return ls;
    }

    public void setLs(LineSeparator ls) {
        this.ls = ls;
    }

    @Override
    public void write(int b) throws IOException {dst.write(b);}

    @Override
    public void write(byte[] b) throws IOException {dst.write(b);}

    @Override
    public void write(byte[] b, int off, int len) throws IOException {dst.write(b, off, len);}

    @Override
    public void flush() throws IOException {dst.flush();}

    @Override
    public void close() throws IOException {dst.close();}
}
