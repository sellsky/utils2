package tk.bolovsrol.utils.socket;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.log.LogLevel;

import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

public class AliasTrustManager implements X509TrustManager {

    private final LogDome log;
    private final KeyStore keyStore;
    private final String[] trustedAliases;

    public AliasTrustManager(LogDome log, KeyStore keyStore, String[] trustedAliases) {
        this.log = log;
        this.keyStore = keyStore;
        this.trustedAliases = trustedAliases;
    }

    public void checkTrusted(X509Certificate[] x509Certificates) throws CertificateException {
        for (X509Certificate certificate : x509Certificates) {
            certificate.checkValidity();
            for (X509Certificate trustedCertificate : getTrustedCertificates(log)) {
                if (certificate.equals(trustedCertificate)) {
                    log.info("Found matching trusted certificate " + Spell.get(trustedCertificate.getSubjectDN()));
                    return;
                }
            }
        }
        throw new CertificateException("No trusted certificate found.");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        checkTrusted(x509Certificates);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        checkTrusted(x509Certificates);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return getTrustedCertificates(log);
    }

    private X509Certificate[] getTrustedCertificates(LogDome log) {
        ArrayList<X509Certificate> trustedCertificates = new ArrayList<>(trustedAliases.length);
        for (String trustedAlias : trustedAliases) {
            X509Certificate certificate;
            try {
                certificate = (X509Certificate) keyStore.getCertificate(trustedAlias);
            } catch (KeyStoreException e) {
                log.warning("Unable to fetch trusted certificate by alias " + Spell.get(trustedAlias) + ", alias ignored", e);
                continue;
            }
            try {
                certificate.checkValidity();
                trustedCertificates.add(certificate);
            } catch (CertificateExpiredException e) {
                log.warning("Ignored expired Trusted Certificate " + Spell.get(trustedAlias) + ", expiration date " + Spell.get(certificate.getNotAfter()), e);
            } catch (CertificateNotYetValidException e) {
                // нормально добавить сертификат на будущее
                if (log.isAllowed(LogLevel.INFO)) {
                    log.info("Ignored not yet valid Trusted Certificate " + Spell.get(trustedAlias) + ", activation date" + Spell.get(certificate.getNotBefore()), e);
                }
            } catch (Exception e) {
                log.warning("Ignored not valid Trusted Certificate " + Spell.get(trustedAlias), e);
            }
        }
        return trustedCertificates.toArray(new X509Certificate[trustedCertificates.size()]);
    }
}
