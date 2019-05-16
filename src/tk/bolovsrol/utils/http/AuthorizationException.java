package tk.bolovsrol.utils.http;

/**
 * В запросе указана неверная авторизационная информация
 * или не указано вообще никакой.
 */
public class AuthorizationException extends Exception {
    private final String wwwAuthenticate;

    public AuthorizationException(String wwwAuthenticate) {
        this.wwwAuthenticate = wwwAuthenticate;
    }

    public AuthorizationException(String message, String wwwAuthenticate) {
        super(message);
        this.wwwAuthenticate = wwwAuthenticate;
    }

    public AuthorizationException(String message, Throwable cause, String wwwAuthenticate) {
        super(message, cause);
        this.wwwAuthenticate = wwwAuthenticate;
    }

    public AuthorizationException(Throwable cause, String wwwAuthenticate) {
        super(cause);
        this.wwwAuthenticate = wwwAuthenticate;
    }

    public String getWwwAuthenticate() {
        return wwwAuthenticate;
    }
}
