package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.MimeUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.caseinsensitive.CaseInsensitiveLinkedHashMap;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.properties.InvalidPropertyValueFormatException;
import tk.bolovsrol.utils.properties.PlainProperties;
import tk.bolovsrol.utils.properties.sources.MapPlainSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashSet;
import java.util.Map;

/** Хттп-заголовки. */
public class HttpHeaders {

    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";

    protected static final char HEADER_VALUE_DELIMITER = '\0';
    protected static final String CR_LF = "\r\n";

    /** Типы контента, которые не «text/», но фактически являющиеся текстом. */
    public static final HashSet<String> TEXT_CONTENT_TYPES = new HashSet<>();

    static {
        TEXT_CONTENT_TYPES.add("application/x-www-form-urlencoded");
        TEXT_CONTENT_TYPES.add("application/json");
        TEXT_CONTENT_TYPES.add("application/xml");
        TEXT_CONTENT_TYPES.add("multipart/form-data");
    }

    private final MapPlainSource headersPlainSource = new MapPlainSource(new CaseInsensitiveLinkedHashMap<>());
    protected final PlainProperties headers = new PlainProperties(headersPlainSource);

    /**
     * Создаёт объект с заголовками и загружает его переданной картой. Исходная карта не модифицируется и после загрузки не используется.
     *
     * @param headers
     */
    public HttpHeaders(Map<String, String> headers) {
        headersPlainSource.setAll(headers);
    }

    /**
     * Создаёт объект с пустыми заголовками.
     */
    public HttpHeaders() {
    }

    /**
     * Возвращает заголовки в виде пропертей.
     * Одноимённые заголовки склеены символом {@link #HEADER_VALUE_DELIMITER}.
     * В качестве основы для пропертей используется {@link CaseInsensitiveLinkedHashMap}.
     *
     * @return заголовки.
     */
    public PlainProperties headers() {
        return headers;
    }

    /** @return содержимое заголовка Accept-Encoding, если такого нет, то нул. */
    public String getAcceptEncoding() {
        return this.headers.get(ACCEPT_ENCODING);
    }

    /** @return содержимое заголовка Content-Encoding, если такого нет, то нул. */
    public String getContentEncoding() {
        return this.headers.get(CONTENT_ENCODING);
    }

    /** @return содержимое заголовка Content-Length, если такого нет, то нул. */
    public Integer getContentLength() throws InvalidPropertyValueFormatException {
        return this.headers.getInteger(CONTENT_LENGTH);
    }

    /** @return содержимое заголовка Content-Type, если такого нет, то нул. */
    public String getContentType() {
        return this.headers.get(CONTENT_TYPE);
    }

    /** @return тип контента содержимого заголовка Content-Type, если такого заголовка нет, то нул. */
    public String getContentTypeMime() {
        return getContentTypeMime(getContentType());
    }

    /** @return тип контента переданного заголовка Content-Type, а если передан нул, то нул. */
    public static String getContentTypeMime(String contentType) {
        if (contentType == null) { return null; }
        int i = contentType.indexOf((int) ';');
        return i < 0 ? contentType : contentType.substring(0, i);
    }

    /**
     * Возвращает чарсет, соответствующий указанному в заголовке Content-Type.
     * Если такого заголовка нет либо там чарсет не указан, возвращает {@link StandardCharsets#US_ASCII}
     * в соответствии с <a href="http://www.w3.org/Protocols/rfc1341/4_Content-Type.html">RFC-1341, §4</a>.
     *
     * @return чарсет
     * @throws UnsupportedCharsetException в контент-тайпе указан непонятный нам чарсет
     */
    public Charset getContentTypeCharsetOrDefault() throws UnsupportedCharsetException {
        String contentType = getContentType();
        return getContentTypeCharsetOrDefault(contentType);
    }

    /**
     * Возвращает чарсет, соответствующий указанному в переданном Content-Type.
     * Если передан нул либо чарсет не указан, возвращает {@link StandardCharsets#US_ASCII}
     * в соответствии с <a href="http://www.w3.org/Protocols/rfc1341/4_Content-Type.html">RFC-1341, §4</a>.
     *
     * @return чарсет
     * @throws UnsupportedCharsetException в контент-тайпе указан непонятный нам чарсет
     */
    public static Charset getContentTypeCharsetOrDefault(String contentType) {
        String contentTypeCharset = getContentTypeCharset(contentType);
        return contentTypeCharset != null ? Charset.forName(contentTypeCharset) : "application/json".equals(getContentTypeMime(contentType)) ? StandardCharsets.UTF_8 : StandardCharsets.US_ASCII;
    }

    /** @return чарсет содержимого заголовка Content-Type, если такого заголовка нет, то нул. */
    public String getContentTypeCharset() {
        return getContentTypeCharset(getContentType());
    }

    /** @return чарсет переданного заголовка Content-Type, а если передан нул, то нул. */
    public static String getContentTypeCharset(String contentType) {
        if (contentType == null) {
            return null;
        }
        int i = contentType.indexOf("charset=");
        if (i < 0) {
            return null;
        }
        i += "charset=".length();
        int to;
        if (contentType.charAt(i) == '"') {
            i++;
            to = contentType.indexOf((int) '"', i);
        } else {
            to = contentType.indexOf((int) ';', i);
        }
        if (to < 0) {
            to = contentType.length();
        }
        return contentType.substring(i, to);
    }

    /** @return true, если контент-тайп не указан либо начинается с «text/» либо один из {@link #TEXT_CONTENT_TYPES}. */
    public boolean isText() {
        String contentType = getContentTypeMime();
        return contentType == null || contentType.startsWith("text/") || TEXT_CONTENT_TYPES.contains(contentType);
    }

    /** @return содержимое заголовка Transfer-Encoding, если такого нет, то нул. */
    public String getTransferEncoding() {
        return this.headers.get(TRANSFER_ENCODING);
    }

    /**
     * Читает порцию заголовков в карту. Так как заголовков может быть несколько, а в карте ключ-то один,
     * мы используем хак: дублирующиеся заголовки склеиваем через {@link HttpConst#CR_LF}. Порядок заголовков в таком случае
     * немножко утрачивается, ну да и чёрт с ним.
     *
     * @param lineInputStream
     * @throws IOException
     * @throws HttpEntityParsingException
     */
    @SuppressWarnings("NonConstantStringShouldBeStringBuffer")
    protected void readHeaders(LineInputStream lineInputStream) throws IOException, HttpEntityParsingException {
        Map<String, String> map = headersPlainSource.getMap(); // тут работаем с собственно картой, потому что хотим делать map#merge()
        map.clear();
        String line = null;
        try {
            String key = null, value = null;
            while (true) {
                line = MimeUtils.decode(lineInputStream.readLine());
                if (line == null || line.isEmpty()) {
                    break;
                }
                if (Character.isWhitespace(line.charAt(0))) {
                    if (key == null || value == null) {
                        throw new HttpEntityParsingException("First header line " + Spell.get(line) + " starts with an unexpected character");
                    }
                    value = value + ' ' + line.trim(); // это бывает довольно редко, так что склеивание строк тут вполне сойдёт.
                } else {
                    int colonPos = line.indexOf((int) ':');
                    if (colonPos < 0) {
                        key = line;
                        value = null;
                    } else {
                        key = line.substring(0, colonPos);
                        value = line.substring(colonPos + 1).trim();
                    }
                }
                map.merge(key, value, (oldValue, newValue) -> oldValue + HttpConst.CR_LF + newValue);
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new HttpEntityParsingException("Cannot parse headers, last read line " + Spell.get(line), e);
        }
    }
}
