package tk.bolovsrol.utils.http;

public class InvalidPasswordException extends AuthorizationFailedException {
    public InvalidPasswordException(String wwwAuthenticate) {
        super(wwwAuthenticate);
    }

    public InvalidPasswordException(String message, String wwwAuthenticate) {
        super(message, wwwAuthenticate);
    }

    public InvalidPasswordException(String message, Throwable cause, String wwwAuthenticate) {
        super(message, cause, wwwAuthenticate);
    }

    public InvalidPasswordException(Throwable cause, String wwwAuthenticate) {
        super(cause, wwwAuthenticate);
    }
}
