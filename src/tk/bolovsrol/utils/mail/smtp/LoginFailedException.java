package tk.bolovsrol.utils.mail.smtp;

/** Логин не удался. */
public class LoginFailedException extends SmtpException {
    public LoginFailedException(String message) {
        super(message);
    }

    public LoginFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginFailedException(Throwable cause) {
        super(cause);
    }
}
