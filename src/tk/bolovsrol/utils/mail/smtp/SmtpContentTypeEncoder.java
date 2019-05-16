package tk.bolovsrol.utils.mail.smtp;

public interface SmtpContentTypeEncoder {

    byte[] encode(String payload);

    String getContentType();
}
