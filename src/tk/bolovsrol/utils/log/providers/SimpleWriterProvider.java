package tk.bolovsrol.utils.log.providers;

import tk.bolovsrol.utils.StringDumpBuilder;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;

/** Вывод в переданный поток. Никакого управления потоком не производится. */
class SimpleWriterProvider implements LogWriterProvider {

    private boolean beforeFirstTime = true;
    private boolean firstTime = false;
    private final String caption;
    private final Writer writer;

    public SimpleWriterProvider(String caption, PrintStream stream) {
        this.caption = caption;
        //noinspection IOResourceOpenedButNotSafelyClosed
        this.writer = new BufferedWriter(new OutputStreamWriter(stream));
    }

    @Override public String getCaption() {
        return caption;
    }

    /**
     * Возвращает поток, в который идёт основной вывод лога или null, если основной вывод не ведётся.
     *
     * @return поток или null
     */
    @Override public Writer getWriter() {
        if (beforeFirstTime) {
            beforeFirstTime = false;
            firstTime = true;
        } else {
            firstTime = false;
        }
        return writer;
    }

    @Override public void close() {
        // nothing to do
    }

    @Override public boolean isNewWriter() {
        return firstTime;
    }

    @Override public String toString() {
        return new StringDumpBuilder()
              .append("caption", caption)
              .append("writer", writer)
              .toString();
    }
}
