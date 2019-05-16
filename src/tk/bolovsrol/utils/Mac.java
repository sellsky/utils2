package tk.bolovsrol.utils;

import javax.crypto.spec.SecretKeySpec;

/**
 * Считалка Mac-ов всяких.
 * <p/>
 * Хороша для редких одноразовых подсчётов.
 */
public final class Mac {
    private Mac() {}

    public static byte[] getHMacSHA1(byte[] key, byte[] data) throws UnexpectedBehaviourException {
        return getMac("HMacSHA1", key, data);
    }

    public static byte[] getHMacMD5(byte[] key, byte[] data) throws UnexpectedBehaviourException {
        return getMac("HMacMD5", key, data);
    }

    private static byte[] getMac(String algorithm, byte[] key, byte[] data) throws UnexpectedBehaviourException {
        try {
            javax.crypto.Mac hm = javax.crypto.Mac.getInstance(algorithm);
            hm.init(new SecretKeySpec(key, 0, key.length, algorithm));
            return hm.doFinal(data);
        }
        catch (Exception e) {
            throw new UnexpectedBehaviourException(e);
        }
    }

}
