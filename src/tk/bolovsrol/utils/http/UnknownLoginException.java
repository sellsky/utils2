package tk.bolovsrol.utils.http;

public class UnknownLoginException extends AuthorizationFailedException {
    public UnknownLoginException(String wwwAuthenticate) {
        super(wwwAuthenticate);
    }

    public UnknownLoginException(String message, String wwwAuthenticate) {
        super(message, wwwAuthenticate);
    }

    public UnknownLoginException(String message, Throwable cause, String wwwAuthenticate) {
        super(message, cause, wwwAuthenticate);
    }

    public UnknownLoginException(Throwable cause, String wwwAuthenticate) {
        super(cause, wwwAuthenticate);
    }
}
