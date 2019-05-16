package tk.bolovsrol.utils.mail.smtp;

import tk.bolovsrol.utils.Base64;

/** Кодирует текст в Base64. */
public class Base64ContentTransferEncoder implements SmtpContentTransferEncoder {

    public static final int DEFAULT_LINE_LENGTH = 80;

    private int lineLength = DEFAULT_LINE_LENGTH;

    public Base64ContentTransferEncoder() {
    }

    @Override
    public String encode(byte[] message) {
        return Base64.byteArrayToBase64(message, lineLength);
    }

    @Override public String getContentTransferEncoding() {
        return "base64";
    }

    public int getLineLength() {
        return lineLength;
    }

    public void setLineLength(int lineLength) {
        this.lineLength = lineLength;
    }

    @Override
    public String toString() {
        return getContentTransferEncoding();
    }
}
