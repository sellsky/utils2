package tk.bolovsrol.utils.mail.smtp;

/**
 *
 */
public interface SmtpContentTransferEncoder {

    String encode(byte[] message);

    String getContentTransferEncoding();

}
