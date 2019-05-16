package tk.bolovsrol.utils.log.providers;

import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.mail.smtp.SmtpWriter;

import java.io.IOException;
import java.io.Writer;

/**
 * Отправляет лог по почте.
 * <p/>
 * Никогда не бывает новым.
 */
public class SmtpWriterProvider implements LogWriterProvider {

    private final String caption;
    private final SmtpWriter smtpWriter;

    public SmtpWriterProvider(String caption, SmtpWriter smtpWriter) {
        this.caption = caption;
        this.smtpWriter = smtpWriter;
    }

    @Override public String getCaption() {
        return caption;
    }

    @Override public Writer getWriter() {
        return smtpWriter;
    }

    @Override public boolean isNewWriter() {
        return false;
    }

    @Override public void close() throws IOException {
        smtpWriter.close();
    }

    @Override public String toString() {
        return new StringDumpBuilder()
              .append("caption", caption)
              .append("smtpWriter", smtpWriter)
              .toString();
    }

}
