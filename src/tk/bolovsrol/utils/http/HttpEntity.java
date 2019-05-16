package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.MimeUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StreamUtils;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.properties.InvalidPropertyValueFormatException;
import tk.bolovsrol.utils.properties.sources.ReadOnlySource;
import tk.bolovsrol.utils.xml.Element;
import tk.bolovsrol.utils.xml.XmlPrinter;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public abstract class HttpEntity extends HttpHeaders {

    protected static final Timer HTTP_IO_TIMER = new Timer("HttpIOTimer", true);

    /** Закрывает переданный объект. К сожалению, чтение из потока никак не прервать, кроме как закрыв поток, так что таким образом приходится соблюдать таймаут. */
    private static class Closer extends TimerTask {
        private final Closeable closeable;
        private volatile boolean ran = false;

        public Closer(Closeable closeable) {
            this.closeable = closeable;
        }

        @Override public void run() {
            try {
                closeable.close();
            } catch (IOException e) {
                // ignore ok?
            } finally {
                ran = true;
            }
        }

        public boolean ran() {
            return ran;
        }
    }

    protected HttpVersion httpVersion;

    public static Integer CUT_BODY_TO_STRING_AT = Cfg.getInteger("log.http.cutBodyAt", null, Log.getInstance());
    protected Integer cutBodyToStringAt = CUT_BODY_TO_STRING_AT;
    protected HttpEntityIOExceptionHandler writeIOExceptionHandler;
    protected HttpEntityAfterWriteHandler afterResponseHandler;

    protected HttpEntity() { }

    protected HttpEntity(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    protected abstract void setStatusLine(String statusLine) throws HttpEntityParsingException;

    /** @return первая строка сущности — Request-Line или Status-Line, как определено в rfc */
    protected abstract String getFirstLine();

    /**
     * Устанавливает тело, считанное из потока.
     *
     * @param readBody
     */
    protected abstract void setReadBody(byte[] readBody) throws HttpEntityParsingException;

    /**
     * Возвращает тело, которое можно отправить получателю.
     *
     * @return тело или нул
     */
    protected abstract byte[] getWriteBody();

    /**
     * Устанавливает тело, предоставленное пользователем.
     *
     * @param userBody
     */
    protected abstract void setUserBody(byte[] userBody);

    /**
     * Возвращает тело, которым может пользоваться пользователь.
     *
     * @return
     */
    protected abstract byte[] getUserBody();

    protected void readBody(LineInputStream lineInputStream) throws IOException, HttpEntityParsingException {
        try {
            setReadBody(retrieveBody(lineInputStream));
        } catch (HttpEntityParsingException | IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new HttpEntityParsingException("Cannot read body.", e);
        }
    }

    private byte[] retrieveBody(LineInputStream lineInputStream) throws InvalidPropertyValueFormatException, IOException, HttpEntityParsingException {
        // there's a body if either Content-Length or Transfer-Encoding header specified as of rfc2616 §4.3
        String transferEncoding = headers.get(TRANSFER_ENCODING);
        if (transferEncoding != null) {
            if ("chunked".equals(transferEncoding)) {
                return readChunkedBody(lineInputStream);
            } else {
                throw new HttpEntityParsingException("Specified Transfer-Encoding " + Spell.get(transferEncoding) + " is not yet supported");
            }
        }

        Integer contentLength = headers.getInteger(CONTENT_LENGTH);
        if (contentLength != null) {
            return StreamUtils.readWhileAvailable(lineInputStream, contentLength);
        }

        // no body expected
        return null;
    }

    private static byte[] readChunkedBody(LineInputStream lineInputStream) throws IOException, HttpEntityParsingException {
        // надо читать по чанкам
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            while (true) {
                String lenStr = lineInputStream.readLine();
                if (lenStr == null) {
                    throw new HttpEntityParsingException("Unexpected end of chunked stream. Read buffer " + Spell.get(baos.toByteArray()));
                }
                lenStr = lenStr.trim();
                if (lenStr.isEmpty()) {
                    continue;
                }
                int len;
                try {
                    len = Integer.parseInt(lenStr, 16);
                } catch (NumberFormatException e) {
                    throw new HttpEntityParsingException("Invalid chunk length " + Spell.get(lenStr) + ". Read buffer " + Spell.get(baos.toByteArray()), e);
                }
                if (len == 0) {
                    if (lineInputStream.read() != 0x0d | lineInputStream.read() != 0x0a) {
                        throw new HttpEntityParsingException("Expected final CR+LF is missing. Read buffer " + Spell.get(baos.toByteArray()));
                    }
                    break;
                }
                byte[] buf = StreamUtils.readWhileAvailable(lineInputStream, len);
                if (buf.length != len) {
                    throw new HttpEntityParsingException("Expected chunk length " + len + ", read only " + buf.length + ". Read buffer " + Spell.get(baos.toByteArray())
                    );
                }
                baos.write(buf);
            }
            return baos.toByteArray();
        }
    }

    protected void readFromStream(InputStream inputStream, long ioTimeout) throws IOException, HttpEntityParsingException {
        Closer closer = new Closer(inputStream);
        LineInputStream lineInputStream = new LineInputStream(inputStream, StandardCharsets.ISO_8859_1);
        try {
            HTTP_IO_TIMER.schedule(closer, ioTimeout);
            readStatusLine(lineInputStream);
            readHeaders(lineInputStream);
            readBody(lineInputStream);
            closer.cancel();
        } catch (IOException e) {
            if (closer.ran()) {
                throw new EOFException("Not enough data available in " + Spell.getDuration(ioTimeout) + ". Http Entity reading timed out. So far read dump: " + Spell.get(toString()));
            } else {
                throw e;
            }
        } catch (HttpEntityParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpEntityParsingException(e);
        }
    }

    public byte[] getBody() {
        return getUserBody();
    }

    public boolean hasBody() {
        return getBody() != null;
    }

    public String getBodyAsString(Charset charset) throws UnsupportedEncodingException {
        return new String(getBody(), charset);
    }

    public String getBodyAsString(String encoding) throws UnsupportedEncodingException {
        return new String(getBody(), encoding);
    }

    public String getBodyAsString() throws UnsupportedCharsetException {
        byte[] body = getBody();
        if (body == null) {
            return null;
        } else {
            return new String(body, getContentTypeCharsetOrDefault());
        }
    }

    public void setBody(Element element) {
        if (element != null) {
            setBody(new XmlPrinter().toBytes(element), "text/xml; charset=utf-8");
        }
    }

    public void setBody(Json json) {
        setBody(json, 0);
    }

    public void setBody(Json json, int indent) {
        if (json != null) {
            setBody(json.toStringIndented(indent).getBytes(StandardCharsets.UTF_8), "application/json; charset=utf-8");
        }
    }

    public void setBody(String body) {
        setBody(body, "text/plain");
    }

    public void setBody(String body, String contentType) {
        if (body != null) {
            setBody(body.getBytes(StandardCharsets.UTF_8), contentType + "; charset=utf-8");
        }
    }

    public void setBody(ReadOnlySource form) {
        if (form != null) {
            setBody(Uri.compileQuery(form.dump()), "application/x-www-form-urlencoded; charset=utf-8");
        }
    }

    public void setBody(byte[] body, String contentType) {
        if (body != null) {
            this.headers.set(CONTENT_TYPE, contentType);
            setUserBody(body);
        }
    }

    protected void readStatusLine(LineInputStream lineInputStream) throws IOException, HttpEntityParsingException {
        String line = MimeUtils.decode(lineInputStream.readLine());
        if (line == null) {
            throw new EOFException("No response within timeout.");
        }
        setStatusLine(line);
    }

    /**
     * Записывает хттп-сущность в указанный исходящий поток в течение таймаута по умолчанию {@link HttpConst#DEFAULT_IO_TIMEOUT}..
     * <p>
     * В случае ошибок ввода-вывода вызывает {@link HttpEntityIOExceptionHandler#handleIOException(IOException)},
     * если соответствующий обработчик назначен методом {@link #setWriteIOExceptionHandler(HttpEntityIOExceptionHandler)}
     * для возможной обработки ошибки.
     * <p>
     * Если запись произошла успешно, вызывает {@link HttpEntityAfterWriteHandler#handleAfterWrite()},
     * если соответствующий обработчик назначен методом {@link #setAfterWriteHandler(HttpEntityAfterWriteHandler)}.
     *
     * @param os поток, в который писать
     * @throws IOException
     * @throws InterruptedException
     * @throws InvalidHttpEntityException
     */
    public void writeToStream(OutputStream os) throws IOException, InterruptedException, InvalidHttpEntityException {
        writeToStream(os, HttpConst.DEFAULT_IO_TIMEOUT);
    }

    /**
     * Записывает хттп-сущность в указанный исходящий поток в течение указанного таймаута.
     * <p>
     * В случае ошибок ввода-вывода вызывает {@link HttpEntityIOExceptionHandler#handleIOException(IOException)},
     * если соответствующий обработчик назначен методом {@link #setWriteIOExceptionHandler(HttpEntityIOExceptionHandler)}
     * для возможной обработки ошибки.
     * <p>
     * Если запись произошла успешно, вызывает {@link HttpEntityAfterWriteHandler#handleAfterWrite()},
     * если соответствующий обработчик назначен методом {@link #setAfterWriteHandler(HttpEntityAfterWriteHandler)}.
     *
     * @param os поток, в который писать
     * @param ioTimeout таймаут записи
     * @throws IOException
     * @throws InterruptedException
     * @throws InvalidHttpEntityException
     */
    public void writeToStream(OutputStream os, long ioTimeout) throws IOException, InterruptedException, InvalidHttpEntityException {
        Closer closer = new Closer(os);
        try {
            StringBuilder sb = new StringBuilder(512);
            sb.append(getFirstLine()).append(CR_LF);
            for (Map.Entry<String, String> entry : headers.dump().entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(CR_LF);
            }
            sb.append(CR_LF);

            HTTP_IO_TIMER.schedule(closer, ioTimeout);
            os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            if (hasBody()) {
                os.write(getWriteBody());
            }
            os.flush();
            closer.cancel();
            if (afterResponseHandler != null) { afterResponseHandler.handleAfterWrite(); }
        } catch (IOException e) {
            if (closer.ran()) {
                handleIOExceptionInternal(new EOFException("No data sent in " + Spell.getDuration(ioTimeout) + ". Http Entity writing timed out."));
            } else {
                handleIOExceptionInternal(e);
            }
        }
    }

    private void handleIOExceptionInternal(IOException e) throws IOException {
        if (writeIOExceptionHandler == null || !writeIOExceptionHandler.handleIOException(e)) {
            throw e;
        }
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    protected void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public void setConnectionKeepAlive(long timeout) {
        headers.set("Connection", "keep-alive");
        headers.set("Keep-Alive", "timeout=" + (timeout / 1000L));
        // некоторые клиенты считают, если кип-элайв, то должно быть тело. Нет, клиенты, не должно. Вот вам ноль, пожалуйста.
        if (!headers.has(CONTENT_LENGTH)) { headers.set(CONTENT_LENGTH, 0); }
    }

    public void setConnectionClose() {
        headers.set("Connection", "close");
    }

    public HttpEntityIOExceptionHandler getWriteIOExceptionHandler() {
        return writeIOExceptionHandler;
    }

    /**
     * Устанавливает обработчик исключений ввода-вывода, которые может выкинуть
     * метод записи хттп-сущности в исходящий поток.
     *
     * @param writeIOExceptionHandler хендлер
     */
    public void setWriteIOExceptionHandler(HttpEntityIOExceptionHandler writeIOExceptionHandler) {
        this.writeIOExceptionHandler = writeIOExceptionHandler;
    }

    public void setAfterWriteHandler(HttpEntityAfterWriteHandler handler) {
        this.afterResponseHandler = handler;
    }

    @Override
    public String toString() {
        StringDumpBuilder sdb = new StringDumpBuilder();
        appendToString(sdb);
        return sdb.toString();
    }

    protected void appendToString(StringDumpBuilder sdb) {
        sdb.append("protocol", httpVersion);
        sdb.append("headers", headers);
    }

    /**
     * Добавляет билдеру дамп переданного тела. Это вспомогательный метод, который удобно вызывать из перегруженного {@link #appendToString(StringDumpBuilder)}.
     * <p>
     * Если контент-тайп сущности начинается со слова «text» или один из перечисленных в {@link #TEXT_CONTENT_TYPES},
     * а переданный <code>contentEncoded</code>=false, добавляет к билдеру текстовое представление тела запроса в наиболее подходящей кодировке.
     * <p>
     * В остальных случаях добавляет дамп запроса — целиком или первые байты в зависимости от установленного {@link #setCutBodyToStringAt(Integer)}.
     *
     * @param sdb
     * @param body
     * @param contentEncoded
     * @param caption
     * @see #getCutBodyToStringAt()
     * @see #setCutBodyToStringAt(Integer)
     */
    protected void appendBodyToString(StringDumpBuilder sdb, byte[] body, boolean contentEncoded, String caption) {
        if (body != null && body.length > 0) {
            if (!contentEncoded && isText()) {
                try {
                    Charset charset = getContentTypeCharsetOrDefault();
                    if (cutBodyToStringAt == null || body.length <= cutBodyToStringAt) {
                        sdb.append(caption + '(' + body.length + " byte(s) " + charset + ')', new String(body, charset));
                    } else {
                        sdb.append(caption + '(' + cutBodyToStringAt + " bytes of " + body.length + " byte(s) " + charset + ')', new String(body, 0, cutBodyToStringAt, charset) + '✂');
                    }
                } catch (Exception ignored) {
                    appendHexDumpToString(sdb, body, caption);
                }
            } else {
                appendHexDumpToString(sdb, body, caption);
            }
        }
    }

    private void appendHexDumpToString(StringDumpBuilder sdb, byte[] body, String caption) {
        if (cutBodyToStringAt == null || body.length <= cutBodyToStringAt) {
            sdb.append(caption + '(' + body.length + " byte(s))" + sdb.getConnector() + '{' + StringUtils.getDelimitedHexDump(body) + '}');
        } else {
            sdb.append(caption + '(' + cutBodyToStringAt + " bytes of " + body.length + " byte(s))" + sdb.getConnector() + '{' + StringUtils.getDelimitedHexDump(body, 0, cutBodyToStringAt) + "✂}");
        }
    }

    public Integer getCutBodyToStringAt() {
        return cutBodyToStringAt;
    }

    public void setCutBodyToStringAt(Integer cutBodyToStringAt) {
        this.cutBodyToStringAt = cutBodyToStringAt;
    }

    /**
     * @return Части тела типа multipart/form-data, рассортированные по именам.
     */
    public Map<String, HttpBodyPart> getBodyParts() {
        return MultipartBodyDecoder.parse(this);
    }
}
