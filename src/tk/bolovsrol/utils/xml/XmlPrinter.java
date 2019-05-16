package tk.bolovsrol.utils.xml;

import tk.bolovsrol.utils.StringUtils;

import java.nio.charset.Charset;
import java.util.Map;

public class XmlPrinter extends AbstractXmlPrinter {

    public XmlPrinter() {
    }

    public XmlPrinter(Charset charset) {
        super(charset);
    }

    public XmlPrinter(String charsetName) {
        super(charsetName);
    }

    /**
     * Добавляет xml-представление элемента (и всех его детей) к стрингбуфферу.
     *
     * @param sb
     * @param element
     */
    @Override
    public void appendXmlTo(StringBuilder sb, Element element) {
        if (element instanceof TextData) {
            TextData td = (TextData) element;
            switch (td.getType()) {
                case CDATA:
                    sb.append("<![CDATA[")
                            .append(StringUtils.substitute(td.getValue(), "]]", "]]]><![CDATA[]"))
                            .append("]]>");
                    break;
                case TEXT:
                    sb.append(XmlUtils.xmlInvalidate(((TextData) element).getValue()));
                    break;
            }
        } else {
            sb.append('<').append(element.getName());
            {
                for (Map.Entry<String, String> attr : element.attributesMap().entrySet()) {
                    sb.append(' ').append(attr.getKey()).append("=\"").append(XmlUtils.xmlInvalidate(attr.getValue())).append('"');
                }
            }
            {
                if (!element.hasChildren()) {
                    sb.append("/>");
                } else {
                    sb.append('>');
                    Element[] childrenEls = element.getChildren();
                    for (Element childrenEl : childrenEls) {
                        appendXmlTo(sb, childrenEl);
                    }
                    sb.append("</").append(element.getName()).append('>');
                }
            }
        }
    }
}
