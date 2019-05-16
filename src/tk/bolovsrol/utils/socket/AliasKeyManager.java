package tk.bolovsrol.utils.socket;

import tk.bolovsrol.utils.Spell;

import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class AliasKeyManager implements X509KeyManager {
    private final KeyStore keyStore;

    private final String keyAlias;
    private final String[] keyAliases;
    private final String keyPassword;

    public AliasKeyManager(KeyStore keyStore, String keyAlias, String keyPassword) {
        this.keyStore = keyStore;
        this.keyAlias = keyAlias;
        this.keyAliases = new String[]{keyAlias};
        this.keyPassword = keyPassword;
    }

    /**
     * Gets the one alias set in constructor.
     * Currently,  keyType and issuers are both ignored.
     *
     * @param keyType the type of private key
     * @param issuers the CA certificates we are narrowing
     * @return the client aliases
     */
    @Override public String[] getClientAliases(String keyType, Principal[] issuers) {
        return keyAliases;
    }

    /**
     * Gets the list of server aliases for the SSLServerSockets.
     * Currently,  keyType and issuers are both ignored.
     *
     * @param keyType the type of private key
     * @param issuers the CA certificates we are narrowing
     * @return the server aliases
     */
    @Override public String[] getServerAliases(String keyType, Principal[] issuers) {
        return keyAliases;
    }

    /**
     * Gets the Certificate chain for a particular alias.
     *
     * @param alias the client alias
     * @return the CertificateChain value
     */
    @Override public X509Certificate[] getCertificateChain(String alias) {
        Certificate[] chain;
        try {
            chain = keyStore.getCertificateChain(alias);
        } catch (KeyStoreException kse) {
            throw new IllegalArgumentException("Unable to obtain certificate chain for alias " + Spell.get(alias), kse);
        }
        final X509Certificate[] certChain = new X509Certificate[chain.length];
        for (int i = 0; i < chain.length; i++) {
            certChain[i] = (X509Certificate) chain[i];
        }
        return certChain;
    }

    /**
     * Gets the Private Key for a particular alias.
     *
     * @param alias the client alias
     * @return the PrivateKey
     */
    @Override public PrivateKey getPrivateKey(String alias) {
        try {
            return (PrivateKey) keyStore.getKey(alias, keyPassword == null ? null : keyPassword.toCharArray());
        } catch (GeneralSecurityException gse) {
            throw new IllegalArgumentException("Unable to obtain private key for alias " + Spell.get(alias), gse);
        }
    }

    /**
     * Gets the alias set in constructor.
     *
     * @see javax.net.ssl.X509KeyManager#chooseClientAlias(java.lang.String[],
     * java.security.Principal[], java.net.Socket)
     */
    @Override public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        return keyAlias;
    }

    /**
     * Choose the server alias for the SSLServerSockets.
     * This is not used.
     *
     * @see javax.net.ssl.X509KeyManager#chooseServerAlias(java.lang.String,
     * java.security.Principal[], java.net.Socket)
     */
    @Override public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return keyAlias;
    }

}