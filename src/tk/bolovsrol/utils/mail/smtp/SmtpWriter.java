package tk.bolovsrol.utils.mail.smtp;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.mail.MailAddress;
import tk.bolovsrol.utils.properties.Cfg;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

public class SmtpWriter extends Writer {

	public static final int DEFAULT_AUTOFLUSH = Cfg.getInteger("smtp.writer.autoflush", 65536, Log.getInstance());

    private final SmtpConnection sc;
    private final MailAddress from;
    private final Collection<MailAddress> to;
    private final String subject;
    private final int autoFlushLength;
    private boolean autoflushAtLineEnd = false;

    private final StringBuilder buf = new StringBuilder(2048);

    public SmtpWriter(
          SmtpConnection sc,
          MailAddress from,
          Collection<MailAddress> to,
          String subject,
          int autoFlushLength,
          boolean autoflushAtLineEnd
    ) {
        this.sc = sc;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.autoFlushLength = autoFlushLength;
        this.autoflushAtLineEnd = autoflushAtLineEnd;
    }

    public SmtpWriter(
          Object lock,
          SmtpConnection sc,
          MailAddress from,
          Collection<MailAddress> to,
          String subject,
          int autoFlushLength,
          boolean autoflushAtLineEnd
    ) {
        super(lock);
        this.sc = sc;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.autoFlushLength = autoFlushLength;
        this.autoflushAtLineEnd = autoflushAtLineEnd;
    }

    @Override public void write(char[] cbuf, int off, int len) throws IOException {
        buf.append(cbuf, off, len);
        flushIfFull();
    }

    @Override public void write(int c) throws IOException {
        buf.append((char) c);
        flushIfFull();
    }

    @Override public void write(char[] cbuf) throws IOException {
        buf.append(cbuf);
        flushIfFull();
    }

    @Override public void write(String str) throws IOException {
        buf.append(str);
        flushIfFull();
    }

    @Override public void write(String str, int off, int len) throws IOException {
        buf.append(str, off, len);
        flushIfFull();
    }

    private void flushIfFull() throws IOException {
        if (buf.length() >= autoFlushLength && (!autoflushAtLineEnd || buf.charAt(buf.length() - 1) == '\n')) {
            flush();
        }
    }

    @Override public void flush() throws IOException {
        if (buf.length() != 0) {
            SmtpMessage sm = new SmtpMessage();
            sm.setPayload(buf.toString());
            sm.setSubject(subject);
            sm.setFrom(from);
            sm.addTo(to);
            try {
                try {
                    sc.open();
                    sc.sendMessage(sm);
                } finally {
                    sc.close();
                }
            } catch (SmtpResponseException e) {
                throw new IOException("SMTP Server reports an error, buffer contents " + Spell.get(buf), e);
            } catch (LoginFailedException e) {
                throw new IOException("Error logging in to SMTP Server, buffer contents " + Spell.get(buf), e);
            } finally {
                buf.setLength(0);
            }
        }
    }

    @Override public void close() throws IOException {
        flush();
    }

    @Override public String toString() {
        return new StringDumpBuilder()
              .append("sc", sc)
              .append("from", from)
              .append("to", to)
              .append("subject", subject)
              .append("autoFlushLength", autoFlushLength)
              .append("autoflushAtLineEnd", autoflushAtLineEnd)
              .toString();
    }
}
