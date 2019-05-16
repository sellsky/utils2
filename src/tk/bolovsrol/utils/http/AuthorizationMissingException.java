package tk.bolovsrol.utils.http;

/**
 *
 */
public class AuthorizationMissingException extends AuthorizationException {
    public AuthorizationMissingException(String wwwAuthenticate) {
        super(wwwAuthenticate);
    }
}
