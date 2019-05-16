package tk.bolovsrol.utils.http;

/**
 *
 */
public enum Method {
    GET(false),
    POST(true),
    OPTIONS(false),
    HEAD(false),
    PUT(true),
    DELETE(false),
    TRACE(false),
    CONNECT(false);

    private final boolean allowsBody;

    Method(boolean allowsBody) {
        this.allowsBody = allowsBody;
    }

    public boolean allowsBody() {
        return allowsBody;
    }
}
