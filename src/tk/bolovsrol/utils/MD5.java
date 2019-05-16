package tk.bolovsrol.utils;

import java.security.MessageDigest;

/** Считалка MD5 */
public final class MD5 {

    private MD5() {
    }

    private static final MessageDigest MD_5;

    static {
        try {
            MD_5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            // shoud never get here
            throw new RuntimeException(e);
        }
    }

    /**
     * Возвращает дамп MD5 для строки.
     * <p/>
     * Цифры 0123456789abcdef.
     *
     * @param string
     * @return дамп MD_5
     */
    public static String getHexDumpFor(String string) {
        return StringUtils.getHexDump(getFor(string.getBytes()));
    }

    /**
     * Computes the MD5 fingerprint of a string.
     *
     * @return the MD5 digest of the input <code>String</code>
     */
    public static synchronized byte[] getFor(byte[] array) {
        return MD_5.digest(array);
    }
}
