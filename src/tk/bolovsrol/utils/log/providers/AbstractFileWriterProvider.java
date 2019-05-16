package tk.bolovsrol.utils.log.providers;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Провайдер, записывающий данные в файл.
 * <p/>
 * Провайдер проверяет наличие файла лога, и если файл отсутствует,
 * он закрывает текущий поток и открывает заново с исходным именем,
 * так что лог-файл можно достаточно легко архивировать, например.
 */
abstract class AbstractFileWriterProvider implements LogWriterProvider {

    protected final File outFile;

    private boolean firstWrite = true;
    private Writer writer = null;
    private boolean newWriter;

    protected AbstractFileWriterProvider(File outFile) {
        this.outFile = outFile;
    }

    @Override public String getCaption() {
        return outFile.getName();
    }

    /**
     * Возвращает поток, в который идёт основной вывод лога или null, если основной вывод не ведётся.
     *
     * @return поток или null
     */
    @Override public Writer getWriter() throws IOException {
        if (writer == null) {
            newWriter = !outFile.exists() || firstWrite;
            firstWrite = false;
            return newWriter();
        } else {
            if (outFile.exists()) {
                newWriter = false;
            } else {
                // трюк! кто-то переименовал/удалил файл, в который мы пишем. Так что мы его закроем и откроем заного новый.
                writer.close();
                newWriter = true;
                return newWriter();
            }
        }
        return writer;
    }

    @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
    private Writer newWriter() {
        try {
            writer = new BufferedWriter(new OutputStreamWriter(newOutputStream()));
            return writer;
        } catch (IOException e) {
            System.err.println(
                  "Error accessing logfile " + Spell.get(outFile.toString()) + ", using unsynchronized uncompressed stderr instead. " + Spell.get(e)
            );
            writer = null;
            return new BufferedWriter(new OutputStreamWriter(System.err));
        }
    }

    protected abstract OutputStream newOutputStream() throws IOException;

    @Override public void close() throws IOException {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }

    @Override public boolean isNewWriter() {
        return newWriter;
    }

    @Override public String toString() {
        return new StringDumpBuilder()
              .append("outFile", outFile)
              .append("writer", writer)
              .toString();
    }
}

