package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.CompressUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.UnaryOperator;

/** http-ответ */
public class HttpResponse extends HttpEntity {

    private static final UnaryOperator<byte[]> CU_UNGZIP = CompressUtils::ungzip;
    private static final UnaryOperator<byte[]> CU_INFLATE = CompressUtils::inflate;
    private static final UnaryOperator<byte[]> CU_GZIP = CompressUtils::gzip;
    private static final UnaryOperator<byte[]> CU_DEFLATE = CompressUtils::deflate;
    private static final String GZIP = "gzip";
    private static final String DEFLATE = "deflate";

    private HttpStatus statusAndReason;
    protected String requestAcceptEncoding;

    private byte[] userBody;
    private byte[] outBody;

    /** Пустой ответ, по умолчанию со статусом OK и без тела. */
    protected HttpResponse() {
        super();
    }

    /**
     * Создаёт http-ответ на входящий запрос
     * со статусом {@link HttpStatus @OK}
     * и без тела.
     *
     * @param request исходный запрос
     * @return http-ответ
     */
    public static HttpResponse generate(HttpRequest request) {
        return generate(request, HttpStatus._200_OK, null);
    }

    /**
     * Создаёт http-ответ на входящий запрос
     * с указанным статусом и без тела.
     *
     * @param request исходный запрос
     * @param statusLine статус-код ответа
     * @return http-ответ
     */
    public static HttpResponse generate(HttpRequest request, HttpStatus statusLine) {
        return generate(request, statusLine, null);
    }

    /**
     * Создаёт http-ответ на входящий запрос с указанными статусом
     * и телом. Content-Type делает равным <code>text/plain; charset=utf-8</code>.
     * <p>
     * Этот метод предназначен в первую очередь для создания http-ответов
     * на ошибочные запросы — с краткой диагностикой неисправности.
     *
     * @param request
     * @param statusLine
     * @param shortMessage
     * @return
     */
    public static HttpResponse generate(HttpRequest request, HttpStatus statusLine, String shortMessage) {
        return generate(request.getHttpVersion(), request.headers().get(ACCEPT_ENCODING), statusLine, shortMessage);
    }

    /**
     * Создаёт http-ответ с указанными протоколом, статусом
     * и телом. Content-Type делает равным <code>text/plain; charset=utf-8</code>.
     * <p>
     * Этот метод предназначен в первую очередь для создания
     * http-ответов-заглушек.
     *
     * @param httpVersion
     * @param statusLine
     * @param shortMessage
     * @return
     */
    public static HttpResponse generate(HttpVersion httpVersion, HttpStatus statusLine, String shortMessage) {
        return generate(httpVersion, null, statusLine, shortMessage);
    }

    /**
     * Создаёт http-ответ с указанными протоколом, строкой со списком допустимых кодировок, статусом
     * и телом. Content-Type делает равным <code>text/plain; charset=utf-8</code>.
     * <p>
     * Этот метод предназначен в первую очередь для создания
     * http-ответов-заглушек.
     *
     * @param httpVersion
     * @param acceptEncoding строка как в заголовке Accept-Encoding
     * @param statusLine
     * @param shortMessage
     * @return
     */
    public static HttpResponse generate(HttpVersion httpVersion, String acceptEncoding, HttpStatus statusLine, String shortMessage) {
        HttpResponse hr = new HttpResponse();
        hr.setHttpVersion(httpVersion);
        hr.setStatusAndReason(statusLine);
        hr.setBody(shortMessage);
        hr.setRequestAcceptEncoding(acceptEncoding);
        return hr;
    }

    public static HttpResponse parse(InputStream inputStream) throws IOException, HttpEntityParsingException {
        return parse(inputStream, HttpConst.DEFAULT_IO_TIMEOUT);
    }

    public static HttpResponse parse(InputStream inputStream, long ioTimeout) throws IOException, HttpEntityParsingException {
        HttpResponse hr = new HttpResponse();
        hr.readFromStream(inputStream, ioTimeout);
        return hr;
    }

