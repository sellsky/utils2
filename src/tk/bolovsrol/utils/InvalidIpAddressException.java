package tk.bolovsrol.utils;

/**
 * Выкидывают {@link InternetUtils}, если в результате проверки оказалось,
 * что IP-адрес совсем не адрес.
 */
public class InvalidIpAddressException extends UnexpectedBehaviourException {
    public InvalidIpAddressException(String message) {
        super(message);
    }
}
