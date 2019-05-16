package tk.bolovsrol.utils.socket;

import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.conf.AutoConfiguration;
import tk.bolovsrol.utils.conf.ConditionalFieldMissingException;
import tk.bolovsrol.utils.conf.InvalidConfigurationException;
import tk.bolovsrol.utils.conf.Param;
import tk.bolovsrol.utils.conf.UnusedKeyAction;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * Кастомизированный генератор SSL-контекста.
 * <p>
 * В большинстве случаев конфигурацию можно явно  не задавать, воспользовавшись настройками,
 * указанными в основном конфиге, методом {@link #newSslContext(LogDome, ReadOnlyProperties)},
 * либо загрузив нужные сертификаты из указанного JKS-файла методом {@link #forJks(LogDome, String, String)}.
 * <p>
 * Для тонкой настройки можно задать алиас ключа, который предполагается использовать, и список алиасов,
 * которым система доверяет, изменяя конфиг.
 * <p>
 * Либо, наоборот, можно воспользоваться более простыми генераторами, использующими приятные умолчания
 * либо вообще доверяющие любым сертификатам.
 */
public final class SslContextGenerator {

    public static class DefaultSslContextConf extends AutoConfiguration {
        @Param(key = "protocol", desc = "протокол (SSL или TLS)")
        public String protocol = "TLS";

        @Param(key = "keystore.type", desc = "тип хранилища ключей")
        public String keystoreType = "jks";

        @Param(key = "keystore.filename", desc = "имя файла хранилища ключей")
        public String keystoreFilename = null;

        @Param(key = "keystore.password", desc = "пароль хранилища ключей")
        public String keystorePassword;

        @Param(key = "keyManagerAlgorithm", hidden = true)
        public String keyManagerAlgorithm = "SunX509";

        @Param(key = "trustManagerAlgorithm", hidden = true)
        public String trustManagerAlgorithm = "SunPKIX";

    }

    public static final DefaultSslContextConf DEFAULT_SSL_CONTEXT_CONF;

    static {
        DEFAULT_SSL_CONTEXT_CONF = new DefaultSslContextConf();
        try {
            DEFAULT_SSL_CONTEXT_CONF.load(Log.getInstance(), Cfg.getBranch("ssl."));
        } catch (InvalidConfigurationException e) {
            throw new IllegalArgumentException("Error initializing default SSL context configuration", e);
        }
    }

    public static class SslContextConf extends DefaultSslContextConf {
        @Param(key = "protocol", desc = "протокол", hideDefValue = true)
        public String protocol = DEFAULT_SSL_CONTEXT_CONF.protocol;

        @Param(key = "keystore.type", desc = "тип хранилища ключей", hideDefValue = true)
        public String keystoreType = DEFAULT_SSL_CONTEXT_CONF.keystoreType;

        @Param(key = "keystore.filename", desc = "имя файла хранилища ключей", hideDefValue = true)
        public String keystoreFilename = DEFAULT_SSL_CONTEXT_CONF.keystoreFilename;

        @Param(key = "keystore.password", desc = "пароль хранилища ключей", hideDefValue = true, password = true)
        public String keystorePassword = DEFAULT_SSL_CONTEXT_CONF.keystorePassword;

        public static final String KEY_ALIAS = "key.alias";
        @Param(key = KEY_ALIAS, desc = "алиас секретного ключа")
        public String keyAlias;

        public static final String KEY_PASSWORD = "key.password";
        @Param(key = KEY_PASSWORD, desc = "пароль секретного ключа", password = true)
        public String keyPassword;

        @Param(key = "trust.aliases", desc = "алиасы сертификатов, которым мы доверяем, через запятую")
        public String[] trustAliases;

        @Override public void afterLoad(LogDome log, ReadOnlyProperties cfg, Map<String, String> unusedKeys, UnusedKeyAction unusedKeyAction) throws InvalidConfigurationException {
            super.afterLoad(log, cfg, unusedKeys, unusedKeyAction);

            if (keyAlias != null && keyPassword == null) {
                throw new ConditionalFieldMissingException("key.alias specified, but key.password not specified", KEY_PASSWORD);
            }
            if (keyPassword != null && keyAlias == null) {
                throw new ConditionalFieldMissingException("key.password specified, but key.alias not specified", KEY_ALIAS);
            }
        }
    }

    private SslContextGenerator() {
    }

    /**
     * Возвращает ссл-контекст по умолчанию либо создаёт и конфигурирует
     * новый контекст. В передаваемых пропертях имеют смысл такие параметры:
     * <ul>
     * <li>ssl.protocol: протокол, если не указан или указан «Default», то возвращается контекст по умолчанию, другие параметры не важны;
     * <li>ssl.provider: провайдер контекста, можно не указывать;
     * <li>ssl.keystore.filename: имя файла хранилища ключей, по умолчанию <code>%user.home%/.keystore</code>;
     * <li>ssl.keystore.password: пароль хранилища ключей, по умолчанию отсутствует;
     * <li>ssl.keystore.keyPassword: пароль секретных ключей, по умолчанию совпадает с паролем ssl.keystore.password;
     * <li>ssl.keystore.type: тип хранилища, по умолчанию «jks».
     * </ul>
     *
     * @return коннектор.
     * @throws UnexpectedBehaviourException
     * @see SSLContext#getDefault()
     * @see SSLContext#getInstance(String)
     */
    public static SSLContext newSslContext(LogDome log, ReadOnlyProperties cfg) throws CustomSslContextException, InvalidConfigurationException {
        SslContextConf conf = new SslContextConf();
        conf.load(log, cfg);
        return newSslContext(log, conf);
    }

    public static SSLContext newSslContext(LogDome log, SslContextConf conf) throws CustomSslContextException {
        try {
            if (conf.keyAlias == null && conf.trustAliases == null && conf.keystoreFilename == null) {
                return SSLContext.getDefault();
            }

            KeyStore ks = KeyStore.getInstance(conf.keystoreType);
            try (InputStream fis = new BufferedInputStream(new FileInputStream(conf.keystoreFilename))) {
                ks.load(fis, conf.keystorePassword == null ? null : conf.keystorePassword.toCharArray());
            }

            KeyManager[] keyManagers;
            if (conf.keyAlias != null) {
                keyManagers = new KeyManager[]{new AliasKeyManager(ks, conf.keyAlias, conf.keyPassword)};
            } else {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(DEFAULT_SSL_CONTEXT_CONF.keyManagerAlgorithm);
                kmf.init(ks, DEFAULT_SSL_CONTEXT_CONF.keystorePassword == null ? null : DEFAULT_SSL_CONTEXT_CONF.keystorePassword.toCharArray());
                keyManagers = kmf.getKeyManagers();
            }

            TrustManager[] trustManagers;
            if (conf.trustAliases != null) {
                trustManagers = new TrustManager[]{new AliasTrustManager(log, ks, conf.trustAliases)};
            } else {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(DEFAULT_SSL_CONTEXT_CONF.trustManagerAlgorithm);
                tmf.init(ks);
                trustManagers = tmf.getTrustManagers();
            }

            SSLContext sc = SSLContext.getInstance(conf.protocol);
            sc.init(keyManagers, trustManagers, null);

            return sc;
        } catch (Exception e) {
            throw new CustomSslContextException("Error creating custom SSL Context.", e);
        }
    }

    /**
     * Создаёт контекст, используя сертификаты указанного JKS-хранилища, используя приятные умолчания для прочих настроек.
     *
     * @param log
     * @param jksFileName
     * @param password
     * @return
     * @throws CustomSslContextException
     */
    public static SSLContext forJks(LogDome log, String jksFileName, String password) throws CustomSslContextException {
        SslContextConf conf = new SslContextConf();
        conf.keystoreFilename = jksFileName;
        conf.keystorePassword = password;
        return newSslContext(log, conf);
    }

    /**
     * Создаёт контекст, доверяющий только сертификату, содержащемуся в указанном файле, и без приватных ключей.
     *
     * @param certFilename имя файла с сертификатом
     * @return
     * @throws CustomSslContextException
     */
    public static SSLContext forCert(String certFilename) throws CustomSslContextException {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, null);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert;
            try (FileInputStream fis = new FileInputStream(certFilename)) {
                cert = cf.generateCertificate(fis);
            }
            ks.setCertificateEntry(certFilename, cert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, tmf.getTrustManagers(), null);

            return sc;
        } catch (Exception e) {
            throw new CustomSslContextException("Error creating custom SSL Context.", e);
        }
    }

    private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    }};

    /**
     * Возвращает контекст, доверяющий любому сертификату.
     *
     * @return
     * @throws CustomSslContextException
     */
    public static SSLContext gullible() throws CustomSslContextException {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, TRUST_ALL_CERTS, null);
            return sc;
        } catch (Exception e) {
            throw new CustomSslContextException("Error creating custom SSL Context.", e);
        }
    }

}
