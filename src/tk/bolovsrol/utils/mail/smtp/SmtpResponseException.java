package tk.bolovsrol.utils.mail.smtp;

public class SmtpResponseException extends SmtpException {
    private final String code;

    public SmtpResponseException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
