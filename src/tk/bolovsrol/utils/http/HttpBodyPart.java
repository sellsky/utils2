package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.box.Box;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Части тела хттп-сущности типа <code>multipart/form-data</code>.
 *
 * @see HttpEntity#getBodyParts()
 * @see MultipartBodyDecoder
 */
public class HttpBodyPart extends HttpHeaders {

    private static final String CONTENT_DISPOSITION = "Content-Disposition: ";

    private final String name;
    private final Map<String, String> contentDisposition;
    private final byte[] body;

    public HttpBodyPart(String name, Map<String, String> contentDisposition, Map<String, String> headers, byte[] body) {
        super(headers);
        this.name = name;
        this.contentDisposition = contentDisposition;
        this.body = body;
    }

    /** @return содержимое заголовка Content-Disposition в виде карты ключей-значений, либо нул. */
    public Map<String, String> getContentDispositionMap() {
        return contentDisposition;
    }

    /** @return имя части, если указано в Content-Disposition, либо нул. */
    public String getName() {
        return name;
    }

    /** @return тело части */
    public byte[] getBody() {
        return body;
    }

    /**
     * @return тело части в виде строки в кодировке, заданной в заголовках
     * @see #getContentTypeCharsetOrDefault()
     */
    public String getBodyAsString() {
        return Box.with(body).mapAndGet(b -> new String(b, getContentTypeCharsetOrDefault()));
    }

    @Override public String toString() {
        return new StringDumpBuilder()
            .append("name", name)
            .append("contentDisposition", contentDisposition)
            .append("body", body)
            .toString();
    }

    /**
     * Разбирает строки заголовков и прицепляет к ним тело. Этот метод вызывают из {@link MultipartBodyDecoder}, нет нужды использовать его вручную.
     *
     * @param headerLines строки заголовков части или нул.
     * @param body тело части
     * @return часть
     */
    public static HttpBodyPart parse(String[] headerLines, byte[] body) {
        if (headerLines == null) {
            return new HttpBodyPart(null, null, null, body);
        }

        Map<String, String> headers = new LinkedHashMap<>();
        Map<String, String> contentDisposition = new LinkedHashMap<>();
        for (String line : headerLines) {
            if (line.isEmpty()) {
                break;
            } else if (line.startsWith(CONTENT_DISPOSITION)) {
                String[] sublines = StringUtils.parseDelimited(line.substring(CONTENT_DISPOSITION.length()), ';');
                // sublines[0] should be “form-data”, we don't care
                for (int i = 1; i < sublines.length; i++) {
                    String subline = sublines[i];
                    int eqPos = subline.indexOf('=');
                    if (eqPos < 0) {
                        contentDisposition.put(subline, null);
                    } else {
                        contentDisposition.put(subline.substring(0, eqPos), StringUtils.trim(subline.substring(eqPos + 1), StringUtils.QUOTE_FILTER, StringUtils.TrimMode.BOTH));
                    }
                }
            }
            int cPos = line.indexOf(':');
            if (cPos < 0) {
                headers.put(line, null);
            } else {
                headers.put(line.substring(0, cPos), StringUtils.trim(line.substring(cPos + 1), StringUtils.WHITESPACE_FILTER, StringUtils.TrimMode.BOTH));
            }
        }
        return new HttpBodyPart(contentDisposition.get("name"), contentDisposition, headers, body);
    }
}
