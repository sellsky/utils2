package tk.bolovsrol.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

/**
 * {@link Writer}-прокси, который все исключения, выкинутые делегатом,
 * дампит в переданный принтстрим.
 */
public class ErrorReportingWriter extends Writer {
    private final Writer w;
    private PrintStream errorPrintStream;
    private boolean fullStackTrace;
    private boolean throwFurther;

    public ErrorReportingWriter(Writer w) {
        this(w, System.err, false, true);
    }

    public ErrorReportingWriter(Object lock, Writer w) {
        this(lock, w, System.err, false, true);
    }

    public ErrorReportingWriter(Writer writer, PrintStream errorPrintStream, boolean fullStackTrace, boolean throwFurther) {
        this.w = writer;
        this.errorPrintStream = errorPrintStream;
        this.fullStackTrace = fullStackTrace;
        this.throwFurther = throwFurther;
    }

    public ErrorReportingWriter(Object lock, Writer w, PrintStream errorPrintStream, boolean fullStackTrace, boolean throwFurther) {
        super(lock);
        this.w = w;
        this.errorPrintStream = errorPrintStream;
        this.fullStackTrace = fullStackTrace;
        this.throwFurther = throwFurther;
    }

    private void print(IOException e) throws IOException {
        if (fullStackTrace) {
            e.printStackTrace(errorPrintStream);
        } else {
            errorPrintStream.print(Spell.get(e));
        }
        if (throwFurther) {
            throw e;
        }
    }

    @Override public void write(char[] cbuf, int off, int len) throws IOException {
        try {
            w.write(cbuf, off, len);
        } catch (IOException e) {
            print(e);
        }
    }

    @Override public void write(int c) throws IOException {
        try {
            w.write(c);
        } catch (IOException e) {
            print(e);
        }
    }

    @Override public void write(char[] cbuf) throws IOException {
        try {
            w.write(cbuf);
        } catch (IOException e) {
            print(e);
        }
    }

    @Override public void write(String str) throws IOException {
        try {
            w.write(str);
        } catch (IOException e) {
            print(e);
        }
    }

    @Override public void write(String str, int off, int len) throws IOException {
        try {
            w.write(str, off, len);
        } catch (IOException e) {
            print(e);
        }
    }


    @Override public void flush() throws IOException {
        try {
            w.flush();
        } catch (IOException e) {
            print(e);
        }
    }

    @Override public void close() throws IOException {
        try {
            w.close();
        } catch (IOException e) {
            print(e);
        }
    }

    public boolean isFullStackTrace() {
        return fullStackTrace;
    }

    public void setFullStackTrace(boolean fullStackTrace) {
        this.fullStackTrace = fullStackTrace;
    }

    public boolean isThrowFurther() {
        return throwFurther;
    }

    public void setThrowFurther(boolean throwFurther) {
        this.throwFurther = throwFurther;
    }

    public PrintStream getErrorPrintStream() {
        return errorPrintStream;
    }

    public void setErrorPrintStream(PrintStream errorPrintStream) {
        this.errorPrintStream = errorPrintStream;
    }
}
