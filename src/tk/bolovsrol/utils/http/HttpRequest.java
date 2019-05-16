package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.NotNull;
import tk.bolovsrol.utils.Nullable;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.UriParsingException;
import tk.bolovsrol.utils.box.Box;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;
import tk.bolovsrol.utils.properties.sources.ReadOnlySource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

/** http-запрос */
public class HttpRequest extends HttpEntity {

    public static final Set<String> NO_ACCEPTENCODING_HOSTNAMES = Box.with(Cfg.get("http.disableAcceptEncoding.hosts"))
        .map(StringUtils::parseDelimited).toCollection(Arrays::asList).orWrap(Collections::emptySet).toSet();

    private Method method;
    private String path = "/";
    private String query = null;
    private byte[] body;

    /** Пустой запрос, без метода и вообще без ничего. Нужно наполнить, прежде чем отправлять. */
    protected HttpRequest() {
    }

    public HttpRequest(@NotNull HttpVersion httpVersion) {
        super(httpVersion);
    }

    public HttpRequest(@NotNull HttpVersion httpVersion, @NotNull Method method) {
        super(httpVersion);
        this.method = method;
    }

    @Override protected void setReadBody(byte[] readBody) { this.body = readBody; }

    @Override protected byte[] getWriteBody() { return this.body; }

    @Override protected void setUserBody(byte[] userBody) {
        this.body = userBody;
        this.headers.set(CONTENT_LENGTH, userBody.length);
    }

    @Override protected byte[] getUserBody() {return this.body; }

    public static HttpRequest parse(
        @NotNull InputStream inputStream
    ) throws IOException, HttpEntityParsingException {
        return parse(inputStream, HttpConst.DEFAULT_IO_TIMEOUT);
    }

    public static HttpRequest parse(
        @NotNull
            InputStream inputStream, long ioTimeout
    ) throws IOException, HttpEntityParsingException {
        HttpRequest hr = new HttpRequest();
        hr.readFromStream(inputStream, ioTimeout);
        return hr;
    }

    public void setMethod(@NotNull Method method) {
        this.method = method;
    }

    public void setFile(@NotNull String file) {
        int pos = file.indexOf((int) '?');
        if (pos == -1) {
            setPath(file);
            dropQuery();
        } else {
            setPath(file.substring(0, pos));
            setQuery(file.substring(pos + 1));
        }
    }

    public void setPath(@Nullable String path) {
        if (path == null) {
            this.path = "/";
        } else if (!path.startsWith("/")) {
            this.path = '/' + path;
        } else {
            this.path = path;
        }
    }

    public void appendPath(String pathAddendum) {
        if (pathAddendum != null) {
            this.path = this.path + pathAddendum;
        }
    }

    public void dropQuery() {
        this.query = null;
    }

    public void setQuery(String queryOrNull) {
        this.query = queryOrNull;
    }

    public void setQuery(Map<String, String> queryOrNull) {
        this.query = queryOrNull == null ? null : Uri.compileQuery(queryOrNull);
    }

    public void setQuery(ReadOnlySource queryOrNull) {
        this.query = queryOrNull == null ? null : Uri.compileQuery(queryOrNull);
    }

    public void setHost(@NotNull String host, int port) {
        String val;
        if (port == HttpConst.DEFAULT_PORT) {
            val = host;
        } else {
            val = host + ':' + port;
        }
        headers.set("Host", val);
        if (!NO_ACCEPTENCODING_HOSTNAMES.contains(val)) {
            headers.set(ACCEPT_ENCODING, "gzip, deflate");
        }
    }

    public void setUrl(@NotNull Uri uri) {
        setHost(uri.getHostname(), uri.getPortIntValue(HttpConst.DEFAULT_PORT));
        setPath(uri.getPath());
        setQuery(uri.getQuery());
        if (uri.hasUsername()) {
            try {
                BasicAuthInfo.parse(uri).putToRequest(this);
            } catch (UnexpectedBehaviourException e) {
                // this won't happen
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void readBody(@NotNull LineInputStream lineInputStream) throws IOException, HttpEntityParsingException {
        if (method.allowsBody()) {
            super.readBody(lineInputStream);
        } else {
            body = null;
        }
    }

    @Override protected String getFirstLine() {
        return getRequestLine();
    }

    public String getRequestLine() {
        StringBuilder sb = new StringBuilder(256);
        sb.append(method).append(' ').append(path);
        if (query != null) {
            sb.append('?').append(query);
        }
        sb.append(' ').append(httpVersion.toString());
        return sb.toString();
    }

    @Override
    protected void setStatusLine(@NotNull String requestLine) throws HttpEntityParsingException {
        StringTokenizer st = new StringTokenizer(requestLine, " ", false);
        try {
            setMethod(Method.valueOf(st.nextToken()));
            setFile(st.nextToken());
            setHttpVersion(HttpVersion.parse(st.nextToken()));
        } catch (HttpEntityParsingException e) {
            throw e;
        } catch (NoSuchElementException e) {
            throw new HttpEntityParsingException("Too few data, cannot parse request line " + Spell.get(requestLine), e);
        } catch (Throwable e) {
            throw new HttpEntityParsingException("Cannot parse request line " + Spell.get(requestLine), e);
        }
        if (st.hasMoreTokens()) {
            throw new HttpEntityParsingException("Too much data in request line " + Spell.get(requestLine));
        }
    }

    public Method getMethod() {
        return method;
    }

    public String getQuery() {
        return query;
    }

    public ReadOnlyProperties getQueryAsProperties() throws UriParsingException {
        return new ReadOnlyProperties(Uri.parseQuery(query));
    }

    public String getPath() {
        return path;
    }

    public HttpResponse createResponse() {
        return HttpResponse.generate(this);
    }

    public HttpResponse createResponse(@NotNull HttpStatus statusCode) {
        return HttpResponse.generate(this, statusCode);
    }

    public HttpResponse createResponse(@NotNull HttpStatus statusCode, String shortMessage) {
        return HttpResponse.generate(this, statusCode, shortMessage);
    }

    @Override public void writeToStream(OutputStream os, long ioTimeout) throws IOException, InterruptedException, InvalidHttpEntityException {
        if (method == null) {
            throw new InvalidHttpEntityException("Request has no Method defined");
        }
        super.writeToStream(os, ioTimeout);
    }

    @Override protected void appendToString(StringDumpBuilder sdb) {
        sdb.append("method", method).append("path", path).append("query", query);
        super.appendToString(sdb);
        appendBodyToString(sdb, body, false, "body");
    }

    public void setAcceptEncoding(String acceptEncoding) {
        headers.set(ACCEPT_ENCODING, acceptEncoding);
    }

    public void dropAcceptEncoding() {
        headers.drop(ACCEPT_ENCODING);
    }

}
