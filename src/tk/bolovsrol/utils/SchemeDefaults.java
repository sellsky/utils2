package tk.bolovsrol.utils;

import tk.bolovsrol.utils.http.HttpConst;
import tk.bolovsrol.utils.mail.smtp.SmtpConst;
import tk.bolovsrol.utils.socket.client.PlainSocketFactory;
import tk.bolovsrol.utils.socket.client.SocketFactory;
import tk.bolovsrol.utils.socket.client.SslSocketFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Порты и генераторы сокетов для известных протоколов.
 */
public final class SchemeDefaults {

    public static final Map<String, SocketFactory> SOCKET_FACTORIES = new HashMap<>();
    public static final Map<String, Integer> PORTS = new HashMap<>();

    static {
        SOCKET_FACTORIES.put("http", PlainSocketFactory.getStatic());
        PORTS.put("http", HttpConst.DEFAULT_PORT);
        SOCKET_FACTORIES.put("https", SslSocketFactory.getDefault());
        PORTS.put("https", HttpConst.DEFAULT_SSL_PORT);

        SOCKET_FACTORIES.put("smtp", PlainSocketFactory.getStatic());
        PORTS.put("smtp", SmtpConst.DEFAULT_SMTP_PORT);
        SOCKET_FACTORIES.put("smtps", SslSocketFactory.getDefault());
        PORTS.put("smtps", SmtpConst.DEFAULT_SMTPS_PORT);

        //  сюда добавлять
    }

    private SchemeDefaults() {
    }
}
