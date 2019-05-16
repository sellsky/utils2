package tk.bolovsrol.utils.mail.smtp;

public class SmtpException extends Exception {
    public SmtpException(String message) {
        super(message);
    }

    public SmtpException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmtpException(Throwable cause) {
        super(cause);
    }
}
