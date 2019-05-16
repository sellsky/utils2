package tk.bolovsrol.utils.xml;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class AbstractXmlPrinter {

    private Charset charset;

    protected AbstractXmlPrinter() {
        this(StandardCharsets.UTF_8);
    }

    protected AbstractXmlPrinter(Charset charset) {
        this.charset = charset;
    }

    protected AbstractXmlPrinter(String charsetName) {
        this(Charset.forName(charsetName));
    }

    public byte[] toBytes(Element element) {
        return toBytes(element, charset);
    }

    /** @deprecated  */
    @Deprecated
    public byte[] toBytes(Element element, String forceEncoding) throws UnsupportedEncodingException {
        return toBytes(element, Charset.forName(forceEncoding));
    }

    protected byte[] toBytes(Element element, Charset charset) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"").append(charset.name()).append("\"?>");
        appendXmlTo(sb, element);
        return XmlUtils.encodeNumericCharacterReferences(sb.toString(), charset).getBytes(charset);
    }

    /**
     * Возвращает xml-представление элемента (и всех его детей).
     *
     * @return текст-xml
     */
    public String toXmlString(Element element) {
        StringBuilder sb = new StringBuilder(256);
        appendXmlTo(sb, element);
        return sb.toString();
    }

    /**
     * Добавляет xml-представление элемента (и всех его детей) к стрингбуфферу.
     *
     * @param sb
     * @param element
     */
    public abstract void appendXmlTo(StringBuilder sb, Element element);


    public void setCharset(String charsetName) {
        setCharset(Charset.forName(charsetName));
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }
}