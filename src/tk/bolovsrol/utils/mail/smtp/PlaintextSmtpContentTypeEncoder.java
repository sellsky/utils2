package tk.bolovsrol.utils.mail.smtp;

import tk.bolovsrol.utils.reflectiondump.ReflectionDump;

import java.nio.charset.Charset;

public class PlaintextSmtpContentTypeEncoder implements SmtpContentTypeEncoder {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    public static final String PLAIN_CONTENT_SUBTYPE = "plain";
    public static final String HTML_CONTENT_SUBTYPE = "html";
    public static final String XML_CONTENT_SUBTYPE = "xml";

    private Charset charset = DEFAULT_CHARSET;
    private String contentSubtype = PLAIN_CONTENT_SUBTYPE;

    public PlaintextSmtpContentTypeEncoder() {
    }

    public PlaintextSmtpContentTypeEncoder(String contentSubtype) {
        this.contentSubtype = contentSubtype;
    }

    public PlaintextSmtpContentTypeEncoder(Charset charset) {
        this.charset = charset;
    }

    public PlaintextSmtpContentTypeEncoder(Charset charset, String contentSubtype) {
        this.charset = charset;
        this.contentSubtype = contentSubtype;
    }

    @Override
    public byte[] encode(String payload) {
        return payload.getBytes(charset);
    }

    @Override
    public String getContentType() {
        return "text/" + contentSubtype + "; charset=" + charset.name();
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String getContentSubtype() {
        return contentSubtype;
    }

    public void setContentSubtype(String contentSubtype) {
        this.contentSubtype = contentSubtype;
    }

    @Override public String toString() {
        return ReflectionDump.getFor(this);
    }

}
