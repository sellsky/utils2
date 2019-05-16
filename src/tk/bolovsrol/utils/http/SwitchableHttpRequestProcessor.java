package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.StringDumpBuilder;

import java.net.Socket;

/**
 * Суперпроцессор.
 * <p/>
 * Обладает способностью включать и выключать исполнение процесосра делегата, а также заменять его.
 */
public class SwitchableHttpRequestProcessor implements HttpRequestProcessor {

    private HttpRequestProcessor delegate;
    private boolean active;

    public static final HttpStatus INACTIVE_STATUS_CODE_DEF = HttpStatus._503_SERVICE_UNAVAILABLE;
    private HttpStatus inactiveStatusCode = INACTIVE_STATUS_CODE_DEF;

    public static final HttpStatus NULL_DELEGATE_STATUS_CODE_DEF = HttpStatus._403_FORBIDDEN;
    private HttpStatus nullDelegateStatusCode = NULL_DELEGATE_STATUS_CODE_DEF;

    /**
     * Создаёт процессор в неактивном состоянии и без делегата.
     */
    public SwitchableHttpRequestProcessor() {
        this(null, false);
    }

    /**
     * Создаёт процессор с указанным делегатом в неактивном состоянии.
     *
     * @param delegate процессор-делегат
     */
    public SwitchableHttpRequestProcessor(HttpRequestProcessor delegate) {
        this(delegate, false);
    }

    /**
     * Создаёт процессор с указанным делегатом в указанном состоянии
     *
     * @param delegate процессор-делегат
     * @param active   true — активен, иначе false
     */
    public SwitchableHttpRequestProcessor(HttpRequestProcessor delegate, boolean active) {
        this.delegate = delegate;
        this.active = active;
    }

    /**
     * Если флаг активности не установлен, то процессор возвращает ответ с {@link #getInactiveStatusCode()}.
     * <p/>
     * Если процессор-делегат не определён, возвращается ответ с {@link #getNullDelegateStatusCode()}.
     * <p/>
     * В остальных случаях возвращается результат обработки запроса процессором-делегатом.
     *
     * @param socket
     * @param httpRequest
     * @return ответ
     */
    @Override
    public final HttpResponse process(Socket socket, HttpRequest httpRequest) throws InterruptedException {
        if (!active) {
            return httpRequest.createResponse(inactiveStatusCode);
        }
        HttpRequestProcessor delegate = this.delegate; // избавимся от синхронизации
        if (delegate == null) {
            return httpRequest.createResponse(nullDelegateStatusCode);
        }
        return delegate.process(socket, httpRequest);
    }


    public boolean isActive() {
        return active;
    }

    public SwitchableHttpRequestProcessor setActive(boolean active) {
        this.active = active;
        return this;
    }

    public HttpRequestProcessor getDelegate() {
        return delegate;
    }

    public SwitchableHttpRequestProcessor setDelegate(HttpRequestProcessor delegate) {
        this.delegate = delegate;
        return this;
    }

    public HttpStatus getInactiveStatusCode() {
        return inactiveStatusCode;
    }

    public SwitchableHttpRequestProcessor setInactiveStatusCode(HttpStatus inactiveStatusCode) {
        this.inactiveStatusCode = inactiveStatusCode;
        return this;
    }

    public HttpStatus getNullDelegateStatusCode() {
        return nullDelegateStatusCode;
    }

    public SwitchableHttpRequestProcessor setNullDelegateStatusCode(HttpStatus nullDelegateStatusCode) {
        this.nullDelegateStatusCode = nullDelegateStatusCode;
        return this;
    }

	@Override public String toString() {
		return new StringDumpBuilder()
			.append("active", active)
			.append("inactiveStatusCode", inactiveStatusCode)
			.append("nullDelegateStatusCode", nullDelegateStatusCode)
			.append("delegate", delegate)
			.toString();
	}
}
