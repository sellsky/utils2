package tk.bolovsrol.utils.xml;

import tk.bolovsrol.utils.ArrayUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.http.HttpRequest;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Парсит массив символов в Element с дитями.
 * <p>
 * Умеет понимать атрибуты, вложенные элементы и простые символьные данные.
 * <p>
 * Xml-директивы в начале буфера -- игнорирует!
 */
public class XmlParser {

    private static final char[] CDATA_HEADER = "![CDATA[".toCharArray(); // без начального "<".
    private static final char[] CDATA_DELIMITER = "]]><![CDATA[".toCharArray();
    private static final char[] CDATA_TERMINATOR = "]]>".toCharArray();
    private static final char[] COMMENT_HEADER = "!--".toCharArray(); // без начального "<"
    private static final char[] COMMENT_TERMINATOR = "-->".toCharArray();

    private char[] buf;
    private int pos;
    private boolean trimWhitespaces = false;

    private Charset defaultCharset;

    /**
     * Создаёт новый парсер, который умеет парсить всякое.
     * <p>
     * Парсер не синхронизирован, нельзя одним парсером одновременно парсить более одного буфера.
     */
    public XmlParser() {
        this(StandardCharsets.UTF_8);
    }

    /**
     * Создаёт новый парсер, который умеет парсить всякое.
     * <p>
     * Парсер не синхронизирован, нельзя одним парсером одновременно парсить более одного буфера.
     */
    public XmlParser(Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    /**
     * Парсит содержимое буфера от начала и до завершения первого элемента.
     *
     * @param buf буфер
     * @return корневой элемент
     * @throws ElementParsingException ошибка парсинга
     */
    public Element parse(byte[] buf) throws ElementParsingException {
        return parse(decode(buf));
    }

    /**
     * Парсит содержимое буфера от начала и до завершения первого элемента.
     *
     * @param buf буфер
     * @param defaultEncoding кодировка по умолчанию
     * @return корневой элемент
     * @throws ElementParsingException ошибка парсинга
     */
    public Element parse(byte[] buf, String defaultEncoding) throws ElementParsingException, UnsupportedEncodingException {
        return parse(decode(buf, defaultEncoding));
    }

    /**
     * Парсит тело хттп-запроса в качестве документа.
     * Проверяет, что у документа Content-Type <code>text/xml</code>.
     *
     * @param httpRequest запрос с xml-документом.
     * @return корневой элемент
     * @throws UnexpectedBehaviourException
     * @throws ElementParsingException
     * @throws UnsupportedEncodingException
     */
    public Element parse(HttpRequest httpRequest) throws UnexpectedBehaviourException, ElementParsingException, UnsupportedEncodingException {
        String contentTypePlain = httpRequest.getContentTypeMime();
        if (contentTypePlain == null
            || !(contentTypePlain.equals("text/xml")
            || contentTypePlain.equals("application/xml")
            || (contentTypePlain.startsWith("application/") && contentTypePlain.endsWith("+xml")))) {
            throw new UnexpectedBehaviourException("Unexpected Content Type " + Spell.get(contentTypePlain));
        }
        return parse(httpRequest.getBody(), httpRequest.getContentTypeCharset());
    }

    /**
     * Парсит содержимое последовательности от начала и до завершения первого элемента.
     *
     * @param seq буфер
     * @return корневой элемент
     * @throws ElementParsingException ошибка парсинга
     */
    public Element parse(CharSequence seq) throws ElementParsingException {
        if (seq == null) {
            throw new ElementParsingException("No data");
        }
        return this.parse(seq.toString().toCharArray());
    }

    /**
     * Парсит содержимое буфера от начала и до завершения первого элемента.
     *
     * @param buf буфер
     * @return корневой элемент
     * @throws ElementParsingException ошибка парсинга
     */
    public Element parse(char[] buf) throws ElementParsingException {
        return parse(buf, 0);
    }

    /**
     * Парсит содержимое буфера от from и до завершения первого элемента.
     *
     * @param buf буфер
     * @return корневой элемент
     * @throws ElementParsingException ошибка парсинга
     */
    public Element parse(char[] buf, int from) throws ElementParsingException {
        if (buf == null) {
            throw new ElementParsingException("No data");
        }
        this.buf = buf;
        this.pos = from;

        checkBounds();
        // пропускаем всякие директивы
        // в принципе, если они понадобятся, можно сделать отдельный объектик, который бы
        // хранил директивы и корневой элемент.
        while (true) {
            skipWhitespace();
            if (buf[pos] != '<') {
                throwException("prespace", "[<]");
            }
            nextPos();
            if (buf[pos] == '?') {
                skipUntilChar('>');
                nextPos();
            } else if (buf[pos] == '!' && checkIfText(COMMENT_HEADER)) {
                pos += COMMENT_HEADER.length;
                skipComment();
            } else {
                return parseInternal();
            }
        }
    }

    private Element parseInternal() throws ElementParsingException {
        checkBounds();
        Element el = new Element(getName());

        // атрибуты
        skipWhitespace();
        while (Character.isLetter(buf[pos])) {
            String attrName = getName();
            skipWhitespace();
            if (buf[pos] != '=') {
                throwException("attribute definition", "[=]");
            }
            nextPos();
            skipWhitespace();
            char attributeQuote = ' ';
            if (buf[pos] == '"') {
                attributeQuote = '\"';
            } else if (buf[pos] == '\'') {
                attributeQuote = '\'';
            } else {
                throwException("attribute definition", "[\"] or [']");
            }
            nextPos();
            el.setAttribute(attrName, getAttributeValue(attributeQuote));

            nextPos();
            skipWhitespace();
        }

        // бездетный эелемент
        if (buf[pos] == '/') {
            nextPos();
            if (buf[pos] != '>') {
                throwException("element definition", "[>]");
            }
            pos++;
            return el;
        }

        // элемент с дитями
        if (buf[pos] != '>') {
            throwException("element definition", "[>]");
        }

        // дети и cdata
        nextPos();
        while (true) {
            if (buf[pos] != '<') {
                String textData = getTextData();
                if (trimWhitespaces) {
                    textData = StringUtils.trim(textData, StringUtils.WHITESPACE_FILTER, StringUtils.TrimMode.BOTH);
                    if (!textData.isEmpty()) {
                        el.addChild(new TextData(XmlUtils.xmlDeinvalidate(textData), TextData.Type.TEXT));
                    }
                } else {
                    el.addChild(new TextData(XmlUtils.xmlDeinvalidate(textData), TextData.Type.TEXT));
                }
                continue;
            }

            nextPos();

            // возможно, это «<![CDATA[»
            if (buf[pos] == '!') {
                if (checkIfText(CDATA_HEADER)) {
                    pos += CDATA_HEADER.length; // проверять границу тут не надо
                    el.addChild(new TextData(getCData(), TextData.Type.CDATA));
                    continue;
                }
                if (checkIfText(COMMENT_HEADER)) {
                    pos += COMMENT_HEADER.length;
                    skipComment();
                    continue;
                }
            }

            if (buf[pos] == '/') {
                // это не новый элемент. это нынешний закрылся. наверное
                nextPos();
                String closingName = getName();
                if (!el.getName().equals(closingName)) {
                    throw new ElementParsingException("Element " + Spell.get(closingName) + " is closed at pos " + pos + " near " + Spell.get(getDiagnosticSubstring()) + " while inner element " + Spell.get(el.getName()) + " remains open.");
                }
                if (buf[pos] != '>') {
                    throwException("element definition", "[>]");
                }
                pos++;
                // Небольшой финт ушами. Если у нас оказался пустой элемент типа <tag></tag>,
                // то считаем, что в нём спрятан текст нулевой длины.
                if (!trimWhitespaces && !el.hasChildren()) {
                    el.addChild(new TextData("", TextData.Type.TEXT));
                }
                return el;
            }

            Element child = parseInternal();
            el.addChild(child);
            try {
                checkBounds();
            } catch (ElementParsingException e) {
                throw new ElementParsingException("Closing tag for element " + Spell.get(el.getName()) + " is missing at the end of document near " + Spell.get(getDiagnosticSubstring()) + '.', e);
            }
        }
    }

    // вызывать, когда pos на первом символе. вернёт строку из символов до ближайшего разделителя
    private String getName() throws ElementParsingException {
        int mark = pos;
        skipCharacters();
        return String.valueOf(buf, mark, pos - mark);
    }

    private String getCData() throws ElementParsingException {
        StringBuilder sb = new StringBuilder(64);
        int mark = pos;
        while (true) {
            skipUntilChar(']');
            if (checkIfText(CDATA_DELIMITER)) {
                sb.append(String.valueOf(buf, mark, pos - mark));
                pos += CDATA_DELIMITER.length;
                mark = pos;
            } else if (checkIfText(CDATA_TERMINATOR)) {
                sb.append(String.valueOf(buf, mark, pos - mark));
                pos += CDATA_TERMINATOR.length;
                return sb.toString();
            }
            nextPos();
        }
    }

    private boolean checkIfText(char[] sample) {
        int u = sample.length;
        int i = pos + u;
        if (i >= buf.length) {
            return false;
        }
        do {
            u--;
            i--;
            if (buf[i] != sample[u]) {
                return false;
            }
        } while (u > 0);
        return true;
    }

    private String getTextData() throws ElementParsingException {
        int mark = pos;
        skipUntilChar('<');
        return String.valueOf(buf, mark, pos - mark);
    }

    private String getAttributeValue(char attributeQuote) throws ElementParsingException {
        int mark = pos;
        skipUntilQuote(attributeQuote);
        return XmlUtils.xmlDeinvalidate(String.valueOf(buf, mark, pos - mark));
    }

    private void checkBounds() throws ElementParsingException {
        if (pos >= buf.length) {
            throw new ElementParsingException("Out of data.");
        }
    }

    private void skipComment() throws ElementParsingException {
        while (true) {
            skipUntilChar('-');
            if (checkIfText(COMMENT_TERMINATOR)) {
                pos += COMMENT_TERMINATOR.length;
                break;
            }
            nextPos();
        }
    }

    private void skipCharacters() throws ElementParsingException {
        while (true) {
            char ch = buf[pos];
            if (!(Character.isLetterOrDigit(ch) || ch == '-' || ch == '_' || ch == ':')) {
                return;
            }
            nextPos();
        }
    }

    private void skipUntilQuote(char attributeQuote) throws ElementParsingException {
        while (buf[pos] != attributeQuote) {
            nextPos();
        }
    }

    private void nextPos() throws ElementParsingException {
        ++pos;
        checkBounds();
    }

    private void skipUntilChar(char ch) throws ElementParsingException {
        while (buf[pos] != ch) {
            nextPos();
        }
    }

    private void skipWhitespace() throws ElementParsingException {
        while (Character.isWhitespace(buf[pos])) {
            nextPos();
        }
    }

    private void throwException(String where, String shouldBe) throws ElementParsingException {
        throw new ElementParsingException("Unexpected char in " + where + " at pos " + pos + ": [" + buf[pos] + "] near " + Spell.get(getDiagnosticSubstring()) + ", should be " + shouldBe);
    }

    /**
     * Возвращает режим обрезания пробелов.
     *
     * @return true, если включен, иначе false
     */
    public boolean isTrimWhitespaces() {
        return trimWhitespaces;
    }

    /**
     * Устанавливает режим обрезания пробелов.
     * <p>
     * Если режим выключен, то все текстовые данные будут сохранены как они представлены
     * в исходном документе, включая пограничные пробелы. Также к пустому элементу, записанному
     * в виде &lt;tag&gt;&lt;/tag&gt; будет добавлена пустая текстовая строка.
     * В результате напечатанный стандартным принтером ({@link XmlPrinter}) элемент будет выглядеть
     * точь-в-точь как исходный документ.
     * <p>
     * Если режим включён, все пустые текстовые данные будут проигнорированы.
     * <p>
     * По умолчанию режим выключен.
     *
     * @param trimWhitespaces true включить, false выключить
     * @return this
     */
    public XmlParser setTrimWhitespaces(boolean trimWhitespaces) {
        this.trimWhitespaces = trimWhitespaces;
        return this;
    }

    /**
     * Находит декларацию и вытаскивает из неё кодировку и возвращает декодированную строку.
     * <p>
     * Если декларации нет или в ней кодировка не указана, подразумевает UTF-8.
     *
     * @param buf
     * @return декодированный исходный документ.
     */
    public String decode(byte[] buf) throws ElementParsingException {
        return decode(buf, defaultCharset);
    }

    /**
     * Находит декларацию и вытаскивает из неё кодировку и возвращает декодированную строку.
     * <p>
     * Если декларации нет или в ней кодировка не указана, подразумевает UTF-8.
     *
     * @param buf
     * @return декодированный исходный документ.
     */
    private String decode(byte[] buf, String charsetName) throws ElementParsingException {
        return decode(buf, charsetName == null ? null : Charset.forName(charsetName));
    }

    /**
     * Находит декларацию и вытаскивает из неё кодировку и возвращает декодированную строку.
     * <p>
     * Если декларации нет или в ней кодировка не указана, подразумевает {@code defaultEncoding}.
     *
     * @param buf
     * @return декодированный исходный документ.
     */
    private String decode(byte[] buf, Charset defaultCharset) throws ElementParsingException {
        Charset documentCharset = retrieveCharset(buf);
        Charset charset = documentCharset != null ? documentCharset : defaultCharset != null ? defaultCharset : this.defaultCharset;
        return XmlUtils.decodeNumericCharacterReferences(new String(buf, charset));
    }

    /**
     * Вытаскивает название кодировки документа.
     * Если ничего такого в документе не нашлось, возвращает null.
     *
     * @param buf
     * @return кодировка документа.
     */
    public static Charset retrieveCharset(byte[] buf) throws ElementParsingException {
        if (buf == null) {
            return null;
        }
        int from = ArrayUtils.indexOf(buf, new byte[]{(byte) '<', (byte) '?', (byte) 'x', (byte) 'm', (byte) 'l'}, 0);
        if (from < 0) {
            return null;
        }
        int first = ArrayUtils.indexOf(buf, new byte[]{(byte) '<'}, 0);
        if (from != first) {
            return null;
        }
        int to = ArrayUtils.indexOf(buf, new byte[]{(byte) '?', (byte) '>'}, from);
        if (to < 0) {
            return null;
        }
        int encodingFrom = ArrayUtils.indexOf(buf, new byte[]{(byte) ' ', (byte) 'e', (byte) 'n', (byte) 'c', (byte) 'o', (byte) 'd', (byte) 'i', (byte) 'n', (byte) 'g'}, from, to);
        if (encodingFrom < 0) {
            return null;
        }
        int equals = ArrayUtils.indexOf(buf, new byte[]{(byte) '='}, encodingFrom, to);
        if (equals < 0) {
            return null;
        }
        byte[] quote = {(byte) '"'};
        int quoteFrom = ArrayUtils.indexOf(buf, quote, equals, to);
        if (quoteFrom < 0) {
            quote = new byte[]{(byte) '\''};
            quoteFrom = ArrayUtils.indexOf(buf, quote, equals, to);
            if (quoteFrom < 0) {
                return null;
            }
        }
        int quoteTo = ArrayUtils.indexOf(buf, quote, quoteFrom + 1, to);
        if (quoteTo < 0) {
            return null;
        }
        try {
            return Charset.forName(new String(buf, quoteFrom + 1, quoteTo - quoteFrom - 1, "ASCII"));
        } catch (Exception e) {
            throw new ElementParsingException("Unsupported charset.", e);
        }
    }

    private String getDiagnosticSubstring() {
        int len = pos < 39 ? pos : 39;
        return new String(buf, pos - len, pos == buf.length ? len : len + 1);
    }

}
