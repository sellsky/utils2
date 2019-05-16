package tk.bolovsrol.utils.xml.soap;

public class SoapConnectionException extends Exception {
    public SoapConnectionException(String message) {
        super(message);
    }

    public SoapConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SoapConnectionException(Throwable cause) {
        super(cause);
    }
}
