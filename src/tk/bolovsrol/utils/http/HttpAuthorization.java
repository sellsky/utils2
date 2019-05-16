package tk.bolovsrol.utils.http;

/**
 *
 */
public interface HttpAuthorization {

    void checkAuthorization(HttpRequest httpRequest) throws AuthorizationMissingException, UnknownLoginException, InvalidPasswordException;

    String getWwwAuthenticate();
}
