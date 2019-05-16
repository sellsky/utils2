package tk.bolovsrol.utils.xml;

import tk.bolovsrol.utils.StringUtils;

import java.nio.charset.Charset;
import java.util.Map;

/** Печататель элементов с отступами перед каждым элементом. */
public class IndentXmlPrinter extends AbstractXmlPrinter {

    private String indentTile = "\t";
    private boolean textDataOnSameLine = true;

    private int level;
    private StringBuilder sb;

    public IndentXmlPrinter() {
    }

    public IndentXmlPrinter(String indentTile) {
        this.indentTile = indentTile;
    }

    @Override
    public byte[] toBytes(Element element, Charset charset) {
        level = 0;
        return super.toBytes(element, charset);
    }

    @Override
    public String toXmlString(Element element) {
        level = 0;
        return super.toXmlString(element);
    }

    private void appendIndent() {
        for (int i = level; i > 0; i--) {
            sb.append(indentTile);
        }
    }

    private void newLine() {
        sb.append('\n');
    }

    /** Добавляет xml-представление элемента (и всех его детей) к стрингбуфферу. */
    @Override
    public void appendXmlTo(StringBuilder sb, Element element) {
        this.sb = sb;
        if (element instanceof TextData) {
            appendIndent();
            TextData td = (TextData) element;
            writeTextData(td);
            newLine();
        } else {
            appendIndent();
            this.sb.append('<').append(element.getName());
            {
                for (Map.Entry<String, String> attr : element.attributesMap().entrySet()) {
                    this.sb.append(' ').append(attr.getKey()).append("=\"").append(XmlUtils.xmlInvalidate(attr.getValue())).append('"');
                }
            }
            {
                if (!element.hasChildren()) {
                    this.sb.append("/>");
                    newLine();
                } else {
                    this.sb.append('>');
                    if (element.getChildrenCount() == 1 && element.getFirstChild() instanceof TextData) {
                        // спецслучай
                        if (!textDataOnSameLine) {
                            newLine();
                            level++;
                            appendIndent();
                        }
                        writeTextData(((TextData) element.getFirstChild()));
                        if (!textDataOnSameLine) {
                            newLine();
                            level--;
                            appendIndent();
                        }
                    } else {
                        newLine();
                        level++;
                        Element[] childrenEls = element.getChildren();
                        for (Element childrenEl : childrenEls) {
                            appendXmlTo(sb, childrenEl);
                        }
                        level--;
                        appendIndent();
                    }
                    this.sb.append("</").append(element.getName()).append('>');
                    newLine();
                }
            }
        }
    }

    private void writeTextData(TextData td) {
        switch (td.getType()) {
            case CDATA:
                sb.append("<![CDATA[")
                        .append(StringUtils.substitute(td.getValue(), "]]", "]]]><![CDATA[]"))
                        .append("]]>");
                break;
            case TEXT:
                sb.append(XmlUtils.xmlInvalidate(td.getValue()));
                break;
        }
    }

    public String getIndentTile() {
        return indentTile;
    }

    public void setIndentTile(String indentTile) {
        this.indentTile = indentTile;
    }

    public boolean isTextDataOnSameLine() {
        return textDataOnSameLine;
    }

    public void setTextDataOnSameLine(boolean textDataOnSameLine) {
        this.textDataOnSameLine = textDataOnSameLine;
    }
}