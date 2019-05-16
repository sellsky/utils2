package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.log.LogDome;

import java.net.Socket;

public class HttpAuthorizationProcessor implements HttpRequestProcessor {

    private final LogDome log;
    private final String realmLine;
    private final HttpRequestProcessor httpRequestProcessor;
    private final HttpAuthorization httpAuthorization;
    private long authFailedResponseDelay = 1000L;

    public HttpAuthorizationProcessor(LogDome log, String realm, HttpRequestProcessor httpRequestProcessor, HttpAuthorization httpAuthorization) {
        this.log = log;
        this.realmLine = "Basic realm=\"" + StringUtils.substitute(realm, new String[]{"\n", "\r", "\""}, new String[]{" ", " ", "\'"}) + '\"';
        this.httpRequestProcessor = httpRequestProcessor;
        this.httpAuthorization = httpAuthorization;
    }

    @Override
    public HttpResponse process(Socket socket, HttpRequest httpRequest) throws InterruptedException {
        try {
            httpAuthorization.checkAuthorization(httpRequest);
            return httpRequestProcessor.process(socket, httpRequest);
        } catch (AuthorizationMissingException e) {
            log.hint(e);
            return createAuthorizationResponse(httpRequest);
        } catch (AuthorizationFailedException e) {
            log.warning(e);
            Thread.sleep(authFailedResponseDelay);
            return createAuthorizationResponse(httpRequest);
        }
    }

    private HttpResponse createAuthorizationResponse(HttpRequest httpRequest) {
        HttpResponse httpResponse = httpRequest.createResponse(HttpStatus._401_UNAUTHORIZED);
        httpResponse.headers().set("WWW-Authenticate", realmLine);
        return httpResponse;
    }

    public long getAuthFailedResponseDelay() {
        return authFailedResponseDelay;
    }

    public void setAuthFailedResponseDelay(long authFailedResponseDelay) {
        this.authFailedResponseDelay = authFailedResponseDelay;
    }
}
