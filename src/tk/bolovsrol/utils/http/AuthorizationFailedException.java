package tk.bolovsrol.utils.http;

/**
 *
 */
public class AuthorizationFailedException extends AuthorizationException {
    public AuthorizationFailedException(String wwwAuthenticate) {
        super(wwwAuthenticate);
    }

    public AuthorizationFailedException(String message, String wwwAuthenticate) {
        super(message, wwwAuthenticate);
    }

    public AuthorizationFailedException(String message, Throwable cause, String wwwAuthenticate) {
        super(message, cause, wwwAuthenticate);
    }

    public AuthorizationFailedException(Throwable cause, String wwwAuthenticate) {
        super(cause, wwwAuthenticate);
    }
}