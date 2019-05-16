package tk.bolovsrol.utils.log.providers;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.UriParser;
import tk.bolovsrol.utils.box.Box;
import tk.bolovsrol.utils.mail.MailAddress;
import tk.bolovsrol.utils.mail.smtp.SmtpConnection;
import tk.bolovsrol.utils.mail.smtp.SmtpConst;
import tk.bolovsrol.utils.mail.smtp.SmtpWriter;
import tk.bolovsrol.utils.properties.PlainProperties;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SmtpWriterProviderPool implements LogWriterProviderPool {

    private final String scheme;
    private final int defaultPort;

    private final ConcurrentMap<Uri, SmtpWriterProvider> pool = new ConcurrentHashMap<>();

    public SmtpWriterProviderPool(String scheme, int defaultPort) {
        this.scheme = scheme;
        this.defaultPort = defaultPort;
    }

    @Override public LogWriterProvider retrieve(String data) throws StreamProviderException {
        try {
            if (data.startsWith("//")) {
                data = data.substring(2);
            }
            Uri uri = new UriParser().parseWithAuthority(data);

            SmtpWriterProvider cached = pool.get(uri);
            if (cached != null) {
                return cached;
            }

            Uri smtpUri = new Uri();
            smtpUri.setScheme(scheme);
            smtpUri.setUsername(uri.getUsername());
            smtpUri.setPassword(uri.getPassword());
            smtpUri.setHostname(uri.getHostname());
            smtpUri.setPort(uri.getPortIntValue(SmtpConst.DEFAULT_SMTP_PORT));

            PlainProperties qp = uri.queryProperties();
            String helo = qp.get("helo", SmtpConnection.DEFAULT_HELO);
            int timeout = qp.getInteger("timeout", SmtpConnection.DEFAULT_IO_TIMEOUT);
            String subject = qp.get("subject");
            int autoflush = qp.getInteger("autoFlush", SmtpWriter.DEFAULT_AUTOFLUSH);

            MailAddress from = MailAddress.parse(qp.get("from", Box.with(uri.getUsername()).getOr("nobody") + '@' + uri.getHostname()));
            List<MailAddress> to = MailAddress.parseMulti(qp.getOrDie("to"));

            String caption = helo + ' ' + uri.getHostname() + ':' + uri.getPortIntValue(defaultPort);

            SmtpConnection smtpConnection = new SmtpConnection(null, smtpUri, helo, timeout);

            @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "resource"})
            SmtpWriter smtpWriter = new SmtpWriter(smtpConnection, from, to, subject, autoflush, true);

            SmtpWriterProvider result = new SmtpWriterProvider(caption, smtpWriter);

            return Box.with(pool.putIfAbsent(uri, result)).peek(duplicate -> smtpWriter.close()).getOr(result);
        } catch (Exception e) {
            throw new StreamProviderException("Error initializing SMTP Writer with parameters " + Spell.get(data), e);
        }
    }

}