    public final void setStatusAndReason(HttpStatus httpStatus) {
        this.statusAndReason = httpStatus;
    }

    public void setStatusAndReason(int statusCode, String reasonPhrase) {
        this.statusAndReason = new HttpStatus(statusCode, reasonPhrase);
    }

    @Override protected String getFirstLine() {
        return getStatusLine();
    }

    public String getStatusLine() {
        return httpVersion.toString() + ' ' + statusAndReason.statusCode + ' ' + statusAndReason.reasonPhrase;
    }

    public HttpStatus getStatus() {
        return statusAndReason;
    }

    @Override
    protected void setStatusLine(String statusLine) throws HttpEntityParsingException {
        try {
            httpVersion = HttpVersion.parse(StringUtils.subWords(statusLine, 0, 1));
            this.statusAndReason = new HttpStatus(Integer.parseInt(StringUtils.subWords(statusLine, 1, 2)), StringUtils.subWords(statusLine, 2));
        } catch (Throwable e) {
            throw new HttpEntityParsingException("Cannot parse response status line " + Spell.get(statusLine), e);
        }
    }

    @Override protected void setReadBody(byte[] readBody) throws HttpEntityParsingException {
        this.outBody = readBody;

        String contentEncoding = headers.get(CONTENT_ENCODING);
        if (contentEncoding == null) {
            this.userBody = readBody;
        } else if (GZIP.equals(contentEncoding)) {
            decompressBody(CU_UNGZIP);
        } else if (DEFLATE.equals(contentEncoding)) {
            decompressBody(CU_INFLATE);
        } else {
            throw new HttpEntityParsingException("Unknown " + CONTENT_ENCODING + ' ' + Spell.get(contentEncoding));
        }
    }

    private void decompressBody(UnaryOperator<byte[]> operator) throws HttpEntityParsingException {
        try {
            userBody = operator.apply(outBody);
        } catch (Exception e) {
            throw new HttpEntityParsingException("Cannot decompress body", e);
        }
    }

    @Override protected byte[] getWriteBody() {
        return outBody;
    }

    @Override protected void setUserBody(byte[] userBody) {
        this.userBody = userBody;
        if (requestAcceptEncoding == null ||
            !(tryCompression(requestAcceptEncoding, GZIP, CU_GZIP) || tryCompression(requestAcceptEncoding, DEFLATE, CU_DEFLATE))) {
            this.outBody = userBody;
        }
        headers.set(CONTENT_LENGTH, outBody.length);
    }

    private boolean tryCompression(String acceptEncoding, String contentEncoding, UnaryOperator<byte[]> operator) {
        return acceptEncoding.contains(contentEncoding) && compressBody(contentEncoding, operator);
    }

    private boolean compressBody(String contentEncoding, UnaryOperator<byte[]> operator) {
        outBody = operator.apply(userBody);
        if (outBody.length >= userBody.length) {
            return false;
        } else {
            headers.set(CONTENT_ENCODING, contentEncoding);
            return true;
        }
    }

    @Override protected byte[] getUserBody() {
        return userBody;
    }

    @Override protected void appendToString(StringDumpBuilder sdb) {
        sdb.append("statusCode", statusAndReason);
        super.appendToString(sdb);
        if (userBody == outBody) {
            appendBodyToString(sdb, outBody, false, "body");
        } else {
            appendBodyToString(sdb, outBody, true, "body");
            appendBodyToString(sdb, userBody, false, "/ uncompressed");
        }
    }

    public String getRequestAcceptEncoding() {
        return requestAcceptEncoding;
    }

    public void setRequestAcceptEncoding(String requestAcceptEncoding) {
        this.requestAcceptEncoding = requestAcceptEncoding;
    }

    public void dropAcceptEncoding() {
        this.requestAcceptEncoding = null;
    }
}